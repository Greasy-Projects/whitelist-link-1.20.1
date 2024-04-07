package co.greasygang;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WhitelistLink implements ModInitializer {
	public static final String MOD_ID = "whitelistlink";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Loading WhitelistLink... | greasygang.co");
	}
}