package com.raynor.demo.batchbulkexcel.storage.rds.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@Entity
@Table(
    name = "user_mileage_history",
    indexes = [Index(name = "idx_mileage", columnList = "user_mileage_id")],
)
class UserMileageHistoryEntity(
    userMileage: UserMileageEntity,
    amount: Long,
    reason: String? = null,
    jobId: String? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0
        private set

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_mileage_id", nullable = false, foreignKey = ForeignKey(name = "fk_hist_mileage"))
    var userMileage: UserMileageEntity = userMileage
        private set

    @Column(nullable = false)
    var amount: Long = amount // +적립 / -차감
        private set

    @Column(length = 255)
    var reason: String? = reason
        private set

    @Column(name = "job_id", length = 36)
    var jobId: String? = jobId
        private set

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
        private set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
        private set
}
