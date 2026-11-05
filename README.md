# Sodium 1.8.9 (Forge) - Fase 1: fundacao

Motor de renderizacao **independente**, inspirado em tecnicas do Sodium moderno, para Minecraft 1.8.9 Forge (Java 8, LWJGL 2).

**Estado atual (Fase 1):** o mod apenas inicializa, le a configuracao, detecta GPU/OpenGL e gera diagnostico.
**Ele NAO altera a renderizacao e NAO aumenta FPS ainda.** O renderizador de chunks comeca na Fase 2.

> Status de teste: esta fase foi verificada apenas por compilacao contra stubs das APIs. Ainda nao foi compilada com ForgeGradle nem executada no jogo.

## Licenca e origem do codigo
Nenhum codigo do Sodium (PolyForm Shield 1.0.0) foi copiado. O Sodium e usado apenas como referencia conceitual. Esta licenca (MIT) cobre somente o codigo deste repositorio. Antes de publicar com o nome "Sodium", verifique a clausula de nao concorrencia da PolyForm Shield.

## Como compilar pelo GitHub (passo a passo)
1. Crie um repositorio no GitHub (ex.: `Sodium-1.8.9-Forge`).
2. Envie o conteudo desta pasta **na raiz** do repositorio (`build.gradle` deve ficar na raiz; a pasta `.github` tambem).
3. Abra a aba **Actions**. O workflow **Build** roda sozinho a cada push (ou use *Run workflow*).
4. Quando terminar, abra a execucao e baixe o artefato **Sodium-1.8.9-Forge** (o `.jar` esta dentro).
5. Copie o `.jar` para `.minecraft/mods/` (Minecraft 1.8.9 + Forge 1.8.9 11.15.1.2318, Java 8).
6. Se o workflow falhar, copie o log do passo **Build** e me envie.

## Como testar a Fase 1
1. Abra o jogo com o mod e veja no `latest.log` as linhas `[Sodium189]` com a GPU detectada.
2. Abra `config/sodium189/diagnostics.txt`.
3. No jogo, digite `/sodiumdiag` para regerar o relatorio e ver um resumo no chat.
4. Confira se o jogo abre normalmente e se o FPS esta igual ao sem o mod.
