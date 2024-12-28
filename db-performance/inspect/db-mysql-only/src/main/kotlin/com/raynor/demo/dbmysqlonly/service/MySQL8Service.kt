package com.raynor.demo.dbmysqlonly.service

import com.raynor.demo.dbmysqlonly.config.DataSourceConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service(MySQL8Service.BEAN_NAME)
class MySQL8Service(
    @Qualifier(DataSourceConfig.MYSQL_8_JDBC_TEMPLATE) jdbcTemplate: JdbcTemplate,
) : MySQLService(jdbcTemplate) {

    companion object {
        const val BEAN_NAME = "mysql8Service"
    }
}
