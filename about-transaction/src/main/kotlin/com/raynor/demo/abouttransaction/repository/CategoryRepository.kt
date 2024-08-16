package com.raynor.demo.abouttransaction.repository

import com.raynor.demo.abouttransaction.entity.CategoryEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CategoryRepository : JpaRepository<CategoryEntity, Int> {

}