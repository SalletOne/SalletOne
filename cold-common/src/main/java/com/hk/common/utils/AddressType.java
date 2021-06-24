package com.hk.common.utils;

public enum AddressType {
    BITCOIN_ADDRESS(0, "bitcoin", "btc"),
    ETHEREUM_ADDRESS(1, "ethereum", "eth"),
    DOGECOIN_ADDRESS(2, "dogecoin", "doge"),
    LITECOIN_ADDRESS(3, "litecoin", "ltc"),
    BITCOIN_CASH_ADDRESS(4, "bitcoincash", "bch"),
//    XRP_ADDRESS(5, "ripple", "xrp")
    ;

    private Integer type;
    private String network;
    private String sn;

    AddressType(Integer type, String network, String sn) {
        this.type = type;
        this.network = network;
        this.sn = sn;
    }


    public String getNetwork() {
        return network;
    }
    public String getSn() {
        return sn;
    }
    public Integer getType(){
        return type;
    }

    public static AddressType getNetwork(Integer type) {
        for(AddressType v : values()){
           if(v.getType() == type){
               return v;
           }
        }
        throw new RuntimeException("params error");
    }

    public static AddressType getNetwork(String network) {
        for(AddressType v : values()){
            if(v.getNetwork().equalsIgnoreCase(network)){
                return v;
            }
        }
        throw new RuntimeException("params error");
    }

}
