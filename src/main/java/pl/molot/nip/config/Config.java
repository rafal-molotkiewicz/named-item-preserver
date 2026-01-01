// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.config;

public class Config {
    public Verbosity verbosity = Verbosity.NORMAL;
    public boolean displayItemName = false;

    public enum Verbosity {
        IMPORTANT(0, "Important"),
        NORMAL(1, "Normal"),
        DEBUG(2, "Debug");

        public final int level;
        public final String displayName;

        Verbosity(int level, String displayName) {
            this.level = level;
            this.displayName = displayName;
        }
    }

    @Override
    public String toString() {
        return "Config{" +
                "verbosity=" + verbosity.displayName +
                ", displayItemName=" + displayItemName +
                '}';
    }
}
