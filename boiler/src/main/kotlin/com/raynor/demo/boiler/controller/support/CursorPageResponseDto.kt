package com.raynor.demo.boiler.controller.support

data class CursorPageResponseDto<T, R>(
    val hasNext: Boolean,
    val nextCursor: T?,
    val items: Collection<R>,
)
