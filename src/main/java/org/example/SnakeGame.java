package org.example;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.image.Image;

public class SnakeGame extends Application {
    private Image grassTexture;
    private Image appleImage;
    private static final double APPLE_SCALE_FACTOR = 2.5;
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int TILE_SIZE = 20;
    private static final int ROWS = HEIGHT / TILE_SIZE;
    private static final int COLUMNS = WIDTH / TILE_SIZE;

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private enum GameState {
        RUNNING, GAME_OVER
    }

    private List<int[]> snake = new ArrayList<>();
    private int[] food = new int[2];
    private Direction direction = Direction.RIGHT;
    private GameState gameState = GameState.GAME_OVER;
    private int score = 0;

    private Timeline timeline;
    private GraphicsContext gc;

    @Override
    public void start(Stage primaryStage) {
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // For grass texture
        try {
            grassTexture = new Image(getClass().getResourceAsStream("/grass_texture.png"));
        } catch (NullPointerException e) {
            System.err.println("Failed to load grass texture! Check the file path.");
            e.printStackTrace();
            // Fallback to a solid color if the texture fails to load
            gc.setFill(Color.GREEN);
        }

        // For apple image
        try {
            appleImage = new Image(getClass().getResourceAsStream("/apple.png"));
        } catch (NullPointerException e) {
            System.err.println("Failed to load apple image! Check the file path.");
            e.printStackTrace();
        }

        // Handle key presses
        scene.setOnKeyPressed(e -> {
            if (gameState == GameState.GAME_OVER) {
                if (e.getCode() == KeyCode.R) {
                    resetGame(); // Restart the game
                } else if (e.getCode() == KeyCode.Q) {
                    System.exit(0); // Quit the game
                }
            } else if (gameState == GameState.RUNNING) {
                switch (e.getCode()) {
                    case UP:
                        if (direction != Direction.DOWN) direction = Direction.UP;
                        break;
                    case DOWN:
                        if (direction != Direction.UP) direction = Direction.DOWN;
                        break;
                    case LEFT:
                        if (direction != Direction.RIGHT) direction = Direction.LEFT;
                        break;
                    case RIGHT:
                        if (direction != Direction.LEFT) direction = Direction.RIGHT;
                        break;
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.setTitle("Snake Game");
        primaryStage.show();

        resetGame();
    }

    private void startGame() {
        gameState = GameState.RUNNING;
        snake.clear();
        snake.add(new int[]{COLUMNS / 2, ROWS / 2});
        spawnFood();
        score = 0;

        timeline = new Timeline(new KeyFrame(Duration.millis(70), e -> update()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void update() {
        if (gameState != GameState.RUNNING) return;

        move();
        checkCollisions();
        draw();
    }

    private void move() {
        int[] head = snake.get(0);
        int newX = head[0], newY = head[1];

        switch (direction) {
            case UP:
                newY--;
                break;
            case DOWN:
                newY++;
                break;
            case LEFT:
                newX--;
                break;
            case RIGHT:
                newX++;
                break;
        }

        snake.add(0, new int[]{newX, newY});
        if (newX == food[0] && newY == food[1]) {
            score += 10;
            spawnFood();
        } else {
            snake.remove(snake.size() - 1);
        }
    }

    private void checkCollisions() {
        int[] head = snake.get(0);

        // Check wall collisions
        if (head[0] < 0 || head[0] >= COLUMNS || head[1] < 0 || head[1] >= ROWS) {
            gameState = GameState.GAME_OVER;
        }

        // Check self-collisions
        for (int i = 1; i < snake.size(); i++) {
            if (head[0] == snake.get(i)[0] && head[1] == snake.get(i)[1]) {
                gameState = GameState.GAME_OVER;
                break;
            }
        }

        if (gameState == GameState.GAME_OVER) {
            timeline.stop();
        }
    }

    private void spawnFood() {
        Random rand = new Random();
        food[0] = rand.nextInt(COLUMNS);
        food[1] = rand.nextInt(ROWS);
    }

    private void drawSnakeHead(double x, double y, Direction direction) {
        double centerX = x + TILE_SIZE / 2; // Center of the tile
        double centerY = y + TILE_SIZE / 2;

        // Define the points of the triangle (pointing upwards by default)
        double[] xPoints = {x + TILE_SIZE / 2, x, x + TILE_SIZE};
        double[] yPoints = {y, y + TILE_SIZE, y + TILE_SIZE};

        // Rotate the points based on the direction
        double angle = 0; // Default rotation (facing upwards)
        switch (direction) {
            case UP:
                angle = 0; // No rotation (default)
                break;
            case DOWN:
                angle = 180; // Rotate 180 degrees
                break;
            case LEFT:
                angle = -90; // Rotate 90 degrees counterclockwise
                break;
            case RIGHT:
                angle = 90; // Rotate 90 degrees clockwise
                break;
        }

        // Rotate the points around the center of the tile
        for (int i = 0; i < xPoints.length; i++) {
            double dx = xPoints[i] - centerX;
            double dy = yPoints[i] - centerY;

            double rotatedX = centerX + dx * Math.cos(Math.toRadians(angle)) - dy * Math.sin(Math.toRadians(angle));
            double rotatedY = centerY + dx * Math.sin(Math.toRadians(angle)) + dy * Math.cos(Math.toRadians(angle));

            xPoints[i] = rotatedX;
            yPoints[i] = rotatedY;
        }

        // Draw the triangle
        gc.setFill(Color.BLACK); // Snake head color
        gc.fillPolygon(xPoints, yPoints, 3);
    }

    private void draw() {
        // Draw tiled grass background
        if (grassTexture != null) {
            gc.drawImage(grassTexture, 0, 0, WIDTH, HEIGHT);
        } else {
            // Fallback to black background if texture fails to load
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, WIDTH, HEIGHT);
        }

        // Draw Food (Apple)
        int foodX = food[0] * TILE_SIZE;
        int foodY = food[1] * TILE_SIZE;

        if (appleImage != null) {
            // Calculate the scaled size of the apple
            double scaledWidth = TILE_SIZE * APPLE_SCALE_FACTOR;
            double scaledHeight = TILE_SIZE * APPLE_SCALE_FACTOR;

            // Calculate the offset to center the apple within the tile
            double offsetX = (TILE_SIZE - scaledWidth) / 2;
            double offsetY = (TILE_SIZE - scaledHeight) / 2;

            // Draw the apple image, scaled and centered
            gc.drawImage(appleImage, foodX + offsetX, foodY + offsetY, scaledWidth, scaledHeight);
        } else {
            // Fallback to drawing a red circle if the apple image fails to load
            gc.setFill(Color.DARKRED);
            gc.fillOval(foodX, foodY, TILE_SIZE, TILE_SIZE);
            gc.setFill(Color.RED);
            gc.fillOval(foodX + 3, foodY + 3, TILE_SIZE - 6, TILE_SIZE - 6);
            gc.setFill(Color.WHITE);
            gc.fillOval(foodX + 8, foodY + 8, 5, 5);
        }

        // Draw Snake
        for (int i = 0; i < snake.size(); i++) {
            int[] part = snake.get(i);
            double opacity = 1.0 - (i * 0.05); // Gradual fading effect

            if (i == 0) {
                // Draw the snake head
                drawSnakeHead(part[0] * TILE_SIZE, part[1] * TILE_SIZE, direction);
            } else {
                // Draw the snake body
                Color snakeColor = Color.rgb(0, 255, 0, opacity);
                gc.setFill(snakeColor);
                gc.fillRoundRect(part[0] * TILE_SIZE, part[1] * TILE_SIZE, TILE_SIZE, TILE_SIZE, 10, 10);
            }
        }

        // Draw Score with effects
        gc.setFill(Color.rgb(0, 0, 0, 0.5)); // Semi-transparent black background
        gc.fillRect(10, 10, 150, 40); // Rectangle behind the score

        gc.setFill(Color.BLACK); // Shadow color
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 30)); // Bold and larger font
        gc.fillText("Score: " + score, 17, 42); // Shadow offset

        gc.setFill(Color.YELLOW); // Main text color
        gc.fillText("Score: " + score, 15, 40); // Main text
        // Game Over Screen
        if (gameState == GameState.GAME_OVER) {
            gc.setFill(Color.rgb(0, 0, 0, 0.7)); // Semi-transparent overlay
            gc.fillRect(0, 0, WIDTH, HEIGHT);

            // Game Over Text with Shadow
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 50));
            gc.fillText("Game Over!", WIDTH / 2 - 142, HEIGHT / 2 - 42); // Shadow
            gc.setFill(Color.ORANGERED);
            gc.fillText("Game Over!", WIDTH / 2 - 140, HEIGHT / 2 - 41);

            // Restart and Quit Instructions
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 30));
            gc.fillText("Press 'R' to Restart", WIDTH / 2 - 120, HEIGHT / 2 + 20);
            gc.fillText("Press 'Q' to Quit", WIDTH / 2 - 100, HEIGHT / 2 + 60);
        }

        // Draw Border
        gc.setStroke(Color.FIREBRICK);
        gc.setLineWidth(8);
        gc.strokeRect(0, 0, WIDTH, HEIGHT);
    }

    private void resetGame() {
        startGame();
    }

    public static void main(String[] args) {
        launch(args);
    }
}