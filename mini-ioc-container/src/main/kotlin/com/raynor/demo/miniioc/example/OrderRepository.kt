package com.raynor.demo.miniioc.example

interface OrderRepository

class DefaultOrderRepository : OrderRepository

class JdbcOrderRepository : OrderRepository
