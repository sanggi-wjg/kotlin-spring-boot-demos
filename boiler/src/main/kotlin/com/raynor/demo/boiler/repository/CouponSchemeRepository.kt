package com.raynor.demo.boiler.repository

import com.raynor.demo.boiler.domain.coupon.CouponScheme
import org.springframework.data.jpa.repository.JpaRepository

interface CouponSchemeRepository : JpaRepository<CouponScheme, Int>
