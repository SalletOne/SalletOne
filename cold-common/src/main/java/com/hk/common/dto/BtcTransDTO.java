package com.hk.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class BtcTransDTO implements Serializable {
    //消息唯一id
    private String id;

    private String sendAddress;
    private String pubKey;
    private String receiveAddress;
    private Long fee;

    //发送者
    public List<Vin> senders = new ArrayList<>();
    //接收者
    private List<Out> receivers = new ArrayList<>();

    @Data
    public static class Vin {
        //hash值
        private String txid;
        private Long index;
        //sat
        private Long value;
    }

    @Data
    public static class  Out {
        //地址
        private String address;
        //sat
        private Long amount;
    }
}
