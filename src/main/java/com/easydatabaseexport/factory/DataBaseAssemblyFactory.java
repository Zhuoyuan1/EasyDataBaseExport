package com.easydatabaseexport.factory;

import com.easydatabaseexport.enums.DataBaseType;
import com.easydatabaseexport.factory.assembly.DataBaseAssembly;
import com.easydatabaseexport.factory.assembly.impl.ConDatabaseModeTableImpl;
import com.easydatabaseexport.factory.assembly.impl.ConDatabaseTableImpl;
import com.easydatabaseexport.factory.assembly.impl.ConModeTableImpl;
import lombok.SneakyThrows;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataBaseAssemblyMenu 数据库配件组装工厂
 *
 * @author lzy
 * @date 2022/7/25 14:26
 **/
public class DataBaseAssemblyFactory {

    /**
     * DataBase实现类缓存池
     */
    private static final Map<String, DataBaseAssembly> REPORT_POOL = new ConcurrentHashMap<>(16);

    static {
        // [连接-库-表]
        ConDatabaseTableImpl tableType = new ConDatabaseTableImpl();
        // [连接-模式-表]
        ConModeTableImpl tableModeType = new ConModeTableImpl();
        // [连接-库-模式-表]
        ConDatabaseModeTableImpl tableMode = new ConDatabaseModeTableImpl();
        REPORT_POOL.put(DataBaseType.MYSQL.name(), tableType);
        REPORT_POOL.put(DataBaseType.SQLITE.name(), tableType);
        REPORT_POOL.put(DataBaseType.ORACLE.name(), tableModeType);
        REPORT_POOL.put(DataBaseType.DM.name(), tableModeType);
        REPORT_POOL.put(DataBaseType.DB2.name(), tableModeType);
        REPORT_POOL.put(DataBaseType.SQLSERVER.name(), tableMode);
        REPORT_POOL.put(DataBaseType.POSTGRESQL.name(), tableMode);
        REPORT_POOL.put(DataBaseType.KINGBASE8.name(), tableMode);
        REPORT_POOL.put(DataBaseType.XUGU.name(), tableMode);
    }

    /**
     * 获取对应接口
     *
     * @param reportType 报表类型
     * @return ITokenGranter
     */
    @SneakyThrows
    public static DataBaseAssembly get(String reportType) {
        DataBaseAssembly dataBaseAssembly = REPORT_POOL.get(reportType);
        if (dataBaseAssembly == null) {
            throw new ClassNotFoundException("no DataBaseAssembly was found");
        } else {
            return dataBaseAssembly;
        }
    }

}
