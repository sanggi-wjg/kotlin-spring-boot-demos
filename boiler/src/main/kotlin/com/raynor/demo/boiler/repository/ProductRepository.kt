package com.raynor.demo.boiler.repository

import com.raynor.demo.boiler.domain.product.Product
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<Product, Long>
