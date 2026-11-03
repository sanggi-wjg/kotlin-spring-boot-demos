package com.raynor.demo.boiler.controller.coupon

import com.raynor.demo.boiler.controller.coupon.dto.CouponResponseDto
import com.raynor.demo.boiler.controller.coupon.dto.CouponSchemeCreateRequestDto
import com.raynor.demo.boiler.controller.coupon.dto.CouponSchemeLimitResponseDto
import com.raynor.demo.boiler.controller.coupon.dto.CouponSchemeResponseDto
import com.raynor.demo.boiler.service.coupon.CouponFacadeService
import com.raynor.demo.boiler.service.coupon.CouponSchemeService
import com.raynor.demo.boiler.service.coupon.LimitedCouponIssueService
import com.raynor.demo.boiler.service.coupon.model.CouponSchemeCreateRequest
import com.raynor.demo.boiler.shared.http.ApiHeaders
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1")
class CouponController(
    private val couponFacadeService: CouponFacadeService,
    private val couponSchemeService: CouponSchemeService,
    private val limitedCouponIssueService: LimitedCouponIssueService,
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
            ResponseEntity
                .created(URI.create("/api/v1/users/me/coupons"))
                .body(CouponResponseDto.fromModel(coupon))
        }
    }

    @GetMapping("/coupon-schemes/{couponSchemeId}")
    fun getCouponScheme(
        @PathVariable("couponSchemeId") couponSchemeId: Int,
    ): ResponseEntity<CouponSchemeResponseDto> {
        return couponSchemeService.getCouponSchemeById(
            couponSchemeId = couponSchemeId,
        ).let { couponScheme ->
            ResponseEntity.ok(CouponSchemeResponseDto.fromModel(couponScheme))
        }
    }

    @PostMapping("/coupon-schemes")
    fun createCouponScheme(
        @Valid @RequestBody request: CouponSchemeCreateRequestDto,
    ): ResponseEntity<CouponSchemeResponseDto> {
        return couponSchemeService.createCouponScheme(
            request = CouponSchemeCreateRequest.fromDto(request),
        ).let { couponScheme ->
            ResponseEntity
                .created(URI.create("/api/v1/coupon-schemes/${couponScheme.id}"))
                .body(CouponSchemeResponseDto.fromModel(couponScheme))
        }
    }
}
