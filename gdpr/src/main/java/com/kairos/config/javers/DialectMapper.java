package com.kairos.config.javers;

import org.hibernate.dialect.*;
import org.javers.common.exception.JaversException;
import org.javers.common.exception.JaversExceptionCode;
import org.javers.repository.sql.DialectName;

class DialectMapper {

    public DialectName map(Dialect hibernateDialect) {

        if (hibernateDialect instanceof SQLServerDialect) {
            return DialectName.MSSQL;
        }
        if (hibernateDialect instanceof H2Dialect){
            return DialectName.H2;
        }
        if (hibernateDialect instanceof Oracle8iDialect){
            return DialectName.ORACLE;
        }
        if (hibernateDialect instanceof PostgreSQL81Dialect){
            return DialectName.POSTGRES;
        }
        if (hibernateDialect instanceof MySQLDialect){
            return DialectName.MYSQL;
        }
        throw new JaversException(JaversExceptionCode.UNSUPPORTED_SQL_DIALECT, hibernateDialect.getClass().getSimpleName());
    }
}
