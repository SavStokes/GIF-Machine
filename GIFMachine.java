port java.io.*;
import com.sun.net.httpserver.*;
import java.net.*;
import java.util.*;

public class GIFMachine {

    // Creates a list of the gifs so that the download can read from it and download the gifs url
    private static final List<String> gifHistory = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/final_destination", new MainPageHandler());
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
                            <img src='%s' alt='Custom GIF' style='display: block; margin: 0 auto; max-width:100%%; height:auto;'/>
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

    static class GenerateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String gifs[] = {
                // Original direct links
                "https://media.tenor.com/m/NHS-2VCdPdMAAAAC/wolfy.gif",
                "https://media.tenor.com/AI_casj8FJwAAAA1/did-not-mean-it.webp",
                "https://media.tenor.com/pGGMMerDTxkAAAA1/laugh-funny.webp",
                "https://media.tenor.com/8WFMlcnmaisAAAA1/black-cat-crazy-cat.webp",
                "https://media.tenor.com/9h8C7tL-dy0AAAAC/dancing-sponge-bob.gif",
                "https://media.tenor.com/XKwD0kNrzQ4AAAAC/monkey-dance.gif",
                "https://media.tenor.com/hqS4hh0oZ5UAAAAC/drake-dance.gif",
                "https://media.tenor.com/4tW7PvDSbfsAAAAC/angry-pikachu.gif",
                "https://media.tenor.com/_G1iIh1kGmUAAAAC/cat-freak-out.gif",
                "https://media.tenor.com/wEPO2E3H0GkAAAAC/will-smith-dance.gif"
            };

            Random rand = new Random();
            int index = rand.nextInt(gifs.length);
            String ranGIF = gifs[index];

            // Add to history
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
                            <img src='%s' alt='Custom GIF' style='display: block; margin: 0 auto; max-width:100%%; height:auto;'/>
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

    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (gifHistory.isEmpty()) {
                String response = """
                    <html>
                        <body style='background-color:black;color:white;text-align:center;'>
                            <h1>No GIF generated yet.</h1>
                            <a href='/final_destination' style='color:yellow;'>Back</a>
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
                    sendResponse(exchange, "Failed to fetch GIF. HTTP " + status, 502);
                    return;
                }
                //Gets the URL name to read and chnges the name for downloading if
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
            //Error Handling if there is a problem downloading the GIF
            } catch (Exception e) {
                sendResponse(exchange, "Error downloading GIF: " + e.getMessage(), 500);
            }
        }
    }

    static class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder sb = new StringBuilder();
            sb.append("""
                <html>
                    <body style='background-color:black;color:white;text-align:center;'>
                        <h1>GIF History Page</h1>
            """);


            sendResponse(exchange, sb.toString());
        }
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, 200);
    }

    private static void sendResponse(HttpExchange exchange, String response, int status) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        byte[] bytes = response.getBytes("UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
