package com.hk.core.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtils {

    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 密码加密
     * @param password
     * @return
     */
    public static String encryptPassword(String password){
        return passwordEncoder.encode(password);
    }

    /**
     * 密码校验
     * @param password
     * @param encodedPassword
     * @return
     */
    public static boolean validatePassword(String password, String encodedPassword){
        return passwordEncoder.matches(password, encodedPassword);
    }


}
