package com.raynor.demo.abouttransaction.service

import com.raynor.demo.abouttransaction.repository.CategoryRepository
import com.raynor.demo.abouttransaction.repository.ProductRepository
import com.raynor.demo.abouttransaction.service.transaction.BasicTransactionService
import io.kotest.core.spec.style.FunSpec
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class BasicTransactionServiceTest(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val basicTransactionService: BasicTransactionService,
) : FunSpec({

    beforeEach {
        productRepository.deleteAll()
        categoryRepository.deleteAll()
    }

    test("t - 1") {
        /*
SET autocommit=0
insert into `product` (`created_at`,`deleted`,`name`,`price`,`stock_quantity`) values ('2024-08-02 07:47:03.609793',0,'상품 1 5b742715-bdbb-4a08-920c-eb79bbfdf23e',20,56)
commit
SET autocommit=1
        * */
        basicTransactionService.insertWithBasicTransaction()
    }
})
