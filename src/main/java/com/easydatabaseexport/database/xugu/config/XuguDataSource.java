package com.easydatabaseexport.database.xugu.config;

import com.easydatabaseexport.core.DataSource;

/**
 * XuguDataSource
 *
 * @author lzy
 * @date 2021/10/25 18:19
 **/
public class XuguDataSource extends DataSource {

    static String driver = "com.xugu.cloudjdbc.Driver";
    static String url;
    static String username;
    static String passwd;

    public XuguDataSource() {
        super(driver, url, username, passwd);
    }
}
