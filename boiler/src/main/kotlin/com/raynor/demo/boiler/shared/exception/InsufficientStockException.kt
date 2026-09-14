package com.raynor.demo.boiler.shared.exception

class InsufficientStockException(
    val current: Long,
    val requested: Long,
) : RuntimeException("stock is not enough. current=$current, requested=$requested")
