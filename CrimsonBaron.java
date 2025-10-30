import java.io.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.net.*;

public class CrimsonBaron {
    public static void main(String[] args) throws IOException {
        // Create a new HttpServer instance
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Create a new context to listen for request with the path /final_destination
        server.createContext("/final_destination", new MyHandler());

        // Start the server
        server.start();
        System.out.println("Can find the site @ http://localhost:8080/final_destination");
    }

    // Define the handler for incoming requests
    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response;

            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())){
                response = """
                    <html>
                        <head><title>GIF Machine</title></head>
                        <body style='background-color: black; text-align: center;'>
                            <h1 style='color: white; margin-top: 100px;'> GIF MACHINE </h1>
                            <img src='https://media.tenor.com/4EhUju6UJtEAAAAm/grrr-rawr.webp'
                                alt='Custom GIF'
                                style='display: block; margin: 0 auto;'/>
                            <form method='post'>
                                <input type='submit' value='Download GIF'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                                    <input type='submit' value='Generate GIF'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                                    <input type='submit' value='GIF History'
                                    style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>
                            </form>
                        </body>
                    </html>
                """;
            }

            // Check if the request method is POST
            else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                response = """
                    <html>
                        <body style='background-color: black; test-align: center;'>
                            <h1 style='color: white;'> New Screen! </h>
                        </body>
                    </html>
                """;
            }

            else{
                response = "unsupported";
            }

            // Set the response headers and send the response
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        private void sendPacket(String message) {
            try {
                DatagramSocket sender = new DatagramSocket();
                InetAddress address = InetAddress.getByName("localhost"); // Replace with the recipient's address
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 8080); // Replace with the recipient's port
                sender.send(packet);
                sender.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

