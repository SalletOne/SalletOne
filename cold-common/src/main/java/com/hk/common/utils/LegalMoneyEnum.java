package com.hk.common.utils;

//法币me
public enum LegalMoneyEnum {
    CNY("CNY"),
    USD("USD");

    private String name;

    LegalMoneyEnum(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public static Boolean isSupport(String name){
        for(LegalMoneyEnum item : LegalMoneyEnum.values()){
            if(item.getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }
}
