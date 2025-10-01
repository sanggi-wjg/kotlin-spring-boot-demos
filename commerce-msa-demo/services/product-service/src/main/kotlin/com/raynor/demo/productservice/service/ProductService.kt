package com.raynor.demo.productservice.service

import com.raynor.demo.productservice.api.dto.request.CreateProductRequestDto
import com.raynor.demo.productservice.api.dto.response.ProductResponseDto
import com.raynor.demo.productservice.rds.repository.ProductRdsRepository
import com.raynor.demo.productservice.service.condition.ProductSearchCondition
import com.raynor.demo.productservice.service.mapper.toEntity
import com.raynor.demo.productservice.service.mapper.toResponseDto
import com.raynor.demo.shared.typed.product.ProductId
import com.raynor.demo.shared.typed.product.toProductId
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRdsRepository: ProductRdsRepository
) {
    @Transactional
    fun createProduct(requestDto: CreateProductRequestDto): ProductId {
        return productRdsRepository.save(requestDto.toEntity()).let {
            it.id!!.toProductId()
        }
    }

    @Transactional(readOnly = true)
    fun getProducts(searchCondition: ProductSearchCondition): List<ProductResponseDto> {
        return productRdsRepository.findPageByCondition(searchCondition).map {
            it.toResponseDto()
        }
    }

    @Transactional(readOnly = true)
    fun getProductById(id: ProductId): ProductResponseDto {
        return productRdsRepository.findByIdOrNull(id.value)?.toResponseDto()
            ?: throw EntityNotFoundException("상품 ID: ${id}를 찾을 수 없습니다.")
    }
}
