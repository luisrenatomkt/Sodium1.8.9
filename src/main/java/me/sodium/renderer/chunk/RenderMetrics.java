package me.sodium.renderer.chunk;

import net.minecraft.util.EnumWorldBlockLayer;

/**
 * Metricas do desenho de terreno. Chamado somente pela thread principal (render).
 * O tempo medido e o de CPU gasto emitindo as chamadas de desenho; NAO e tempo de GPU
 * (o driver pode enfileirar ou bloquear as chamadas).
 */
public final class RenderMetrics {
    private static final long WINDOW_NANOS = 1000000000L;

    private static long windowStart = System.nanoTime();
    private static long accNanos;
    private static int accChunkDraws;
    private static int accFrames;

    private static volatile boolean hasSnapshot;
    private static volatile double snapTerrainCpuMs;
    private static volatile double snapDrawsPerFrame;
    private static volatile double snapFramesPerSecond;

    private RenderMetrics() {
    }

    /** Uma chamada por camada (4 por frame em modo normal). Um "draw" = um chunk-camada desenhado. */
    public static void record(EnumWorldBlockLayer layer, int chunkDraws, long nanos) {
        accNanos += nanos;
        accChunkDraws += chunkDraws;
        if (layer == EnumWorldBlockLayer.SOLID) {
            accFrames++;
        }
        long now = System.nanoTime();
        long elapsed = now - windowStart;
        if (elapsed >= WINDOW_NANOS) {
            if (accFrames > 0) {
                snapTerrainCpuMs = (accNanos / 1.0e6) / accFrames;
                snapDrawsPerFrame = (double) accChunkDraws / accFrames;
                snapFramesPerSecond = accFrames * 1.0e9 / elapsed;
                hasSnapshot = true;
            }
            windowStart = now;
            accNanos = 0L;
            accChunkDraws = 0;
            accFrames = 0;
        }
    }

    public static boolean hasSnapshot() {
        return hasSnapshot;
    }

    public static double terrainCpuMsPerFrame() {
        return snapTerrainCpuMs;
    }

    public static double drawsPerFrame() {
        return snapDrawsPerFrame;
    }

    public static double framesPerSecond() {
        return snapFramesPerSecond;
    }
}
