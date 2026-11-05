# Changelog

## 0.2.1-phase2a
- Perfil de frame (FrameProfiler): periodo do frame, CPU no render, CPU nos ticks, restante (swap/espera) e pior frame por janela de 1s.
- Medicao de GPU via consulta GL_TIME_ELAPSED (lida com atraso, sem travar a CPU); desativa sozinha se o driver ou outro mod causar erro.
- Leitura indicativa: limite de CPU, de GPU ou inconclusivo.
- Nova opcao `enableFrameProfiler` (padrao true).
- Motivo: primeira medicao real (12 chunks, GT 610) mostrou terreno = 518 draws e 5,3 ms de CPU em frames de ~50 ms; o gargalo nao esta no desenho do terreno.

## 0.2.0-phase2a
- Hook por reflexao no `RenderGlobal` (container achado pelo tipo; sem coremod).
- Containers `MeteredVboRenderList` e `MeteredRenderList`: desenho identico ao original, com medicao de chunk-draws por frame e CPU emitindo terreno.
- Fallback automatico: OptiFine detectado, container desconhecido ou falha de reflexao.
- Nova opcao `enableRenderHook` (padrao true).
- `/sodiumdiag` e `diagnostics.txt` mostram o estado do hook e as metricas.
- Nenhuma otimizacao de desempenho ainda.

## 0.1.0-phase1
- Fundacao: configuracao, deteccao de GPU/OpenGL, diagnostico e logs.
