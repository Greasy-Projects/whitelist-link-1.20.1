package co.greasygang;

import co.greasygang.utils.WhitelistLinkConfig;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static net.minecraft.server.command.CommandManager.literal;

public class WhitelistLink implements ModInitializer {
	public static final String MOD_ID = "whitelistlink";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		WhitelistLinkConfig.init();
		LOGGER.info("Loading WhitelistLink... | greasygang.co");

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) -> {
			// Reload command
			dispatcher.register(literal("whitelistlink")
				.executes(context -> {
					context.getSource().sendFeedback(() -> Text.literal("§6WhitelistLink §eby VeryCrunchy, dargy\n§7Connects the greasygang.co whitelist to your Minecraft server"), false);
					return 1;
				})
				.then((literal("reload")
					.requires(source -> source.hasPermissionLevel(4))
					.executes(context -> {
						WhitelistLinkConfig.init();
						context.getSource().sendFeedback(() -> Text.literal("§6WhitelistLink config reloaded."), false);
						return 1;
					})))
				.then((literal("toggle")
					.executes(context -> {
						WhitelistLinkConfig.toggleEnabled();

						if (WhitelistLinkConfig.isEnabled()) {
							context.getSource().sendFeedback(() -> Text.literal("§aWhitelistLink is now enabled."), false);
						} else {
							context.getSource().sendFeedback(() -> Text.literal("§cWhitelistLink is now disabled."), false);
						}
						return 1;
					})))
			);
		});
	}
}