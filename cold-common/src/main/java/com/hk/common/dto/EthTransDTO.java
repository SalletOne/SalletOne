package com.hk.common.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Builder
public class EthTransDTO implements Serializable {
    //消息唯一id
    private String id;
    //发送地址
    private String sender;
    //server返回值
    private BigInteger nonce;
    //交易价格
    private BigInteger gasPrice;

    private Long gasLimit;
    //接收地址
    private String receiver;
    //交易金额
    private BigDecimal amount;
    //签名hash
    private String txHash;
}
