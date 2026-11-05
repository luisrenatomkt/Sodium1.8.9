package me.sodium.hooks;

import me.sodium.Sodium;
import me.sodium.compatibility.ModEnvironment;
import me.sodium.core.ModuleStatus;
import me.sodium.core.SodiumCore;
import me.sodium.renderer.chunk.MeteredRenderList;
import me.sodium.renderer.chunk.MeteredVboRenderList;
import me.sodium.renderer.chunk.SodiumContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkRenderContainer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderList;
import net.minecraft.client.renderer.VboRenderList;

import java.lang.reflect.Field;

/**
 * Troca o ChunkRenderContainer do RenderGlobal por reflexao, sem coremod/ASM.
 * O campo e localizado pelo TIPO (ChunkRenderContainer), o que funciona tanto no ambiente
 * de desenvolvimento quanto no jogo normal (onde campos tem nomes SRG).
 *
 * O RenderGlobal recria o container quando o jogador liga/desliga VBO; por isso a verificacao
 * roda a cada tick e reinstala quando necessario. Deve ser chamado na thread principal.
 */
public final class RenderGlobalHook {
    private static final String MODULE = "Chunk Renderer";

    private static Field containerField;
    private static boolean permanentlyDisabled;
    private static boolean installedOnce;
    private static String state = "nao iniciado";

    private RenderGlobalHook() {
    }

    public static String state() {
        return state;
    }

    public static void tick() {
        if (permanentlyDisabled || !SodiumCore.config().enableRenderHook) {
            if (!permanentlyDisabled && !"desativado na configuracao".equals(state)) {
                state = "desativado na configuracao";
                SodiumCore.setModuleStatus(MODULE, ModuleStatus.DISABLED);
            }
            return;
        }
        RenderGlobal renderGlobal = Minecraft.getMinecraft().renderGlobal;
        if (renderGlobal == null) {
            return;
        }
        try {
            if (containerField == null) {
                if (ModEnvironment.looksLikeOptiFinePresent()) {
                    disable("OptiFine detectado: renderizador original/OptiFine mantido (sem conflito)");
                    return;
                }
                containerField = findContainerField();
            }
            Object current = containerField.get(renderGlobal);
            if (current instanceof SodiumContainer) {
                return;
            }
            ChunkRenderContainer replacement;
            if (current instanceof VboRenderList) {
                replacement = new MeteredVboRenderList();
            } else if (current instanceof RenderList) {
                replacement = new MeteredRenderList();
            } else {
                String name = current == null ? "null" : current.getClass().getName();
                disable("container desconhecido (" + name + "): outro mod substituiu o renderizador");
                return;
            }
            containerField.set(renderGlobal, replacement);
            state = "instalado: " + replacement.getClass().getSimpleName();
            SodiumCore.setModuleStatus(MODULE, ModuleStatus.METRICS_ONLY);
            if (!installedOnce) {
                installedOnce = true;
                Sodium.LOGGER.info("Container de render instalado ({}); somente medicao, desenho original.",
                        replacement.getClass().getSimpleName());
            }
        } catch (ReflectiveOperationException | RuntimeException ex) {
            Sodium.LOGGER.error("Falha ao instalar o container de render; renderizador original mantido.", ex);
            disable("falha de reflexao: " + ex);
        }
    }

    private static void disable(String reason) {
        permanentlyDisabled = true;
        state = "DESATIVADO - " + reason;
        SodiumCore.setModuleStatus(MODULE, ModuleStatus.FALLBACK);
        Sodium.LOGGER.warn("Chunk Renderer em fallback: {}", reason);
    }

    private static Field findContainerField() throws NoSuchFieldException {
        Field found = null;
        for (Field f : RenderGlobal.class.getDeclaredFields()) {
            if (f.getType() == ChunkRenderContainer.class) {
                if (found != null) {
                    throw new NoSuchFieldException("Mais de um campo ChunkRenderContainer em RenderGlobal");
                }
                found = f;
            }
        }
        if (found == null) {
            throw new NoSuchFieldException("Campo ChunkRenderContainer nao encontrado em RenderGlobal");
        }
        found.setAccessible(true);
        return found;
    }
}
