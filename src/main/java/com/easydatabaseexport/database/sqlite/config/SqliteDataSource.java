package com.easydatabaseexport.database.sqlite.config;

import com.easydatabaseexport.core.DataSource;

/**
 * SqliteDataSource
 *
 * @author lzy
 * @date 2023/3/2 10:37
 **/
public class SqliteDataSource extends DataSource {

    static String driver = "org.sqlite.JDBC";
    static String url;
    static String username;
    static String passwd;

    public SqliteDataSource() {
        super(driver, url, username, passwd);
    }
}
