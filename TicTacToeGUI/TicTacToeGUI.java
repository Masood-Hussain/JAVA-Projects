import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TicTacToeGUI {
    private JFrame frame;
    private JPanel panel;
    private JButton[][] buttons;
    private char[][] board;
    private char player;
    private boolean gameOver;

    TicTacToeGUI() {
        board = new char[3][3];
        player = 'X';
        gameOver = false;

        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                board[row][col] = ' ';
            }
        }

        initializeGUI();
    }

    private void initializeGUI() {
        frame = new JFrame("Tic-Tac-Toe");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);

        panel = new JPanel();
        panel.setLayout(new GridLayout(3, 3));

        buttons = new JButton[3][3];

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                buttons[row][col] = new JButton();
                buttons[row][col].setFont(new Font("Arial", Font.PLAIN, 40));
                buttons[row][col].setFocusPainted(false);
                buttons[row][col].addActionListener(new ButtonClickListener(row, col));
                panel.add(buttons[row][col]);
            }
        }

        frame.add(panel);
        frame.setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        private int row;
        private int col;

        ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (gameOver || board[row][col] != ' ') {
                return;
            }

            board[row][col] = player;
            buttons[row][col].setText(String.valueOf(player));

            if (bfsCheck(player)) {
                gameOver = true;
                JOptionPane.showMessageDialog(frame, "Player " + player + " has won!");
            } else {
                player = (player == 'X') ? 'O' : 'X';
            }
        }
    }

    private boolean bfsCheck(char player) {
        int n = board.length;

        for (int row = 0; row < n; row++) {
            boolean win = true;
            for (int col = 0; col < n; col++) {
                if (board[row][col] != player) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }

        for (int col = 0; col < n; col++) {
            boolean win = true;
            for (int row = 0; row < n; row++) {
                if (board[row][col] != player) {
                    win = false;
                    break;
                }
            }
            if (win) return true;
        }

        boolean win = true;
        for (int i = 0; i < n; i++) {
            if (board[i][i] != player) {
                win = false;
                break;
            }
        }
        if (win) return true;

        win = true;
        for (int i = 0; i < n; i++) {
            if (board[i][n - 1 - i] != player) {
                win = false;
                break;
            }
        }
        return win;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TicTacToeGUI());
    }
}