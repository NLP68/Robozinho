package com.exemplo.base

import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val HOST = "https://m.pgsoft-games.com/"

/** Lê o cabeçalho Date do servidor (ms desde 1970) ou null se não responder. */
suspend fun lerHoraServidor(): Long? = withContext(Dispatchers.IO) {
    try {
        val c = URL(HOST).openConnection() as HttpURLConnection
        c.requestMethod = "HEAD"; c.connectTimeout = 4000; c.readTimeout = 4000
        val d = c.date; c.disconnect()
        d.takeIf { it > 0 }
    } catch (e: Exception) { null }
}

/** Hora sincronizada individualmente por [chave]; ressincroniza a cada 60 s. Retorna (hora, veioDoServidor). */
@Composable
fun horaSincronizada(chave: String): Pair<String, Boolean> {
    var offset by remember(chave) { mutableStateOf<Long?>(null) }
    var agora by remember(chave) { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(chave) {
        while (true) {
            offset = lerHoraServidor()?.let { it - System.currentTimeMillis() }
            delay(60_000)
        }
    }
    LaunchedEffect(chave) { while (true) { agora = System.currentTimeMillis(); delay(1000) } }
    val fmt = remember { SimpleDateFormat("HH:mm:ss", Locale("pt", "BR")) }
    return fmt.format(Date(agora + (offset ?: 0L))) to (offset != null)
}
