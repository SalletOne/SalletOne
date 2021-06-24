package com.hk.core.utils;

import com.hk.core.constant.BizConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

@Slf4j
public class RSAUtils {

    //私钥加密
    public static String encryptByPrivateKey(String content) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(BizConstant.ENCRYPT_KEY));
        KeyFactory keyFactory = KeyFactory.getInstance(BizConstant.SIGN_TYPE_RSA);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        Cipher cipher = Cipher.getInstance(BizConstant.SIGN_TYPE_RSA);
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(content.getBytes());
        return Base64.encodeBase64String(result);
    }

    //私钥解密
    public static String decryptByPrivateKey(String content) throws Exception {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec5 = new PKCS8EncodedKeySpec(Base64.decodeBase64(BizConstant.ENCRYPT_KEY));
        KeyFactory keyFactory = KeyFactory.getInstance(BizConstant.SIGN_TYPE_RSA);
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec5);
        Cipher cipher = Cipher.getInstance(BizConstant.SIGN_TYPE_RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(Base64.decodeBase64(content));
        return new String(result);
    }

}
