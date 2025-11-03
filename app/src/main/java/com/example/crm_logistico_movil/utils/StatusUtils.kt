package com.example.crm_logistico_movil.utils

import androidx.compose.ui.graphics.Color

fun getStatusColor(status: String): Color {
    return when (status.uppercase()) {
        "EN_TRANSITO", "EN_PROCESO" -> Color(0xFF1976D2)
        "ENTREGADO", "COMPLETADO" -> Color(0xFF388E3C)
        "EN_ADUANA" -> Color(0xFFF57C00)
        "PENDIENTE_DOCUMENTOS" -> Color(0xFFE91E63)
        "CANCELADO" -> Color(0xFF757575)
        else -> Color(0xFF6200EE)
    }
}

fun getStatusText(status: String): String {
    return when (status.uppercase()) {
        "EN_TRANSITO" -> "En Tránsito"
        "EN_PROCESO" -> "En Proceso"
        "ENTREGADO" -> "Entregado"
        "COMPLETADO" -> "Completado"
        "EN_ADUANA" -> "En Aduana"
        "PENDIENTE_DOCUMENTOS" -> "Pendiente Documentos"
        "CANCELADO" -> "Cancelado"
        else -> status.split("_").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { char ->
                if (char.isLowerCase()) char.titlecase() else char.toString()
            }
        }
    }
}