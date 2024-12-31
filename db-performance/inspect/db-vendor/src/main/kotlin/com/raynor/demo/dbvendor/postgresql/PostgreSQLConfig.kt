package com.raynor.demo.dbvendor.postgresql

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

@ConfigurationProperties(prefix = "spring.jpa-postgresql")
data class PostgreSQLJpaProperty(
    val openInView: Boolean,
    val ddlAuto: String,
    val useSqlComments: Boolean,
    val defaultBatchFetchSize: Int,
    val showSql: Boolean,
    val formatSql: Boolean,
    val globallyQuotedIdentifiers: Boolean,
    val defaultSchema: String,
)

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = [PostgreSQLConfig.PACKAGE_ROOT],
    entityManagerFactoryRef = PostgreSQLConfig.ENTITY_MANAGER_FACTORY,
    transactionManagerRef = PostgreSQLConfig.TRANSACTION_MANAGER,
)
class PostgreSQLConfig(
    val postgreSQLJpaProperty: PostgreSQLJpaProperty
) {

    companion object {
        const val PACKAGE_ROOT = "com.raynor.demo.dbvendor.postgresql"
        const val DATA_SOURCE = "postgresqlDataSource"
        const val DATA_SOURCE_PROPERTIES = "postgresqlDataSourceProperties"
        const val ENTITY_MANAGER_FACTORY = "postgresqlEntityManagerFactory"
        const val TRANSACTION_MANAGER = "postgresqlTransactionManager"
    }

    @Bean(DATA_SOURCE_PROPERTIES)
    @ConfigurationProperties(prefix = "spring.datasource.postgresql")
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

            this.setJpaPropertyMap(
                mapOf(
                    "hibernate.dialect" to "org.hibernate.dialect.PostgreSQLDialect",
                    "hibernate.open_in_view" to postgreSQLJpaProperty.openInView,
                    "hibernate.hbm2ddl.auto" to postgreSQLJpaProperty.ddlAuto,
                    "hibernate.use_sql_comments" to postgreSQLJpaProperty.useSqlComments,
                    "hibernate.default_batch_fetch_size" to postgreSQLJpaProperty.defaultBatchFetchSize,
                    "hibernate.show_sql" to postgreSQLJpaProperty.showSql,
                    "hibernate.format_sql" to postgreSQLJpaProperty.formatSql,
                    "hibernate.globally_quoted_identifiers" to postgreSQLJpaProperty.globallyQuotedIdentifiers,
                    "hibernate.default_schema" to postgreSQLJpaProperty.defaultSchema,
                )
            )
        }
    }

    @Bean(TRANSACTION_MANAGER)
    fun transactionManager(
        @Qualifier(ENTITY_MANAGER_FACTORY) entityManagerFactory: LocalContainerEntityManagerFactoryBean
    ): PlatformTransactionManager {
        return JpaTransactionManager().apply {
            this.entityManagerFactory = entityManagerFactory.`object`

        }
    }
//    class CustomJpaTransactionManager : JpaTransactionManager() {
//
//        override fun doBegin(transaction: Any, definition: TransactionDefinition) {
//            super.doBegin(transaction, definition)
//            TransactionSynchronizationManager.setCurrentTransactionIsolationLevel(TransactionDefinition.ISOLATION_DEFAULT)
//
//        }
//    }
}
