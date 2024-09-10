package com.raynor.demo.abouttransaction.service

import com.raynor.demo.abouttransaction.repository.CategoryRepository
import com.raynor.demo.abouttransaction.repository.ProductRepository
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EntityManagerService(
    private val entityManager: EntityManager,
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun permanenceSimple() {
        /*
        2024-08-16T16:55:41.921+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : Product: 2, true

        2024-08-16T16:55:41.929+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : ProductOption: 3, M1 Vanilla, 5000.00, true
        2024-08-16T16:55:41.929+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : ProductOption: 4, M1 Prod, 10000.00, true
        2024-08-16T16:55:41.929+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : ProductOption: 5, M1 Ultra, 20000.00, true

        2024-08-16T16:55:41.936+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : ProductCategoryMapping: 2, 노트북, true
        2024-08-16T16:55:41.936+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : ProductCategoryMapping: 3, 전자기기, true

        2024-08-16T16:55:41.937+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : First Category: 2, 노트북, true

        2024-08-16T16:55:41.990+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : Category: 2, 노트북, true
        2024-08-16T16:55:41.991+09:00  INFO 82869 --- [sat] [nio-8080-exec-1] c.r.d.a.service.EntityManagerService     : Category: 3, 전자기기, true

        2024-08-16T07:55:41.918174Z       446 Query     select pe1_0.`id`,pe1_0.`name` from `product` pe1_0 where pe1_0.`id`=2
        2024-08-16T07:55:41.924795Z       446 Query     select mpo1_0.`product_id`,mpo1_0.`id`,mpo1_0.`name`,mpo1_0.`price` from `product_option` mpo1_0 where mpo1_0.`product_id`=2
        2024-08-16T07:55:41.929730Z       446 Query     select mpcm1_0.`product_id`,mpcm1_0.`id`,mpcm1_0.`category_id` from `product_category_mapping` mpcm1_0 where mpcm1_0.`product_id`=2
        2024-08-16T07:55:41.933864Z       446 Query     select ce1_0.`id`,ce1_0.`name` from `category` ce1_0 where ce1_0.`id` in (2,3,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null)
        2024-08-16T07:55:41.989694Z       446 Query     select ce1_0.`id`,ce1_0.`name` from `category` ce1_0 where ce1_0.`id` in (2,3)
        * * */
        val product = productRepository.findByIdOrNull(2) ?: throw Exception("product not found")
        logger.info("Product: ${product.id}, ${entityManager.contains(product)}")

        product.productOptions.forEach {
            logger.info("ProductOption: ${it.id}, ${it.name}, ${it.price}, ${entityManager.contains(it)}")
        }

        product.productCategoryMappings.forEach {
            logger.info("ProductCategoryMapping: ${it.id}, ${it.category.name}, ${entityManager.contains(it)}")
        }

        val categoryIds = product.productCategoryMappings.map { it.category.id }
        val firstCategory = categoryRepository.findByIdOrNull(categoryIds.first()) ?: throw Exception("category not found")
        logger.info("First Category: ${firstCategory.id}, ${firstCategory.name}, ${entityManager.contains(firstCategory)}")

        val categories = categoryRepository.findAllById(categoryIds)
        categories.forEach {
            logger.info("Category: ${it.id}, ${it.name}, ${entityManager.contains(it)}")
        }
    }

    @Transactional
    fun permanenceWitSimpleQueryDsl() {
        /*
SET autocommit=0
select pe1_0.`id`,pe1_0.`name` from `product` pe1_0 join `product_option` poe1_0 on poe1_0.`product_id`=pe1_0.`id` join `product_category_mapping` pcme1_0 on pcme1_0.`product_id`=pe1_0.`id` join `category` ce1_0 on ce1_0.`id`=pcme1_0.`category_id`
select mpo1_0.`product_id`,mpo1_0.`id`,mpo1_0.`name`,mpo1_0.`price` from `product_option` mpo1_0 where mpo1_0.`product_id` in (1,2,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null)
select mpcm1_0.`product_id`,mpcm1_0.`id`,mpcm1_0.`category_id` from `product_category_mapping` mpcm1_0 where mpcm1_0.`product_id` in (1,2,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null)
select ce1_0.`id`,ce1_0.`name` from `category` ce1_0 where ce1_0.`id` in (2,1,3,4,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null)
commit
SET autocommit=1
        * */
        val products = productRepository.findAllWithRelated()

        products.forEach { product ->
            product.productOptions.forEach {
                logger.info("ProductOption: ${it.id}, ${it.name}, ${it.price}")
            }

            product.productCategoryMappings.forEach { categoryMapping ->
                logger.info("ProductCategoryMapping: ${categoryMapping.id}, ${categoryMapping.category.name}")
                logger.info("Category: ${categoryMapping.category.id}, ${categoryMapping.category.name}")
            }
        }

        products.forEach { product ->
            product.productOptions.forEach {
                logger.info("ProductOption: ${it.id}, ${it.name}, ${it.price}, ${entityManager.contains(it)}")
            }

            product.productCategoryMappings.forEach { categoryMapping ->
                logger.info("ProductCategoryMapping: ${categoryMapping.id}, ${categoryMapping.category.name}, ${entityManager.contains(categoryMapping)}")
                logger.info("Category: ${categoryMapping.category.id}, ${categoryMapping.category.name}, ${entityManager.contains(categoryMapping.category)}")
            }
        }
    }
}