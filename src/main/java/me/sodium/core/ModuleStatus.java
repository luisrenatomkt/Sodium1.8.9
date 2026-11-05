package me.sodium.core;

/** Estado real de cada modulo. Nenhum modulo e marcado ACTIVE sem implementacao funcional. */
public enum ModuleStatus {
    NOT_IMPLEMENTED,
    DETECTION_ONLY,
    /** Integrado ao jogo, mas apenas medindo (nao altera o desenho). */
    METRICS_ONLY,
    DISABLED,
    ACTIVE,
    FALLBACK
}
