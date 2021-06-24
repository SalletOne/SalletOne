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

public class Dogecoin {

    private static final Dogecoin INSTANCE = new Dogecoin(BitcoinNetworks.DOGECOIN_NETWORK);
    private final NetworkParameters dogeNetwork;
    private final String HD_PATH;

    public static Dogecoin getInstance() {
        return INSTANCE;
    }

    private Dogecoin(BitcoinNetworks networkParameters ){
        this.dogeNetwork = networkParameters.getNetworkParameters();
        this.HD_PATH = networkParameters.getPath();
    }

    public AddressDTO address(DeterministicHierarchy deterministicHierarchy, int addressIndex){
        AddressDTO addressDTO = seedToAddress(deterministicHierarchy, addressIndex);
        addressDTO.setPrivateKey(null);
        return addressDTO;
    }

    public String signTx(BtcTransDTO btcTransDTO, DeterministicHierarchy deterministicHierarchy, int addressIndex){
        AddressDTO addressDTO = seedToAddress(deterministicHierarchy, addressIndex);

        txParamsCheck(btcTransDTO, addressDTO);

        ECKey ecKey = DumpedPrivateKey.fromBase58(dogeNetwork, addressDTO.getPrivateKey()).getKey();

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

        Map<String, Long> receiveAddressAndValue = new HashMap<>();
        for(BtcTransDTO.Out item : btcTransDTO.getReceivers()){
            receiveAddressAndValue.put(item.getAddress(), item.getAmount());
        }

        Transaction transaction = new Transaction(dogeNetwork);

        addOutputs(transaction, receiveAddressAndValue);
        addInputs(transaction, utxoKeys, ecKey);
        String rawTransactionHex = HEX.encode(transaction.bitcoinSerialize());
        return rawTransactionHex;
    }


    private void txParamsCheck(BtcTransDTO btcTransDTO, AddressDTO addressDTO){
        if(btcTransDTO.getSenders().size() < 1) throw new RuntimeException(LangUtils.message("btc.sender.not.null"));
        LegacyAddress.fromBase58(dogeNetwork, btcTransDTO.getSendAddress()); //address type check
        if(!btcTransDTO.getSendAddress().equalsIgnoreCase(addressDTO.getAddress())){
            throw new RuntimeException(LangUtils.message("btc.sender.err"));
        }
    }

    private void addOutputs(Transaction transaction, Map<String, Long> receiveAddressAndValue) {
        for (Object o : receiveAddressAndValue.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String receiveAddress = (String) entry.getKey();
            Address address;
            if(isLegacyAddress(receiveAddress)) {
                address = LegacyAddress.fromBase58(dogeNetwork, receiveAddress);
            } else {
                throw new AddressFormatException.InvalidPrefix(receiveAddress+" "+LangUtils.message("err.msg"));
            }
            Coin value = Coin.valueOf((Long) entry.getValue());
            //添加OUTPUT
            transaction.addOutput(value, address);
        }
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

    private void signLegacyTransaction(Transaction transaction, UTXO utxo, ECKey ecKey) {
        TransactionOutPoint outPoint = new TransactionOutPoint(dogeNetwork, utxo.getIndex(), utxo.getHash());
        transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);
    }

    private boolean isLegacyAddress(String addressBase58) {
        try {
            byte[] versionAndDataBytes = Base58.decodeChecked(addressBase58);
            int version = versionAndDataBytes[0] & 0xFF;
            return version == dogeNetwork.getAddressHeader() ||
                    version == dogeNetwork.getP2SHHeader();
        } catch (Exception e) {
            return false;
        }
    }

    //狗币签名
    public static void dogeCoinSign(){

        NetworkParameters dogeNet = BitcoinNetworks.DOGECOIN_NETWORK.getNetworkParameters();

        ECKey ecKey = DumpedPrivateKey.fromBase58(dogeNet, "QSfKkcpviTEnMrJqWk8uMbxLXTMcyZqB42K4iNAyPmDmmuMAa7Ru").getKey();

        List<UTXO> utxos = new ArrayList<>();

        UTXO utxo = new UTXO(
                Sha256Hash.wrap("d3e725203a5936f06c123302906e4760f65fa4c339bde3f6a5213c1b210276cf"),
                1L,
                Coin.parseCoinInexact("18.63000000"),
                0,
                false,
                new Script(Hex.decode("76a91499cefb1e3704ea1361ef76c87231b24d769e5d0788ac")),
                "DKAMstu7Cp1nCKCmmq66GzEa1cDRzcNxvJ"
        );
        utxos.add(utxo);


        Map<String, String> receiveAddressAndValue = new HashMap<>();
        receiveAddressAndValue.put("DQDrq7qmRG2erToqwapws5hx2LhABBHc4a", "1.00000000");
        receiveAddressAndValue.put("DKAMstu7Cp1nCKCmmq66GzEa1cDRzcNxvJ", "16.00000000");


        Transaction transaction = new Transaction(dogeNet);

        //构建outputs
        for (Map.Entry<String, String> entry : receiveAddressAndValue.entrySet()) {
            String receiveAddress = entry.getKey();
            Address address = LegacyAddress.fromBase58(dogeNet, receiveAddress);
            Coin value = Coin.parseCoinInexact(entry.getValue());
            //添加OUTPUT
            transaction.addOutput(value, address);
        }


        //foreach 签名
        TransactionOutPoint outPoint = new TransactionOutPoint(dogeNet, utxo.getIndex(), utxo.getHash());
        transaction.addSignedInput(outPoint, utxo.getScript(), ecKey, Transaction.SigHash.ALL, true);

        String signedHash = HEX.encode(transaction.bitcoinSerialize());

        System.out.println(signedHash);

    }

    private AddressDTO seedToAddress(DeterministicHierarchy deterministicHierarchy, int addressIndex){
        List<ChildNumber> parsePath = HDUtils.parsePath(HD_PATH);

        DeterministicKey accountKey0 = deterministicHierarchy.get(parsePath, true, true);

        DeterministicKey childKey = HDKeyDerivation.deriveChildKey(accountKey0, addressIndex);

        ECKey ecKey = ECKey.fromPrivate(childKey.getPrivKey());
        String privateKey = ecKey.getPrivateKeyAsWiF(dogeNetwork);
        String address = Address.fromKey(dogeNetwork, childKey, Script.ScriptType.P2PKH).toString();

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress(address);
        addressDTO.setIndex(addressIndex);
        addressDTO.setPrivateKey(privateKey);
        return addressDTO;
    }
}
