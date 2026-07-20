package com.example.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object CsvParser {
    /**
     * Parses an input stream of CSV content and returns a list of maps matching headers to values.
     */
    fun parseCsv(inputStream: InputStream): List<Map<String, String>> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = mutableListOf<List<String>>()
        
        try {
            reader.useLines { sequence ->
                sequence.forEach { line ->
                    if (line.isNotBlank()) {
                        lines.add(parseLine(line))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }

        if (lines.isEmpty()) return emptyList()

        // Normalize headers
        val headers = lines.first().map { it.trim().lowercase() }
        val dataRows = lines.drop(1)

        val result = mutableListOf<Map<String, String>>()
        for (row in dataRows) {
            val rowMap = mutableMapOf<String, String>()
            for (i in headers.indices) {
                if (i < row.size) {
                    rowMap[headers[i]] = row[i]
                } else {
                    rowMap[headers[i]] = ""
                }
            }
            result.add(rowMap)
        }
        return result
    }

    private fun parseLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var curVal = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val ch = line[i]
            if (inQuotes) {
                if (ch == '\"') {
                    if (i + 1 < line.length && line[i + 1] == '\"') {
                        curVal.append('\"')
                        i++ // skip next quote
                    } else {
                        inQuotes = false
                    }
                } else {
                    curVal.append(ch)
                }
            } else {
                if (ch == '\"') {
                    inQuotes = true
                } else if (ch == ',') {
                    result.add(curVal.toString().trim())
                    curVal = StringBuilder()
                } else {
                    curVal.append(ch)
                }
            }
            i++
        }
        result.add(curVal.toString().trim())
        return result
    }
}
