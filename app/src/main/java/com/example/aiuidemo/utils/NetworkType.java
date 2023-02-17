package com.example.aiuidemo.utils;

/**
 * @author : 时光
 * e-mail : qurongzhen@pay.media
 * date   : 2021/4/8 19:47
 * desc   : <功能简述>
 */
public enum NetworkType {
    NETWORK_WIFI("WiFi"),
    NETWORK_5G("5G"),
    NETWORK_4G("4G"),
    NETWORK_2G("2G"),
    NETWORK_3G("3G"),
    NETWORK_UNKNOWN("Unknown"),
    NETWORK_NO("No network"),
    NETWORK_ETHERNET("Ethernet");

    private String desc;

    NetworkType(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
