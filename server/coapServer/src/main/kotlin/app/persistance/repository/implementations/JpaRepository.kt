package com.nyanthingy.app.persistance.repository.implementations

import com.nyanthingy.app.config.JpaConfig
import com.nyanthingy.app.persistance.repository.interfaces.Repository
import jakarta.persistence.EntityManager

/**
 * A generic implementation of repository using Jpa
 */
open class JpaRepository<T, ID> protected constructor(protected val entityClass: Class<T>) :
    Repository<T, ID> {

    override fun insert(entity: T): T {
        val tx = entityManager.transaction
        tx.begin()
        entityManager.persist(entity)
        tx.commit()
        return entity
    }

    override fun update(entity: T): T {
        val tx = entityManager.transaction
        tx.begin()
        entityManager.merge(entity)
        tx.commit()
        return entity
    }

    override fun delete(entity: T) {
        val tx = entityManager.transaction
        tx.begin()
        entityManager.remove(if (entityManager.contains(entity)) entity else entityManager.merge(entity))
        tx.commit()
    }

    override fun findById(id: ID): T {
        return entityManager.find(
            entityClass, id
        )
    }

    override fun findAll(): List<T> {
        val criteriaQuery = entityManager.criteriaBuilder.createQuery(entityClass)
        criteriaQuery.select(criteriaQuery.from(entityClass))
        return entityManager.createQuery(criteriaQuery).resultList
    }

    protected val entityManager: EntityManager = JpaConfig.entityManagerFactory.createEntityManager()
    override fun close() = entityManager.close()
}