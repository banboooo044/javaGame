import java.awt.*;
import java.awt.geom.Arc2D;

/**
 * ルーレットのアニメーション処理を行う.
 */
public class rouletteAnimation implements Runnable {
    /** ルーレットのタイル数 */
    private static final int ROULETTE_DIV_NUM = 12;
    /** (空気)抵抗定数*/
    private static final double AIR_RESISTANCE = 0.960;
    /** 最終的にルーレットが静止する位置 */
    private int finalPos;
    /** 最終的にルーレットが静止した位置の色 */
    private int color = 2;
    /** ルーレットの表示位置の基準点 */
    private double x,y;
    /** 回転の初速度*/
    private double velocity = 800.0;

    /** 画面描写用のグラフィックスオブジェクト */
    private Graphics2D g2;
    /** バッファ描写用のグラフィックスオブジェクト */
    private Graphics2D dbg;

    /**
     * メンバの初期化
     * @param g 画面描写用のグラフィックスオブジェクト
     * @param dbg バッファ描写用のグラフィックスオブジェクト
     * @param x ルーレットの表示位置の基準点(x座標)
     * @param y ルーレットの表示位置の基準点(y座標
     * @param finalPos ルーレットが最終的に静止する位置
     */
    public rouletteAnimation (Graphics g,Graphics dbg,double x,double y,int finalPos) {
        this.x = x;
        this.y = y;
        this.finalPos = finalPos;
        this.g2 = (Graphics2D) g;
        this.dbg = (Graphics2D) dbg;
    }

    /**
     * ルーレットを画面に描写する.
     * @param g 画面描写用のグラフィックスオブジェクト
     * @param initAng 描写するルーレットの回転角度
     */
    public void drawRoulette(Graphics2D g,double initAng) {
        for (int i = 0; i < ROULETTE_DIV_NUM ; i++) {
            if (i % 2 == 0) g.setColor(Color.WHITE);
            else g.setColor(Color.BLACK);
            g.fill(new Arc2D.Double(x, y, 2.0*x , 2.0*y, i*360/ROULETTE_DIV_NUM + initAng,  360/ROULETTE_DIV_NUM,Arc2D.PIE));
        }
        g.setColor(Color.RED);
        g.fill(new Polygon(new int[] {(int) x, (int) x, (int)x + 30 },new int[] {(int) (2*y - 10), (int) (2*y + 10), (int) (2*y)},3));
    }


    /**
     * 回転角度を微小に変化させながら, 一定時間間隔でルーレットを描写
     */
    @Override
    public void run() {
        if (finalPos > 270) color = ((630-finalPos) / (360/ROULETTE_DIV_NUM)) % 2;
        else color = ((270-finalPos) / (360/ROULETTE_DIV_NUM)) % 2;
        while(velocity > 0.2) {
            drawRoulette(g2,finalPos + velocity);
            velocity *= AIR_RESISTANCE;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (g2 != null) g2.dispose();
        // 最後の状態はずっと表示
        drawRoulette(dbg,finalPos);
    }

    /** 最終的にルーレットが静止した位置の色を返す */
    public int getColor() {
        return color;
    }
}
