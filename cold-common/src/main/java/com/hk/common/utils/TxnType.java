package com.hk.common.utils;

public enum  TxnType {

    BTC_TX(0, "btc", "比特币交易"),
    ETH_TX(1, "eth", "以太坊交易"),
    ERC20_USDT_TX(2, "usdt", "usdt交易"),
    ERC20_USDC_TX(3, "usdc", "usdc交易"),
    DOGE_TX(4, "doge", "狗币交易"),
    LTC_TX(5, "ltc", "莱特币交易"),
    BCH_TX(6, "bch", "bch交易"),

//    XRP_TX(7, "xrp", "xrp交易")
    ;

    private Integer type;
    private String symbol;
    private String desc;

    TxnType(Integer type, String symbol, String desc) {
        this.type = type;
        this.symbol = symbol;
        this.desc = desc;
    }

    public String getSymbol(){
        return symbol;
    }
    public Integer getType(){
        return type;
    }

    public static Boolean isSupport(String symbol){
        for (TxnType e: TxnType.values()){
            if(e.getSymbol().equalsIgnoreCase(symbol)){
                return true;
            }
        }
        return false;
    }
}
