package com.demo.doubanApi.dto;

public class ProxyDto {
    private String host;
    private int port;
    private int flag; //0:未使用 1：正在使用 2：被禁用 3：无法代理

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
