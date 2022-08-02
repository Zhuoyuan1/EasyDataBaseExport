package com.easydatabaseexport.database.postgresql.config;

import com.easydatabaseexport.core.DataSource;

/**
 * PostgreSqlDataSource
 *
 * @author lzy
 * @date 2022/7/21 10:37
 **/
public class PostgreSqlDataSource extends DataSource {

    static String driver = "org.postgresql.Driver";
    static String url;
    static String username;
    static String passwd;

    public PostgreSqlDataSource() {
        super(driver, url, username, passwd);
    }
}
