package com.hk.core.currency;

import com.hk.common.dto.EthTransDTO;
import com.hk.core.constant.BizConstant;
import com.hk.core.dto.AddressDTO;
import com.hk.core.utils.LangUtils;
import org.bitcoinj.crypto.*;
import org.web3j.crypto.*;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class Ethereum {

    private static final Ethereum INSTANCE = new Ethereum(BizConstant.ETH_PATH);
    private final String passphrase = "51888";
    private final String HD_PATH;

    public static Ethereum getInstance() {
        return INSTANCE;
    }

    private Ethereum(final String addressPath){
        HD_PATH = addressPath;
    }

    public AddressDTO address(DeterministicHierarchy deterministicHierarchy, Integer addressIndex) {
        AddressDTO addressDTO = seedToAddress(deterministicHierarchy, addressIndex);
        addressDTO.setPrivateKey(null);
        String checksumAddress = Keys.toChecksumAddress(addressDTO.getAddress());
        addressDTO.setAddress(checksumAddress);
        return addressDTO;
    }

    // eth 交易签名
    public String signTx(EthTransDTO ethTransDTO, DeterministicHierarchy deterministicHierarchy, Integer addressIndex) {

        AddressDTO addressDTO = seedToAddress(deterministicHierarchy, addressIndex);

        txParamsCheck(ethTransDTO, addressDTO);

        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(
                                            ethTransDTO.getNonce(),
                                            Convert.toWei(ethTransDTO.getGasPrice().toString(), Convert.Unit.GWEI).toBigInteger(),
                                            BigInteger.valueOf(21000),
                                            ethTransDTO.getReceiver(),
                                            Convert.toWei(ethTransDTO.getAmount(), Convert.Unit.ETHER).toBigInteger()
        );
        Credentials credentials = Credentials.create("0x"+addressDTO.getPrivateKey());
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, ChainId.MAINNET, credentials);
        String signTx = Numeric.toHexString(signMessage);
        return signTx;
    }

    // erc20代币交易签名
    public String erc20Sign(EthTransDTO ethTransDTO, DeterministicHierarchy deterministicHierarchy, Integer addressIndex){
        AddressDTO addressDTO = seedToAddress(deterministicHierarchy, addressIndex);

        txParamsCheck(ethTransDTO, addressDTO);

        String contractAdress = "";
        if(ethTransDTO.getId().toLowerCase().contains("usdt")){
            contractAdress = BizConstant.CONTRACT_ADDRESS;
        }else if(ethTransDTO.getId().toLowerCase().contains("usdc")){
            contractAdress = BizConstant.CONTRACT_ADDRESS_CIRCLE;
        }else {
            throw new RuntimeException(LangUtils.message("erc20.msg.id.err"));
        }

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                ethTransDTO.getNonce(),
                Convert.toWei(ethTransDTO.getGasPrice().toString(), Convert.Unit.GWEI).toBigInteger(),
                BigInteger.valueOf(ethTransDTO.getGasLimit()),
                contractAdress,
                erc20Data(ethTransDTO.getReceiver(), ethTransDTO.getAmount())
        );
        Credentials credentials = Credentials.create("0x"+addressDTO.getPrivateKey());
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, ChainId.MAINNET, credentials);
        String signTx = Numeric.toHexString(signMessage);
        return signTx;
    }

/*    private AddressDTO address(List<String> mnemonicCodes, Integer addressIndex) {
        Long creationTimeSeconds = System.currentTimeMillis()/1000;
        DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonicCodes, null, "", creationTimeSeconds);

        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(deterministicSeed.getSeedBytes());

        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(rootPrivateKey);

        List<ChildNumber> parsePath = HDUtils.parsePath(BizConstant.ETH_PATH);

        DeterministicKey accountKey0 = deterministicHierarchy.get(parsePath, true, true);

        DeterministicKey childKey0 = HDKeyDerivation.deriveChildKey(accountKey0, addressIndex);

        ECKeyPair keyPair = ECKeyPair.create(childKey0.getPrivKeyBytes());

        WalletFile walletFile = null;
        try {
            walletFile = Wallet.createLight(passphrase, keyPair);
        } catch (CipherException e) {
            throw new RuntimeException(e.getMessage());
        }

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress("0x"+walletFile.getAddress());
        addressDTO.setIndex(addressIndex);
        addressDTO.setPrivateKey( keyPair.getPrivateKey().toString(16));
        return addressDTO;
    }*/

    private AddressDTO seedToAddress(DeterministicHierarchy deterministicHierarchy, Integer addressIndex) {
        List<ChildNumber> parsePath = HDUtils.parsePath(BizConstant.ETH_PATH);

        DeterministicKey accountKey0 = deterministicHierarchy.get(parsePath, true, true);

        DeterministicKey childKey0 = HDKeyDerivation.deriveChildKey(accountKey0, addressIndex);

        ECKeyPair keyPair = ECKeyPair.create(childKey0.getPrivKeyBytes());

        WalletFile walletFile = null;
        try {
            walletFile = Wallet.createLight(passphrase, keyPair);
        } catch (CipherException e) {
            throw new RuntimeException(e.getMessage());
        }

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress("0x"+walletFile.getAddress());
        addressDTO.setIndex(addressIndex);
        addressDTO.setPrivateKey( keyPair.getPrivateKey().toString(16));
        return addressDTO;
    }

    private void txParamsCheck(EthTransDTO ethTransDTO, AddressDTO addressDTO){
        if(!addressDTO.getAddress().toLowerCase().equals(ethTransDTO.getSender().toLowerCase())){
            throw new RuntimeException(LangUtils.message("btc.sender.err"));
        }
    }

    private String erc20Data(String to, BigDecimal amount){
        Long b = Convert.toWei(amount, Convert.Unit.MWEI).toBigInteger().longValue();
        StringBuffer data = new StringBuffer();
        data.append(BizConstant.TS_METHOD);
        data.append(leftZeroPadded(Numeric.cleanHexPrefix(to), 64));
        data.append(leftZeroPadded(Long.toHexString(b), 64));
        return data.toString();
    }

    private String leftZeroPadded(String oriStr, int len){
        char zero = '0';
        int strlen = oriStr.length();
        String str = "";
        if(strlen < len){
            for(int i=0; i<len-strlen; i++){
                str +=zero;
            }
        }
        str +=oriStr;
        return str;
    }

}
