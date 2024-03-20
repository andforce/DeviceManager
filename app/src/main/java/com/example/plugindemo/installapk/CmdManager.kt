package com.example.plugindemo.installapk

import java.io.BufferedReader
import java.io.InputStreamReader

object CmdManager {
    fun executeShellCommand(command: String): String? {
        var result: String? = null
        var reader: BufferedReader? = null
        try {
            val process: Process = Runtime.getRuntime().exec(command)
            reader = BufferedReader(InputStreamReader(process.inputStream))
            val buffer = StringBuffer()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                buffer.appendLine(line)
            }
            result = buffer.toString()
            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            runCatching {
                reader?.close()
            }
        }
        return result
    }
}


