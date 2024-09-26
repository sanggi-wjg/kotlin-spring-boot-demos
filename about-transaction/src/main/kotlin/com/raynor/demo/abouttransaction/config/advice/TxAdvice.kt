package com.raynor.demo.abouttransaction.config.advice

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class Tx(advice: TxAdvice) {
    init {
        txAdvice = advice
    }

    companion object {
        lateinit var txAdvice: TxAdvice

        fun <T> writeable(block: () -> T): T {
            return txAdvice.writer(block)
        }

        fun <T> readonly(block: () -> T): T {
            return txAdvice.reader(block)
        }
    }

    @Component
    class TxAdvice {

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