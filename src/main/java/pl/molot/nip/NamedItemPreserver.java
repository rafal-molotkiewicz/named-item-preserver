// SPDX-License-Identifier: LicenseRef-Charity

package pl.molot.nip;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.molot.nip.config.ConfigManager;
import pl.molot.nip.config.NipLogger;

public class NamedItemPreserver implements ModInitializer {
	public static final String MOD_ID = "named-item-preserver";

	private static final Logger VANILLA_LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static NipLogger LOGGER;

    @Override
    public void onInitialize() {
        // Initialize logger first before using it in ConfigManager
        LOGGER = new NipLogger(VANILLA_LOGGER);
        
        // Then load config which will use LOGGER
        ConfigManager.load(FabricLoader.getInstance().getConfigDir());

        LOGGER.important("Named Item Preserver loaded: named items will not despawn. Config: {}", 
                ConfigManager.get());
    }
}
