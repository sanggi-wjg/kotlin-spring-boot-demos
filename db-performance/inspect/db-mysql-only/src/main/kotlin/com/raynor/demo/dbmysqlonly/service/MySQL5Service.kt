package com.raynor.demo.dbmysqlonly.service

import com.raynor.demo.dbmysqlonly.config.DatabaseConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service(MySQL5Service.BEAN_NAME)
class MySQL5Service(
    @Qualifier(DatabaseConfig.MYSQL_5_JDBC_TEMPLATE) jdbcTemplate: JdbcTemplate,
) : MySQLService(jdbcTemplate) {

    companion object {
        const val BEAN_NAME = "mysql5Service"
    }
}
