package com.easydatabaseexport.database.kingbase.config;

import com.easydatabaseexport.core.DataSource;

/**
 * KingBaseDataV8Source
 *
 * @author lzy
 * @date 2022/7/21 10:37
 **/
public class KingBaseDataV8Source extends DataSource {

    static String driver = "com.kingbase8.Driver";
    static String url;
    static String username;
    static String passwd;

    public KingBaseDataV8Source() {
        super(driver, url, username, passwd);
    }
}
