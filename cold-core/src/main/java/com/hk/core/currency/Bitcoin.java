package com.hk.core.currency;

import com.hk.common.dto.BtcTransDTO;
import com.hk.core.base.BitcoinNetworks;
import com.hk.core.dto.AddressDTO;
import com.hk.core.utils.LangUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.*;
import org.bitcoinj.script.Script;
import org.bouncycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bitcoinj.core.Utils.HEX;

public class Bitcoin {
    private static final Bitcoin INSTANCE = new Bitcoin(BitcoinNetworks.MAIN_NETWORK);
    private final NetworkParameters bitcoinNetwork;
    private final String HD_PATH;

    public static Bitcoin getInstance() {
        return INSTANCE;
    }

    private Bitcoin(BitcoinNetworks bitcoinNetworks){
        this.bitcoinNetwork = bitcoinNetworks.getNetworkParameters();
        this.HD_PATH = bitcoinNetworks.getPath();
    }

    public AddressDTO address(DeterministicHierarchy deterministicHierarchy, int addressIndex){
        AddressDTO addressDTO = seedToAddress(deterministicHierarchy, addressIndex);
        addressDTO.setPrivateKey(null);
        return addressDTO;
    }

    /**
     * 交易签名
     * @param btcTransDTO
     * @return
     */
    public String signTx(BtcTransDTO btcTransDTO, DeterministicHierarchy deterministicHierarchy, int addressIndex){
        AddressDTO addressDTO = seedToAddress(deterministicHierarchy, addressIndex);

        txParamsCheck(btcTransDTO, addressDTO);

        ECKey ecKey = DumpedPrivateKey.fromBase58(bitcoinNetwork, addressDTO.getPrivateKey()).getKey();

        List<UTXO> utxoKeys = new ArrayList<>();

        for(BtcTransDTO.Vin item : btcTransDTO.getSenders()){
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(item.getTxid()),
                    item.getIndex(),
                    Coin.valueOf(item.getValue()),
                    0,
                    false,
                    new Script(Hex.decode(btcTransDTO.getPubKey()))
            );
            utxoKeys.add(utxo);
        }
/*        btcTransDTO.getSenders().forEach(item -> {
            UTXO utxo = new UTXO(
                    Sha256Hash.wrap(item.getTxid()),
                    item.getIndex(),
                    Coin.valueOf(item.getValue()),
                    0,
                    false,
                    new Script(Hex.decode(btcTransDTO.getPubKey()))
            );
            utxoKeys.add(utxo);
        });*/

        Map<String, Long> receiveAddressAndValue = new HashMap<>();
        for(BtcTransDTO.Out item : btcTransDTO.getReceivers()){
            receiveAddressAndValue.put(item.getAddress(), item.getAmount());
        }
//        btcTransDTO.getReceivers().forEach(item -> receiveAddressAndValue.put(item.getAddress(), item.getAmount()));

        Transaction transaction = new Transaction(bitcoinNetwork);
        addOutputs(transaction, receiveAddressAndValue);
        addInputs(transaction, utxoKeys, ecKey);
        String rawTransactionHex = HEX.encode(transaction.bitcoinSerialize());
        return rawTransactionHex;
    }


    private void addInputs(Transaction transaction, List<UTXO> utxoKeys, ECKey ecKey){
        //遍历UTXO并签名
        for (UTXO utxo : utxoKeys) {
            //获取UTXO脚本类型
            Script script = utxo.getScript();
            Script.ScriptType scriptType = script.getScriptType();
            //区分legacy和SegWit的utxo
            if (scriptType == Script.ScriptType.P2PK || scriptType == Script.ScriptType.P2PKH || scriptType == Script.ScriptType.P2SH) {
                signLegacyTransaction(transaction, utxo,ecKey);
            } else if (scriptType == Script.ScriptType.P2WPKH || scriptType == Script.ScriptType.P2WSH) {
               //todo continue
            }
        }
    }

    private void txParamsCheck(BtcTransDTO btcTransDTO, AddressDTO addressDTO){
        if(btcTransDTO.getSenders().size() < 1) throw new RuntimeException(LangUtils.message("btc.sender.not.null"));
        LegacyAddress.fromBase58(bitcoinNetwork, btcTransDTO.getSendAddress()); //address type check
        if(!btcTransDTO.getSendAddress().equalsIgnoreCase(addressDTO.getAddress())){
            throw new RuntimeException(LangUtils.message("btc.sender.err"));
        }
    }

    private void signLegacyTransaction(Transaction transaction, UTXO utxo, ECKey ecKey) {
        TransactionOutPoint outPoint = new TransactionOutPoint(bitcoinNetwork, utxo.getIndex(), utxo.getHash());
        transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
    }

    private void addOutputs(Transaction transaction, Map<String, Long> receiveAddressAndValue) {
        for (Object o : receiveAddressAndValue.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String receiveAddress = (String) entry.getKey();
            Address address;
            if(isLegacyAddress(receiveAddress)) {
                address = LegacyAddress.fromBase58(bitcoinNetwork, receiveAddress);
            } else if (isSegWitAddress(receiveAddress)) {
                address = SegwitAddress.fromBech32(bitcoinNetwork, receiveAddress);
            } else {
                throw new AddressFormatException.InvalidPrefix(receiveAddress+" "+LangUtils.message("err.msg"));
            }
            Coin value = Coin.valueOf((Long) entry.getValue());
            //添加OUTPUT
            transaction.addOutput(value, address);
        }
    }

    private boolean isLegacyAddress(String addressBase58) {
        try {
            byte[] versionAndDataBytes = Base58.decodeChecked(addressBase58);
            int version = versionAndDataBytes[0] & 0xFF;
            return version == bitcoinNetwork.getAddressHeader() ||
                    version == bitcoinNetwork.getP2SHHeader();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSegWitAddress(String addressBase58) {
        try {
            Bech32.Bech32Data bechData = Bech32.decode(addressBase58);
            return bechData.hrp.equals(bitcoinNetwork.getSegwitAddressHrp());
        } catch (Exception e) {
            return false;
        }
    }

    private AddressDTO seedToAddress(DeterministicHierarchy deterministicHierarchy, int addressIndex){
        List<ChildNumber> parsePath = HDUtils.parsePath(HD_PATH);

        DeterministicKey accountKey0 = deterministicHierarchy.get(parsePath, true, true);

        DeterministicKey childKey = HDKeyDerivation.deriveChildKey(accountKey0, addressIndex);

        String privateKey = childKey.getPrivateKeyAsWiF(bitcoinNetwork);
        String address = Address.fromKey(bitcoinNetwork, childKey, Script.ScriptType.P2PKH).toString();

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress(address);
        addressDTO.setIndex(addressIndex);
        addressDTO.setPrivateKey(privateKey);
        return addressDTO;
    }

}
