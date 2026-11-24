package com.exemplo.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import java.util.Locale

private const val AVISO = "Simulação educativa: RTP e volatilidade são valores fixos e não preveem nenhuma rodada."

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme()) { App() } }
    }
}

@Composable
fun App() {
    val ctx = LocalContext.current
    val db = remember { Db(ctx) }
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    val atual = usuario
    if (atual == null) { LoginScreen(db) { usuario = it }; return }

    val nav = rememberNavController()
    NavHost(nav, startDestination = "menu") {
        composable("menu") {
            MenuScreen(atual, onAbrir = { nav.navigate("jogo/$it") },
                onAdmin = { nav.navigate("admin") }, onSair = { usuario = null })
        }
        composable("jogo/{id}") { entry ->
            val jogo = JOGOS.first { it.id == entry.arguments?.getString("id") }
            JogoScreen(jogo) { nav.popBackStack() }
        }
        composable("admin") { AdminScreen(db, atual) { nav.popBackStack() } }
    }
}

@Composable
fun AvisoChip(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(AVISO, Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HoraTexto(chave: String, titulo: String? = null) {
    val (hora, doServidor) = horaSincronizada(chave)
    Column {
        if (titulo != null) Text(titulo, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(hora, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        if (!doServidor) Text("Sem resposta de m.pgsoft-games.com. Exibindo a hora do aparelho.", fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(usuario: Usuario, onAbrir: (String) -> Unit, onAdmin: () -> Unit, onSair: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Jogos") }, actions = {
                if (usuario.admin) TextButton(onClick = onAdmin) { Text("Usuários") }
                TextButton(onClick = onSair) { Text("Sair") }
            })
        },
        bottomBar = { Box(Modifier.fillMaxWidth().padding(12.dp), Alignment.Center) { AvisoChip() } }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Card(Modifier.fillMaxWidth()) { Box(Modifier.padding(16.dp)) { HoraTexto("provedor", "HORA ATUALIZADA DO PROVEDOR") } } }
            item { Text("Cada jogo tem a hora sincronizada individualmente. Toque para abrir.", fontSize = 12.sp) }
            items(JOGOS) { j ->
                Card(Modifier.fillMaxWidth().clickable { onAbrir(j.id) }) {
                    Column(Modifier.padding(16.dp)) {
                        Text(j.nome, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("RTP teórico: ${fmt(j.rtp)}%  ·  Volatilidade: ${j.volatilidade}")
                        Spacer(Modifier.height(8.dp))
                        HoraTexto(j.id)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JogoScreen(jogo: GameInfo, onVoltar: () -> Unit) {
    val ctx = LocalContext.current
    val sim = remember { Simulator(jogo) }
    var tick by remember { mutableIntStateOf(0) }
    var contagem by remember { mutableIntStateOf(10) }

    // A cada 10 segundos roda mais 100 giros fictícios e atualiza a tela.
    LaunchedEffect(jogo.id) {
        sim.rodar(100); tick++
        while (true) {
            for (s in 9 downTo 0) { delay(1000); contagem = s }
            sim.rodar(100); tick++; contagem = 10
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(jogo.nome) }, navigationIcon = { TextButton(onClick = onVoltar) { Text("Voltar") } }) },
        bottomBar = { Box(Modifier.fillMaxWidth().padding(12.dp), Alignment.Center) { AvisoChip() } }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Informações do jogo", fontWeight = FontWeight.Bold)
                    HoraTexto(jogo.id, "HORA SINCRONIZADA")
                    Text("RTP teórico publicado: ${fmt(jogo.rtp)}%")
                    Text("Volatilidade: ${jogo.volatilidade}")
                    Text("Grade: ${jogo.grade}")
                    Text("Mecânicas: " + jogo.mecanicas.joinToString(", "))
                    TextButton(onClick = { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(jogo.url))) }) {
                        Text("Ver página oficial")
                    }
                }
            }
            key(tick) {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Simulação (saldo fictício)", fontWeight = FontWeight.Bold)
                        Text("Próxima atualização em ${contagem}s", fontSize = 12.sp)
                        Text("Saldo: R$ ${fmt(sim.saldo)}  ·  Giros: ${sim.giros}")
                        Text("RTP observado: ${fmt(sim.rtpObservado)}%")
                        Text("Maior sequência sem prêmio: ${sim.maiorSequenciaDePerdas} giros")
                        Grafico(sim.historico.toList())
                    }
                }
            }
        }
    }
}

@Composable
fun Grafico(pontos: List<Double>) {
    val cor = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(160.dp).background(Color(0x11FFFFFF), RoundedCornerShape(8.dp))) {
        if (pontos.size < 2) return@Canvas
        val min = pontos.min(); val max = pontos.max()
        val faixa = (max - min).takeIf { it > 0 } ?: 1.0
        val path = Path()
        pontos.forEachIndexed { i, v ->
            val x = size.width * i / (pontos.size - 1)
            val y = size.height - (((v - min) / faixa).toFloat() * size.height)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, cor, style = Stroke(width = 3f))
    }
}

private fun fmt(v: Double) = String.format(Locale("pt", "BR"), "%.2f", v)
