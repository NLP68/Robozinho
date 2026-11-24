package com.exemplo.base

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(db: Db, atual: Usuario, onVoltar: () -> Unit) {
    var lista by remember { mutableStateOf(db.listar()) }
    var criando by remember { mutableStateOf(false) }
    var redefinindo by remember { mutableStateOf<Usuario?>(null) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Usuários") },
            navigationIcon = { TextButton(onClick = onVoltar) { Text("Voltar") } },
            actions = { TextButton(onClick = { criando = true }) { Text("Novo") } }
        )
    }) { pad ->
        LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(lista) { u ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(u.nome, fontWeight = FontWeight.Bold)
                            if (u.admin) Text("Administrador", fontSize = 12.sp)
                        }
                        TextButton(onClick = { redefinindo = u }) { Text("Senha") }
                        if (u.nome != atual.nome) TextButton(onClick = { db.excluir(u.id, atual.nome); lista = db.listar() }) { Text("Excluir") }
                    }
                }
            }
        }
    }

    if (criando) {
        var nome by remember { mutableStateOf("") }
        var senha by remember { mutableStateOf("") }
        var admin by remember { mutableStateOf(false) }
        var erro by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { criando = false },
            title = { Text("Novo usuário") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(nome, { nome = it }, label = { Text("Usuário") }, singleLine = true)
                    OutlinedTextField(senha, { senha = it }, label = { Text("Senha (mín. 6)") }, singleLine = true)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(admin, { admin = it }); Spacer(Modifier.width(8.dp)); Text("Administrador")
                    }
                    erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    when {
                        nome.isBlank() -> erro = "Informe o usuário"
                        senha.length < 6 -> erro = "A senha deve ter ao menos 6 caracteres"
                        !db.criar(nome, senha, admin) -> erro = "Esse usuário já existe"
                        else -> { lista = db.listar(); criando = false }
                    }
                }) { Text("Criar") }
            },
            dismissButton = { TextButton(onClick = { criando = false }) { Text("Cancelar") } }
        )
    }

    redefinindo?.let { u ->
        var senha by remember { mutableStateOf("") }
        var erro by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { redefinindo = null },
            title = { Text("Redefinir senha de ${u.nome}") },
            text = {
                Column {
                    OutlinedTextField(senha, { senha = it }, label = { Text("Nova senha (mín. 6)") }, singleLine = true)
                    erro?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (senha.length < 6) erro = "A senha deve ter ao menos 6 caracteres"
                    else { db.redefinir(u.id, senha); redefinindo = null }
                }) { Text("Salvar") }
            },
            dismissButton = { TextButton(onClick = { redefinindo = null }) { Text("Cancelar") } }
        )
    }
}
