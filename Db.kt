package com.exemplo.base

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class Usuario(val id: Long, val nome: String, val admin: Boolean)

/** Usuários locais (SQLite). Senha com salt + PBKDF2; nunca é guardada em texto puro. */
class Db(ctx: Context) : SQLiteOpenHelper(ctx, "usuarios_v2.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "usuario TEXT NOT NULL UNIQUE COLLATE NOCASE,salt TEXT NOT NULL," +
                "hash TEXT NOT NULL,admin INTEGER NOT NULL DEFAULT 0,criado_em INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, o: Int, n: Int) {}

    private fun hash(senha: String, salt: String): String {
        val spec = PBEKeySpec(senha.toCharArray(), salt.chunked(2).map { it.toInt(16).toByte() }.toByteArray(), 120_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            .joinToString("") { "%02x".format(it) }
    }

    private fun novoSalt(): String =
        ByteArray(16).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }

    fun contar(): Int = readableDatabase.rawQuery("SELECT COUNT(*) FROM usuarios", null).use { it.moveToFirst(); it.getInt(0) }

    /** Retorna false se o usuário já existir. */
    fun criar(nome: String, senha: String, admin: Boolean): Boolean {
        val salt = novoSalt()
        val v = ContentValues().apply {
            put("usuario", nome.trim()); put("salt", salt); put("hash", hash(senha, salt))
            put("admin", if (admin) 1 else 0); put("criado_em", System.currentTimeMillis())
        }
        return writableDatabase.insert("usuarios", null, v) != -1L
    }

    fun entrar(nome: String, senha: String): Usuario? {
        readableDatabase.rawQuery(
            "SELECT id, usuario, salt, hash, admin FROM usuarios WHERE usuario = ?", arrayOf(nome.trim())
        ).use { c ->
            if (!c.moveToFirst()) return null
            val ok = hash(senha, c.getString(2)) == c.getString(3)
            return if (ok) Usuario(c.getLong(0), c.getString(1), c.getInt(4) == 1) else null
        }
    }

    fun listar(): List<Usuario> =
        readableDatabase.rawQuery("SELECT id, usuario, admin FROM usuarios ORDER BY admin DESC, usuario", null).use { c ->
            buildList { while (c.moveToNext()) add(Usuario(c.getLong(0), c.getString(1), c.getInt(2) == 1)) }
        }

    fun redefinir(id: Long, senha: String) {
        val salt = novoSalt()
        val v = ContentValues().apply { put("salt", salt); put("hash", hash(senha, salt)) }
        writableDatabase.update("usuarios", v, "id = ?", arrayOf(id.toString()))
    }

    fun excluir(id: Long, usuarioAtual: String) {
        writableDatabase.delete("usuarios", "id = ? AND usuario <> ?", arrayOf(id.toString(), usuarioAtual))
    }
}
