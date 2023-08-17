package com.easydatabaseexport.enums;

import com.easydatabaseexport.util.StringUtil;

/**
 * DataBaseType
 *
 * @author lzy
 * @date 2021/11/2 9:31
 **/
public enum DataBaseType {
    /**
     * MYSQL
     */
    MYSQL("jdbc:mysql://%s:%s/%s?autoReconnect=true&useUnicode=true&maxWait=3000&connectTimeout=5000&" +
            "socketTimeout=5000&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"),
    /**
     * ORACLE
     */
    ORACLE("jdbc:oracle:thin:@//%s:%s/%s"),
    /**
     * SQL_SERVER
     */
    SQLSERVER("jdbc:sqlserver://%s:%s;databaseName=%s"),
    /**
     * SQL_SERVER
     */
    SQLITE("jdbc:sqlite:%s"),
    /**
     * POSTGRESQL
     **/
    POSTGRESQL("jdbc:postgresql://%s:%s/%s"),
    /**
     * DB2
     **/
    DB2("jdbc:db2://%s:%s/%s"),
    /**
     * DM
     **/
    DM("jdbc:dm://%s:%s/%s"),
    /**
     * KINGBASE8
     **/
    KINGBASE8("jdbc:kingbase8://%s:%s/%s"),
    /**
     * XUGU
     **/
    XUGU("jdbc:xugu://%s:%s/%s?characterEncoding=UTF-8");

    private String urlString;

    DataBaseType(String urlString) {
        this.urlString = urlString;
    }

    public String getUrlString() {
        return urlString;
    }

    public void setUrlString(String urlString) {
        this.urlString = urlString;
    }

    public static DataBaseType matchType(String value) {
        if (StringUtil.isNotEmpty(value)) {
            for (DataBaseType dataBase : DataBaseType.values()) {
                if (dataBase.name().equals(value.toUpperCase())) {
                    return dataBase;
                }
            }
        }
        return null;
    }

}
