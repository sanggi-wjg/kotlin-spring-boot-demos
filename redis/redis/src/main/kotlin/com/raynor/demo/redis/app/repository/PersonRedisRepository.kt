package com.raynor.demo.redis.app.repository

import com.raynor.demo.redis.app.model.Person
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRedisRepository : CrudRepository<Person, Int>
