import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class SnakeGame extends JFrame implements KeyListener {

    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 20;
    private static final char EMPTY = '.';
    private static final char SNAKE = 'S';
    private static final char FOOD = 'F';

    private static final int[] UP = {-1, 0};
    private static final int[] DOWN = {1, 0};
    private static final int[] LEFT = {0, -1};
    private static final int[] RIGHT = {0, 1};

    
    private char[][] grid = new char[GRID_SIZE][GRID_SIZE];
    private LinkedList<int[]> snake = new LinkedList<>();
    private int[] direction = RIGHT;
    private int[] food = new int[2];
    private Random random = new Random();
    private boolean gameOver = false;

    private JPanel gamePanel;

    public SnakeGame() {
        
        initializeGame();

       
        setTitle("Snake Game");
        setSize(GRID_SIZE * CELL_SIZE, GRID_SIZE * CELL_SIZE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };
        gamePanel.setBackground(Color.BLACK);
        add(gamePanel);

        
        addKeyListener(this);
        setFocusable(true);


        startGameLoop();
    }


    private void initializeGame() {
     
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = EMPTY;
            }
        }


        int startX = GRID_SIZE / 2;
        int startY = GRID_SIZE / 2;
        snake.add(new int[]{startX, startY});
        grid[startX][startY] = SNAKE;


        placeFood();
    }

    private void placeFood() {
        int x, y;
        do {
            x = random.nextInt(GRID_SIZE);
            y = random.nextInt(GRID_SIZE);
        } while (grid[x][y] != EMPTY);
        food[0] = x;
        food[1] = y;
        grid[x][y] = FOOD;
    }


    private void drawGrid(Graphics g) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == SNAKE) {
                    g.setColor(Color.GREEN);
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else if (grid[i][j] == FOOD) {
                    g.setColor(Color.RED);
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else {
                    g.setColor(Color.BLACK);
                    g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    
    private boolean moveSnake() {
        int[] head = snake.getLast();
        int newX = head[0] + direction[0];
        int newY = head[1] + direction[1];

    
        if (newX < 0 || newX >= GRID_SIZE || newY < 0 || newY >= GRID_SIZE || grid[newX][newY] == SNAKE) {
            return false; 
        }

        
        if (newX == food[0] && newY == food[1]) {
            snake.add(new int[]{newX, newY});
            grid[newX][newY] = SNAKE;
            placeFood();
        } else {
   
            int[] tail = snake.removeFirst();
            grid[tail[0]][tail[1]] = EMPTY;
            snake.add(new int[]{newX, newY});
            grid[newX][newY] = SNAKE;
        }

        return true;
    }


    private void startGameLoop() {
        new Thread(() -> {
            while (!gameOver) {
                gameOver = !moveSnake();
                gamePanel.repaint();
                try {
                    Thread.sleep(200); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            JOptionPane.showMessageDialog(this, "Game Over!");
        }).start();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_W:
                if (direction != DOWN) direction = UP;
                break;
            case KeyEvent.VK_S:
                if (direction != UP) direction = DOWN;
                break;
            case KeyEvent.VK_A:
                if (direction != RIGHT) direction = LEFT;
                break;
            case KeyEvent.VK_D:
                if (direction != LEFT) direction = RIGHT;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new SnakeGame().setVisible(true);
        });
    }
}