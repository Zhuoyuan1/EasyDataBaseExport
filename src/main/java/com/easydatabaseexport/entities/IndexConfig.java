package com.easydatabaseexport.entities;

import com.easydatabaseexport.util.StringUtil;
import lombok.Data;

/**
 * IndexConfig
 *
 * @author lzy
 * @date 2021/7/7 21:52
 **/
@Data
public class IndexConfig {

    private String url;
    private String database;
    private String user;
    private String dataType;
    private String passwd;
    private String group;

    public IndexConfig(String url, String database, String user, String dataType, String passwd, String group) {
        this.url = url;
        this.database = database;
        this.dataType = dataType;
        this.user = user;
        this.passwd = passwd;
        this.group = group;
    }

    @Override
    public String toString() {
        return url;
    }

    public String toMyString() {
        if (StringUtil.isNotEmpty(group)) {
            return group + "/" + url + "|" + database + "|" + user + "|" + dataType;
        }
        if (StringUtil.isNotEmpty(dataType)) {
            return url + "|" + database + "|" + user + "|" + dataType;
        }
        return url + "|" + database + "|" + user;
    }
}
