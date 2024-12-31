package com.raynor.demo.dbvendor.mysql

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@ConfigurationProperties(prefix = "spring.jpa-mysql")
data class MySQLJpaProperty(
    val openInView: Boolean,
    val ddlAuto: String,
    val useSqlComments: Boolean,
    val defaultBatchFetchSize: Int,
    val showSql: Boolean,
    val formatSql: Boolean,
    val globallyQuotedIdentifiers: Boolean,
)

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = [MySQLConfig.PACKAGE_ROOT],
    entityManagerFactoryRef = MySQLConfig.ENTITY_MANAGER_FACTORY,
    transactionManagerRef = MySQLConfig.TRANSACTION_MANAGER,
)
class MySQLConfig(
    private val mySQLJpaProperty: MySQLJpaProperty,
) {

    companion object {
        const val PACKAGE_ROOT = "com.raynor.demo.dbvendor.mysql"
        const val DATA_SOURCE = "mysqlDataSource"
        const val DATA_SOURCE_PROPERTIES = "mysqlDataSourceProperties"
        const val ENTITY_MANAGER_FACTORY = "mysqlEntityManagerFactory"
        const val TRANSACTION_MANAGER = "mysqlTransactionManager"
    }

    @Bean(DATA_SOURCE_PROPERTIES)
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    fun dataSourceProperties(): HikariConfig {
        return HikariConfig()
    }

    @Bean(DATA_SOURCE)
    fun dataSource(): DataSource {
        return HikariDataSource(dataSourceProperties())
    }

    @Bean(ENTITY_MANAGER_FACTORY)
    fun entityManagerFactory(
        @Qualifier(DATA_SOURCE) dataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        return LocalContainerEntityManagerFactoryBean().apply {
            this.dataSource = dataSource
            this.setPackagesToScan(PACKAGE_ROOT)
            this.jpaVendorAdapter = HibernateJpaVendorAdapter()

            // fixme hibernate key 이름이 제대로 안되어있음
            this.setJpaPropertyMap(
                mapOf(
                    "hibernate.dialect" to "org.hibernate.dialect.MySQL8Dialect",
                    "hibernate.open_in_view" to mySQLJpaProperty.openInView,
                    "hibernate.hbm2ddl.auto" to mySQLJpaProperty.ddlAuto,
                    "hibernate.use_sql_comments" to mySQLJpaProperty.useSqlComments,
                    "hibernate.default_batch_fetch_size" to mySQLJpaProperty.defaultBatchFetchSize,
                    "hibernate.show_sql" to mySQLJpaProperty.showSql,
                    "hibernate.format_sql" to mySQLJpaProperty.formatSql,
                    "hibernate.globally_quoted_identifiers" to mySQLJpaProperty.globallyQuotedIdentifiers
                )
            )
        }
    }

    @Bean(name = [TRANSACTION_MANAGER])
    fun transactionManager(
        @Qualifier(ENTITY_MANAGER_FACTORY) entityManagerFactory: LocalContainerEntityManagerFactoryBean
    ): PlatformTransactionManager {
        return JpaTransactionManager().apply {
            this.entityManagerFactory = entityManagerFactory.`object`
        }
    }
}
