package com.raynor.demo.producer.controller

import com.raynor.demo.producer.service.EventProducer
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/events")
class EventController(
    private val eventProducer: EventProducer,
) {

    @PostMapping("/first-scenario")
    fun publishFirstScenario(): ResponseEntity<Map<String, String>> {
        return eventProducer.publishFirstScenarioEvent().let { eventId ->
            ResponseEntity.accepted().body(
                mapOf("eventId" to eventId)
            )
        }
    }

    @PostMapping("/second-scenario")
    fun publishSecondScenario(): ResponseEntity<Map<String, List<String>>> {
        return eventProducer.publishSecondScenarioEvent().let { eventIds ->
            ResponseEntity.accepted().body(
                mapOf("eventId" to eventIds)
            )
        }
    }

    @PostMapping("/third-scenario")
    fun publishThirdScenario(): ResponseEntity<Map<String, String>> {
        return eventProducer.publishThirdScenarioEvent().let { eventId ->
            ResponseEntity.accepted().body(
                mapOf("eventId" to eventId)
            )
        }
    }
}
