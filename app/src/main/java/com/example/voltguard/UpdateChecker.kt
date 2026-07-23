package com.example.voltguard

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val isNewer: Boolean
)

object UpdateChecker {

    private const val GITHUB_API = "https://api.github.com/repos/AnythingDevelopmentTeam/VoltGuard/releases/latest"
    private const val GITHUB_RELEASES = "https://github.com/AnythingDevelopmentTeam/VoltGuard/releases/tag/"

    suspend fun check(currentVersion: String): UpdateInfo = withContext(Dispatchers.IO) {
        val connection = URL(GITHUB_API).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        connection.setRequestProperty("Accept", "application/json")

        try {
            val response = connection.inputStream.bufferedReader().readText()
            val json = JSONObject(response)
            val tagName = json.getString("tag_name").removePrefix("v")
            val downloadUrl = GITHUB_RELEASES + json.getString("tag_name")

            UpdateInfo(
                latestVersion = tagName,
                downloadUrl = downloadUrl,
                isNewer = compareVersions(tagName, currentVersion) > 0
            )
        } catch (_: Exception) {
            UpdateInfo(currentVersion, "", false)
        } finally {
            connection.disconnect()
        }
    }

    private fun compareVersions(a: String, b: String): Int {
        val aParts = a.split(Regex("[.\\-]"))
        val bParts = b.split(Regex("[.\\-]"))
        val maxLen = maxOf(aParts.size, bParts.size)
        for (i in 0 until maxLen) {
            val aNum = aParts.getOrElse(i) { "0" }.toIntOrNull() ?: 0
            val bNum = bParts.getOrElse(i) { "0" }.toIntOrNull() ?: 0
            if (aNum != bNum) return aNum - bNum
            val aStr = aParts.getOrElse(i) { "" }
            val bStr = bParts.getOrElse(i) { "" }
            if (aStr != bStr) {
                val order = listOf("alpha", "beta", "rc", "release")
                val aIdx = order.indexOf(aStr).let { if (it < 0) order.size else it }
                val bIdx = order.indexOf(bStr).let { if (it < 0) order.size else it }
                if (aIdx != bIdx) return aIdx - bIdx
            }
        }
        return 0
    }
}
