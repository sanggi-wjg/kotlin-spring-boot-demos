package com.raynor.demo.transactionaloutbox.infra.kafka

object KafkaTopic {
    const val USER_SIGNED = "USER_SIGNED.events"
    const val USER_UPDATED = "USER_UPDATED.events"
    const val USER_LEFT = "USER_LEFT.events"

    const val PRODUCT_CREATED = "PRODUCT_CREATED.events"
    const val PRODUCT_UPDATED = "PRODUCT_UPDATED.events"
    const val PRODUCT_DELETED = "PRODUCT_DELETED.events"

    const val ORDER_CREATED = "ORDER_CREATED.events"
}
