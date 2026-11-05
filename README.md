# Sodium 1.8.9 (Forge) - Fase 2A: integracao e medicao (0.2.1)

Motor de renderizacao **independente**, inspirado em tecnicas do Sodium moderno, para Minecraft 1.8.9 Forge (Java 8, LWJGL 2).

**Estado atual (Fase 2A):** alem da fundacao (config, GPU/OpenGL, diagnostico), o mod instala um container de render no `RenderGlobal` que **apenas mede** (chunk-draws por frame e tempo de CPU no desenho do terreno).
**Ele NAO altera a renderizacao e NAO aumenta FPS ainda.** Otimizacoes reais (regioes de buffer, culling, workers) vem nas proximas etapas.

> Status de teste: a Fase 1 compilou e rodou no jogo. A Fase 2A foi verificada apenas por compilacao contra os fontes reais do Minecraft/Forge 1.8.9 (com stubs de LWJGL/log4j); ainda nao foi executada no jogo.
> Teste sem OptiFine: com OptiFine instalado o mod entra em fallback de proposito.

## Licenca e origem do codigo
Nenhum codigo do Sodium (PolyForm Shield 1.0.0) foi copiado. O Sodium e usado apenas como referencia conceitual. Esta licenca (MIT) cobre somente o codigo deste repositorio. Antes de publicar com o nome "Sodium", verifique a clausula de nao concorrencia da PolyForm Shield.

## Como compilar pelo GitHub (passo a passo)
1. Crie um repositorio no GitHub (ex.: `Sodium-1.8.9-Forge`).
2. Envie o conteudo desta pasta **na raiz** do repositorio (`build.gradle` deve ficar na raiz; a pasta `.github` tambem).
3. Abra a aba **Actions**. O workflow **Build** roda sozinho a cada push (ou use *Run workflow*).
4. Quando terminar, abra a execucao e baixe o artefato **Sodium-1.8.9-Forge** (o `.jar` esta dentro).
5. Copie o `.jar` para `.minecraft/mods/` (Minecraft 1.8.9 + Forge 1.8.9 11.15.1.2318, Java 8).
6. Se o workflow falhar, copie o log do passo **Build** e me envie.

## Como testar a Fase 2A
1. **Remova o OptiFine** da pasta `mods` (senao o hook se desativa).
2. Abra o jogo, entre em um mundo e ande um pouco.
3. Digite `/sodiumdiag`. Deve aparecer `Hook de render: instalado: MeteredVboRenderList` (ou `MeteredRenderList` se "Usar VBOs" estiver desligado) e os numeros por frame.
4. Veja `config/sodium189/diagnostics.txt`, secao Renderizacao.
5. Confira se o mundo continua igual ao vanilla (sem chunks faltando) e se o FPS nao piorou.
6. Em `/sodiumdiag` veja tambem o **Perfil de frame**: CPU no render, ticks, restante e GPU. A linha "Leitura" indica se o limite parece ser CPU ou GPU (indicativo, nao definitivo).
7. Ligue e desligue "Usar VBOs" em Opcoes > Video e rode `/sodiumdiag` de novo: o nome do container deve acompanhar.
