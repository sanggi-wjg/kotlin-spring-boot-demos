package com.raynor.demo.abouttransaction.service.persisit

import com.raynor.demo.abouttransaction.entity.CategoryEntity
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
    fun copyEntity() {
        val categories = categoryRepository.findAll()
        categoryRepository.saveAll(
            categories.map { it.copy() }
        )
    }

    @Transactional
    fun permanenceSimple() {
        /*
        Product: 2, true

        ProductOption: 3, M1 Vanilla, 5000.00, true
        ProductOption: 4, M1 Prod, 10000.00, true
        ProductOption: 5, M1 Ultra, 20000.00, true

        ProductCategoryMapping: 2, 노트북, true
        ProductCategoryMapping: 3, 전자기기, true

        First Category: 2, 노트북, true

        Category: 2, 노트북, true
        Category: 3, 전자기기, true

        select pe1_0.`id`,pe1_0.`name` from `product` pe1_0 where pe1_0.`id`=2
        select mpo1_0.`product_id`,mpo1_0.`id`,mpo1_0.`name`,mpo1_0.`price` from `product_option` mpo1_0 where mpo1_0.`product_id`=2
        select mpcm1_0.`product_id`,mpcm1_0.`id`,mpcm1_0.`category_id` from `product_category_mapping` mpcm1_0 where mpcm1_0.`product_id`=2
        select ce1_0.`id`,ce1_0.`name` from `category` ce1_0 where ce1_0.`id` in (2,3,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null)
        select ce1_0.`id`,ce1_0.`name` from `category` ce1_0 where ce1_0.`id` in (2,3)
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

        val categoryForAssert = CategoryEntity("노트북").apply { updateId(4) }
        logger.info("@@@ categoryForAssert: ${entityManager.contains(categoryForAssert)}, ${entityManager.find(CategoryEntity::class.java, 4)}")

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

        logger.info("@@@111 categoryForAssert: ${entityManager.contains(categoryForAssert)}, ${entityManager.find(CategoryEntity::class.java, 4)}")

        val categoryForAssert2 = categoryRepository.findByIdOrNull(4) ?: throw Exception("category not found")
        logger.info("@@@ categoryForAssert2: ${entityManager.contains(categoryForAssert2)}, ${entityManager.find(CategoryEntity::class.java, 4)}")
    }
}