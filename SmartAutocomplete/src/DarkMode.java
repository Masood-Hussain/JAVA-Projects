package SmartAutocomplete.src;

import javax.swing.*;
import java.awt.*;

public class DarkMode {
    public static void applyDarkMode(JFrame frame) {
        frame.getContentPane().setBackground(Color.DARK_GRAY);
        for (Component c : frame.getContentPane().getComponents()) {
            if (c instanceof JTextField || c instanceof JList || c instanceof JLabel) {
                c.setBackground(Color.DARK_GRAY);
                c.setForeground(Color.WHITE);
            }
        }
    }
}
