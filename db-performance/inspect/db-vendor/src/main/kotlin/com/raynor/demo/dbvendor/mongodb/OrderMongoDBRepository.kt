package com.raynor.demo.dbvendor.mongodb

import com.raynor.demo.dbvendor.enum.OrderStatus
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderMongoDBRepository : MongoRepository<OrderDocument, ObjectId> {
    fun findAllByStatus(status: OrderStatus): List<OrderDocument>
}