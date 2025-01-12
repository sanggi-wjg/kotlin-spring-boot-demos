package com.raynor.demo.mysql.entity

import jakarta.persistence.*
import org.jetbrains.annotations.NotNull
import java.time.Instant

@Entity
@Table(name = "user", schema = "security")
class UserEntity(
    name: String,
    email: String,
    hashedPassword: String,
    isAdmin: Boolean,
    createdAt: Instant,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Int? = null
        private set

    @NotNull
    @Column(name = "name", nullable = false, length = 100)
    var name: String = name
        private set

    @NotNull
    @Column(name = "email", nullable = false, length = 100, unique = true)
    var email: String = email
        private set

    @NotNull
    @Column(name = "password", nullable = false, length = 100)
    var password: String = hashedPassword
        private set

    @NotNull
    @Column(name = "is_admin", nullable = false)
    var isAdmin: Boolean = isAdmin
        private set

    @NotNull
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = createdAt
        private set

    @NotNull
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = createdAt
        private set

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    private val mutableDevices = mutableListOf<DeviceEntity>()
    val devices: List<DeviceEntity>
        get() = mutableDevices.toList()

    fun addDevice(device: DeviceEntity) {
        mutableDevices.add(device)
    }
}
