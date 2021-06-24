package com.hk.core.base;

import com.hk.common.params.DogecoinMainNetParams;
import com.hk.common.params.LitecoinMainNetParams;
import com.hk.core.constant.BizConstant;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet3Params;

/**
 * bitcoin网络
 */
public enum BitcoinNetworks {
    BITCOIN_CASH_NETWORK(MainNetParams.get(), BizConstant.BCH_PATH, "BCH网络"),
    LITECOIN_NETWORK(LitecoinMainNetParams.get(), BizConstant.LITECOIN_PATH, "LTC网络"),
    DOGECOIN_NETWORK(DogecoinMainNetParams.get(), BizConstant.DOGECOIN_PATH, "狗币网络"),
    MAIN_NETWORK(MainNetParams.get(), BizConstant.BTC_PATH, "正式网络"),
    Test_network(TestNet3Params.get(), BizConstant.BTC_TEST_PATH, "测试网络"),
    REGTEST_NETWORK(RegTestParams.get(), BizConstant.BTC_TEST_PATH, "regtest网络");

    private NetworkParameters networkParameters;
    private String path;
    private String desc;

    BitcoinNetworks(NetworkParameters networkParameters, String path, String desc) {
        this.networkParameters = networkParameters;
        this.path = path;
        this.desc = desc;
    }

    public NetworkParameters getNetworkParameters() {
        return networkParameters;
    }

    public String getPath(){
        return path;
    }
}
