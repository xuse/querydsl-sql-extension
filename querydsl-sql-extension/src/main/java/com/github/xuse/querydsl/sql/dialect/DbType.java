package com.github.xuse.querydsl.sql.dialect;

import com.github.xuse.querydsl.util.ArrayUtils;
import com.github.xuse.querydsl.util.StringUtils;
import com.github.xuse.querydsl.util.lang.Enums;
import com.querydsl.sql.CUBRIDTemplates;
import com.querydsl.sql.DB2Templates;
import com.querydsl.sql.DerbyTemplates;
import com.querydsl.sql.FirebirdTemplates;
import com.querydsl.sql.H2Templates;
import com.querydsl.sql.HSQLDBTemplates;
import com.querydsl.sql.OracleTemplates;
import com.querydsl.sql.PostgreSQLTemplates;
import com.querydsl.sql.SQLServer2005Templates;
import com.querydsl.sql.SQLServerTemplates;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.SQLiteTemplates;
import com.querydsl.sql.TeradataTemplates;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum DbType {
    mysql{public SQLTemplates templates() {return new MySQLWithJSONTemplates();}}, 
    mariadb{public SQLTemplates templates() {return new MySQLWithJSONTemplates();}}, 
    oracle{public SQLTemplates templates() {return OracleTemplates.builder().newLineToSingleSpace().build();}}, 
    db2{public SQLTemplates templates() {return new DB2Templates();}}, 
    h2{public SQLTemplates templates() {return H2Templates.builder().newLineToSingleSpace().build();}}, 
    hsql{public SQLTemplates templates() {return new HSQLDBTemplates();}}, 
    sqlite{public SQLTemplates templates() {return SQLiteTemplates.builder().newLineToSingleSpace().build();}}, 
    postgresql{public SQLTemplates templates() {return PostgreSQLTemplates.builder().newLineToSingleSpace().build();}}, 
    sqlserver2005("SQLServer2005"){public SQLTemplates templates() {return SQLServer2005Templates.builder().newLineToSingleSpace().build();}},
    sqlserver("SQLServer"){public SQLTemplates templates() {return SQLServerTemplates.builder().newLineToSingleSpace().build();}}, 
    dm("达梦"){public SQLTemplates templates() {return new OracleTemplates();}},
    xugu("虚谷"), 
    phoenix("Phoenix HBase"), 
    zenith("Gauss"), 
    clickhouse, 
    gbase8s("南大通用/Gbase8S", "gbasedbt", "gbasedbt_sqli"),
    gbase8c("南大通用 GBase 8c"), 
    sinodb("星瑞格"), 
    oscar("神通"),
    sybase("Sybase ASE"), 
    oceanbase, 
    Firebird("firebirdsql"){public SQLTemplates templates() {return new FirebirdTemplates();}},
    highgo("瀚高"), 
    cubrid{public SQLTemplates templates() {return new CUBRIDTemplates();}}, 
    sundb,
    hana("SAP_HANA"), 
    impala,
    vertica, xcloud("行云"), 
    redshift, 
    openGauss("华为 opengauss"), 
    gaussdb, 
    TDengine("taos", "taos-rs"),
    informix, 
    uxdb("优炫"), 
    lealone, 
    trino, 
    presto, 
    jtds("SQLServer And Sybase driver"), 
    derby{public SQLTemplates templates() {return DerbyTemplates.builder().newLineToSingleSpace().build();}}, 
    hive, kingbase("人大金仓", "kingbase8"), odps,
    teradata{public SQLTemplates templates() {return new TeradataTemplates();}}, 
    edb, kylin, ads, elastic_search, hbase, drds, blink, antspark, 
    oceanbase_oracle{public SQLTemplates templates() {return new OracleTemplates();}}, 
    polardb, 
    ali_oracle{public SQLTemplates templates() {return new OracleTemplates();}}, 
    mock, greenplum{public SQLTemplates templates() {return new PostgreSQLTemplates();}},
    tidb, tydb, starrocks, ingres, cloudscape, timesten, as400, sapdb, kdb, log4jdbc, JSQLConnect, JTurbo, interbase,
    pointbase, edbc, mimer, other;

    private DbType() {
        name = name();
        this.alias = ArrayUtils.EMPTY_STRING_ARRAY;
    }

    private DbType(String name, String... alias) {
        this.name = name;
        this.alias = alias;
    }

    public final String name;

    public final String[] alias;

    public SQLTemplates templates() {
        throw new UnsupportedOperationException(name());
    }

    public static DbType find(String productName) {
        DbType dbType = Enums.valueOf(DbType.class, productName, null);
        if (dbType == null) {
            dbType = DbType.ofAlias(productName);
        }
        if (dbType != null) {
            return dbType;
        }
        log.warn("Unable to determine dbtype for {}", productName);
        return DbType.other;
    }

    public static DbType ofAlias(String alias) {
        for (DbType type : values()) {
            if (type.alias.length > 0) {
                for (String a : type.alias) {
                    if (a.equals(alias)) {
                        return type;
                    }
                }
            }
        }
        return null;
    }
    
    public static String extractDbNameFromURL(String url) {
        if(url.startsWith("jdbc:")) {
            String s=url.substring(5);
            s=StringUtils.substringBefore(s, ":");
        }
        return url;
    }
}
