package me.sodium.diagnostics;

import me.sodium.Sodium;
import me.sodium.core.SodiumCore;
import me.sodium.graphics.GraphicsCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

/**
 * Perfil de frame para descobrir se o limite e CPU ou GPU. Somente thread principal.
 *
 * Segmentos medidos (ordem real em Minecraft.runGameLoop):
 *   runTick (ClientTick START..END) -> RenderTick START -> render -> RenderTick END -> swap/sync.
 * O tempo da GPU e medido com uma consulta GL_TIME_ELAPSED em volta de todo o render
 * (RenderTick START..END), lida com alguns frames de atraso para nao travar a CPU.
 * O swap de buffers nao entra no tempo da GPU.
 */
public final class FrameProfiler {
    private static final long WINDOW_NANOS = 1000000000L;
    private static final int GL_TIME_ELAPSED = 0x88BF;
    private static final int RING = 4;

    // janela atual
    private static long windowStart;
    private static long lastRenderStart;
    private static long renderStart;
    private static long tickStart;
    private static int frames;
    private static long accPeriod;
    private static long accRender;
    private static long accTick;
    private static long maxPeriod;
    private static long accGpu;
    private static int gpuSamples;

    // consultas de GPU
    private static boolean gpuInitTried;
    private static boolean gpuEnabled;
    private static int[] queryIds;
    private static boolean[] inFlight;
    private static int head;
    private static int activeSlot = -1;
    private static String gpuState = "nao iniciado";

    // ultimo resultado
    private static volatile boolean hasSnapshot;
    private static volatile double snapFps;
    private static volatile double snapPeriodMs;
    private static volatile double snapMaxPeriodMs;
    private static volatile double snapRenderMs;
    private static volatile double snapTickMs;
    private static volatile double snapGpuMs = -1.0;

    private FrameProfiler() {
    }

    public static boolean hasSnapshot() {
        return hasSnapshot;
    }

    public static String gpuState() {
        return gpuState;
    }

    public static void onClientTickStart() {
        if (!SodiumCore.config().enableFrameProfiler) {
            return;
        }
        tickStart = System.nanoTime();
    }

    public static void onClientTickEnd() {
        if (tickStart != 0L) {
            accTick += System.nanoTime() - tickStart;
            tickStart = 0L;
        }
    }

    public static void onRenderStart() {
        if (!SodiumCore.config().enableFrameProfiler) {
            return;
        }
        long now = System.nanoTime();
        if (lastRenderStart != 0L) {
            long period = now - lastRenderStart;
            accPeriod += period;
            frames++;
            if (period > maxPeriod) {
                maxPeriod = period;
            }
        }
        lastRenderStart = now;
        renderStart = now;
        if (windowStart == 0L) {
            windowStart = now;
        }
        beginGpu();
        if (now - windowStart >= WINDOW_NANOS) {
            closeWindow(now);
        }
    }

    public static void onRenderEnd() {
        if (renderStart != 0L) {
            accRender += System.nanoTime() - renderStart;
            renderStart = 0L;
            endGpu();
        }
    }

    private static void closeWindow(long now) {
        if (frames > 0) {
            snapFps = frames * 1.0e9 / (now - windowStart);
            snapPeriodMs = accPeriod / 1.0e6 / frames;
            snapMaxPeriodMs = maxPeriod / 1.0e6;
            snapRenderMs = accRender / 1.0e6 / frames;
            snapTickMs = accTick / 1.0e6 / frames;
            snapGpuMs = gpuSamples > 0 ? accGpu / 1.0e6 / gpuSamples : -1.0;
            hasSnapshot = true;
        }
        windowStart = now;
        frames = 0;
        accPeriod = 0L;
        accRender = 0L;
        accTick = 0L;
        maxPeriod = 0L;
        accGpu = 0L;
        gpuSamples = 0;
    }

    // ---------------------------------------------------------------- GPU

    private static void initGpu() {
        gpuInitTried = true;
        GraphicsCapabilities g = SodiumCore.graphics();
        if (g == null) {
            gpuState = "indisponivel: GPU ainda nao detectada";
            gpuInitTried = false; // tenta de novo depois
            return;
        }
        if (!g.extensions.contains("GL_ARB_timer_query") && !g.extensions.contains("GL_EXT_timer_query")
                && !g.atLeast(3, 3)) {
            gpuState = "indisponivel: driver sem timer query";
            return;
        }
        try {
            queryIds = new int[RING];
            inFlight = new boolean[RING];
            for (int i = 0; i < RING; i++) {
                queryIds[i] = GL15.glGenQueries();
            }
            gpuEnabled = true;
            gpuState = "ativo (consulta GL_TIME_ELAPSED)";
        } catch (RuntimeException ex) {
            disableGpu("falha ao criar consultas", ex);
        }
    }

