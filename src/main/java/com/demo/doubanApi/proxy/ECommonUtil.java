package com.demo.doubanApi.proxy;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ECommonUtil {
    private static  Logger LOG = Logger.getLogger(ECommonUtil.class);

    public static Logger getLog() {
        return LOG;
    }

    public static void printSQL(String sql, Object... params) {
        getLog().info("SQL: " + sql);
        getLog().info("PARAMS: [" + StringUtils.join(params, ",") + "]");
    }

}