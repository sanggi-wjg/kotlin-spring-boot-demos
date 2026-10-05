package com.raynor.demo.boiler.controller.user

import com.raynor.demo.boiler.controller.coupon.dto.CouponResponseDto
import com.raynor.demo.boiler.controller.support.CursorPageResponseDto
import com.raynor.demo.boiler.service.coupon.CouponService
import com.raynor.demo.boiler.shared.http.ApiHeaders
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class UserController(
    private val couponService: CouponService,
) {
    @GetMapping("/users/me/coupons")
    fun getUserCoupons(
        @Min(1) @Max(100)
        @RequestParam("size", required = false, defaultValue = "20") size: Int,
        @RequestParam("cursor", required = false) cursor: Long?,
        @RequestHeader(ApiHeaders.USER_ID) userId: Int,
    ): ResponseEntity<CursorPageResponseDto<Long, CouponResponseDto>> {
        return couponService.getUserCoupons(size, cursor, userId).let { cursorSlice ->
            ResponseEntity.ok(
                CursorPageResponseDto(
                    hasNext = cursorSlice.hasNext,
                    nextCursor = cursorSlice.nextCursor,
                    items = cursorSlice.items.map { item -> CouponResponseDto.fromModel(item) },
                ),
            )
        }
    }
}
