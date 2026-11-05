# Pipeline de renderizacao do Minecraft 1.8.9 (verificado nos fontes MCP/Forge 11.15.1.2318)

## Como o terreno e desenhado hoje
- `RenderGlobal.setupTerrain`: percorre os chunks por BFS a partir da camera (frustum + `VisGraph`), cria um objeto `ContainerLocalRenderInformation` por chunk visivel e refaz isso sempre que a camera muda. Chunks sujos vao para `chunksToUpdate`; o chunk em que o jogador esta e reconstruido na hora (`updateChunkNow`).
- `ChunkRenderDispatcher`: **2 threads fixas** ("Chunk Batcher"), fila de 100 tarefas, **5** `RegionRenderCacheBuilder` compartilhados. O upload para a GPU (`uploadChunk`) e enfileirado e executado na thread principal em `runChunkUploads`.
- `RenderChunk.rebuildChunk`: cria um `RegionRenderCache` (20x20x20 estados), percorre os 4096 blocos, chama `BlockRendererDispatcher.renderBlock` por camada e calcula `VisGraph`.
- Desenho (`RenderGlobal.renderBlockLayer`): para cada camada (SOLID, CUTOUT_MIPPED, CUTOUT, TRANSLUCENT) o container percorre os chunks visiveis. Com VBO (`VboRenderList`): por chunk-camada `pushMatrix`, `translate`, `multMatrix`, `bindBuffer`, 4 ponteiros de vertice, `glDrawArrays`, `popMatrix`. Sem VBO (`RenderList`): um `glCallList` por chunk-camada.
- Cada `RenderChunk` possui um `VertexBuffer` (VBO) por camada.

## Pontos de integracao (todos existem nos fontes)
| Ponto | Uso previsto |
|---|---|
| `RenderGlobal.renderContainer` (privado, tipo `ChunkRenderContainer`) | Fase 2A: trocado por reflexao, achado pelo tipo |
| `RenderGlobal.renderChunkFactory` (`IRenderChunkFactory`) | Fase 2B: `RenderChunk` proprio com buffers por regiao |
| `RenderGlobal.renderDispatcher` (privado final) | Fase 4: fila/threads proprias |
| `RenderChunk.getVertexBufferByLayer(int)` (publico) | Fase 2B: devolver buffer de regiao |
| `ChunkRenderDispatcher.uploadChunk` | Fase 2B: upload para arena de regiao |

## Fase 2A (esta versao)
O mod troca o container por `MeteredVboRenderList` ou `MeteredRenderList`, que chamam o desenho original e medem o tempo de CPU e o numero de chunk-draws por frame. **O desenho nao muda.** Objetivos: provar o ponto de integracao no jogo real e obter a linha de base para os benchmarks.

## Limitacoes conhecidas
- O container e recriado pelo vanilla ao ligar/desligar VBO; o mod reinstala no tick seguinte.
- Se outro mod trocar o container, o hook entra em fallback e registra o motivo.
- O tempo medido e de CPU, nao de GPU.
