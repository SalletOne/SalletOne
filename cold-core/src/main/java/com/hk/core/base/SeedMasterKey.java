package com.hk.core.base;

import com.hk.core.utils.LangUtils;
import org.bitcoinj.crypto.DeterministicHierarchy;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.wallet.DeterministicSeed;

import java.util.List;

public class SeedMasterKey {

    //助记词生成root key
    public static DeterministicHierarchy seedMasterKey(List<String> mnemonics){
        if(mnemonics == null || mnemonics.size() != 12) throw new RuntimeException(LangUtils.message("mnemonic.word.err"));
        Long creationTimeSeconds = System.currentTimeMillis()/1000;
        DeterministicSeed deterministicSeed = new DeterministicSeed(mnemonics, null, "", creationTimeSeconds);
        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(deterministicSeed.getSeedBytes());
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(rootPrivateKey);
        return deterministicHierarchy;
    }

}
