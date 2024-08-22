package com.raynor.demo.transactionaloutbox.enums

enum class AggregateType {
    USER_SIGNED,
    USER_UPDATED,
    USER_LEFT,

    PRODUCT_CREATED,
    PRODUCT_UPDATED,
    PRODUCT_DELETED,

    ORDER_CREATED,
    ORDER_TRANSIT_TO_SHIPPING,
}