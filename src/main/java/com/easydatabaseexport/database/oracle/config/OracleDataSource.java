package com.easydatabaseexport.database.oracle.config;

import com.easydatabaseexport.core.DataSource;

/**
 * OracleDataSource
 *
 * @author lzy
 * @date 2021/10/25 18:19
 **/
public class OracleDataSource extends DataSource {

    static String driver = "oracle.jdbc.driver.OracleDriver";
    static String url;
    static String username;
    static String passwd;

    public OracleDataSource() {
        super(driver, url, username, passwd);
    }
}
