package com.hk.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Slf4j
public class AESEncrypt {
    private static final String CHARSET = "UTF-8";
    private static final String AES_ALG         = "AES";
    private static final String AES_CBC_PCK_ALG = "AES/CBC/PKCS5Padding";
    private static final byte[] AES_IV          = initIv();

    private static Cipher cipher = null;
    static {
        try {
            cipher = Cipher.getInstance(AES_CBC_PCK_ALG);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    public static String encryptContent(String content, String encryptType, String encryptKey) throws RuntimeException {
        if(cipher == null) throw new RuntimeException("encryptContent init cipher error");
        if (AES_ALG.equals(encryptType)) {
            return aesEncrypt(content, encryptKey);
        } else {
            log.error("not support encryptType : {} " , encryptType);
            throw new RuntimeException("当前不支持该算法类型：encryptType=" + encryptType);
        }
    }

    public static String decryptContent(String content, String encryptType, String encryptKey) throws RuntimeException {
        if(cipher == null) throw new RuntimeException("decryptContent init cipher error");
        if (AES_ALG.equals(encryptType)) {
            return aesDecrypt(content, encryptKey);
        } else {
            log.error("not support encryptType : {} " , encryptType);
            throw new RuntimeException("当前不支持该算法类型：encrypeType=" + encryptType);
        }
    }


    private static String aesEncrypt(String content, String aesKey) throws RuntimeException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALG);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(aesKey.getBytes());
            keyGenerator.init(128, secureRandom);

            IvParameterSpec iv = new IvParameterSpec(AES_IV);
            cipher.init(Cipher.ENCRYPT_MODE, keyGenerator.generateKey(), iv);

            byte[] encryptBytes = cipher.doFinal(content.getBytes(CHARSET));
            return new String(Base64.encodeBase64(encryptBytes));
        } catch (Exception e) {
            log.error("AES encrypt failure Aescontent : {} ,charset : {} , cause : {} " , content, CHARSET, e.getMessage());
            throw new RuntimeException("AES加密失败：Aescontent = " + content + "; charset = " + CHARSET, e);
        }

    }

    private static String aesDecrypt(String content, String key) throws RuntimeException {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALG);
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(key.getBytes());
            keyGenerator.init(128, secureRandom);

            IvParameterSpec iv = new IvParameterSpec(initIv());
            cipher.init(Cipher.DECRYPT_MODE, keyGenerator.generateKey(), iv);

            byte[] cleanBytes = cipher.doFinal(Base64.decodeBase64(content.getBytes()));
            return new String(cleanBytes, CHARSET);
        } catch (Exception e) {
            log.error("AES decrypt failure Aescontent : {} ,charset : {} , cause : {} ", content, CHARSET, e.getMessage());
            throw new RuntimeException("AES解密失败：Aescontent = " + content + "; charset = "+ CHARSET, e);
        }
    }

    private static byte[] initIv() {
        try {
            int blockSize = cipher.getBlockSize();
            byte[] iv = new byte[blockSize];
            for (int i = 0; i < blockSize; ++i) {
                iv[i] = 0;
            }
            return iv;
        } catch (Exception e) {
            int blockSize = 16;
            byte[] iv = new byte[blockSize];
            for (int i = 0; i < blockSize; ++i) {
                iv[i] = 0;
            }
            return iv;
        }
    }

}
