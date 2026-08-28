package com.raynor.demo.batchbulkexcel.api.validator

import com.raynor.demo.batchbulkexcel.api.config.ExcelImportProperties
import com.raynor.demo.batchbulkexcel.api.exception.FileValidationException
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.zip.ZipException
import java.util.zip.ZipInputStream

@Component
class ExcelFileValidator(
    private val props: ExcelImportProperties,
) {
    fun validate(file: MultipartFile) {
        if (file.isEmpty) throw FileValidationException("빈 파일입니다.")
        validateExtension(file)
        validateSize(file)
        validateSingleSheet(file)
    }

    private fun validateExtension(file: MultipartFile) {
        val ext = file.originalFilename?.substringAfterLast('.', "")?.lowercase()
        if (ext != props.allowedExtension.lowercase()) {
            throw FileValidationException("허용되지 않는 확장자입니다. (.${props.allowedExtension} 만 허용)")
        }
    }

    private fun validateSize(file: MultipartFile) {
        val max = props.maxFileSize.toBytes()
        if (file.size > max) {
            throw FileValidationException("파일 크기가 허용치(${props.maxFileSize})를 초과했습니다.")
        }
    }

    private fun validateSingleSheet(file: MultipartFile) {
        var worksheets = 0
        var sawEntry = false
        try {
            file.inputStream.use { input ->
                ZipInputStream(input).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        sawEntry = true
                        if (!entry.isDirectory && WORKSHEET_REGEX.matches(entry.name)) worksheets++
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }
        } catch (_: ZipException) {
            throw FileValidationException("유효한 xlsx(zip) 파일이 아닙니다.")
        } catch (_: IOException) {
            throw FileValidationException("파일을 읽을 수 없습니다.")
        }

        if (!sawEntry) throw FileValidationException("유효한 xlsx(zip) 파일이 아닙니다.")
        if (worksheets != 1) throw FileValidationException("시트는 정확히 1개여야 합니다. (발견: $worksheets)")
    }

    companion object {
        private val WORKSHEET_REGEX = Regex("""xl/worksheets/sheet\d+\.xml""", RegexOption.IGNORE_CASE)
    }
}
