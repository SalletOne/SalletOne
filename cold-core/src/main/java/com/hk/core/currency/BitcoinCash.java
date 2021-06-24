package com.hk.core.currency;

import com.hk.core.base.BitcoinNetworks;
import com.hk.core.dto.AddressDTO;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.Script;

import java.util.List;

public class BitcoinCash {

    private static final BitcoinCash INSTANCE = new BitcoinCash(BitcoinNetworks.BITCOIN_CASH_NETWORK);
    private final NetworkParameters bchNetwork;
    private final String HD_PATH;

    public static BitcoinCash getInstance() {
        return INSTANCE;
    }

    private BitcoinCash(BitcoinNetworks networkParameters ){
        this.bchNetwork = networkParameters.getNetworkParameters();
        this.HD_PATH = networkParameters.getPath();
    }

    public AddressDTO address(DeterministicHierarchy deterministicHierarchy, int addressIndex){
        AddressDTO addressDTO = seedToAddress(deterministicHierarchy, addressIndex);
        addressDTO.setPrivateKey(null);
        return addressDTO;
    }



    private AddressDTO seedToAddress(DeterministicHierarchy deterministicHierarchy, int addressIndex){
        List<ChildNumber> parsePath = HDUtils.parsePath(HD_PATH);
        DeterministicKey accountKey0 = deterministicHierarchy.get(parsePath, true, true);
        DeterministicKey childKey = HDKeyDerivation.deriveChildKey(accountKey0, addressIndex);

        String privateKey = childKey.getPrivateKeyAsWiF(bchNetwork);
        String address = Address.fromKey(MainNetParams.get(), childKey, Script.ScriptType.P2PKH).toString();

        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddress(address);
        addressDTO.setPrivateKey(privateKey);
        addressDTO.setIndex(addressIndex);
        return addressDTO;
    }

}
