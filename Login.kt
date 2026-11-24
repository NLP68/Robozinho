package com.exemplo.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(db: Db, onEntrou: (Usuario) -> Unit) {
    val primeiroAcesso = remember { db.contar() == 0 }
    var usuario by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var visivel by remember { mutableStateOf(false) }
    var erroUsuario by remember { mutableStateOf<String?>(null) }
    var erroSenha by remember { mutableStateOf<String?>(null) }
    var esqueceu by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Image(painterResource(R.drawable.fundo_login), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Color(0x99000000)))
        Card(Modifier.align(Alignment.Center).padding(24.dp).fillMaxWidth()) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (primeiroAcesso) "Criar administrador" else "Entrar", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    usuario, { usuario = it; erroUsuario = null }, Modifier.fillMaxWidth(),
                    label = { Text("Seu usuário") }, singleLine = true,
                    isError = erroUsuario != null, supportingText = { erroUsuario?.let { Text(it) } }
                )
                OutlinedTextField(
                    senha, { senha = it; erroSenha = null }, Modifier.fillMaxWidth(),
                    label = { Text("Sua senha") }, singleLine = true,
                    visualTransformation = if (visivel) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = { TextButton(onClick = { visivel = !visivel }) { Text(if (visivel) "Ocultar" else "Mostrar") } },
                    isError = erroSenha != null, supportingText = { erroSenha?.let { Text(it) } }
                )
                Button(
                    onClick = {
                        if (usuario.isBlank()) { erroUsuario = "Informe o usuário"; return@Button }
                        if (senha.isEmpty()) { erroSenha = "Informe a senha"; return@Button }
                        if (primeiroAcesso) {
                            if (senha.length < 6) { erroSenha = "A senha deve ter ao menos 6 caracteres"; return@Button }
                            db.criar(usuario, senha, true)
                        }
                        val u = db.entrar(usuario, senha)
                        if (u != null) onEntrou(u) else erroSenha = "Usuário ou senha incorretos"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text(if (primeiroAcesso) "Criar e entrar" else "Entrar") }
                if (!primeiroAcesso) TextButton(onClick = { esqueceu = true }) { Text("Esqueceu a senha?") }
            }
        }
    }
    if (esqueceu) AlertDialog(
        onDismissRequest = { esqueceu = false },
        confirmButton = { TextButton(onClick = { esqueceu = false }) { Text("OK") } },
        title = { Text("Esqueceu a senha?") },
        text = { Text("Fale com o administrador para redefinir sua senha.") }
    )
}
