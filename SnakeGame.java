import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {

    private class Tile {
        int x, y;
        Tile(int x, int y) { this.x = x; this.y = y; }
    }

    private final int TILE_SIZE = 25;
    private final int WIDTH = 600, HEIGHT = 400;
    private ArrayList<Tile> snakeBody;
    private Tile snakeHead;
    private Tile food;
    private int velocityX, velocityY;
    private Timer gameLoop;
    private boolean gameOver;
    private boolean isPaused;
    private Random random;
    private int score;
    private int gameSpeed;
    private ArrayList<Tile> obstacles;
    private int currentLevel;
    private final int MAX_LEVEL = 5;  // Maximum Level
    private String[] levelLayouts;
    private Clip backgroundMusic;
    private File[] musicFiles;
    private int currentMusicFileIndex;
    private Image backgroundImage;  // Variable für das Hintergrundbild

    public SnakeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addKeyListener(this);
        setFocusable(true);

        random = new Random();
        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();
        food = new Tile(10, 10);
        velocityX = 1;
        velocityY = 0;
        gameOver = false;
        isPaused = false;
        score = 0;
        currentLevel = 1;

        levelLayouts = new String[MAX_LEVEL];
        generateLevelLayouts();
        loadLevel(currentLevel);

        gameSpeed = 100;
        gameLoop = new Timer(gameSpeed, this);

        // Hintergrundbild laden
        try {
            backgroundImage = ImageIO.read(new File("C:\\Users\\Student\\OneDrive - GFN GmbH (EDU)\\Dokumente\\IntelliJ\\Snake 2\\src\\background.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Initialize music files
        musicFiles = new File[] {
                new File("C:\\Users\\Student\\OneDrive - GFN GmbH (EDU)\\Dokumente\\IntelliJ\\Snake 2\\src\\background_music1.wav"),
                new File("C:\\Users\\Student\\OneDrive - GFN GmbH (EDU)\\Dokumente\\IntelliJ\\Snake 2\\src\\background_music2.wav")
        };
        currentMusicFileIndex = 0;

        // Start playing the background music
        playBackgroundMusic();
    }

    private void generateLevelLayouts() {
        // Erstelle verschiedene Layouts mit Hindernissen
        levelLayouts[0] = "....#..............#..............";
        levelLayouts[1] = ".............#....#............";
        levelLayouts[2] = "...........###............###....";
        levelLayouts[3] = "###..........###........##......";
        levelLayouts[4] = "#...#..#..............#........";
    }

    private void loadLevel(int level) {
        obstacles = new ArrayList<>();
        String layout = levelLayouts[level - 1];

        // Hindernisse aus dem Layout generieren
        for (int i = 0; i < layout.length(); i++) {
            if (layout.charAt(i) == '#') {
                int x = i % (WIDTH / TILE_SIZE);
                int y = i / (WIDTH / TILE_SIZE);
                obstacles.add(new Tile(x, y));
            }
        }
    }

    private void playBackgroundMusic() {
        try {
            // Load and play the first music file
            File musicFile = musicFiles[currentMusicFileIndex];
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);  // Loop the music

            // Add a listener to play the next file when the current one ends
            backgroundMusic.addLineListener(new LineListener() {
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        switchToNextMusicFile();
                    }
                }
            });
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void switchToNextMusicFile() {
        // Switch to the next music file and play it
        currentMusicFileIndex = (currentMusicFileIndex + 1) % musicFiles.length;
        playBackgroundMusic(); // Restart music with new file
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);  // Methode zum Zeichnen des Hintergrunds
        drawGame(g);
    }

    private void drawBackground(Graphics g) {
        if (backgroundImage != null) {
            // Hintergrundbild skalieren, um die ganze Fläche zu füllen
            g.drawImage(backgroundImage, 0, 0, WIDTH, HEIGHT, this);
        } else {
            // Fallback zu Schwarz, falls das Bild nicht geladen werden konnte
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    private void drawGame(Graphics g) {
        drawGrid(g);
        drawSnake(g);
        drawFood(g);
        drawObstacles(g);
        drawScore(g);

        if (gameOver) {
            drawGameOver(g);
        } else if (isPaused) {
            drawPause(g);
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.GRAY);
        for (int i = 0; i < WIDTH / TILE_SIZE; i++) {
            g.drawLine(i * TILE_SIZE, 0, i * TILE_SIZE, HEIGHT);
            g.drawLine(0, i * TILE_SIZE, WIDTH, i * TILE_SIZE);
        }
    }

    private void drawSnake(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(snakeHead.x * TILE_SIZE, snakeHead.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

        for (Tile bodyPart : snakeBody) {
            g.setColor(new Color(0, 128, 0));
            g.fillRect(bodyPart.x * TILE_SIZE, bodyPart.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawFood(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(food.x * TILE_SIZE, food.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    }

    private void drawObstacles(Graphics g) {
        g.setColor(Color.GRAY);
        for (Tile obstacle : obstacles) {
            g.fillRect(obstacle.x * TILE_SIZE, obstacle.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawScore(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Score: " + snakeBody.size(), 10, 20);
        g.drawString("Level: " + currentLevel, 10, 40);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Game Over", WIDTH / 2 - 100, HEIGHT / 2);
    }

    private void drawPause(Graphics g) {
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Paused", WIDTH / 2 - 80, HEIGHT / 2);
    }

    private void move() {
        if (gameOver || isPaused) return;

        // Save the current position of the head
        Tile newHead = new Tile(snakeHead.x + velocityX, snakeHead.y + velocityY);

        // Check if the snake goes out of bounds and wrap it around to the other side
        if (newHead.x < 0) newHead.x = WIDTH / TILE_SIZE - 1;
        if (newHead.x >= WIDTH / TILE_SIZE) newHead.x = 0;
        if (newHead.y < 0) newHead.y = HEIGHT / TILE_SIZE - 1;
        if (newHead.y >= HEIGHT / TILE_SIZE) newHead.y = 0;

        // Check if the new head collides with the snake's body or an obstacle
        for (Tile bodyPart : snakeBody) {
            if (newHead.x == bodyPart.x && newHead.y == bodyPart.y) {
                gameOver = true;
                gameLoop.stop();
                return;
            }
        }

        for (Tile obstacle : obstacles) {
            if (newHead.x == obstacle.x && newHead.y == obstacle.y) {
                gameOver = true;
                gameLoop.stop();
                return;
            }
        }

        // Check if the snake eats the food
        boolean ateFood = newHead.x == food.x && newHead.y == food.y;
        if (ateFood) {
            score++;
            snakeBody.add(0, snakeHead); // Add the head to the body
            food = new Tile(random.nextInt(WIDTH / TILE_SIZE), random.nextInt(HEIGHT / TILE_SIZE));
            if (score % 5 == 0) {
                currentLevel++;
                if (currentLevel > MAX_LEVEL) {
                    currentLevel = MAX_LEVEL;
                }
                loadLevel(currentLevel);
            }
        }

        // Update the snake's body
        snakeBody.add(0, snakeHead);
        if (!ateFood) {
            snakeBody.remove(snakeBody.size() - 1);
        }

        snakeHead = newHead;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if (key == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if (key == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if (key == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        } else if (key == KeyEvent.VK_P) {
            togglePause();
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            gameLoop.stop();
        } else {
            gameLoop.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Snake Game");
        SnakeGame gamePanel = new SnakeGame();
        frame.add(gamePanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        gamePanel.gameLoop.start();
    }
}













