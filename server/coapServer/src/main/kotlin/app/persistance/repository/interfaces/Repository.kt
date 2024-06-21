package app.persistance.repository.interfaces


/**
 * @param T entity
 * @param ID identifier
 */
interface Repository<T, ID> {

    fun insert(entity: T): T

    fun update(entity: T): T

    fun delete(entity: T)

    fun findById(id: ID): T

    fun findAll(): List<T>

    fun close()
}