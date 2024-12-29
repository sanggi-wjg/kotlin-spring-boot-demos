package com.raynor.demo.dbvendor.mysql

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderMySQLRepository : JpaRepository<OrderMySQLEntity, Int>
