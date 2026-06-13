package com.raynor.demo.batchbulkexcel.validator

import com.raynor.demo.batchbulkexcel.api.config.ExcelImportProperties
import com.raynor.demo.batchbulkexcel.api.exception.FileValidationException
import com.raynor.demo.batchbulkexcel.api.validator.ExcelFileValidator
import com.raynor.demo.batchbulkexcel.support.TestXlsx
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import org.springframework.mock.web.MockMultipartFile
import org.springframework.util.unit.DataSize

class ExcelFileValidatorTest :
    FunSpec(
        {
            val validator = ExcelFileValidator(ExcelImportProperties(maxFileSize = DataSize.ofMegabytes(100), allowedExtension = "xlsx"))

            fun file(
                name: String,
                bytes: ByteArray,
            ) = MockMultipartFile("file", name, "application/octet-stream", bytes)

            test("정상 1-시트 xlsx 는 통과한다") {
                validator.validate(file("data.xlsx", TestXlsx.bytes(sheets = 1)))
            }

            test("확장자가 xlsx 가 아니면 실패한다") {
                shouldThrow<FileValidationException> {
                    validator.validate(file("data.csv", TestXlsx.bytes(sheets = 1)))
                }
            }

            test("크기 상한을 초과하면 실패한다") {
                val tiny = ExcelFileValidator(ExcelImportProperties(maxFileSize = DataSize.ofBytes(10), allowedExtension = "xlsx"))
                shouldThrow<FileValidationException> {
                    tiny.validate(file("data.xlsx", TestXlsx.bytes(sheets = 1)))
                }
            }

            test("시트가 2개면 실패한다") {
                shouldThrow<FileValidationException> {
                    validator.validate(file("data.xlsx", TestXlsx.bytes(sheets = 2)))
                }
            }

            test("zip 이 아니면 실패한다") {
                shouldThrow<FileValidationException> {
                    validator.validate(file("data.xlsx", "not a zip".toByteArray()))
                }
            }

            test("빈 파일이면 실패한다") {
                shouldThrow<FileValidationException> {
                    validator.validate(file("data.xlsx", ByteArray(0)))
                }
            }
        },
    )
