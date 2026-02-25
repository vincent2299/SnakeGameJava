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









}