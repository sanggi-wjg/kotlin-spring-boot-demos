package com.raynor.demo.abouttransaction.repository

import com.raynor.demo.abouttransaction.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductEntity, Int> {

}