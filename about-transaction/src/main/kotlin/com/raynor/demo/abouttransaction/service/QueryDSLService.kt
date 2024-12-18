package com.raynor.demo.abouttransaction.service

import com.raynor.demo.abouttransaction.model.condition.ProductSearchCondition
import com.raynor.demo.abouttransaction.model.projection.ProductWitRelatedProjection
import com.raynor.demo.abouttransaction.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class QueryDSLService(
    private val productRepository: ProductRepository,
) {

    fun testProjection() {
        val searchCondition = ProductSearchCondition(
            productIds = emptyList(),
            categoryIds = emptyList(),
        )
        val products = productRepository.findAllByCondition(
            searchCondition = searchCondition,
            constructor = ProductWitRelatedProjection.projection()
        )
    }
}
