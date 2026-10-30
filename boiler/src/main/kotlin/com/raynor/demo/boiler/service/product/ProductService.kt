package com.raynor.demo.boiler.service.product

import com.raynor.demo.boiler.domain.product.Product
import com.raynor.demo.boiler.domain.support.Money
import com.raynor.demo.boiler.domain.support.Stock
import com.raynor.demo.boiler.repository.ProductLikeRepository
import com.raynor.demo.boiler.repository.ProductRepository
import com.raynor.demo.boiler.repository.UserRepository
import com.raynor.demo.boiler.service.product.model.ProductLikeModel
import com.raynor.demo.boiler.service.product.model.ProductModel
import com.raynor.demo.boiler.service.support.CursorSlice
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class ProductService(
    private val userRepository: UserRepository,
    private val productRepository: ProductRepository,
    private val productLikeRepository: ProductLikeRepository,
) {
    @Transactional(readOnly = true)
    fun getProducts(
        size: Int,
        cursor: Int?,
    ): CursorSlice<Int, ProductModel> {
        val products = productRepository.findAllByCursor(size.toLong() + 1, cursor)
        val items = products.take(size).map { ProductModel.fromEntity(it) }
        return CursorSlice(
            hasNext = products.size > size,
            nextCursor = items.lastOrNull()?.id,
            items = items,
        )
    }

    @Transactional(readOnly = true)
    fun getProduct(productId: Int): ProductModel {
        val product = productRepository.findByIdAndDeletedAtIsNull(productId)
            ?: throw EntityNotFoundException("상품을 찾을 수 없습니다. id=$productId")
        return ProductModel.fromEntity(product)
    }

    @Transactional
    fun createProduct(
        name: String,
        price: BigDecimal,
        quantity: Long,
    ): ProductModel {
        val product = Product(name = name, price = Money(price), stock = Stock(quantity))
        return ProductModel.fromEntity(productRepository.save(product))
    }

    @Transactional
    fun likeProduct(
        userId: Int,
        productId: Int,
    ): ProductLikeModel {
        val product = productRepository.findByIdAndDeletedAtIsNull(productId)
            ?: throw EntityNotFoundException("상품을 찾을 수 없습니다. id=$productId")

        // 중복 생성 시도에도 문제 안되도록 insert ignore로 처리
        productLikeRepository.insertIgnore(productId = productId, userId = userId)
        return ProductLikeModel(
            productId = product.id!!,
            productLikeCount = productLikeRepository.countByProductId(productId),
            isLiked = true,
        )
    }

    @Transactional
    fun unlikeProduct(
        userId: Int,
        productId: Int,
    ): ProductLikeModel {
        val product = productRepository.findByIdAndDeletedAtIsNull(productId)
            ?: throw EntityNotFoundException("상품을 찾을 수 없습니다. id=$productId")

        productLikeRepository.deleteByUserIdAndProductId(userId, productId)
        return ProductLikeModel(
            productId = product.id!!,
            productLikeCount = productLikeRepository.countByProductId(productId),
            isLiked = false,
        )
    }
}
