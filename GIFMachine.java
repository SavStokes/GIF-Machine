import java.io.*;
import com.sun.net.httpserver.*;
import java.net.*;
import com.google.gson.*;
import java.util.*;


public class GIFMachine {
    private static final List<String> gifHistory = new ArrayList<>();
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
            String lastGIF = gifHistory.isEmpty() ? "https://media.tenor.com/4EhUju6UJtEAAAAm/grrr-rawr.webp" : gifHistory.get(gifHistory.size() - 1);
            String response = """
                <html>
                    <head><title>GIF Machine</title></head>
                    <body style='background-color: black; text-align: center;'>
                        <h1 style='color: white; margin-top: 100px;'>GIF MACHINE</h1>
                        <div style='background-color: grey; width: 400px; margin: 0 auto; padding: 20px; border-radius: 15px;'>
                            <img src='https://media.tenor.com/bA5Z9nSS5LsAAAAM/gif-machine.gif'
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
            """.formatted(lastGIF);
            sendResponse(exchange, response);
        }
    }

    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (gifHistory.isEmpty()) {
            String response = """
                <html>
                    <body style='background-color: black; text-align: center; color: white;'>
                        <h1>Download Page</h1>
                        <a href='/final_destination' style='color: yellow;'>Back</a>
                    </body>
                </html>
            """;
            sendResponse(exchange, response);
            return;
        }
        String lastGIF = gifHistory.get(gifHistory.size() - 1);
            try {
                URL url = new URL(lastGIF);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(20000);

                int status = conn.getResponseCode();
                if (status != 200) {
                    //sendResponse(exchange, "Failed to fetch GIF. HTTP " + status, 502);
                    return;
                }

                String path = url.getPath();
                String filename = path.substring(path.lastIndexOf('/') + 1);
                if (!filename.contains(".")) filename = "download.gif";

                exchange.getResponseHeaders().set("Content-Type", conn.getContentType());
                exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");

                try (InputStream in = conn.getInputStream(); OutputStream out = exchange.getResponseBody()) {
                    exchange.sendResponseHeaders(200, 0);
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                }

                conn.disconnect();
            } catch (Exception e) {
                //sendResponse(exchange, "Error downloading GIF: " + e.getMessage(), 500);
            }
    }
}

    static class GenerateHandler implements HttpHandler {

        // Keep track of seen GIF URLs (avoid repeats)
        private static final Set<String> seenGifs = new HashSet<>();
    // Store Tenor pagination token for continuing deeper searches
        private static String nextPos = null;
    // Topics to randomly choose from
        private static final String[] TOPICS = {
        "funny", "cats", "dogs", "memes", "reactions", "dance", "celebration",
        "fail", "happy", "sad", "excited", "sports", "gaming", "cute", "anime"
        };

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            
            String API_KEY = "AIzaSyCd-hEmDQxPiVHfxHiMOz4JR4tSDFHbH0A";
            String topic = TOPICS[new Random().nextInt(TOPICS.length)];

            String apiURL = String.format(
            "https://tenor.googleapis.com/v2/search?q=%s&key=%s&limit=20&contentfilter=medium%s",
            topic,
            API_KEY,
            (nextPos != null ? "&pos=" + nextPos : "")
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
                    String url = mediaFormats.getAsJsonObject("gif").get("url").getAsString();
                    gifUrls.add(url);
                }
            }

            // Pick one random GIF that hasn't been shown yet
        String ranGIF = null;
        int attempts = 0;
        while (attempts < 10 && (ranGIF == null || seenGifs.contains(ranGIF))) {
            ranGIF = gifUrls.get(new Random().nextInt(gifUrls.size()));
            attempts++;
        }
        if (ranGIF == null) {
            ranGIF = "https://media.tenor.com/4EhUju6UJtEAAAAM/grrr-rawr.webp";
        } else {
            seenGifs.add(ranGIF);
        }

         gifHistory.add(ranGIF);
            if (gifHistory.size() > 9) {
                gifHistory.remove(0);
            }

            String response = """
                <html>
                    <head><title>GIF Machine</title></head>
                    <body style='background-color: black; text-align: center;'>
                        <h1 style='color: white; margin-top: 100px;'>GIF MACHINE</h1>
                        <div style='background-color: grey; width: 400px; margin: 0 auto; padding: 20px; border-radius: 15px;'>
                            <img src='%s' alt='Custom GIF' style='display: block; margin: 0 auto; max-width: 100%%; max-height: 300px; border-radius: 10px; object-fit: contain;'/>
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

