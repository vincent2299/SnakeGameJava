package backend;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;

public class GameServer {

    public static void main(String[] args) throws IOException {
        // intialize the Game Engine and start it in a separate thread
        GameEngine engine = new GameEngine();
        new Thread(engine).start();

        // create HTTP server on port 8000
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // map urls to http handlers, passing the engine to them
        server.createContext("/api/state", new StateHandler(engine));
        server.createContext("/api/input", new InputHandler(engine));
        server.createContext("/api/start". new StartHandler(engine));

        server.setExecutor(null); // use default executor
        server.start();
        System.out.println("Game server started on port 8080");

    }

    // cors utility to allow reacto to access the api
    public static boolean handleCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1); // No content for preflight
            return true; // indicate that the request was handled
        }
        return false; // indicate that the request should be processed normally
    }

    // end points

    // converts the game engine state to json and sends it back to the client
    static class StateHandler implements HttpHandler {
        private GameEngine engine;

        public StateHandler(GameEngine engine) {
            this.engine = engine;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return; // handle CORS preflight

            // convert the game state to json
            StringBuilder json = new StringBuilder("{");
            json.append("\"score\":").append(engine.score).append(",");
            json.append("\"gameOver\":").append(engine.isGameOver).append(",");
            json.append("\"food\":{\"x\":").append(engine.food.x).append(",\"y\":").append(engine.food.y).append("},");
            
            json.append("\"snake\":[");

            // convert the snake body points to json array
            for (int i = 0; i < engine.snake.size(); i++) {
                Point p = engine.snake.get(i);
                json.append("{\"x\":").append(engine.snake.get(i).x).append(",\"y\":").append(engine.snake.get(i).y).append("}");
                if (i < engine.snake.size() - 1) json.append(",");
            }
            json.append("]}");
            
            // send the json response
            byte[] response = json.toString().getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(response); }
        }
    }
}










}