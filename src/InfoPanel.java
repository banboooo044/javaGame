import javax.swing.*;
import javax.swing.plaf.basic.BasicArrowButton;

/** 石の数の結果を表示する */
public class InfoPanel extends JPanel {
    /** 黒の石の数 */
    private JLabel blackLabel;

    /** 白の石の数 */
    private JLabel whiteLabel;

    /** 黒側のタイマー */
    private JLabel blackTime;

    /** 白側のタイマー */
    private JLabel whiteTime;

    /**
     * BLACK : 0 , WHITE : 0 でラベルを初期化.
     */
    public InfoPanel() {
        add(new JLabel("BLACK:"));
        blackLabel = new JLabel("0");
        add(blackLabel);
        add(new JLabel("WHITE:"));
        whiteLabel = new JLabel("0");
        add(whiteLabel);
        add(new JLabel("残り時間(黒):"));
        blackTime = new JLabel("500");
        add(blackTime);
        add(new JLabel("残り時間(白):"));
        whiteTime = new JLabel("500");
        add(whiteTime);
    }

    /**
     * 黒の石の数情報の表示を変更する.
     * @param count 黒の石の数.
     */
    public void setBlackLabel(int count) {
        blackLabel.setText(count + "");
    }

    /**
     * 白の石の数情報の表示を変更する.
     * @param count 白の石の数.
     */
    public void setWhiteLabel(int count) {
        whiteLabel.setText(count + "");
    }

    public void setBlackTime(double time) {
        blackTime.setText(time + "");
    }

    public void setWhiteTime(double time) {
        whiteTime.setText(time + "");
    }
}