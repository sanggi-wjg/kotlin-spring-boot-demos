package com.raynor.demo.miniioc.beans

interface OrderRepository

class DefaultOrderRepository : OrderRepository

class JdbcOrderRepository : OrderRepository
