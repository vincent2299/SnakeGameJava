package backend;


// imports from the Java standard library
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameServer {


    // decalre fields for snake game
    static final int GRID_SIZE = 20, MAX = 400;
    static volatile boolean isgameOver = true;
    static volatile int score = 0;
    static volatile String direction = "RIGHT";
    static volatile Point food = new Point(0, 0);
    // use CopyOnWriteArrayList to avoid concurrent modification exception when multiple threads access the snake list
    static CopyOnWriteArrayList<Point> snake = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        // create background engine
        new Thread(GameServer::runGameLoop).start();

        // create HTTP server at local port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/state", new StateHandler());
        server.createContext("/api/input", new inputHandler());
        server.createContext("/api/start", new StartHandler());

        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server started on port 8080");

    }

    
}
