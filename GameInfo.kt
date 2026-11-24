package com.exemplo.base

/**
 * Dados informativos de cada jogo. RTP e volatilidade são valores FIXOS
 * definidos pela fabricante e conferidos em fontes públicas em 24/11/2026.
 * Fortune Dragon tem divergência entre fontes (96,74% na tabela baseada no
 * portfólio oficial brasileiro; algumas listam 96,53%). Operadoras podem
 * exibir outro valor: o "i" dentro do jogo é a referência final.
 * As URLs são as mesmas usadas no APK original.
 *
 * hit/small/bigProb/bigMult alimentam apenas o simulador educativo.
 */
data class GameInfo(
    val id: String,
    val nome: String,
    val rtp: Double,            // retorno teórico, ex.: 96.81
    val volatilidade: String,
    val grade: String,
    val mecanicas: List<String>,
    val url: String,
    val hit: Double,            // chance de prêmio pequeno (simulador)
    val bigProb: Double,        // chance de prêmio grande (simulador)
    val bigMult: Double         // múltiplo do prêmio grande (simulador)
)

val JOGOS = listOf(
    GameInfo("tiger", "Fortune Tiger", 96.81, "Média", "3x3, 5 linhas · vitória máx. 2.500x",
        listOf("Símbolos Wild", "Respin com símbolo escolhido", "Multiplicador x10 na grade cheia"),
        "https://www.pgsoft.com/pt/games/83/", 0.25, 0.010, 20.0),
    GameInfo("ox", "Fortune Ox", 96.75, "Média", "3-4-3, até 10 formas de ganhar · vitória máx. 2.000x",
        listOf("Símbolo Wild", "Boi da Fortuna (trava rolos laterais e gira o central)", "Multiplicador até 10x"),
        "https://www.pgsoft.com/pt/games/110/", 0.24, 0.010, 22.0),
    GameInfo("rabbit", "Fortune Rabbit", 96.75, "Média", "3-4-3 · vitória máx. 5.000x",
        listOf("Símbolos de dinheiro com valores", "Rodadas da Fortuna", "Coleta de prêmios"),
        "https://www.pgsoft.com/pt/games/129/", 0.28, 0.008, 25.0),
    GameInfo("mouse", "Fortune Mouse", 96.96, "Média", "3x3, 5 linhas · vitória máx. 1.000x",
        listOf("Símbolo Wild", "Respin", "Multiplicadores"),
        "https://www.pgsoft.com/en/games/53/", 0.27, 0.009, 20.0),
    GameInfo("dragon", "Fortune Dragon", 96.74, "Média", "3x3, 5 linhas · vitória máx. 2.500x",
        listOf("Wild Dragão", "Respins", "Multiplicadores"),
        "https://www.pgsoft.com/en/games/149/", 0.22, 0.012, 30.0)
)
