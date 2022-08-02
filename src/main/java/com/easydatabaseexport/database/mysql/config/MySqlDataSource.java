package com.easydatabaseexport.database.mysql.config;

import com.easydatabaseexport.core.DataSource;

/**
 * MysqlDataSource
 *
 * @author lzy
 * @date 2021/5/31 15:32
 **/
public class MySqlDataSource extends DataSource {

    static String driver = "com.mysql.cj.jdbc.Driver";
    static String url;
    static String username;
    static String passwd;

    public MySqlDataSource() {
        super(driver, url, username, passwd);
    }
}
