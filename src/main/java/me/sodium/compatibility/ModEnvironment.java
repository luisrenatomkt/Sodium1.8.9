package me.sodium.compatibility;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Fase 1: apenas DETECCAO do ambiente de mods (somente leitura, sem alterar nada).
 * Resolucao de conflitos e fallbacks entram na Fase 5.
 */
public final class ModEnvironment {
    private ModEnvironment() {
    }

    /** Heuristica: presenca de classe do OptiFine no classpath. Pode falhar com versoes diferentes. */
    public static boolean looksLikeOptiFinePresent() {
        ClassLoader cl = ModEnvironment.class.getClassLoader();
        return cl.getResource("optifine/OptiFineClassTransformer.class") != null
                || cl.getResource("optifine/OptiFineForgeTweaker.class") != null;
    }

    public static List<String> listLoadedMods() {
        List<String> out = new ArrayList<String>();
        for (ModContainer mod : Loader.instance().getModList()) {
            out.add(mod.getModId() + " " + mod.getVersion());
        }
        return out;
    }
}
