package com.raynor.demo.batchbulkexcel.api.controller

import com.raynor.demo.batchbulkexcel.api.controller.dto.ExportRequestDto
import com.raynor.demo.batchbulkexcel.api.controller.dto.ExportResponseDto
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import com.raynor.demo.batchbulkexcel.storage.rds.enum.OrderStatus
import com.raynor.demo.batchbulkexcel.storage.rds.repository.ExcelRequestRepository
import com.raynor.demo.batchbulkexcel.support.IntegrationTestSpec
import io.kotest.core.extensions.ApplyExtension
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper

class ExcelExportControllerTest(
    private val mockMvc: MockMvc,
    private val repository: ExcelRequestRepository,
    private val objectMapper: ObjectMapper,
) : IntegrationTestSpec({
        beforeEach { repository.deleteAllInBatch() }

        test("status 조건으로 EXPORT_ORDER PENDING 적재 후 201 + jobId 를 반환한다") {
            val result =
                mockMvc
                    .perform(
                        post("/excel/export")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""{"status":"PAID"}"""),
                    ).andExpect(status().isCreated)
                    .andReturn()

            val jobId = objectMapper.readValue(result.response.contentAsString, ExportResponseDto::class.java).jobId
            result.response.getHeader("Location") shouldBe "/excel/jobs/$jobId"

            val row = repository.findById(jobId).orElseThrow()
            row.requestType shouldBe ExcelRequestType.EXPORT_ORDER
            row.status shouldBe ExcelRequestStatus.PENDING
            // MySQL JSON 컬럼은 round-trip 시 정규화되므로 문자열 동치 대신 역직렬화로 검증
            val params = objectMapper.readValue(row.params.shouldNotBeNull(), ExportRequestDto::class.java)
            params.status shouldBe OrderStatus.PAID
        }

        test("잘못된 status 는 400 을 반환한다") {
            mockMvc
                .perform(
                    post("/excel/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""{"status":"NOPE"}"""),
                ).andExpect(status().isBadRequest)
        }
    })
