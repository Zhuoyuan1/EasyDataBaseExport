package com.easydatabaseexport.database.db2.config;

import com.easydatabaseexport.core.DataSource;

/**
 * Db2DataSource
 *
 * @author lzy
 * @date 2022/7/21 10:37
 **/
public class Db2DataSource extends DataSource {

    static String driver = "com.ibm.db2.jcc.DB2Driver";
    static String url;
    static String username;
    static String passwd;

    public Db2DataSource() {
        super(driver, url, username, passwd);
    }
}
