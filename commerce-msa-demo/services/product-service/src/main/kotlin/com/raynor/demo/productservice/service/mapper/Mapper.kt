package com.raynor.demo.productservice.service.mapper

import com.raynor.demo.productservice.api.dto.request.CreateProductRequestDto
import com.raynor.demo.productservice.api.dto.response.ProductResponseDto
import com.raynor.demo.productservice.rds.entity.ProductEntity
import com.raynor.demo.shared.typed.product.toProductId
import com.raynor.demo.shared.typed.product.toProductName
import com.raynor.demo.shared.typed.toMoney


fun CreateProductRequestDto.toEntity() = ProductEntity(
    name = name.value,
    price = price.value,
    stockQuantity = stockQuantity.value
)

fun ProductEntity.toResponseDto() = ProductResponseDto(
    id = id!!.toProductId(),
    name = name.toProductName(),
    price = price.toMoney(),
)
