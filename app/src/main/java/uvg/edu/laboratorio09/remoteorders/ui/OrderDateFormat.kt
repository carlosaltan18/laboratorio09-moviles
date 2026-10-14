package uvg.edu.laboratorio09.remoteorders.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal fun formatOrderDate(epochMillis: Long): String =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(Instant.ofEpochMilli(epochMillis))
