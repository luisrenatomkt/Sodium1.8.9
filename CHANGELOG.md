# Changelog

## 0.1.0-phase1
- Fundacao: projeto ForgeGradle 2.1 / Forge 1.8.9 / Java 8, somente cliente.
- Configuracao (config/sodium189/sodium.cfg): debugLogging, writeDiagnosticsOnStartup, chunkBuilderThreads.
- Deteccao de GPU, versao OpenGL, extensoes e VRAM (quando o driver expoe NVX_gpu_memory_info).
- Diagnostico em config/sodium189/diagnostics.txt e comando de cliente /sodiumdiag.
- Deteccao (somente leitura) de OptiFine e lista de mods.
- Nenhuma otimizacao de renderizacao ainda: o renderizador original continua em uso.
