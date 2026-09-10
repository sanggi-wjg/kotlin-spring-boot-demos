package com.raynor.demo.boiler.repository

import com.raynor.demo.boiler.domain.coupon.Coupon
import org.springframework.data.jpa.repository.JpaRepository

interface CouponRepository : JpaRepository<Coupon, Long>
