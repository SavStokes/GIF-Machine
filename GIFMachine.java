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
            <head>
                <meta charset="UTF-8">
                <title>GIF Machine</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        margin: 0;
                        padding: 0;
                        background-color: #1a1a1a;
                        color: #fff;
                        text-align: center;
                    }

                    .sidebar {
                        height: 100%%;
                        width: 0;
                        position: fixed;
                        top: 0;
                        left: 0;
                        background-color: #111;
                        overflow-x: hidden;
                        transition: 0.3s;
                        padding-top: 60px;
                        z-index: 1000;
                    }

                    .sidebar a {
                        padding: 10px 20px;
                        text-decoration: none;
                        font-size: 18px;
                        color: #ddd;
                        display: block;
                        transition: 0.2s;
                    }

                    .sidebar a:hover {
                        background-color: crimson;
                        color: white;
                    }

                    .open-btn {
                        font-size: 20px;
                        background-color: grey;
                        color: white;
                        border: none;
                        padding: 10px 20px;
                        cursor: pointer;
                        position: fixed;
                        top: 15px;
                        left: 15px;
                        border-radius: 6px;
                        z-index: 1100;
                    }

                    .open-btn:hover {
                        background-color: crimson;
                    }

                    .close-btn {
                        position: absolute;
                        top: 15px;
                        right: 20px;
                        font-size: 28px;
                        color: white;
                        cursor: pointer;
                    }

                    .content {
                        margin-top: 100px;
                    }

                    .gif-box {
                        background-color: grey;
                        width: 400px;
                        margin: 0 auto;
                        padding: 20px;
                        border-radius: 15px;
                    }

                    h1 { color: crimson; }
                </style>
            </head>

            <body>
                <!-- Sidebar structure -->
                <div id="mySidebar" class="sidebar">
                    <span class="close-btn" onclick="closeSidebar()">&times;</span>
                    <a href="/final_destination">GIF History is empty</a>
                </div>

                <!-- Button to toggle sidebar -->
                <button class="open-btn" onclick="openSidebar()">☰ GIF History</button>

                <!-- Main content -->
                <div class="content">
                    <h1>GIF MACHINE</h1>
                    <div class="gif-box">
                        <img src="%s" alt="Custom GIF" 
                             style="display: block; margin: 0 auto; max-width: 100%%; border-radius: 10px;"/>

                        <form action="/download" method="post">
                            <input type="submit" value="Download GIF"
                                   style="margin-top: 10px; width: 150px; height: 50px; font-size: 16px;"/>
                        </form>
                        <form action="/generate" method="post">
                            <input type="submit" value="Generate GIF"
                                   style="margin-top: 10px; width: 150px; height: 50px; font-size: 16px;"/>
                        </form>
                        <form action="/history" method="post">
                            <input type="submit" value="GIF History"
                                   style="margin-top: 10px; width: 150px; height: 50px; font-size: 16px;"/>
                        </form>
                    </div>
                </div>

                <script>
                    function openSidebar() {
                        document.getElementById("mySidebar").style.width = "250px";
                    }
                    function closeSidebar() {
                        document.getElementById("mySidebar").style.width = "0";
                    }
                </script>
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

    private static final Set<String> seenGifs = new HashSet<>();
    private static String nextPos = null;

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

        // Pick one random GIF not seen before
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

        // Track history, keep last 10
        gifHistory.add(ranGIF);
        if (gifHistory.size() > 10) {
            gifHistory.remove(0);
        }

        // ✅ Build sidebar dynamically
        StringBuilder sidebarList = new StringBuilder();
        if (gifHistory.isEmpty()) {
            sidebarList.append("<p style='color:gray; text-align:center;'>No recent GIFs</p>");
        } else {
            // Most recent first
            for (int i = gifHistory.size() - 1; i >= 0; i--) {
                String gif = gifHistory.get(i);
                sidebarList.append(String.format(
                    "<a href='/view?gif=%s'>GIF %d</a>", gif, gifHistory.size() - i
                ));
            }
        }

        // ✅ Build full page response
        String response = """
<html>
<head>
    <meta charset="UTF-8">
    <title>GIF Machine</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #1a1a1a;
            color: #fff;
            text-align: center;
        }

        .sidebar {
            height: 100%%;
            width: 0;
            position: fixed;
            top: 0;
            left: 0;
            background-color: #111;
            overflow-x: hidden;
            transition: 0.3s;
            padding-top: 60px;
            z-index: 1000;
        }

        .sidebar a {
            padding: 10px 20px;
            text-decoration: none;
            font-size: 18px;
            color: #ddd;
            display: block;
            transition: 0.2s;
        }

        .sidebar a:hover {
            background-color: crimson;
            color: white;
        }

        .open-btn {
            font-size: 20px;
            background-color: grey;
            color: white;
            border: none;
            padding: 10px 20px;
            cursor: pointer;
            position: fixed;
            top: 15px;
            left: 15px;
            border-radius: 6px;
            z-index: 1100;
        }

        .open-btn:hover {
            background-color: crimson;
        }

        .close-btn {
            position: absolute;
            top: 15px;
            right: 20px;
            font-size: 28px;
            color: white;
            cursor: pointer;
        }

        .gif-box {
            background-color: grey;
            width: 400px;
            margin: 120px auto;
            padding: 20px;
            border-radius: 15px;
        }

        h1 { color: crimson; }
    </style>
</head>

<body>
    <!-- Sidebar -->
    <div id="mySidebar" class="sidebar">
        <span class="close-btn" onclick="closeSidebar()">&times;</span>
        <h3 style="color:white; text-align:center;">Recent GIFs</h3>
        %s
    </div>

    <!-- Sidebar toggle button -->
    <button class="open-btn" onclick="openSidebar()">☰ GIF History</button>

    <!-- Main content -->
    <h1>GIF MACHINE</h1>
    <div class="gif-box">
        <img src="%s" alt="Custom GIF"
             style="display: block; margin: 0 auto; max-width: 100%%; max-height: 300px;
                    border-radius: 10px; object-fit: contain;"/>

        <form action="/download" method="post">
            <input type="submit" value="Download GIF"
                   style="margin-top: 10px; width: 150px; height: 50px; font-size: 16px;"/>
        </form>
        <form action="/generate" method="post">
            <input type="submit" value="Generate GIF"
                   style="margin-top: 10px; width: 150px; height: 50px; font-size: 16px;"/>
        </form>
    </div>

    <script>
        function openSidebar() {
            document.getElementById("mySidebar").style.width = "250px";
        }
        function closeSidebar() {
            document.getElementById("mySidebar").style.width = "0";
        }
    </script>
</body>
</html>
""".formatted(sidebarList.toString(), ranGIF);

        sendResponse(exchange, response);
    }
}

    static class ViewHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String gifUrl = null;

        if (query != null && query.startsWith("gif=")) {
            gifUrl = URLDecoder.decode(query.substring(4), "UTF-8");
        }

        if (gifUrl == null || gifUrl.isEmpty()) {
            gifUrl = "https://media.tenor.com/4EhUju6UJtEAAAAM/grrr-rawr.webp";
        }

        // rebuild sidebar
        StringBuilder sidebarList = new StringBuilder();
        if (gifHistory.isEmpty()) {
            sidebarList.append("<p style='color:gray; text-align:center;'>No recent GIFs</p>");
        } else {
            for (int i = gifHistory.size() - 1; i >= 0; i--) {
                String gif = gifHistory.get(i);
                sidebarList.append(String.format(
                    "<a href='/view?gif=%s'>GIF %d</a>", URLEncoder.encode(gif, "UTF-8"), gifHistory.size() - i
                ));
            }
        }

        // reuse the same HTML structure
        String response = String.format("""
<html>
<head>
    <meta charset="UTF-8">
    <title>GIF Machine</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #1a1a1a; color: #fff; text-align: center; }
        .sidebar { height: 100%%; width: 0; position: fixed; top: 0; left: 0; background-color: #111;
                   overflow-x: hidden; transition: 0.3s; padding-top: 60px; z-index: 1000; }
        .sidebar a { padding: 10px 20px; text-decoration: none; font-size: 18px; color: #ddd; display: block; transition: 0.2s; }
        .sidebar a:hover { background-color: crimson; color: white; }
        .open-btn { font-size: 20px; background-color: grey; color: white; border: none;
                    padding: 10px 20px; cursor: pointer; position: fixed; top: 15px; left: 15px; border-radius: 6px; z-index: 1100; }
        .open-btn:hover { background-color: crimson; }
        .close-btn { position: absolute; top: 15px; right: 20px; font-size: 28px; color: white; cursor: pointer; }
        .gif-box { background-color: grey; width: 400px; margin: 120px auto; padding: 20px; border-radius: 15px; }
        h1 { color: crimson; }
    </style>
</head>
<body>
    <div id="mySidebar" class="sidebar">
        <span class="close-btn" onclick="closeSidebar()">&times;</span>
        <h3 style="color:white; text-align:center;">Recent GIFs</h3>
        %s
    </div>
    <button class="open-btn" onclick="openSidebar()">☰ GIF History</button>

    <h1>GIF MACHINE</h1>
    <div class="gif-box">
        <img src="%s" alt="GIF" style="max-width: 100%%; max-height: 300px; border-radius: 10px;">
        <form action="/generate" method="post">
            <input type="submit" value="Generate GIF" style="margin-top: 10px; width: 150px; height: 50px; font-size: 16px;">
        </form>
    </div>

    <script>
        function openSidebar() { document.getElementById("mySidebar").style.width = "250px"; }
        function closeSidebar() { document.getElementById("mySidebar").style.width = "0"; }
    </script>
</body>
</html>
""", sidebarList.toString(), gifUrl);

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
