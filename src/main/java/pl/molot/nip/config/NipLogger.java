// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.config;

import org.slf4j.Logger;

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
        }
    }

    public void debug(String message, Object... args) {
        if (ConfigManager.get().verbosity.level >= Config.Verbosity.DEBUG.level) {
            delegate.info(message, args);
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
}
