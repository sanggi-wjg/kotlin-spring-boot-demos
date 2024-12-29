package com.raynor.demo.dbvendor.mongodb

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderMongoDBRepository : MongoRepository<OrderDocument, ObjectId>