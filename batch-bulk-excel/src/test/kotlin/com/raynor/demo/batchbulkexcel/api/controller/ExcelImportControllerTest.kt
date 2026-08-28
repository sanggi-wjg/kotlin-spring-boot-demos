package com.raynor.demo.batchbulkexcel.api.controller

import com.raynor.demo.batchbulkexcel.api.controller.dto.ImportResponseDto
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestStatus
import com.raynor.demo.batchbulkexcel.storage.rds.enum.ExcelRequestType
import com.raynor.demo.batchbulkexcel.storage.rds.repository.ExcelRequestRepository
import com.raynor.demo.batchbulkexcel.support.IntegrationTestSpec
import com.raynor.demo.batchbulkexcel.support.TestXlsx
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import tools.jackson.databind.ObjectMapper
import java.nio.file.Files

class ExcelImportControllerTest(
    private val mockMvc: MockMvc,
    private val repository: ExcelRequestRepository,
    private val objectMapper: ObjectMapper,
) : IntegrationTestSpec({

        beforeEach {
            repository.deleteAllInBatch()
        }

        test("xlsx 업로드 시 파일을 저장하고 PENDING ExcelRequest 적재 후 jobId 를 반환한다") {
            val file = MockMultipartFile("file", "data.xlsx", "application/octet-stream", TestXlsx.bytes(sheets = 1))

            val result =
                mockMvc
                    .perform(multipart("/excel/import").file(file))
                    .andExpect(status().isCreated)
                    .andReturn()

            val jobId = objectMapper.readValue(result.response.contentAsString, ImportResponseDto::class.java).jobId
            result.response.getHeader("Location") shouldBe "/excel/jobs/$jobId"

            val row = repository.findById(jobId).orElseThrow()
            row.status shouldBe ExcelRequestStatus.PENDING
            row.requestType shouldBe ExcelRequestType.IMPORT_USER_MILEAGE
            val key = row.inputFileUrl.shouldNotBeNull()
            Files.exists(IntegrationTestSpec.storageDir.resolve(key)) shouldBe true
        }

        test("xlsx 가 아니면 400 을 반환한다") {
            val bad = MockMultipartFile("file", "data.txt", "text/plain", "not a zip".toByteArray())

            mockMvc
                .perform(multipart("/excel/import").file(bad))
                .andExpect(status().isBadRequest)
        }
    })
