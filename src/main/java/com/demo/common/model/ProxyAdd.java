package com.demo.common.model;

import com.demo.common.model.base.BaseProxyAdd;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ProxyAdd extends BaseProxyAdd<ProxyAdd> {
	public static final ProxyAdd dao = new ProxyAdd().dao();

    public ProxyAdd getOneProxy() {
        synchronized (this){
            ProxyAdd proxyAdd =  dao.findFirst("select * from proxy_add where flag = 0 order by err_total limit 1");
            proxyAdd.setFlag(1).update();
            return proxyAdd;
        }

    }
}
