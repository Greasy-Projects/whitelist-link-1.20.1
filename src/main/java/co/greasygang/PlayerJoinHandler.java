package co.greasygang;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.text.MutableText;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;

public class PlayerJoinHandler {
    public static void init() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            MutableText text = Text.literal("Text");
            Style textStyle = text.getStyle()
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://greasygang.co/minecraft"));
            text.setStyle(textStyle);
            handler.getPlayer().networkHandler.disconnect(Text.literal("[ Click Here Generate Config ]")
                    .setStyle(
                            Style.EMPTY
                                    .withFormatting(Formatting.GREEN)
                                    .withClickEvent(
                                            new ClickEvent(ClickEvent.Action.OPEN_URL,
                                                    "https://greasygang.co/minecraft"))));

        });
    }

    // import java.io.IOException;
    // import java.net.URI;
    // import java.net.http.HttpClient;
    // import java.net.http.HttpResponse;
    // import java.net.http.HttpRequest;

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
