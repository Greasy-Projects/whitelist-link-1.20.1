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
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.net.http.HttpRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

@Mixin(PlayerManager.class)
public class WhitelistMixin {
	@Inject(method = "checkCanJoin", at = @At("TAIL"), cancellable = true)
	private void interceptJoin(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
		if (!WhitelistLinkConfig.isEnabled())
			return;
		String uuid = profile.getId().toString();
		Text errorMessage = Text.of("§6Could not connect to whitelist server.\nIf this issue persists, please contact §3verycrunchy §6on discord.");

		// Check if the player is whitelisted
		String whitelistCheckResponse = null;
		try {

			whitelistCheckResponse = graphqlQuery(String.format(
					"query{checkWhitelistByUUID(uuid: \"%s\")}",
					uuid));
		} catch (URISyntaxException | IOException | InterruptedException e) {
			cir.setReturnValue(errorMessage);
			e.printStackTrace();
			return;
		}

		JsonObject whitelistCheckResponseJson = null;
		Gson gson = new Gson();
		try {
			whitelistCheckResponseJson = gson.fromJson(whitelistCheckResponse, JsonObject.class);
			if (whitelistCheckResponseJson.getAsJsonObject("data").get("checkWhitelistByUUID").getAsBoolean())
				return;
		} catch (JsonSyntaxException e) {
			cir.setReturnValue(errorMessage);
			e.printStackTrace();
			return;
		}

		// if the player is not whitelisted, generate a code and disconnect them
		String codeGenerationResponse = null;
		try {
			codeGenerationResponse = graphqlQuery(String.format(
					"mutation{whitelistCode(uuid: \"%s\")}",
					uuid));
		} catch (URISyntaxException | IOException | InterruptedException e) {
			cir.setReturnValue(errorMessage);
			e.printStackTrace();
		}

		JsonObject codeGenerationResponseJson = gson.fromJson(codeGenerationResponse, JsonObject.class);

		String token = codeGenerationResponseJson.getAsJsonObject("data").get("whitelistCode").getAsString();
		cir.setReturnValue(Text.of("""
				§6You are not yet whitelisted on this server!\n
				§6Visit §b""" + WhitelistLinkConfig.getLoginUrl()
				+ "§r§6 and enter your whitelist code to gain access to the server." +
				"\n\n§6Your whitelist code is: §e§l" + token + "\n\n§r§7§o(code is valid for 5 minutes)."));
	}

	private static String graphqlQuery(String query)
			throws URISyntaxException, IOException, InterruptedException {
		URI uri = new URI(WhitelistLinkConfig.getApiBase() + "/graphql");
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("query", query);
		String requestBodyJson = new Gson().toJson(requestBody);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(uri)
				.header("Authorization", "Bearer " + WhitelistLinkConfig.getApiKey())
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
				.build();

		return HttpClient.newHttpClient().send(request,
				HttpResponse.BodyHandlers.ofString()).body();
	}
}