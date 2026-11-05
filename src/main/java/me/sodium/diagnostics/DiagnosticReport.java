package me.sodium.diagnostics;

import me.sodium.Sodium;
import me.sodium.compatibility.ModEnvironment;
import me.sodium.core.ModuleStatus;
import me.sodium.core.SodiumCore;
import me.sodium.graphics.GraphicsCapabilities;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.Loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Monta o texto do relatorio de diagnostico. Somente leitura. */
public final class DiagnosticReport {
    private DiagnosticReport() {
    }

    public static String build() {
        StringBuilder sb = new StringBuilder(4096);
        String nl = "\n";
        sb.append("=== ").append(Sodium.NAME).append(' ').append(Sodium.VERSION).append(" - Diagnostico ===").append(nl);
        sb.append("Minecraft: ").append(Loader.instance().getMCVersionString()).append(nl);
        sb.append("Forge: ").append(ForgeVersion.getVersion()).append(nl);
        sb.append("Java: ").append(System.getProperty("java.version")).append(" (")
                .append(System.getProperty("java.vendor")).append(')').append(nl);
        sb.append("SO: ").append(System.getProperty("os.name")).append(' ')
                .append(System.getProperty("os.arch")).append(nl);
        sb.append("CPUs logicas: ").append(Runtime.getRuntime().availableProcessors())
                .append(" | Heap max: ").append(Runtime.getRuntime().maxMemory() / (1024 * 1024)).append(" MiB").append(nl);
        sb.append("Threads de chunk configuradas: ").append(SodiumCore.config().chunkBuilderThreads)
                .append(" (uso a partir da Fase 4)").append(nl);

        sb.append(nl).append("--- Graficos ---").append(nl);
        GraphicsCapabilities g = SodiumCore.graphics();
        if (g == null) {
            sb.append("Estado da sondagem: ").append(SodiumCore.probeState());
            if (SodiumCore.probeFailure() != null) {
                sb.append(" - ").append(SodiumCore.probeFailure());
            }
            sb.append(nl);
        } else {
            sb.append("Fabricante: ").append(g.vendorString).append(" [").append(g.vendor).append(']').append(nl);
            sb.append("Modelo: ").append(g.rendererString).append(nl);
            sb.append("OpenGL: ").append(g.versionString).append(" (interpretado como ")
                    .append(g.glMajor).append('.').append(g.glMinor).append(')').append(nl);
            sb.append("VRAM dedicada: ").append(g.dedicatedVramMiB >= 0 ? g.dedicatedVramMiB + " MiB"
                    : "indisponivel (driver nao expoe GL_NVX_gpu_memory_info)").append(nl);
            sb.append("Recursos anunciados pelo driver:").append(nl);
            feature(sb, "VBO", g.vbo);
            feature(sb, "glMultiDrawArrays", g.multiDrawArrays);
            feature(sb, "Occlusion query", g.occlusionQuery);
            feature(sb, "VAO", g.vertexArrayObject);
            feature(sb, "glMapBufferRange", g.mapBufferRange);
            feature(sb, "Copy buffer", g.copyBuffer);
            feature(sb, "DrawElementsBaseVertex", g.drawElementsBaseVertex);
            feature(sb, "Fence sync", g.fenceSync);
            feature(sb, "Multi-draw indirect", g.multiDrawIndirect);
            feature(sb, "Buffer storage", g.bufferStorage);
            List<String> ext = new ArrayList<String>(g.extensions);
            Collections.sort(ext);
            sb.append("Extensoes (").append(ext.size()).append("):").append(nl);
            for (String e : ext) {
                sb.append("  ").append(e).append(nl);
            }
        }

        sb.append(nl).append("--- Modulos ---").append(nl);
        for (Map.Entry<String, ModuleStatus> e : SodiumCore.modules().entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append(nl);
        }

        sb.append(nl).append("--- Compatibilidade (somente deteccao) ---").append(nl);
        sb.append("OptiFine (heuristica por classe): ")
                .append(ModEnvironment.looksLikeOptiFinePresent() ? "provavelmente presente" : "nao detectado").append(nl);
        sb.append("Conflitos detectados: nenhum (analise de conflitos ainda nao implementada)").append(nl);
        sb.append("Recursos graficos desativados: nenhum (nenhuma otimizacao esta ativa na Fase 1)").append(nl);
        sb.append("Mods carregados:").append(nl);
        for (String mod : ModEnvironment.listLoadedMods()) {
            sb.append("  ").append(mod).append(nl);
        }
        return sb.toString();
    }

    private static void feature(StringBuilder sb, String name, boolean value) {
        sb.append("  ").append(name).append(": ").append(value ? "sim" : "nao").append('\n');
    }
}
