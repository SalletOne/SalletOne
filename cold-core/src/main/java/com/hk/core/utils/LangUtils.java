package com.hk.core.utils;

import cn.hutool.core.io.resource.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LangUtils {

    public static Locale defaultLocale = Locale.SIMPLIFIED_CHINESE;

    private static ConcurrentMap<String, Map<String, String>> resources = new ConcurrentHashMap<>();

    private static List<Locale> supportLang= Arrays.asList(Locale.SIMPLIFIED_CHINESE, Locale.US);

    private static final String PROPERTIES_SUFFIX = ".properties";
    private static final String PROPERTIES_PREFIX = "i18n/message_";


    static {
        for(Locale item : supportLang){
            Map<String,String> words = new HashMap<>();
            try{
                InputStream is = new ClassPathResource(PROPERTIES_PREFIX+item.toString()+PROPERTIES_SUFFIX).getStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String s;
                while ((s = br.readLine()) != null) {
                    String[] prop = s.split("=");
                    words.put(prop[0], prop[1]);
                }
                resources.put(item.toString(), words);
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("加载语言资源失败");
                System.exit(-500);
            }
        }
    }

    public static String message(String key){
        String msg = resources.get(defaultLocale.toString()).get(key);
        if(msg==null || msg.equals("")) msg = resources.get(defaultLocale.toString()).get("lang.not.find");
        return msg;
    }

    public static String message(String key, Locale locale){
        if(!supportLang.contains(locale)) locale = defaultLocale;
        String msg = resources.get(locale.toString()).get(key);
        if(msg==null || msg.equals("")) msg = resources.get(locale.toString()).get("lang.not.find");
        return msg;
    }
}
