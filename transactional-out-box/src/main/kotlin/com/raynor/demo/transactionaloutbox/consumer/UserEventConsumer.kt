package com.raynor.demo.transactionaloutbox.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.raynor.demo.transactionaloutbox.infra.kafka.KafkaGroup
import com.raynor.demo.transactionaloutbox.infra.kafka.KafkaTopic
import com.raynor.demo.transactionaloutbox.model.event.UserEvent
import com.raynor.demo.transactionaloutbox.repository.OutboxRepository
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserEventConsumer(
    private val objectMapper: ObjectMapper,
    private val outboxRepository: OutboxRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(
        topics = [KafkaTopic.USER_SIGNED],
        groupId = KafkaGroup.SPRING_TOB,
    )
    fun consumerUserEvents(
        @Header(value = "id") outboxId: String,
//        @Header(value = "eventType") eventType: String,
        @Payload value: String,
    ) {
        /*
        Hibernate: insert into `outbox` (`aggregate_id`,`aggregate_type`,`completed_at`,`created_at`,`payload`,`status`) values (?,?,?,?,cast(? as json),?)
        2025-03-28T14:05:42.307+09:00  INFO 11806 --- [sat] [nio-8080-exec-1] c.r.d.t.service.UserService              : User created: User(id=1, name=123, email=123@dev.com, createdAt=2025-03-28T05:05:42.257785Z, updatedAt=2025-03-28T05:05:42.257785Z)
        2025-03-28T14:05:42.839+09:00  INFO 11806 --- [sat] [ntainer#0-0-C-1] c.r.d.t.consumer.UserEventConsumer       : outboxId: 4, value: {"schema":{"type":"struct","fields":[{"type":"int32","optional":true,"field":"userId"}],"optional":true,"name":"payload"},"payload":{"userId":1}}
        Hibernate: select oe1_0.`id`,oe1_0.`aggregate_id`,oe1_0.`aggregate_type`,oe1_0.`completed_at`,oe1_0.`created_at`,oe1_0.`payload`,oe1_0.`status` from `outbox` oe1_0 where oe1_0.`id`=?
        2025-03-28T14:05:42.868+09:00  INFO 11806 --- [sat] [ntainer#0-0-C-1] c.r.d.t.consumer.UserEventConsumer       : do something with user event payload: UserEvent(userId=1)
        Hibernate: update `outbox` set `aggregate_id`=?,`aggregate_type`=?,`completed_at`=?,`created_at`=?,`payload`=cast(? as json),`status`=? where `id`=?
        * */
        logger.info("outboxId: $outboxId, value: $value")

        val outbox = outboxRepository.findByIdOrNull(outboxId.toLong())
            ?: return // go to dead letter queue
        val payload = objectMapper.convertValue(outbox.payload, UserEvent::class.java)
        logger.info("do something with user event payload: $payload")

        outbox.done()
    }

    @KafkaListener(
        topics = [KafkaTopic.PRODUCT_UPDATED],
        groupId = KafkaGroup.SPRING_TOB,
    )
    fun consumerProductEvents(
        @Header(value = "id") outboxId: String,
//        @Header(value = "eventType") eventType: String,
        @Payload value: String,
    ) {
        /*
        Hibernate: insert into `outbox` (`aggregate_id`,`aggregate_type`,`completed_at`,`created_at`,`payload`,`status`) values (?,?,?,?,cast(? as json),?)
        2025-03-28T14:06:34.088+09:00  INFO 11806 --- [sat] [nio-8080-exec-3] c.r.d.t.service.ProductService           : Product updated: Product(id=10, name=연금술사 - 72, price=60047, createdAt=2025-01-02T03:04:05Z, updatedAt=2025-03-28T05:06:34.076970Z)
        2025-03-28T14:06:34.549+09:00  INFO 11806 --- [sat] [ntainer#1-0-C-1] c.r.d.t.consumer.UserEventConsumer       : outboxId: 5, value: {"schema":{"type":"struct","fields":[{"type":"int32","optional":true,"field":"productId"}],"optional":true,"name":"payload"},"payload":{"productId":10}}
        Hibernate: select oe1_0.`id`,oe1_0.`aggregate_id`,oe1_0.`aggregate_type`,oe1_0.`completed_at`,oe1_0.`created_at`,oe1_0.`payload`,oe1_0.`status` from `outbox` oe1_0 where oe1_0.`id`=?
        2025-03-28T14:06:34.553+09:00  INFO 11806 --- [sat] [ntainer#1-0-C-1] c.r.d.t.consumer.UserEventConsumer       : do something with product event payload: UserEvent(userId=0)
        Hibernate: update `outbox` set `aggregate_id`=?,`aggregate_type`=?,`completed_at`=?,`created_at`=?,`payload`=cast(? as json),`status`=? where `id`=?
        * */
        logger.info("outboxId: $outboxId, value: $value")

        val outbox = outboxRepository.findByIdOrNull(outboxId.toLong())
            ?: return // go to dead letter queue
        val payload = objectMapper.convertValue(outbox.payload, UserEvent::class.java)
        logger.info("do something with product event payload: $payload")

        outbox.done()
    }
}
