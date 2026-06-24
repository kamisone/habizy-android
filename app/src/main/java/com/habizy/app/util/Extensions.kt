package com.habizy.app.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Returns a user-friendly error message from a [Throwable].
 *
 * Maps common exception types to French strings so they can be shown directly in the UI.
 */
fun Throwable.userMessage(): String = when (this) {
    is java.net.UnknownHostException -> "Pas de connexion Internet"
    is java.net.SocketTimeoutException -> "Le serveur met trop de temps a repondre"
    is java.net.ConnectException -> "Impossible de joindre le serveur"
    is retrofit2.HttpException -> {
        when (code()) {
            401 -> "Session expiree, veuillez vous reconnecter"
            403 -> "Acces refuse"
            404 -> "Ressource introuvable"
            409 -> "Conflit : cette operation a deja ete effectuee"
            422 -> "Donnees invalides"
            429 -> "Trop de requetes, veuillez reessayer plus tard"
            in 500..599 -> "Erreur serveur, veuillez reessayer plus tard"
            else -> "Erreur (${ code() })"
        }
    }
    else -> this.localizedMessage ?: "Une erreur inattendue est survenue"
}

/**
 * Formats an ISO-8601 date string into a relative "time ago" label in French,
 * matching the iOS [formatTimeAgo] helper.
 *
 * Examples: "a l'instant", "il y a 5min", "il y a 3h", "il y a 2j", "14/03/2025"
 */
fun formatTimeAgo(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return ""

    val instant = try {
        Instant.parse(isoDate)
    } catch (_: Exception) {
        try {
            // Fallback: try without fractional seconds
            DateTimeFormatter.ISO_INSTANT.parse(isoDate, Instant::from)
        } catch (_: Exception) {
            return isoDate
        }
    }

    val now = Instant.now()
    val minutes = ChronoUnit.MINUTES.between(instant, now)
    val hours = ChronoUnit.HOURS.between(instant, now)
    val days = ChronoUnit.DAYS.between(instant, now)

    return when {
        minutes < 1 -> "a l'instant"
        minutes < 60 -> "il y a ${minutes}min"
        hours < 24 -> "il y a ${hours}h"
        days < 7 -> "il y a ${days}j"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                .withZone(ZoneId.systemDefault())
            formatter.format(instant)
        }
    }
}
