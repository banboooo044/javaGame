import java.awt.*;
import javax.swing.*;


public class Othello extends JFrame {
    public Othello() {
        setTitle("Othelo");
        setResizable(false);

        Container contentPane = getContentPane();

        InfoPanel infoPanel = new InfoPanel();
        contentPane.add(infoPanel, BorderLayout.NORTH);

        MainPanel mainPanel = new MainPanel(infoPanel);
        contentPane.add(mainPanel, BorderLayout.CENTER);


        pack();
    }

    public static void main(String[] args) {
        Othello frame = new Othello();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}