# Changelog

## 0.2.0-phase2a
- Hook por reflexao no `RenderGlobal` (container achado pelo tipo; sem coremod).
- Containers `MeteredVboRenderList` e `MeteredRenderList`: desenho identico ao original, com medicao de chunk-draws por frame e CPU emitindo terreno.
- Fallback automatico: OptiFine detectado, container desconhecido ou falha de reflexao.
- Nova opcao `enableRenderHook` (padrao true).
- `/sodiumdiag` e `diagnostics.txt` mostram o estado do hook e as metricas.
- Nenhuma otimizacao de desempenho ainda.

## 0.1.0-phase1
- Fundacao: configuracao, deteccao de GPU/OpenGL, diagnostico e logs.
