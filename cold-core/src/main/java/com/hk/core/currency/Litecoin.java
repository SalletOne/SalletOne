package com.hk.core.currency;

import com.hk.core.base.BitcoinNetworks;
import com.hk.core.dto.AddressDTO;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.*;
import org.bitcoinj.script.Script;

import java.util.List;

public class Litecoin {

    private static final Litecoin INSTANCE = new Litecoin(BitcoinNetworks.LITECOIN_NETWORK);
    private final NetworkParameters ltcNetwork;
    private final String HD_PATH;

    public static Litecoin getInstance() {
        return INSTANCE;
    }

    private Litecoin(BitcoinNetworks networkParameters ){
        this.ltcNetwork = networkParameters.getNetworkParameters();
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

        ECKey ecKey = ECKey.fromPrivate(childKey.getPrivKey());
        String pvk = ecKey.getPrivateKeyAsWiF(ltcNetwork);
        String address = Address.fromKey(ltcNetwork, childKey, Script.ScriptType.P2PKH).toString();
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setIndex(addressIndex);
        addressDTO.setAddress(address);
        addressDTO.setPrivateKey(pvk);
        return addressDTO;
    }

}