package app.persistance.repository.implementations

import app.persistance.model.GSPoint
import app.persistance.repository.interfaces.GSPointRepository
import java.util.*

class GSPointRepositoryImpl : GSPointRepository,
    JpaRepository<GSPoint, Date>(GSPoint::class.java) {
}