import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;


public class Othello  extends JFrame implements ActionListener {
    ArrayList<JFrame> fr = new ArrayList<JFrame>();
    Random rnd = new Random();
    public Othello() {
        frameInit(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        JPanel panel = new JPanel();
        JButton button1 = new JButton("1P vs CPU");
        JButton button2 = new JButton("1P vs 2P");
        JButton button3 = new JButton("1P vs CPU Hard Mode");
        button1.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 16));
        button2.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 16));
        button3.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 16));
        button1.setPreferredSize(new Dimension(200,100));
        button2.setPreferredSize(new Dimension(200,100));
        button3.setPreferredSize(new Dimension(200,100));
        button1.setMargin(new Insets(20, 2, 20, 2));
        button2.setMargin(new Insets(20, 2, 20, 2));
        button3.setMargin(new Insets(20, 2, 20, 2));
        button1.addActionListener(this);
        button2.addActionListener(this);
        button3.addActionListener(this);
        panel.add(button1);
        panel.add(button2);
        panel.add(button3);
        Container contentPane = this.getContentPane();
        contentPane.add(panel,BorderLayout.CENTER);
        framePack(this);
    }

    public static void main(String[] args) {
        new Othello();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();
        if (command.equals("1P vs CPU") || command.equals("1P vs 2P") || command.equals("1P vs CPU Hard Mode")) {
            JFrame frame = new JFrame();
            //frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frameInit(frame);
            /** contentPane を取得 */
            Container contentPane = frame.getContentPane();
            /** infoPanel を上部に追加 */

            InfoPanel infoPanel = new InfoPanel();
            contentPane.add(infoPanel, BorderLayout.NORTH);


            /** mainPanel を真ん中に追加 */
            MainPanel mainPanel;
            if (command.equals("1P vs CPU")) {
                mainPanel = new MainPanel(infoPanel, MainPanel.SOLO, rnd.nextInt(360));
                contentPane.add(mainPanel, BorderLayout.CENTER);
                frame.addWindowListener(new WindowClose(mainPanel));
            }
            else if (command.equals("1P vs 2P")) {
                mainPanel = new MainPanel(infoPanel,MainPanel.COMP,rnd.nextInt(360));
                contentPane.add(mainPanel, BorderLayout.CENTER);
                frame.addWindowListener(new WindowClose(mainPanel));
            }
            else if (command.equals("1P vs CPU Hard Mode")) {
                mainPanel = new MainPanel(infoPanel,MainPanel.COMP_HARD,rnd.nextInt(360));
                contentPane.add(mainPanel, BorderLayout.CENTER);
                frame.addWindowListener(new WindowClose(mainPanel));
            }

            framePack(frame);
            fr.add(frame);
        }
    }

    private void frameInit(JFrame frame) {
        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("GameMode");
        menubar.add(menu);
        JMenuItem menuitem1 = new JMenuItem("1P vs CPU");
        JMenuItem menuitem2 = new JMenuItem("2P");
        JMenuItem menuitem3 = new JMenuItem("1P vs CPU Hard Mode");
        menu.add(menuitem1);
        menu.add(menuitem2);
        menu.add(menuitem3);
        menuitem1.addActionListener(this);
        menuitem2.addActionListener(this);
        menuitem3.addActionListener(this);
        frame.setJMenuBar(menubar);
    }

    private void framePack(JFrame frame) {
        frame.setTitle("Torus Othelo");
        frame.setVisible(true);
        frame.pack();
    }
}

class WindowClose extends WindowAdapter {
    MainPanel panel;
    WindowClose(MainPanel panel) {
        this.panel = panel;
    }
    @Override
    public void windowClosing(WindowEvent windowEvent) {
        panel.timerEnd = true;
    }
}