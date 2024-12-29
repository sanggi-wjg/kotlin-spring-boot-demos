package com.raynor.demo.dbvendor.postgresql

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderPostgreSQLRepository : JpaRepository<OrderPostgreSQLEntity, Int>
