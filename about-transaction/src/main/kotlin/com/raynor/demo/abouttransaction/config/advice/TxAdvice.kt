package com.raynor.demo.abouttransaction.config.advice

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class Transaction(advice: TransactionAdvice) {
    init {
        transactionAdvice = advice
    }

    companion object {
        lateinit var transactionAdvice: TransactionAdvice

        fun <T> writeable(block: () -> T): T {
            return transactionAdvice.writer(block)
        }

        fun <T> readonly(block: () -> T): T {
            return transactionAdvice.reader(block)
        }
    }

    @Component
    class TransactionAdvice {

        @Transactional
        fun <T> writer(block: () -> T): T {
            return block.invoke()
        }

        @Transactional(readOnly = true)
        fun <T> reader(block: () -> T): T {
            return block.invoke()
        }
    }
}