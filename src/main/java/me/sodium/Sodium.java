package me.sodium;

import me.sodium.core.SodiumCore;
import me.sodium.hooks.RenderGlobalHook;
import me.sodium.diagnostics.FrameProfiler;
import me.sodium.diagnostics.SodiumDiagCommand;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ponto de entrada do mod. Somente cliente: o mod nao altera nada no servidor.
 */
@Mod(modid = Sodium.MODID, name = Sodium.NAME, version = Sodium.VERSION,
        acceptedMinecraftVersions = "[1.8.9]", clientSideOnly = true, acceptableRemoteVersions = "*")
public class Sodium {
    public static final String MODID = "sodium189";
    public static final String NAME = "Sodium 1.8.9";
    /** Manter igual a "version" em build.gradle. */
    public static final String VERSION = "0.2.1-phase2a";

    public static final Logger LOGGER = LogManager.getLogger("Sodium189");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        SodiumCore.preInit(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new SodiumDiagCommand());
        // Tick: repete a sondagem da GPU se necessario e mantem o container de render instalado.
        FMLCommonHandler.instance().bus().register(this);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        SodiumCore.postInit();
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            FrameProfiler.onClientTickStart();
            return;
        }
        FrameProfiler.onClientTickEnd();
        SodiumCore.probeGraphics();
        RenderGlobalHook.tick();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            FrameProfiler.onRenderStart();
        } else {
            FrameProfiler.onRenderEnd();
        }
    }
}
