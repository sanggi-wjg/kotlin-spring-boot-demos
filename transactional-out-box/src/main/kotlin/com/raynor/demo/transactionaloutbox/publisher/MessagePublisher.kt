package com.raynor.demo.transactionaloutbox.publisher

import com.raynor.demo.transactionaloutbox.repository.OutboxPollingRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@EnableScheduling
@Service
@Transactional
class MessagePublisher(
    private val outboxPollingRepository: OutboxPollingRepository,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Scheduled(fixedDelay = 60_000)
    fun publish() {
        outboxPollingRepository.findByStatusFalse().forEach { outbox ->
            logger.info("outbox id: ${outbox.id}")
            outbox.done()
            outboxPollingRepository.save(outbox)
        }
    }
}
