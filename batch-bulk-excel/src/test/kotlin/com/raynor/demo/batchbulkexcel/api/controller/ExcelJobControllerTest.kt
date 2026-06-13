package com.raynor.demo.batchbulkexcel.api.controller

import com.raynor.demo.batchbulkexcel.storage.rds.entity.ExcelRequestEntity
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import com.raynor.demo.batchbulkexcel.storage.rds.repository.ExcelRequestRepository
import com.raynor.demo.batchbulkexcel.support.IntegrationTestSpec
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ExcelJobControllerTest(
    private val mockMvc: MockMvc,
    private val repository: ExcelRequestRepository,
) : IntegrationTestSpec({

        beforeEach {
            repository.deleteAllInBatch()
        }

        test("존재하는 잡 조회 시 200 + 상태/타입을 반환한다") {
            val saved =
                repository.save(
                    ExcelRequestEntity(
                        requestType = ExcelRequestType.EXPORT_ORDER,
                        status = ExcelRequestStatus.PENDING,
                    ),
                )

            mockMvc
                .perform(get("/excel/jobs/${saved.id}"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.jobId").value(saved.id))
                .andExpect(jsonPath("$.type").value("EXPORT_ORDER"))
                .andExpect(jsonPath("$.status").value("PENDING"))
        }

        test("없는 jobId 는 404 를 반환한다") {
            mockMvc
                .perform(get("/excel/jobs/does-not-exist"))
                .andExpect(status().isNotFound)
        }

        fun seedThree() {
            repository.saveAll(
                listOf(
                    ExcelRequestEntity(requestType = ExcelRequestType.EXPORT_ORDER, status = ExcelRequestStatus.PENDING),
                    ExcelRequestEntity(requestType = ExcelRequestType.IMPORT_USER_MILEAGE, status = ExcelRequestStatus.SUCCESS),
                    ExcelRequestEntity(requestType = ExcelRequestType.EXPORT_ORDER, status = ExcelRequestStatus.FAILED),
                ),
            )
        }

        test("필터 없이 조회하면 전체를 페이징해 반환한다") {
            seedThree()

            mockMvc
                .perform(get("/excel/jobs"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
        }

        test("type 필터로 EXPORT_ORDER 만 반환한다") {
            seedThree()

            mockMvc
                .perform(get("/excel/jobs").param("type", "EXPORT_ORDER"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
        }

        test("status 필터로 SUCCESS 만 반환한다") {
            seedThree()

            mockMvc
                .perform(get("/excel/jobs").param("status", "SUCCESS"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value("IMPORT_USER_MILEAGE"))
        }

        test("size 로 페이지 크기를 제한하면 totalPages 가 늘어난다") {
            seedThree()

            mockMvc
                .perform(get("/excel/jobs").param("size", "1"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3))
        }

        test("기간 필터가 ISO instant 로 바인딩되어 createdAt 범위로 거른다") {
            seedThree()

            // 넓은 범위는 전체 포함
            mockMvc
                .perform(
                    get("/excel/jobs")
                        .param("from", "2000-01-01T00:00:00Z")
                        .param("to", "2999-01-01T00:00:00Z"),
                ).andExpect(status().isOk)
                .andExpect(jsonPath("$.totalElements").value(3))

            // 미래 시점 이후만 조회하면 비어 있음 (>= :from 술어 동작 확인)
            mockMvc
                .perform(get("/excel/jobs").param("from", "2999-01-01T00:00:00Z"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.totalElements").value(0))
        }
    })
