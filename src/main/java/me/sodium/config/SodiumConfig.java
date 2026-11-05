package me.sodium.config;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

/**
 * Configuracao persistente (config/sodium189/sodium.cfg).
 * Fase 1: somente opcoes que ja tem efeito real. Novas opcoes entram junto com a
 * funcionalidade correspondente.
 */
public final class SodiumConfig {
    public static final int MAX_THREADS = 16;

    /** Log detalhado do nucleo. */
    public final boolean debugLogging;
    /** Escreve diagnostics.txt ao detectar a GPU. */
    public final boolean writeDiagnosticsOnStartup;
    /** Threads de construcao de chunks. Lido a partir da Fase 4 (Worker System); ja validado aqui. */
    public final int chunkBuilderThreads;

    private SodiumConfig(boolean debugLogging, boolean writeDiagnosticsOnStartup, int chunkBuilderThreads) {
        this.debugLogging = debugLogging;
        this.writeDiagnosticsOnStartup = writeDiagnosticsOnStartup;
        this.chunkBuilderThreads = chunkBuilderThreads;
    }

    public static int defaultThreads() {
        int cores = Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(4, cores - 1));
    }

    public static SodiumConfig load(File file) {
        Configuration cfg = new Configuration(file);
        try {
            cfg.load();
            boolean debug = cfg.getBoolean("debugLogging", "general", false,
                    "Ativa logs detalhados do nucleo.");
            boolean diag = cfg.getBoolean("writeDiagnosticsOnStartup", "general", true,
                    "Grava config/sodium189/diagnostics.txt assim que a GPU for detectada.");
            int threads = cfg.getInt("chunkBuilderThreads", "performance", defaultThreads(), 1, MAX_THREADS,
                    "Numero de threads de construcao de chunks (usado a partir da Fase 4).");
            return new SodiumConfig(debug, diag, threads);
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }
}