    private static void beginGpu() {
        if (!gpuInitTried) {
            initGpu();
        }
        if (!gpuEnabled) {
            return;
        }
        try {
            pollGpu();
            int slot = head;
            if (inFlight[slot]) {
                return; // resultado antigo ainda nao chegou; descarta esta amostra
            }
            GL11.glGetError(); // limpa erro pendente de outro codigo
            GL15.glBeginQuery(GL_TIME_ELAPSED, queryIds[slot]);
            if (GL11.glGetError() != GL11.GL_NO_ERROR) {
                disableGpu("glBeginQuery gerou erro (outro mod pode estar usando consultas de tempo)", null);
                return;
            }
            activeSlot = slot;
        } catch (RuntimeException ex) {
            disableGpu("excecao em glBeginQuery", ex);
        }
    }

    private static void endGpu() {
        if (!gpuEnabled || activeSlot < 0) {
            return;
        }
        try {
            GL15.glEndQuery(GL_TIME_ELAPSED);
            inFlight[activeSlot] = true;
            head = (head + 1) % RING;
            activeSlot = -1;
        } catch (RuntimeException ex) {
            disableGpu("excecao em glEndQuery", ex);
        }
    }

    private static void pollGpu() {
        for (int i = 0; i < RING; i++) {
            if (inFlight[i] && GL15.glGetQueryObjectui(queryIds[i], GL15.GL_QUERY_RESULT_AVAILABLE) != 0) {
                long ns = GL15.glGetQueryObjectui(queryIds[i], GL15.GL_QUERY_RESULT) & 0xFFFFFFFFL;
                accGpu += ns;
                gpuSamples++;
                inFlight[i] = false;
            }
        }
    }

    private static void disableGpu(String reason, RuntimeException ex) {
        gpuEnabled = false;
        activeSlot = -1;
        gpuState = "desativado: " + reason;
        if (ex != null) {
            Sodium.LOGGER.warn("Medicao de GPU desativada: " + reason, ex);
        } else {
            Sodium.LOGGER.warn("Medicao de GPU desativada: {}", reason);
        }
    }

    // ------------------------------------------------------------- relatorio

    /** Texto do relatorio; vazio se ainda nao houver dados. */
    public static String describe() {
        StringBuilder sb = new StringBuilder();
        if (!hasSnapshot) {
            sb.append("Perfil de frame: sem dados ainda (entre em um mundo)\n");
            sb.append("Medicao de GPU: ").append(gpuState).append('\n');
            return sb.toString();
        }
        java.util.Locale l = java.util.Locale.ROOT;
        double outside = Math.max(0.0, snapPeriodMs - snapRenderMs - snapTickMs);
        sb.append(String.format(l, "Ultima janela de 1s: %.1f fps | frame medio %.1f ms | pior frame %.1f ms%n",
                snapFps, snapPeriodMs, snapMaxPeriodMs));
        sb.append(String.format(l, "  CPU no render (mundo + GUI + mods): %.1f ms%n", snapRenderMs));
        sb.append(String.format(l, "  CPU nos ticks do jogo (mods incluidos): %.1f ms%n", snapTickMs));
        sb.append(String.format(l, "  Restante (swap de buffers, espera da GPU, limite de FPS): %.1f ms%n", outside));
        if (snapGpuMs >= 0.0) {
            sb.append(String.format(l, "  GPU executando o render: %.1f ms%n", snapGpuMs));
        } else {
            sb.append("  GPU executando o render: sem amostra (").append(gpuState).append(")\n");
        }
        sb.append("Leitura (indicativa): ").append(verdict()).append('\n');
        return sb.toString();
    }

    private static String verdict() {
        if (snapPeriodMs <= 0.0) {
            return "sem dados";
        }
        if (snapGpuMs >= 0.0 && snapGpuMs / snapPeriodMs >= 0.8) {
            return "a GPU ocupa a maior parte do frame; provavel limite de GPU (otimizar CPU/draw calls ajuda pouco)";
        }
        if ((snapRenderMs + snapTickMs) / snapPeriodMs >= 0.8) {
            return "a CPU da thread principal ocupa a maior parte do frame; provavel limite de CPU";
        }
        return "inconclusivo (frame dividido entre CPU, GPU e espera)";
    }
}
