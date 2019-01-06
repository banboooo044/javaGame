import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;


public class Othello  implements ActionListener {
    JFrame menuFrame = new JFrame();
    ArrayList<JFrame> fr = new ArrayList<JFrame>();
    Random rnd = new Random();
    public Othello() {
        frameInit(menuFrame);
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        JButton button1 = new JButton("1P vs CPU");
        JButton button2 = new JButton("2P");
        button1.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 16));
        button2.setFont(new Font("ＭＳ ゴシック", Font.BOLD, 16));
        button1.setPreferredSize(new Dimension(200,100));
        button2.setPreferredSize(new Dimension(200,100));
        button1.setMargin(new Insets(20, 2, 20, 2));
        button2.setMargin(new Insets(20, 2, 20, 2));
        button1.addActionListener(this);
        button2.addActionListener(this);
        panel.add(button1);
        panel.add(button2);
        Container contentPane = menuFrame.getContentPane();
        contentPane.add(panel,BorderLayout.CENTER);
        framePack(menuFrame);
    }

    public static void main(String[] args) {
        new Othello();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand() == "1P vs CPU") {
            JFrame frame = new JFrame();
            frameInit(frame);
            /** contentPane を取得 */
            Container contentPane = frame.getContentPane();

            /** infoPanel を上部に追加 */
            InfoPanel infoPanel = new InfoPanel();
            contentPane.add(infoPanel, BorderLayout.NORTH);

            /** mainPanel を真ん中に追加 */
            MainPanel mainPanel = new MainPanel(infoPanel,MainPanel.SOLO,rnd.nextInt(360));
            contentPane.add(mainPanel, BorderLayout.CENTER);

            framePack(frame);
            fr.add(frame);
        }
        else if (e.getActionCommand() == "2P") {
            JFrame frame = new JFrame();
            frameInit(frame);
            /** contentPane を取得 */
            Container contentPane = frame.getContentPane();

            /** infoPanel を上部に追加 */
            InfoPanel infoPanel = new InfoPanel();
            contentPane.add(infoPanel, BorderLayout.NORTH);

            /** mainPanel を真ん中に追加 */
            MainPanel mainPanel = new MainPanel(infoPanel,MainPanel.COMP,rnd.nextInt(360));
            contentPane.add(mainPanel, BorderLayout.CENTER);

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
        menu.add(menuitem1);
        menu.add(menuitem2);
        menuitem1.addActionListener(this);
        menuitem2.addActionListener(this);
        frame.setJMenuBar(menubar);
    }

    private void framePack(JFrame frame) {
        frame.setTitle("Torus Othelo");
        frame.setVisible(true);
        frame.pack();
    }


}