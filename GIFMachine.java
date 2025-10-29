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
            String response = "<html>" +
                    //website title
                    "<head><title> Gateway into my computer</title></head>" +
                    "<body style='background-color: black;'>" +
                    //First words listed on site/ call tag
                    "<body>" +
                    "<div style='text-align: center;'>" +
                    // "<h1 style='color: white; style='margin-top: 10px;'>What brings you here? </h1>" +
                    "<img src='https://media.tenor.com/RtepJNKCLQcAAAAM/game-skull.gif' alt='Custom GIF' style='display: block; margin: 0 auto;'/>" +
                    //making the input promt
                    "<form method='post'>" +
                    "<label for='name' style='color: white; style='margin-top: 1000px;'>Say my name: <input type='text' name='name'style='display: block; margin: 0 auto;'/>" +
                    //reading and taking the input prompt in
                    "<input type='submit' value='Submit' block; style='margin-top: 10px; width: 150px; height: 50px; font-size: 16px;'/>" +
                    "</form>" +
                    "</body>" +
                    "</html>";

            // Check if the request method is POST
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                // Parse the request body to get the input value
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "utf-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder buf = new StringBuilder();
                int b;
                while ((b = br.read()) != -1) {
                    buf.append((char) b);
                }
                br.close();
                isr.close();
                String Body = buf.toString();
                String[] parts = Body.split("=");
                String name;
                if(parts.length > 1){
                    name = parts[1];
                }
                else{
                    name = "";
                }
                // Filler input with the name password
                if ("DarthTyrannus2002".equals(name)) {
                    response = "<html><body><h1>CHECK FOR PACKETS FROM 8080</h1></body></html>";
                    //receivePacket(59037);
                    sendPacket("I was Betrayed");
                } 

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

