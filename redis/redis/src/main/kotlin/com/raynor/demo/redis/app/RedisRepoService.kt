package com.raynor.demo.redis.app

import com.raynor.demo.redis.app.model.Address
import com.raynor.demo.redis.app.model.Person
import com.raynor.demo.redis.app.repository.PersonRedisRepository
import org.springframework.stereotype.Service

@Service
class RedisRepoService(
    private val personRedisRepository: PersonRedisRepository,
) {

    fun create(): Person {
        /*
        * "SADD" "person" "1"
        * "DEL" "person:1"
        * "HMSET" "person:1" "_class" "com.raynor.demo.redis.app.model.Person" "address.city" "city" "address.street" "street" "age" "1" "id" "1" "name" "hello"
        * */
        return Person(
            id = 1,
            name = "hello",
            age = 1,
            address = Address("street", "city")
        ).let {
            personRedisRepository.save(it)
        }
    }

    fun createList(): List<Person> {
        /*
        * "DEL" "person:2"
        * "HMSET" "person:2" "_class" "com.raynor.demo.redis.app.model.Person" "address.city" "city-2" "address.street" "street-2" "age" "2" "id" "2" "name" "snow"
        * "DEL" "person:3"
        * "HMSET" "person:3" "_class" "com.raynor.demo.redis.app.model.Person" "address.city" "city-3" "address.street" "street-3" "age" "3" "id" "3" "name" "email"
        * */
        return listOf(
            Person(
                id = 2,
                name = "snow",
                age = 2,
                address = Address("street-2", "city-2")
            ),
            Person(
                id = 3,
                name = "email",
                age = 3,
                address = Address("street-3", "city-3")
            )
        ).let {
            personRedisRepository.saveAll(it).toList()
        }
    }

    fun update(): Person {
        /*
        * "DEL" "person:1"
        * "HMSET" "person:1" "_class" "com.raynor.demo.redis.app.model.Person" "address.city" "city" "address.street" "street" "age" "1" "id" "1" "name" "updated kk"
        * */
        return Person(
            id = 1,
            name = "updated kk",
            age = 1,
            address = Address("street", "city")
        ).let {
            personRedisRepository.save(it)
        }
    }
    
    fun delete(): Boolean {
        /*
        * "HGETALL" "person:1"
        * "DEL" "person:1"
        * "SREM" "person" "1"
        * "SMEMBERS" "person:1:idx"
        * "DEL" "person:1:idx"
        * */
        personRedisRepository.deleteById(1)
        return true
    }
}
