package me.sodium.core;

import me.sodium.Sodium;
import me.sodium.config.SodiumConfig;
import me.sodium.diagnostics.DiagnosticReport;
import me.sodium.graphics.GraphicsCapabilities;
import org.lwjgl.opengl.Display;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Nucleo: inicializacao, estado global, deteccao de hardware e registro de modulos.
 * Todas as chamadas OpenGL daqui devem ocorrer na thread principal do cliente.
 */
public final class SodiumCore {
    public enum ProbeState { PENDING, DONE, FAILED }

    private static SodiumConfig config;
    private static File diagnosticsFile;
    private static volatile GraphicsCapabilities graphics;
    private static volatile ProbeState probeState = ProbeState.PENDING;
    private static volatile String probeFailure;
    private static final Map<String, ModuleStatus> MODULES = new LinkedHashMap<String, ModuleStatus>();

    private SodiumCore() {
    }

    public static void preInit(File configRoot) {
        File dir = new File(configRoot, Sodium.MODID);
        if (!dir.isDirectory() && !dir.mkdirs()) {
            Sodium.LOGGER.warn("Nao foi possivel criar o diretorio de configuracao: {}", dir);
        }
        config = SodiumConfig.load(new File(dir, "sodium.cfg"));
        diagnosticsFile = new File(dir, "diagnostics.txt");

        MODULES.put("Core", ModuleStatus.ACTIVE);
        MODULES.put("Diagnostics", ModuleStatus.ACTIVE);
        MODULES.put("Compatibility Layer", ModuleStatus.DETECTION_ONLY);
        MODULES.put("Chunk Renderer", ModuleStatus.NOT_IMPLEMENTED);
        MODULES.put("Culling Engine", ModuleStatus.NOT_IMPLEMENTED);
        MODULES.put("Memory Manager", ModuleStatus.NOT_IMPLEMENTED);
        MODULES.put("Worker System", ModuleStatus.NOT_IMPLEMENTED);
        MODULES.put("GUI", ModuleStatus.NOT_IMPLEMENTED);

        Sodium.LOGGER.info("{} {} (Fase 1) iniciado. O renderizador original do Minecraft continua em uso.",
                Sodium.NAME, Sodium.VERSION);
        if (config.debugLogging) {
            Sodium.LOGGER.info("Configuracao: threads={}, diagnosticsOnStartup={}",
                    config.chunkBuilderThreads, config.writeDiagnosticsOnStartup);
        }
    }

    public static void postInit() {
        probeGraphics();
    }

    /**
     * Tenta detectar GPU e recursos OpenGL. Retorna true quando a sondagem terminou
     * (com sucesso ou falha definitiva) e false se o contexto ainda nao existe.
     */
    public static boolean probeGraphics() {
        if (probeState != ProbeState.PENDING) {
            return true;
        }
        if (!Display.isCreated()) {
            return false;
        }
        try {
            graphics = GraphicsCapabilities.query();
            probeState = ProbeState.DONE;
            Sodium.LOGGER.info("GPU: {} | {} | OpenGL {}", graphics.vendorString, graphics.rendererString,
                    graphics.versionString);
            if (config.writeDiagnosticsOnStartup) {
                writeDiagnostics();
            }
        } catch (RuntimeException ex) {
            probeState = ProbeState.FAILED;
            probeFailure = ex.toString();
            Sodium.LOGGER.error("Falha ao detectar recursos OpenGL; nenhuma otimizacao sera ativada.", ex);
        }
        return true;
    }

    /** Grava o relatorio em disco. Erros de E/S sao registrados, nunca ignorados. */
    public static boolean writeDiagnostics() {
        try {
            Files.write(diagnosticsFile.toPath(), DiagnosticReport.build().getBytes(StandardCharsets.UTF_8));
            Sodium.LOGGER.info("Diagnostico gravado em {}", diagnosticsFile.getAbsolutePath());
            return true;
        } catch (IOException ex) {
            Sodium.LOGGER.error("Nao foi possivel gravar " + diagnosticsFile, ex);
            return false;
        }
    }

    public static SodiumConfig config() {
        return config;
    }

    public static GraphicsCapabilities graphics() {
        return graphics;
    }

    public static ProbeState probeState() {
        return probeState;
    }

    public static String probeFailure() {
        return probeFailure;
    }

    public static boolean isGraphicsProbeFinished() {
        return probeState != ProbeState.PENDING;
    }

    public static File diagnosticsFile() {
        return diagnosticsFile;
    }

    public static Map<String, ModuleStatus> modules() {
        return Collections.unmodifiableMap(MODULES);
    }
}
