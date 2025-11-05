import java.io.*;
import com.sun.net.httpserver.*;
import java.net.*;
import java.util.Random;


public class GIFMachine {
    public static void main(String[] args) throws IOException {

        String gifs[] = {
            "tenor.com/view/feeling-good-gif-XXX", 
            "tenor.com/view/i-m-in-a-glass-case-of-emotion-gif-YYY",
            "tenor.com/view/crying-cute-cat-gif-ZZZ",
            "tenor.com/view/dancing-penguin-feeling-good-gif-AAA",
            "tenor.com/view/hug-bear-sending-you-hugs-gif-BBB",
            "tenor.com/view/how-are-you-feeling-gif-CCC",
            "tenor.com/view/i-just-have-a-lot-of-feelings-gif-DDD", 
            "tenor.com/view/roller-coaster-of-emotions-gif-EEE",
            "tenor.com/view/get-well-soon-teddy-bear-gif-FFF",
            "tenor.com/view/feels-good-man-cat-gif-GGG"};

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
            
            String gifs[] = {
            "https://media1.tenor.com/m/NHS-2VCdPdMAAAAC/wolfy.gif", 
            "https://media.tenor.com/AI_casj8FJwAAAA1/did-not-mean-it.webp",
            "https://media.tenor.com/pGGMMerDTxkAAAA1/laugh-funny.webp",
            "https://media.tenor.com/8WFMlcnmaisAAAA1/black-cat-crazy-cat.webp",
            "https://tenor.com/view/hug-bear-sending-you-hugs-gif-BBB",
            "https://tenor.com/view/how-are-you-feeling-gif-CCC",
            "https://tenor.com/view/i-just-have-a-lot-of-feelings-gif-DDD", 
            "https://tenor.com/view/roller-coaster-of-emotions-gif-EEE",
            "https://tenor.com/view/get-well-soon-teddy-bear-gif-FFF",
            "https://tenor.com/view/feels-good-man-cat-gif-GGG"};

            Random rand = new Random();

            int index = rand.nextInt(gifs.length);
            String ranGIF = gifs[index];

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

