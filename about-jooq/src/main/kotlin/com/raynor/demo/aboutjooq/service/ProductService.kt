package com.raynor.demo.aboutjooq.service

import com.raynor.demo.aboutjooq.entity.tables.records.ProductRecord
import com.raynor.demo.aboutjooq.model.ProductCreate
import com.raynor.demo.aboutjooq.model.ProductModel
import com.raynor.demo.aboutjooq.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import kotlin.random.Random

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
) {
    @Transactional(readOnly = true)
    fun getAllProducts(): List<ProductModel> {
        return productRepository.findAll().map {
            ProductModel.from(it)
        }
    }

    @Transactional(readOnly = true)
    fun getProductById(id: Int): ProductModel {
        return productRepository.findById(id)?.let {
            ProductModel.from(it)
        } ?: throw RuntimeException("Product not found")
    }

    @Transactional
    fun createProducts1() {
        /*
2025-07-22T08:50:12.132721Z        70 Query     SET autocommit=1
2025-07-22T08:50:12.133029Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('99ee7e82-1ef3-47d6-a7ee-43c613e14d4f', null, 1451, '2025-07-22 17:50:11.952766', '2025-07-22 17:50:11.952778')
2025-07-22T08:50:12.135370Z        70 Query     SELECT @@session.transaction_read_only
2025-07-22T08:50:12.139086Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('94571215-8406-4f7d-ac79-7669db6d2a6f', null, 716, '2025-07-22 17:50:11.95289', '2025-07-22 17:50:11.952894')
2025-07-22T08:50:12.140375Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('3ca35be0-5cd3-448c-836e-9b1be6c7aa42', null, 8098, '2025-07-22 17:50:11.952921', '2025-07-22 17:50:11.952925')
2025-07-22T08:50:12.142458Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('49a57ae0-e1ed-46b5-9e85-95c9a699ecc2', null, 5787, '2025-07-22 17:50:11.952966', '2025-07-22 17:50:11.952968')
2025-07-22T08:50:12.144387Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('7f463c39-42fd-4a00-897d-62ebfb669972', null, 7328, '2025-07-22 17:50:11.953007', '2025-07-22 17:50:11.953011')
2025-07-22T08:50:12.146200Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('78643ff5-7085-467c-b356-ac293da97d14', null, 1309, '2025-07-22 17:50:11.953048', '2025-07-22 17:50:11.953051')
2025-07-22T08:50:12.147953Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('fcf0898e-385a-4189-907f-2dff4ab5248d', null, 127, '2025-07-22 17:50:11.953091', '2025-07-22 17:50:11.953093')
2025-07-22T08:50:12.150815Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('bd884bfc-ef71-4622-b56a-7819875c1d21', null, 1009, '2025-07-22 17:50:11.953125', '2025-07-22 17:50:11.953129')
2025-07-22T08:50:12.152202Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('a84b96c5-d6c5-4f14-a72f-14fed8f026f5', null, 9978, '2025-07-22 17:50:11.953168', '2025-07-22 17:50:11.953171')
2025-07-22T08:50:12.155652Z        68 Query     COMMIT
2025-07-22T08:50:12.161628Z        68 Query     SET autocommit=1
        * */
        return (1..10).map {
            ProductRecord(
                name = UUID.randomUUID().toString(),
                memo = Random.nextInt(4).takeIf { it == 4 }?.let { UUID.randomUUID().toString() },
                price = Random.nextInt(10000).toBigDecimal(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        }.let {
            productRepository.saveWithMultipleInsert(it)
        }
    }

    @Transactional
    fun createProducts2() {
        /*
2025-07-22T08:50:51.542797Z        68 Query     SET autocommit=0
2025-07-22T08:50:51.583477Z        68 Query     SELECT @@session.transaction_read_only
2025-07-22T08:50:51.593137Z        68 Query     insert into `jooq`.`product` (`name`, `memo`, `price`, `created_at`, `updated_at`) values ('850689d7-bcbd-4104-a08d-b0989d850a72', null, 3769, '2025-07-22 17:50:51.547463', '2025-07-22 17:50:51.547639'), ('64f3b0e3-f3eb-44e0-8d0f-d8074722e2b6', null, 5817, '2025-07-22 17:50:51.547743', '2025-07-22 17:50:51.547748'), ('8bcfd77f-8d0e-49e7-a37c-fb68dfb6a462', null, 4727, '2025-07-22 17:50:51.547764', '2025-07-22 17:50:51.547879'), ('a2458876-ec55-4668-a4d4-57559cfb1508', null, 8968, '2025-07-22 17:50:51.547903', '2025-07-22 17:50:51.547907'), ('a1518a8b-dfe5-49e0-ac78-4e7325aba046', null, 3188, '2025-07-22 17:50:51.547923', '2025-07-22 17:50:51.547926'), ('e7c511c7-3059-49a5-8400-ee3ec68a5f61', null, 246, '2025-07-22 17:50:51.547938', '2025-07-22 17:50:51.547941'), ('5823e6ac-dcb3-4ddc-a9d6-8140c5ee46a0', null, 8631, '2025-07-22 17:50:51.547952', '2025-07-22 17:50:51.547955'), ('b533ec1e-50b5-4e2b-9bfb-cb4a277723eb', null, 2099, '2025-07-22 17:50:51.547966', '2025-07-22 17:50:51.547969'), ('0534393c-19fb-4d1d-b9b6-11d05a1f1383', null, 2736, '2025-07-22 17:50:51.547978', '2025-07-22 17:50:51.547981'), ('06840d37-c26a-4383-ac1c-a43af83dbbe4', null, 4844, '2025-07-22 17:50:51.547998', '2025-07-22 17:50:51.548001')
2025-07-22T08:50:51.596811Z        68 Query     COMMIT
2025-07-22T08:50:51.599099Z        68 Query     SET autocommit=1
        * */
        return (1..10).map {
            ProductCreate(
                name = UUID.randomUUID().toString(),
                memo = null,
                price = Random.nextInt(10000).toBigDecimal(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
        }.let {
            productRepository.saveWithValues(it)
        }
    }
}