package com.raynor.demo.dbperformance.service

import com.raynor.demo.dbperformance.config.DataSourceConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service(MySQL5Service.BEAN_NAME)
class MySQL5Service(
    @Qualifier(DataSourceConfig.MYSQL_5_JDBC_TEMPLATE) jdbcTemplate: JdbcTemplate,
) : MySQLService(jdbcTemplate) {

    companion object {
        const val BEAN_NAME = "mysql5Service"
    }
}
