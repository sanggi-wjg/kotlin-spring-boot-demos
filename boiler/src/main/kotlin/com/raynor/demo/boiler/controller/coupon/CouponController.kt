package com.raynor.demo.boiler.controller.coupon

import com.raynor.demo.boiler.controller.coupon.dto.CouponResponseDto
import com.raynor.demo.boiler.service.coupon.CouponFacadeService
import com.raynor.demo.boiler.shared.http.ApiHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1")
class CouponController(
    private val couponFacadeService: CouponFacadeService,
) {
    @PostMapping("/coupon-schemes/{couponSchemeId}/coupons")
    fun issueCoupon(
        @RequestHeader(ApiHeaders.USER_ID) userId: Int,
        @PathVariable("couponSchemeId") couponSchemeId: Int,
    ): ResponseEntity<CouponResponseDto> {
        return couponFacadeService.issueCoupon(
            couponSchemeId = couponSchemeId,
            userId = userId,
        ).let { coupon ->
            ResponseEntity.created(URI.create("/api/v1/coupons/${coupon.id}")).body(
                CouponResponseDto.fromModel(coupon),
            )
        }
    }
}
