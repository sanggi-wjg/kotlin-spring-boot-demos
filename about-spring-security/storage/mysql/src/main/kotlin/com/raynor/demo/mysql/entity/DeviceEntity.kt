package com.raynor.demo.mysql.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull

@Entity
@Table(name = "device", schema = "security")
class DeviceEntity(
    accessToken: String,
    refreshToken: String,
    userAgent: String,
    user: UserEntity,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        private set

    @NotNull
    @Column(name = "access_token", nullable = false, length = 2048)
    var accessToken: String = accessToken
        private set

    @NotNull
    @Column(name = "refresh_token", nullable = false, length = 2048)
    var refreshToken: String = refreshToken
        private set

    @NotNull
    @Column(name = "user_agent", nullable = false, length = 1024)
    var userAgent: String = userAgent
        private set

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity = user
        private set
}