package co.greasygang;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.text.Text;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import com.google.gson.*;

public class PlayerJoinHandler {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // first, check if the player is whitelisted
            HttpRequest whitelistCheckRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4000/checkWhitelistByUuid?uuid=" + handler.getPlayer().getUuidAsString()))
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> whitelistCheckResponse = null;

            try {
                whitelistCheckResponse = HttpClient.newHttpClient().send(whitelistCheckRequest,
                        HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                handler.getPlayer().networkHandler.disconnect(Text.of("§6An error occurred while trying to whitelist you on this server."));
                e.printStackTrace();
            } catch (InterruptedException e) {
                handler.getPlayer().networkHandler.disconnect(Text.of("§6An error occurred while trying to whitelist you on this server."));
                e.printStackTrace();
            }

            // parse response as json
            Gson gson = new Gson();
            JsonObject whitelistCheckResponseJson = gson.fromJson(whitelistCheckResponse.body(), JsonObject.class);

            // print json response
            System.out.println(whitelistCheckResponseJson);

            // check if the player is whitelisted
            if (whitelistCheckResponseJson.get("linked").getAsBoolean()) {
                System.out.println("Player is whitelisted on greasygang.co!");
                return;
            }

            // if the player is not whitelisted, generate a code and disconnect them
            HttpRequest codeGenerationRequest = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:4000/createWhitelistCode?uuid=" + handler.getPlayer().getUuidAsString()))
                    .method("POST", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> codeGenerationResponse = null;

            try {
                codeGenerationResponse = HttpClient.newHttpClient().send(codeGenerationRequest,
                        HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                handler.getPlayer().networkHandler.disconnect(Text.of("§6An error occurred while trying to generate a code for you on the server."));
                e.printStackTrace();
            } catch (InterruptedException e) {
                handler.getPlayer().networkHandler.disconnect(Text.of("§6An error occurred while trying to generate a code for you on the server."));
                e.printStackTrace();
            }

            // parse response as json
            JsonObject codeGenerationResponseJson = gson.fromJson(codeGenerationResponse.body(), JsonObject.class);

            // print json response
            System.out.println(codeGenerationResponseJson);

            // disconnect the player
            handler.getPlayer().networkHandler.disconnect(Text.of("""
                    §6You are not whitelisted on this server!

                    §6Please visit §egreasygang.co §6to gain access to the server.
                    §6Your code is: §e""" + codeGenerationResponseJson.get("token").getAsString() + """

                    §c§lDO NOT SHARE THIS CODE WITH ANYBODY!"""));
            });
    }

    // private static void main(String[] args) {
    // // TODO: change to greasygang.co
    // HttpRequest request = HttpRequest.newBuilder()
    // .uri(URI.create("http://localhost:4000/mc/"))
    // .header("X-RapidAPI-Host", "jokes-by-api-ninjas.p.rapidapi.com")
    // .header("X-RapidAPI-Key", "your-rapidapi-key")
    // .method("GET", HttpRequest.BodyPublishers.noBody())
    // .build();
    // HttpResponse<String> response = null;
    // try {
    // response = HttpClient.newHttpClient().send(request,
    // HttpResponse.BodyHandlers.ofString());
    // } catch (IOException e) {
    // e.printStackTrace();
    // } catch (InterruptedException e) {
    // e.printStackTrace();
    // }
    // System.out.println(response.body());
    // }
}
