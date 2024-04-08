package co.greasygang.mixin;

import java.net.SocketAddress;

import co.greasygang.utils.WhitelistLinkConfig;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.text.Text;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import com.google.gson.*;

@Mixin(PlayerManager.class)
public class WhitelistMixin {
	@Inject(method = "checkCanJoin", at = @At("TAIL"), cancellable = true)
	private void interceptJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
		if(WhitelistLinkConfig.isEnabled()) {
			String uuid = profile.getId().toString();
			String api_base = WhitelistLinkConfig.getApiBase();

			// first, check if the player is whitelisted
			HttpRequest whitelistCheckRequest = HttpRequest.newBuilder()
					.uri(URI.create(api_base + "/checkWhitelistByUuid?uuid=" + uuid))
					.method("GET", HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> whitelistCheckResponse = null;

			try {
				whitelistCheckResponse = HttpClient.newHttpClient().send(whitelistCheckRequest,
						HttpResponse.BodyHandlers.ofString());
			} catch (IOException | InterruptedException e) {
				cir.setReturnValue(Text.of("§6An error occurred while trying to validate your whitelist status on the server."));
				e.printStackTrace();
			}

			// parse response as json
			Gson gson = new Gson();
			JsonObject whitelistCheckResponseJson = gson.fromJson(whitelistCheckResponse.body(), JsonObject.class);

			// check if the player is whitelisted
			if (whitelistCheckResponseJson.get("linked").getAsBoolean()) {
				System.out.println("Player is whitelisted on greasygang.co!");
				return;
			}

			// if the player is not whitelisted, generate a code and disconnect them
			HttpRequest codeGenerationRequest = HttpRequest.newBuilder()
					.uri(URI.create(api_base + "/createWhitelistCode?uuid=" + uuid))
					.method("POST", HttpRequest.BodyPublishers.noBody())
					.build();

			HttpResponse<String> codeGenerationResponse = null;

			try {
				codeGenerationResponse = HttpClient.newHttpClient().send(codeGenerationRequest,
						HttpResponse.BodyHandlers.ofString());
			} catch (IOException | InterruptedException e) {
				cir.setReturnValue(Text.of("§6An error occurred while trying to whitelist you on this server."));
				e.printStackTrace();
			}

			// parse response as json
			JsonObject codeGenerationResponseJson = gson.fromJson(codeGenerationResponse.body(), JsonObject.class);

			// disconnect the player
			if (codeGenerationResponseJson.get("success").getAsBoolean()) {
				cir.setReturnValue(Text.of("""
						§6You are not whitelisted on this server!
							
						§6Please visit §e""" + WhitelistLinkConfig.getLoginUrl() + " §6to gain access to the server." +
						"\n§6Your code is: §e" + codeGenerationResponseJson.get("token").getAsString() + """
							
							
						§c§lDO NOT SHARE THIS CODE WITH ANYBODY!"""));
			} else {
				cir.setReturnValue(Text.of("§6An error occurred while trying to whitelist you on this server."));
			}
		}
	}
}