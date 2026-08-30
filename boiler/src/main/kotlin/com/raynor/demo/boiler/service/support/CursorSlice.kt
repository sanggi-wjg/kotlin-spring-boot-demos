package com.raynor.demo.boiler.service.support

data class CursorSlice<T, R>(
    val hasNext: Boolean,
    val nextCursor: T?,
    val items: Collection<R>,
)
