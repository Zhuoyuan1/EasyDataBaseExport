package com.easydatabaseexport.database.dm.config;

import com.easydatabaseexport.core.DataSource;

/**
 * DmDataSource
 *
 * @author lzy
 * @date 2022/7/21 10:37
 **/
public class DmDataSource extends DataSource {

    static String driver = "dm.jdbc.driver.DmDriver";
    static String url;
    static String username;
    static String passwd;

    public DmDataSource() {
        super(driver, url, username, passwd);
    }
}
