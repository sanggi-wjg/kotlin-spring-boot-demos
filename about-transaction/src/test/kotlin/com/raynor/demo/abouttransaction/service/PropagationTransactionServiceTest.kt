package com.raynor.demo.abouttransaction.service

import com.raynor.demo.abouttransaction.repository.CategoryRepository
import com.raynor.demo.abouttransaction.repository.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PropagationTransactionServiceTest(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val propagationTransactionService: PropagationTransactionService
) : FunSpec({

    beforeEach {
        productRepository.deleteAll()
        categoryRepository.deleteAll()
    }

    test("t - 2") {
        /*
        부모 트랜잭션은 롤백이 되었다.
        자식 트랜잭션들은 모두 성공적으로 처리 되었다.
SET autocommit=0
insert into `product` (`created_at`,`deleted`,`name`,`price`,`stock_quantity`) values ('2024-08-02 08:05:24.18801',0, '상품 2 408c16b6-0606-4335-bba4-24f0fa8836ca',36,16)
insert into `product` (`created_at`,`deleted`,`name`,`price`,`stock_quantity`) values ('2024-08-02 08:05:24.214487',0,'상품 3 4906ca6f-acb6-4680-ac7f-db0986784997',85,24)
commit
SET autocommit=1
        * */
        shouldThrow<Exception> {
            propagationTransactionService.insertWithNewTransactionAndMainMethodException()
        }
    }

    test("t - 3") {
        /*
        부모 트랜잭션은 당연히 롤백이 안되었고
        자식 트랜잭션들중 2번째 트랜잭션이 롤백이 안되었다.
SET autocommit=0
insert into `product` (`created_at`,`deleted`,`name`,`price`,`stock_quantity`) values ('2024-08-02 08:08:47.557494',0,'상품 1 e4b3bb4c-de27-4c4f-a030-a4afda7d1b4a',58,87)
insert into `product` (`created_at`,`deleted`,`name`,`price`,`stock_quantity`) values ('2024-08-02 08:08:47.578947',0,'상품 2 7066f251-e45f-440b-8a08-7a54a1ed376f',41,75)
insert into `product` (`created_at`,`deleted`,`name`,`price`,`stock_quantity`) values ('2024-08-02 08:08:47.581054',0,'상품 3 5a98eb6b-0e04-43ef-9105-e1bb3e099c67',56,6)
commit
SET autocommit=1
        * */
        propagationTransactionService.insertWithNewTransaction()
    }
})