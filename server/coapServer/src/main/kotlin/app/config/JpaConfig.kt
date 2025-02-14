package com.nyanthingy.app.config

import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence


object JpaConfig {

    private const val PERSISTENCE_UNIT_NAME = "jpa-hibernate-mariadb"
    private val properties = mapOf(
        "jakarta.persistence.jdbc.url" to "jdbc:mariadb://mariadb-nyan:3306/nyan",
        "jakarta.persistence.jdbc.user" to "user",
        "jakarta.persistence.jdbc.password" to "pass",
        "jakarta.persistence.jdbc.driver" to "org.mariadb.jdbc.Driver",
        "jakarta.persistence.schema-generation.database.action" to "none",
        "hibernate.dialect" to "org.hibernate.dialect.MariaDBDialect",
        "hibernate.show_sql" to "false",
        "hibernate.format_sql" to "false"
    )

    /**
     * As HibernatePersistenceProvider takes PersistanceUnitInfo that has a deprecated function,
     * use the default Persistance with persistance.xml file
     */
    val entityManagerFactory: EntityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, properties)
}