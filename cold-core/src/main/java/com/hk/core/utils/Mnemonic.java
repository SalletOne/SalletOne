package com.hk.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.wallet.DeterministicSeed;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;

/**
 * 生成助记词
 */
@Slf4j
public class Mnemonic {

    public List<String> wordList(){
        try {
            MnemonicCode mnemonicCode = new MnemonicCode();
            return mnemonicCode.getWordList();
        } catch (IOException e) {
            log.error("加载助记词失败:{}", e.getMessage(), e);
            throw new RuntimeException("助记词加载失败!");
        }
    }

    public List<String> words(){
        DeterministicSeed seed = new DeterministicSeed(new SecureRandom(), DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS, "");
        List<String> mnemonicCodes = seed.getMnemonicCode();
       return mnemonicCodes;
    }
}
