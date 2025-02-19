import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class Calculator extends JFrame implements ActionListener {
    
    private JTextField display;
    private JButton[] numberButtons = new JButton[10];
    private JButton addButton, subButton, mulButton, divButton;
    private JButton decButton, equButton, delButton, clrButton;
    private JPanel panel;

    
    private double num1 = 0, num2 = 0, result = 0;
    private char operator;


    private final Color DISPLAY_BG = new Color(240, 240, 240);
    private final Color BUTTON_BG = new Color(50, 50, 50);
    private final Color BUTTON_FG = Color.WHITE;
    private final Color OPERATOR_BG = new Color(255, 140, 0);
    private final Color OPERATOR_FG = Color.WHITE;
    private final Color HOVER_BG = new Color(70, 70, 70);

    public Calculator() {
        
        setTitle("Calculator");
        setSize(420, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null); 
        getContentPane().setBackground(new Color(30, 30, 30)); 

        
        display = new JTextField();
        display.setBounds(50, 25, 300, 60);
        display.setEditable(false);
        display.setFont(new Font("Roboto", Font.PLAIN, 32));
        display.setBackground(DISPLAY_BG);
        display.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(display);

        
        addButton = createButton("+", OPERATOR_BG, OPERATOR_FG);
        subButton = createButton("-", OPERATOR_BG, OPERATOR_FG);
        mulButton = createButton("*", OPERATOR_BG, OPERATOR_FG);
        divButton = createButton("/", OPERATOR_BG, OPERATOR_FG);
        decButton = createButton(".", BUTTON_BG, BUTTON_FG);
        equButton = createButton("=", OPERATOR_BG, OPERATOR_FG);
        delButton = createButton("Del", BUTTON_BG, BUTTON_FG);
        clrButton = createButton("Clr", BUTTON_BG, BUTTON_FG);

        
        addButton.addActionListener(this);
        subButton.addActionListener(this);
        mulButton.addActionListener(this);
        divButton.addActionListener(this);
        equButton.addActionListener(this);
        clrButton.addActionListener(this);
        delButton.addActionListener(this);

    
        for (int i = 0; i < 10; i++) {
            numberButtons[i] = createButton(String.valueOf(i), BUTTON_BG, BUTTON_FG);
            numberButtons[i].addActionListener(this);
        }

        
        panel = new JPanel();
        panel.setBounds(50, 100, 300, 300);
        panel.setLayout(new GridLayout(4, 4, 10, 10));
        panel.setBackground(new Color(30, 30, 30)); // Dark background

        
        panel.add(numberButtons[1]);
        panel.add(numberButtons[2]);
        panel.add(numberButtons[3]);
        panel.add(addButton);
        panel.add(numberButtons[4]);
        panel.add(numberButtons[5]);
        panel.add(numberButtons[6]);
        panel.add(subButton);
        panel.add(numberButtons[7]);
        panel.add(numberButtons[8]);
        panel.add(numberButtons[9]);
        panel.add(mulButton);
        panel.add(decButton);
        panel.add(numberButtons[0]);
        panel.add(equButton);
        panel.add(divButton);

       
        add(panel);

        
        delButton.setBounds(50, 430, 145, 50);
        clrButton.setBounds(205, 430, 145, 50);
        add(delButton);
        add(clrButton);

        setVisible(true);
    }

  
    private JButton createButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                g2.setColor(fg);
                g2.setFont(new Font("Roboto", Font.BOLD, 20));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusable(false);
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(HOVER_BG);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       
        for (int i = 0; i < 10; i++) {
            if (e.getSource() == numberButtons[i]) {
                display.setText(display.getText().concat(String.valueOf(i)));
            }
        }

        
        if (e.getSource() == decButton) {
            if (!display.getText().contains(".")) {
                display.setText(display.getText().concat("."));
            }
        }

      
        if (e.getSource() == addButton) {
            num1 = Double.parseDouble(display.getText());
            operator = '+';
            display.setText("");
        }
        if (e.getSource() == subButton) {
            num1 = Double.parseDouble(display.getText());
            operator = '-';
            display.setText("");
        }
        if (e.getSource() == mulButton) {
            num1 = Double.parseDouble(display.getText());
            operator = '*';
            display.setText("");
        }
        if (e.getSource() == divButton) {
            num1 = Double.parseDouble(display.getText());
            operator = '/';
            display.setText("");
        }

     
        if (e.getSource() == equButton) {
            try {
                num2 = Double.parseDouble(display.getText());
                switch (operator) {
                    case '+':
                        result = num1 + num2;
                        break;
                    case '-':
                        result = num1 - num2;
                        break;
                    case '*':
                        result = num1 * num2;
                        break;
                    case '/':
                        if (num2 != 0) {
                            result = num1 / num2;
                        } else {
                            display.setText("Error");
                            return;
                        }
                        break;
                }
                display.setText(String.valueOf(result));
                num1 = result; 
            } catch (NumberFormatException ex) {
                display.setText("Invalid Input");
            }
        }

        if (e.getSource() == clrButton) {
            display.setText("");
        }

        if (e.getSource() == delButton) {
            String currentText = display.getText();
            if (!currentText.isEmpty()) {
                display.setText(currentText.substring(0, currentText.length() - 1));
            }
        }
    }

    public static void main(String[] args) {
        new Calculator();
    }
}