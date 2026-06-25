package com.habizy.app.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

fun Throwable.userMessage(): String = when (this) {
    is java.net.UnknownHostException,
    is java.net.NoRouteToHostException -> "Pas de connexion Internet"

    is java.net.SocketTimeoutException -> "Le serveur met trop de temps à répondre"

    is java.net.ConnectException -> "Impossible de joindre le serveur"

    is retrofit2.HttpException -> {
        val bodyMsg = try {
            val raw = response()?.errorBody()?.string()
            if (!raw.isNullOrBlank()) {
                val json = org.json.JSONObject(raw)
                // NestJS returns message as String or String[]
                runCatching { json.getString("message") }.getOrNull()
                    ?: runCatching { json.getJSONArray("message").getString(0) }.getOrNull()
            } else null
        } catch (_: Exception) { null }

        mapServerError(code(), bodyMsg)
    }

    else -> "Une erreur inattendue est survenue"
}

private fun mapServerError(statusCode: Int, serverMsg: String?): String {
    if (!serverMsg.isNullOrBlank()) {
        val s = serverMsg.lowercase()
        return when {
            "invalid credentials" in s || "credentials" in s -> "Email ou mot de passe incorrect"
            "user not found" in s                            -> "Aucun compte trouvé avec cet email"
            "current password" in s                          -> "Mot de passe actuel incorrect"
            "email already" in s || "already registered" in s -> "Cette adresse email est déjà utilisée"
            "invitation" in s || "invite" in s               -> "Code d'invitation invalide ou expiré"
            "not found" in s                                 -> "Ressource introuvable"
            "expired" in s                                   -> "Session expirée, veuillez vous reconnecter"
            "access denied" in s || "forbidden" in s         -> "Accès refusé"
            "conflict" in s                                  -> "Cette action a déjà été effectuée"
            "validation" in s || "invalid" in s              -> "Données invalides"
            else                                             -> mapStatusCode(statusCode)
        }
    }
    return mapStatusCode(statusCode)
}

private fun mapStatusCode(code: Int): String = when (code) {
    400  -> "Requête invalide"
    401  -> "Email ou mot de passe incorrect"
    403  -> "Accès refusé"
    404  -> "Ressource introuvable"
    409  -> "Cette action a déjà été effectuée"
    422  -> "Données invalides"
    429  -> "Trop de requêtes, veuillez réessayer"
    in 500..599 -> "Erreur serveur, veuillez réessayer"
    else -> "Une erreur inattendue est survenue"
}

/**
 * Formats an ISO-8601 date string into a relative "time ago" label in French,
 * matching the iOS [formatTimeAgo] helper.
 *
 * Examples: "à l'instant", "il y a 5min", "il y a 3h", "il y a 2j", "14/03/2025"
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
        minutes < 1 -> "à l'instant"
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
