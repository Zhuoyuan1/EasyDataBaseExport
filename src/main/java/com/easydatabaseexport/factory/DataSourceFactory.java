package com.easydatabaseexport.factory;

import com.easydatabaseexport.core.DataSource;
import com.easydatabaseexport.database.db2.config.Db2DataSource;
import com.easydatabaseexport.database.dm.config.DmDataSource;
import com.easydatabaseexport.database.kingbase.config.KingBaseDataV8Source;
import com.easydatabaseexport.database.mysql.config.MySqlDataSource;
import com.easydatabaseexport.database.oracle.config.OracleDataSource;
import com.easydatabaseexport.database.postgresql.config.PostgreSqlDataSource;
import com.easydatabaseexport.database.sqlserver.config.SqlServerDataSource;
import com.easydatabaseexport.database.xugu.config.XuguDataSource;
import com.easydatabaseexport.enums.DataBaseType;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataSourceFactory
 *
 * @author lzy
 * @date 2021/11/2 10:58
 **/
public class DataSourceFactory {

    /**
     * DataSource缓存池
     */
    private static final Map<String, DataSource> REPORT_POOL = new ConcurrentHashMap<>(0);

    static {
        REPORT_POOL.put(DataBaseType.MYSQL.name(), new MySqlDataSource());
        REPORT_POOL.put(DataBaseType.ORACLE.name(), new OracleDataSource());
        REPORT_POOL.put(DataBaseType.SQLSERVER.name(), new SqlServerDataSource());
        REPORT_POOL.put(DataBaseType.POSTGRESQL.name(), new PostgreSqlDataSource());
        REPORT_POOL.put(DataBaseType.DB2.name(), new Db2DataSource());
        REPORT_POOL.put(DataBaseType.DM.name(), new DmDataSource());
        REPORT_POOL.put(DataBaseType.KINGBASE8.name(), new KingBaseDataV8Source());
        REPORT_POOL.put(DataBaseType.XUGU.name(), new XuguDataSource());
    }

    /**
     * 获取对应数据源
     *
     * @param type 类型
     * @return ITokenGranter
     */
    @SneakyThrows
    public static DataSource get(String type) {
        DataSource dataSource = REPORT_POOL.get(type);
        if (dataSource == null) {
            throw new ClassNotFoundException("no DataBaseType was found");
        } else {
            return dataSource;
        }
    }
}
