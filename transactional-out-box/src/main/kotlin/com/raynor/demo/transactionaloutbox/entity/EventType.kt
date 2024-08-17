package com.raynor.demo.transactionaloutbox.entity

enum class EventType {
    USER_SIGNED,
    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,
    ORDER_CREATED,
}