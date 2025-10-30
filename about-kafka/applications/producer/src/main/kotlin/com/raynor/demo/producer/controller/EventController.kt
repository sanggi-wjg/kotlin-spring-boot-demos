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
            ResponseEntity.accepted().body(mapOf("eventId" to eventId))
        }
    }
}
