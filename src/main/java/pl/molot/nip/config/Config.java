// SPDX-License-Identifier: LicenseRef-Charity
package pl.molot.nip.config;

import com.google.gson.annotations.SerializedName;

public class Config {
    public Verbosity verbosity = Verbosity.NORMAL;
    public boolean displayItemName = false;
    public BroadcastTo broadcastTo = BroadcastTo.NONE;

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

    public enum BroadcastTo {
        @SerializedName(value = "None", alternate = {"NONE", "none"})
        NONE,

        @SerializedName(value = "OP", alternate = {"op", "Op"})
        OP,

        @SerializedName(value = "All", alternate = {"ALL", "all"})
        ALL
    }

    @Override
    public String toString() {
        return "Config{" +
                "verbosity=" + verbosity.displayName +
                ", displayItemName=" + displayItemName +
                ", broadcastTo=" + broadcastTo +
                '}';
    }
}
