package com.raynor.demo.aboutgctuning.controller

import com.raynor.demo.aboutgctuning.model.ComputeResult
import com.raynor.demo.aboutgctuning.model.People
import com.raynor.demo.aboutgctuning.model.PeopleAdapter
import com.raynor.demo.aboutgctuning.service.LoadService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/load")
class LoadController(
    private val loadService: LoadService,
) {

    @GetMapping("/computing")
    fun computing(): ResponseEntity<ComputeResult> {
        return loadService.compute().let {
            ResponseEntity.ok(it)
        }
    }

    @PostMapping("/json")
    fun jsonIntensive(
        @RequestBody people: People,
    ): ResponseEntity<PeopleAdapter> {
        return loadService.convert(people).let {
            ResponseEntity.ok(it)
        }
    }
}
