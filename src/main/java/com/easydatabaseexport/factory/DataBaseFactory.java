package com.easydatabaseexport.factory;

import com.easydatabaseexport.core.DataResult;
import com.easydatabaseexport.database.db2.impl.Db2DataResultImpl;
import com.easydatabaseexport.database.dm.impl.DmDataResultImpl;
import com.easydatabaseexport.database.kingbase.impl.KingBaseDataV8ResultImpl;
import com.easydatabaseexport.database.mysql.impl.MySqlDataResultImpl;
import com.easydatabaseexport.database.oracle.impl.OracleDataResultImpl;
import com.easydatabaseexport.database.postgresql.impl.PostgreSqlDataResultImpl;
import com.easydatabaseexport.database.sqlserver.impl.SqlServerDataResultImpl;
import com.easydatabaseexport.database.xugu.impl.XuguDataResultImpl;
import com.easydatabaseexport.enums.DataBaseType;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataBaseFactory
 *
 * @author lzy
 * @date 2021/11/2 10:16
 **/
public class DataBaseFactory {

    /**
     * DataBase实现类缓存池
     */
    private static final Map<String, DataResult> REPORT_POOL = new ConcurrentHashMap<>(16);

    static {
        REPORT_POOL.put(DataBaseType.MYSQL.name(), new MySqlDataResultImpl());
        REPORT_POOL.put(DataBaseType.ORACLE.name(), new OracleDataResultImpl());
        REPORT_POOL.put(DataBaseType.SQLSERVER.name(), new SqlServerDataResultImpl());
        REPORT_POOL.put(DataBaseType.POSTGRESQL.name(), new PostgreSqlDataResultImpl());
        REPORT_POOL.put(DataBaseType.DB2.name(), new Db2DataResultImpl());
        REPORT_POOL.put(DataBaseType.DM.name(), new DmDataResultImpl());
        REPORT_POOL.put(DataBaseType.KINGBASE8.name(), new KingBaseDataV8ResultImpl());
        REPORT_POOL.put(DataBaseType.XUGU.name(), new XuguDataResultImpl());
    }

    /**
     * 获取对应接口
     *
     * @param reportType 报表类型
     * @return ITokenGranter
     */
    @SneakyThrows
    public static DataResult get(String reportType) {
        DataResult dataResult = REPORT_POOL.get(reportType);
        if (dataResult == null) {
            throw new ClassNotFoundException("no DataBaseType was found");
        } else {
            return dataResult;
        }
    }

}
