# Arquitetura (Fase 1)

## Referencias estudadas
- **Sodium moderno** (CaffeineMC, `sodium-dev`): `client/render/chunk` com regioes (`region`, `storage`), grafo de oclusao (`occlusion`, `tree`), compilacao assincrona (`compile/executor`, `tasks`), ordenacao de translucidos (`translucent_sorting`), formato compacto de vertice (`vertex/format`). Depende de GL 3.3+/4.5 (multi-draw indirect, buffer storage, shaders).
- **Minecraft 1.8.9 (MCP)**: classes a confirmar no codigo descompilado na Fase 2: `RenderGlobal`, `ChunkRenderDispatcher`, `ChunkRenderWorker`, `RenderChunk`, `CompiledChunk`, `VisGraph`, `VboRenderList`, `ListedRenderChunk`, `WorldRenderer` (buffer builder), `Tessellator`, `BlockRendererDispatcher`.

## Reaproveitavel apenas como conceito
Regioes de VBO, fila priorizada por distancia com cancelamento, cache de visibilidade, reducao de draw calls.

## Reimplementacao necessaria
Todo o codigo. Alvo realista em OpenGL 2.1/LWJGL 2 (ex.: GT 610): VBO por regiao, menos draw calls, construcao assincrona. Multi-draw indirect e buffer storage so entram como caminho opcional detectado em runtime.

## Integracao com Forge
Fase 1 nao usa coremod. Fase 2 decide entre ASM/coremod e Mixin; sempre com fallback ao renderizador original.

## Modulos e estado (Fase 1)
| Modulo | Estado |
|---|---|
| Core / Diagnostics | ativo |
| Compatibility Layer | somente deteccao |
| Chunk Renderer, Culling, Memory, Workers, GUI | nao implementados |

O estado real de cada modulo aparece em `diagnostics.txt`.

## Riscos
OptiFine (reescreve as mesmas classes), mods com TESR/renderizadores proprios, drivers antigos com VBO instavel.
