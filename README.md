# Robozinho

App Android (Kotlin + Jetpack Compose): login local, administração de usuários,
hora sincronizada com m.pgsoft-games.com, menu dos jogos Fortune e simulador
educativo (saldo fictício, atualização a cada 10 s).

## Gerar o APK
Android Studio: abrir a pasta, aguardar o Sync e usar Build > Build APK(s).
Sem instalar nada: subir o projeto no GitHub e rodar o workflow "Build APK"
(aba Actions); o APK fica em Artifacts > robozinho-debug-apk.

## Dados dos jogos
Editar em app/src/main/java/com/exemplo/base/GameInfo.kt.
RTP e volatilidade são valores fixos e não preveem nenhuma rodada.
