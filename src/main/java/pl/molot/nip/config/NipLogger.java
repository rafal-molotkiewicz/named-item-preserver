// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import pl.molot.nip.NamedItemPreserver;

public class NipLogger {
    private final Logger delegate;

    public NipLogger(Logger delegate) {
        this.delegate = delegate;
    }

    public void important(String message, Object... args) {
        if (ConfigManager.get().verbosity.level >= Config.Verbosity.IMPORTANT.level) {
            delegate.info(message, args);
        }
    }

    public void info(String message, Object... args) {
        if (ConfigManager.get().verbosity.level >= Config.Verbosity.NORMAL.level) {
            delegate.info(message, args);
			broadcastInfo(message, args);
        }
    }

    public void debug(String message, Object... args) {
        if (ConfigManager.get().verbosity.level >= Config.Verbosity.DEBUG.level) {
            delegate.info(message, args);
			broadcastDebug(message, args);
        }
    }

    public void warn(String message, Object... args) {
        delegate.warn(message, args);
    }

    public void error(String message, Object... args) {
        delegate.error(message, args);
    }

    public void error(String message, Throwable throwable) {
        delegate.error(message, throwable);
    }

    private void broadcastInfo(String message, Object... args) {
        Config.BroadcastTo target = ConfigManager.get().broadcastTo;
        if (target == null || target == Config.BroadcastTo.NONE) return;

        String formatted = format(message, args);
        if (target == Config.BroadcastTo.ALL) {
            broadcastToPlayers(formatted, false);
        } else if (target == Config.BroadcastTo.OP) {
            broadcastToPlayers(formatted, true);
        }
    }

    private void broadcastDebug(String message, Object... args) {
        Config.BroadcastTo target = ConfigManager.get().broadcastTo;
        if (target == null || target == Config.BroadcastTo.NONE) return;
        broadcastToPlayers(format(message, args), true);
    }

    private static String format(String message, Object... args) {
        FormattingTuple tuple = MessageFormatter.arrayFormat(message, args);
        String formatted = tuple.getMessage();
        return formatted == null ? String.valueOf(message) : formatted;
    }

    private static void broadcastToPlayers(String message, boolean opsOnly) {
        MinecraftServer server = NamedItemPreserver.getServer();
        if (server == null) return;

        server.execute(() -> {
            Text text = Text.literal(message);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!opsOnly || player.hasPermissionLevel(2)) {
                    player.sendMessage(text, false);
                }
            }
        });
    }
}
