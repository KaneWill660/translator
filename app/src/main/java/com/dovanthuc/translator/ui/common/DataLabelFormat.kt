package com.dovanthuc.translator.ui.common

import com.dovanthuc.translator.data.prefs.DataSyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
private val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

fun shortVersionLabel(version: String?): String =
    if (version.isNullOrBlank()) "…" else "v.${version.take(6)}"

/** Compact label for the Home screen footer, e.g. "Dữ liệu: v.a1b2c3 · đồng bộ 16/09/2026". */
fun homeDataLabel(status: DataSyncStatus): String {
    val versionText = shortVersionLabel(status.version)
    val dateText = status.lastSyncedAtMillis?.let { dateFormatter.format(Date(it)) }
    return if (dateText != null) {
        "Dữ liệu: $versionText · đồng bộ $dateText"
    } else {
        "Dữ liệu: $versionText · dùng bản cài sẵn"
    }
}

fun lastSyncedFullText(status: DataSyncStatus): String =
    status.lastSyncedAtMillis?.let { "Đồng bộ lần cuối: ${dateTimeFormatter.format(Date(it))}" }
        ?: "Chưa đồng bộ lần nào — đang dùng dữ liệu cài sẵn"
