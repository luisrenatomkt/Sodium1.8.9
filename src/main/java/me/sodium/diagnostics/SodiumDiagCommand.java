package me.sodium.diagnostics;

import me.sodium.core.SodiumCore;
import me.sodium.graphics.GraphicsCapabilities;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/** Comando de cliente /sodiumdiag: regrava diagnostics.txt e mostra um resumo no chat. */
public class SodiumDiagCommand extends CommandBase {
    @Override
    public String getCommandName() {
        return "sodiumdiag";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/sodiumdiag";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        SodiumCore.probeGraphics();
        GraphicsCapabilities g = SodiumCore.graphics();
        if (g == null) {
            say(sender, "Sondagem grafica: " + SodiumCore.probeState()
                    + (SodiumCore.probeFailure() != null ? " (" + SodiumCore.probeFailure() + ")" : ""));
        } else {
            say(sender, "GPU: " + g.rendererString + " | OpenGL " + g.versionString);
            say(sender, "VBO=" + g.vbo + " VAO=" + g.vertexArrayObject + " MapRange=" + g.mapBufferRange
                    + " MultiDrawIndirect=" + g.multiDrawIndirect);
        }
        if (SodiumCore.writeDiagnostics()) {
            say(sender, "Relatorio completo: " + SodiumCore.diagnosticsFile().getAbsolutePath());
        } else {
            say(sender, "Falha ao gravar o relatorio; veja latest.log.");
        }
    }

    private static void say(ICommandSender sender, String msg) {
        sender.addChatMessage(new ChatComponentText("[Sodium189] " + msg));
    }
}
