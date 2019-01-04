import java.awt.*;
import javax.swing.*;


public class Othello extends JFrame {
    public Othello() {
        setTitle("Othelo");
        setResizable(false);

        /** contentPane を取得 */
        Container contentPane = getContentPane();

        /** infoPanel を上部に追加 */
        InfoPanel infoPanel = new InfoPanel();
        contentPane.add(infoPanel, BorderLayout.NORTH);

        /** mainPanel を真ん中に追加 */
        MainPanel mainPanel = new MainPanel(infoPanel);
        contentPane.add(mainPanel, BorderLayout.CENTER);

        JMenuBar menubar = new JMenuBar();
        JMenu menu = new JMenu("GameMode");
        menubar.add(menu);
        JMenuItem menuitem1 = new JMenuItem("1P vs CPU");
        JMenuItem menuitem2 = new JMenuItem("2P");
        menu.add(menuitem1);
        menu.add(menuitem2);
        setJMenuBar(menubar);

        /** フレームサイズを最適化 */
        pack();
    }

    public static void main(String[] args) {
        Othello frame = new Othello();

        /** xボタンでアプリケーション終了 */
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        /** フレームを可視化 */
        frame.setVisible(true);
    }
}