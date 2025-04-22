package com.raynor.demo.aboutgctuning.repository

import com.raynor.demo.aboutgctuning.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProductRepository : JpaRepository<ProductEntity, Int>
