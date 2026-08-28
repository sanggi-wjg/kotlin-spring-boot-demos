package com.raynor.demo.batchbulkexcel.support

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 검증용 최소 xlsx(zip) 바이트를 생성한다. 셀 데이터는 의미가 없고 `xl/worksheets/sheetN.xml`
 * 엔트리 개수만 검증에 쓰이므로, 실제 FastExcel 없이 zip 엔트리만 구성한다.
 */
object TestXlsx {
    fun bytes(sheets: Int = 1): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            zip.entry("[Content_Types].xml", "<Types/>")
            zip.entry("xl/workbook.xml", "<workbook/>")
            for (i in 1..sheets) {
                zip.entry("xl/worksheets/sheet$i.xml", "<worksheet/>")
            }
        }
        return out.toByteArray()
    }

    private fun ZipOutputStream.entry(
        name: String,
        content: String,
    ) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray())
        closeEntry()
    }
}
