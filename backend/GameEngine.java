package backend;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameEngine implements Runnable {

    // decalre fields for snake game
    static final int GRID_SIZE = 20, MAX = 400;

    // use volatile to ensure visibility of changes across threads
    static volatile boolean isgameOver = true;
    static volatile int score = 0;
    static volatile String direction = "RIGHT";
    static volatile Point food = new Point(0, 0);

    // use CopyOnWriteArrayList to avoid concurrent modification exception when multiple threads access the snake list
    static CopyOnWriteArrayList<Point> snake = new CopyOnWriteArrayList<>();

    public void resetGame() {
        snake.clear();
        snake.add(new Point(200, 200)); // initialize snake with one segment at (200, 200)
        direction = "RIGHT";
        score = 0;
        isgameOver = false;
        spawnFood();
    }

    // handle keyboard inputs
    public void updateDirection(String newDir) {
        if (newDir.equals("UP") && !direction.equals("DOWN")) direction = "UP";
        if (newDir.equals("DOWN") && !direction.equals("UP")) direction = "DOWN";
        if (newDir.equals("LEFT") && !direction.equals("RIGHT")) direction = "LEFT";
        if (newDir.equals("RIGHT") && !direction.equals("LEFT")) direction = "RIGHT";
    }

    // spawn food at random location (within the grid and not on the snake)
    private void spawnFood() {
        Random rand = new Random();
        Point newFood;
        boolean onSnake;
        do {
            onSnake = false;
            newFood = new Point(rand.nextInt(MAX / GRID_SIZE) * GRID_SIZE, rand.nextInt(MAX / GRID_SIZE) * GRID_SIZE);
            for (Point segment : snake) {
                if (segment.equals(newFood)) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake);
        food = newFood;
    }
    
    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(100); // game loop runs every 100ms
                if (isgameOver || snake.isEmpty()) continue; // skip if game is over or snake is not initialized

                // calculate new head position based on current direction
                Point head = snake.get(0);
                int newX = head.x, newY = head.y;
                switch (direction) {
                    case "UP": newY -= GRID_SIZE; break;
                    case "DOWN": newY += GRID_SIZE; break;
                    case "LEFT": newX -= GRID_SIZE; break;
                    case "RIGHT": newX += GRID_SIZE; break;
                }

                // check for wall collision
                if (newX < 0 || newX >= MAX || newY < 0 || newY >= MAX) {
                    isgameOver = true;
                    continue;
                }

                // check for self collision
                boolean selfCollision = false;
                for (Point segment : snake) {
                    if (segment.x == newX && segment.y == newY) {
                        selfCollision = true;
                        break;
                    }
                }
                if (selfCollision) {
                    isgameOver = true;
                    continue;
                }
                
                // move snake by adding new head and removing tail
                snake.add(0, new Point(newX, newY));

                // check for food collision
                if (newX == food.x && newY == food.y) {
                    score += 10; // increase score
                    spawnFood(); // spawn new food
                } else {
                    snake.remove(snake.size() - 1); // remove tail if no food eaten
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
