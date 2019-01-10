import java.awt.*;
import java.awt.geom.Arc2D;

public class rouletteAnimation implements Runnable {
    private static final int ROULETTE_DIV_NUM = 12;
    private static final double AIR_RESISTANCE = 0.960;
    private int finalPos;
    private double x,y;
    private double velocity = 800.0;
    private int color = 2; // 0 : black , 1 : white
    private Graphics2D g2;
    private Graphics2D dbg;

    public rouletteAnimation (Graphics g,Graphics dbg,double x,double y,int finalPos) {
        this.x = x;
        this.y = y;
        this.finalPos = finalPos;
        this.g2 = (Graphics2D) g;
        this.dbg = (Graphics2D) dbg;
    }


    public void drawRoulette(Graphics2D g,double initAng) {
        for (int i = 0; i < ROULETTE_DIV_NUM ; i++) {
            if (i % 2 == 0) g.setColor(Color.WHITE);
            else g.setColor(Color.BLACK);
            g.fill(new Arc2D.Double(x, y, 2.0*x , 2.0*y, i*360/ROULETTE_DIV_NUM + initAng,  360/ROULETTE_DIV_NUM,Arc2D.PIE));
        }
        g.setColor(Color.RED);
        g.fill(new Polygon(new int[] {(int) x, (int) x, (int)x + 30 },new int[] {(int) (2*y - 10), (int) (2*y + 10), (int) (2*y)},3));
    }

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
        drawRoulette(dbg,finalPos);
    }

    public int getColor() {
        return color;
    }
}
