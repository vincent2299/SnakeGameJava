package backend;

import backend.GameServer.StartHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
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
        server.createContext("/api/start", new StartHandler(engine));

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
            json.append("\"gameOver\":").append(engine.isgameOver).append(",");
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

    // reads arrow keys sent from react
    static class InputHandler implements HttpHandler {

        // reference to the game engine to update the direction based on client input
        private GameEngine engine;

        public InputHandler(GameEngine engine) { 
            this.engine = engine; 
        }

        public void handle(HttpExchange exchange) throws IOException {
            // handle CORS preflight
            if (handleCors(exchange)) return;

            // read the new direction from the request body and update the game engine
            String newDir = new String(exchange.getRequestBody().readAllBytes());
            engine.updateDirection(newDir);
            
            // send a simple response back to the client
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        }
    }

    // starts a new game by resetting the game engine state
    static class StartHandler implements HttpHandler {
        private GameEngine engine;

        public StartHandler(GameEngine engine) {
            this.engine = engine;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (handleCors(exchange)) return; // handle CORS preflight

            engine.resetGame();
            
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().close();
        }
    }
}










