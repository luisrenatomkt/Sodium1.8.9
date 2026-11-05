package me.sodium.renderer.chunk;

import net.minecraft.client.renderer.RenderList;
import net.minecraft.util.EnumWorldBlockLayer;

/** Fase 2A: mesmo desenho do RenderList (display lists) original, apenas com medicao. */
public class MeteredRenderList extends RenderList implements SodiumContainer {
    @Override
    public void renderChunkLayer(EnumWorldBlockLayer layer) {
        int draws = this.initialized ? this.renderChunks.size() : 0;
        long start = System.nanoTime();
        super.renderChunkLayer(layer);
        RenderMetrics.record(layer, draws, System.nanoTime() - start);
    }
}
