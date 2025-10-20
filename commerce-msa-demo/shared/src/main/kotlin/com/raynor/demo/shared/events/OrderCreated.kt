package com.raynor.demo.shared.events

sealed interface OrderEvent {
    sealed class OrderCreated : OrderEvent
}