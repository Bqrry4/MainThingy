package com.nyanthingy.app.persistance.repository.implementations

import com.nyanthingy.app.persistance.model.GSPoint
import com.nyanthingy.app.persistance.repository.interfaces.GSPointRepository
import java.util.*

class GSPointRepositoryImpl : GSPointRepository,
    JpaRepository<GSPoint, Date>(GSPoint::class.java) {
}