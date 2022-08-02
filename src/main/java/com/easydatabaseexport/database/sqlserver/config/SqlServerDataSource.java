package com.easydatabaseexport.database.sqlserver.config;

import com.easydatabaseexport.core.DataSource;

/**
 * OracleDataSource
 *
 * @author lzy
 * @date 2021/10/25 18:19
 **/
public class SqlServerDataSource extends DataSource {

    static String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    static String url;
    static String username;
    static String passwd;

    public SqlServerDataSource() {
        super(driver, url, username, passwd);
    }
}
