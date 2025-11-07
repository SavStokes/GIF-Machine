import java.io.*;
import com.sun.net.httpserver.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import com.google.gson.*;
import java.util.*;


public class GIFMachine {
    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Main page
        server.createContext("/final_destination", new MainPageHandler());
        // Each button goes to its own path
        server.createContext("/download", new DownloadHandler());
        server.createContext("/generate", new GenerateHandler());
        server.createContext("/history", new HistoryHandler());

        server.start();
        System.out.println("Can find the site @ http://localhost:8080/final_destination");
    }

    static class MainPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <html>
                    <head><title>GIF Machine</title></head>
                    <body style='background-color: black; text-align: center;'>
                        <h1 style='color: white; margin-top: 100px;'>GIF MACHINE</h1>
                        <div style='background-color: grey; width: 400px; margin: 0 auto; padding: 20px; border-radius: 15px;'>
                            <img src='https://media.tenor.com/4EhUju6UJtEAAAAm/grrr-rawr.webp'
                                 alt='Custom GIF'
                                 style='display: block; margin: 0 auto;'/>
                            <form action='/download' method='post'>
                                <input type='submit' value='Download GIF'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                            </form>
                            <form action='/generate' method='post'>
                                <input type='submit' value='Generate GIF'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                            </form>
                            <form action='/history' method='post'>
                                <input type='submit' value='GIF History'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                            </form>
                        </div>
                    </body>
                </html>
            """;
            sendResponse(exchange, response);
        }
    }

    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <html>
                    <body style='background-color: black; text-align: center; color: white;'>
                        <h1>Download Page</h1>
                        <a href='/final_destination' style='color: yellow;'>Back</a>
                    </body>
                </html>
            """;
            sendResponse(exchange, response);
        }
    }

    static class GenerateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            
            String API_KEY = "AIzaSyCd-hEmDQxPiVHfxHiMOz4JR4tSDFHbH0A";
            String apiURL = String.format(
            "https://tenor.googleapis.com/v2/featured?key=%s&limit=20&contentfilter=medium",
            API_KEY
        );

            HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                content.append(line);
            }
            in.close();
            conn.disconnect();

            // Parse JSON using Gson
            JsonObject root = JsonParser.parseString(content.toString()).getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");

            List<String> gifUrls = new ArrayList<>();

            for (JsonElement e : results) {
                JsonObject mediaFormats = e.getAsJsonObject().getAsJsonObject("media_formats");
                if (mediaFormats.has("gif")) {
                    String url = mediaFormats.getAsJsonObject("gif")
                            .get("url").getAsString();
                    gifUrls.add(url);
                }
            }

            // Pick one random GIF
            String ranGIF = gifUrls.isEmpty()
                    ? "https://media.tenor.com/4EhUju6UJtEAAAAM/grrr-rawr.webp"
                    : gifUrls.get(new Random().nextInt(gifUrls.size()));

            String response = """
                <html>
                    <head><title>GIF Machine</title></head>
                    <body style='background-color: black; text-align: center;'>
                        <h1 style='color: white; margin-top: 100px;'>GIF MACHINE</h1>
                        <div style='background-color: grey; width: 400px; margin: 0 auto; padding: 20px; border-radius: 15px;'>
                            <img src='%s' alt='Custom GIF' style='display: block; margin: 0 auto;'/>
                            <form action='/download' method='post'>
                                <input type='submit' value='Download GIF'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                            </form>
                            <form action='/generate' method='post'>
                                <input type='submit' value='Generate GIF'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                            </form>
                            <form action='/history' method='post'>
                                <input type='submit' value='GIF History'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                            </form>
                        </div>
                    </body>
                </html>
            """.formatted(ranGIF);
            sendResponse(exchange, response);
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = """
                <html>
                    <body style='background-color: black; text-align: center; color: white;'>
                        <h1>GIF History Page</h1>
                        <a href='/final_destination' style='color: yellow;'>Back</a>
                    </body>
                </html>
            """;
            sendResponse(exchange, response);
        }
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

