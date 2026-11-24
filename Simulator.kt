package com.exemplo.base

import kotlin.random.Random

/**
 * Simulador DEMONSTRATIVO e simplificado. Não reproduz o jogo real:
 * apenas mostra como uma média de retorno (RTP) convive com resultados
 * muito irregulares no curto prazo. Saldo 100% fictício.
 *
 * Modelo: cada giro custa 1. Com prob. [GameInfo.bigProb] paga [GameInfo.bigMult];
 * com prob. [GameInfo.hit] paga um prêmio pequeno calculado para que o retorno
 * esperado seja exatamente o RTP do jogo; caso contrário, perde.
 */
class Simulator(private val jogo: GameInfo, private val rng: Random = Random.Default) {
    private val smallMult = (jogo.rtp / 100.0 - jogo.bigProb * jogo.bigMult) / jogo.hit

    var saldo = 1000.0; private set
    var giros = 0; private set
    var apostado = 0.0; private set
    var retornado = 0.0; private set
    var maiorSequenciaDePerdas = 0; private set
    private var seqPerdas = 0
    val historico = mutableListOf(saldo)

    val rtpObservado: Double get() = if (apostado == 0.0) 0.0 else retornado / apostado * 100

    fun rodar(qtd: Int) {
        repeat(qtd) {
            val premio = when (val r = rng.nextDouble()) {
                in 0.0..jogo.bigProb -> jogo.bigMult
                in jogo.bigProb..(jogo.bigProb + jogo.hit) -> smallMult
                else -> 0.0
            }
            giros++; apostado += 1.0; retornado += premio; saldo += premio - 1.0
            if (premio == 0.0) { seqPerdas++; if (seqPerdas > maiorSequenciaDePerdas) maiorSequenciaDePerdas = seqPerdas }
            else seqPerdas = 0
            if (giros % 10 == 0) historico.add(saldo)
        }
    }
}
