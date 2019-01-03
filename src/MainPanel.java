
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;

public class MainPanel extends JPanel implements MouseListener ,KeyListener , Runnable {

    /** マスの大きさ(pixel) */
    private static final int GS = 80;

    /** マスの数 */
    public static final int MASU_NUM = 8;

    /** フレームの横幅 */
    private static final int WIDTH = GS * MASU_NUM;
    /** フレームの縦幅 */
    private static final int HEIGHT = WIDTH;

    /** 空きマス */
    private static final int BLANK = 0;

    /** 黒のマス */
    private static final int BLACK_STONE = 1;

    /** 白のマス */
    private static final int WHITE_STONE = -1;

    /** 休止時間 */
    private static final int SLEEP_TIME = 500;


    private static final int END_NUMBER = 60;


    private static final int START = 0;
    private static final int PLAY = 1;
    private static final int YOU_WIN = 2;
    private static final int YOU_LOSE = 3;
    private static final int DRAW = 4;

    private static final int[] dx = { 0 , 1, 0, -1, 1, -1, -1, 1 };
    private static final int[] dy = { 1, 0, -1, 0, 1, -1, 1, -1 };


    /** ゲームの状態 ( START / PLAY / YOU_WIN / YOU_LOSE / DRAW */
    private int gameState;


    /** 盤面 */
    private int[][] board = new int[MASU_NUM][MASU_NUM];

    private boolean[] okPutDown = { false,false,false,false,false,false,false,false };

    private boolean isWhiteTurn;

    private int putNumber;

    private int verticleSlide = 0;
    private int horizonSlided = 0;

    private double whiteTimer = 500;
    private double blackTimer = 500;
    private double whiteStartTime = 0.0;
    private double blackStartTime = 0.0;
    private boolean timerChange;


    private AI ai;

    private InfoPanel infoPanel;

    private Thread timer;


    public MainPanel(InfoPanel infoPanel) {
        // パネルのサイズを指定
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        setFocusable(true);
        addKeyListener(this);

        // 石の数の結果を表示するクラスのオブジェクト
        this.infoPanel = infoPanel;

        // 盤面の初期化
        initBoard();

        // AIの初期化
        ai = new AI(this);


        // メニュー画面を表示
        gameState = START;

        timer = new Thread(this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 盤面の表示
        drawBoard(g);
        switch (gameState) {
            case START :
                // 中心に OTHELLO と書く
                drawTextCentering(g, "OTHELLO");
                break;
            case PLAY :
                // 石を描く
                drawStone(g);

                // 盤面の石のそれぞれの個数をカウント
                Counter counter = countStone();

                // 石の個数の表示を変更
                infoPanel.setBlackLabel(counter.blackCount);
                infoPanel.setWhiteLabel(counter.whiteCount);
                break;

            // 勝ち負け引き分け
            case YOU_WIN :
                drawStone(g);
                drawTextCentering(g, "YOU WIN");
                break;
            case YOU_LOSE :
                drawStone(g);
                drawTextCentering(g, "YOU LOSE");
                break;
            case DRAW :
                drawStone(g);
                drawTextCentering(g, "DRAW");
                break;
        }
    }

    public void mouseClicked(MouseEvent e) {
        switch (gameState) {
            case START :
                // ゲーム開始へ遷移
                gameState = PLAY;
                break;
            case PLAY :
                // クリック場所(マス目)を取得
                Point click = clickPlaceToRotatePlace(new Point(e.getX() / GS,e.getY() / GS));
                int x = click.x;
                int y = click.y;
                // クリックした(x,y)で石を取ることができるか.
                if (canPutDown(x, y)) { // 石を取れるので石を置く

                    Undo undo = new Undo(x, y);
                    // 石を置く
                    putDownStone(x, y, false);
                    // 挟んだ石をひっくり返す
                    reverse(undo, false);
                    // ゲームの終了判定
                    endGame();
                    // ターンを交代
                    nextTurn();

                    //
                    if (countCanPutDownStone() == 0) {
                        System.out.println("AI PASS!");
                        nextTurn();
                        return;
                    } else {
                        ai.compute();
                    }
                }
                break;
            case DRAW :
                gameState = START;
                initBoard();
                break;
            default:
        }
        repaint();
    }


    /** 盤面の初期化 */
    private void initBoard() {
        for (int y = 0; y < MASU_NUM; y++) {
            for (int x = 0; x < MASU_NUM; x++) {
                board[y][x] = BLANK;
            }
        }
        board[3][3] = board[4][4] = WHITE_STONE;
        board[3][4] = board[4][3] = BLACK_STONE;

        // 最初は黒のターンから開始
        isWhiteTurn = false;
        timerChange = true;
        putNumber = 0;
    }

    /** マス目を描く */
    private void drawBoard(Graphics g) {
        g.setColor(new Color(0, 203, 0));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        for (int y = 0; y < MASU_NUM; y++) {
            for (int x = 0; x < MASU_NUM; x++) {
                g.setColor(Color.BLACK);
                g.drawRect(x * GS, y * GS, GS, GS);
            }
        }
    }

    /** 石を描く */
    private void drawStone(Graphics g) {
        for (int y = 0; y < MASU_NUM; y++) {
            for (int x = 0; x < MASU_NUM; x++) {
                Point clickPoint = rotatePlaceToclickPlace(new Point(x,y));
                int newX = clickPoint.x;
                int newY = clickPoint.y;
                if (board[y][x] == BLANK) {
                    continue;
                } else if (board[y][x] == BLACK_STONE) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillOval(newX * GS + 3, newY * GS + 3, GS - 6, GS - 6);
            }
        }
    }

    // クリックした場所をひっくり返す.
    public void putDownStone(int x, int y, boolean tryAndError) {
        int stone;
        if (isWhiteTurn) {
            stone = WHITE_STONE;
        } else {
            stone = BLACK_STONE;
        }

        board[y][x] = stone;
        if (!tryAndError) {
            // 操作回数に1を足す
            putNumber++;
            // 画面の更新
            update(getGraphics());
            sleep();
        }
    }

    /**
     * (x,y) に石を置けるか判定
     * @param x 石をおく位置(x座標)
     * @param y 石をおく位置(y座標)
     * @return 石を置けるかどうかの判定
     */
    public boolean canPutDown(int x, int y) {
        // マス目外のクリック
        if ( x < 0 || x >= MASU_NUM || y < 0 || y >= MASU_NUM)
            return false;

        // マス目にすでに石がある
        if (board[y][x] != BLANK)
            return false;

        boolean canput = false;

        // マスを置けるか判定.
        for ( int i = 0;i < 8;i++ ) {
            if (canPutDown(x, y, dx[i], dy[i])) {
                canput = true;
                okPutDown[i] = true;
            }
            else {
                okPutDown[i] = false;
            }
        }
        return canput;
    }

    /**
     * ( x,y )　から (vecX , vecY) 方向に石を取ることができるか
     * @param x 石をおく位置(x座標)
     * @param y 石をおく位置(y座標)
     * @param vecX 挟んで取る石の並び(dx)
     * @param vecY 挟んで取る石の並び(dy)
     * @return 石を置けるかどうかの判定
     */
    private boolean canPutDown(int x, int y, int vecX, int vecY) {
        // 今の操作で置く石の色
        int putStone;
        if (isWhiteTurn) {
            putStone = WHITE_STONE;
        } else {
            putStone = BLACK_STONE;
        }

        x = (x + vecX + MASU_NUM) % MASU_NUM;
        y = (y + vecY + MASU_NUM) % MASU_NUM;

        if (board[y][x] == putStone)
            return false;

        if (board[y][x] == BLANK)
            return false;

        x = (x + vecX + MASU_NUM) % MASU_NUM;
        y = (y + vecY + MASU_NUM) % MASU_NUM;

        while (true) {
            if (board[y][x] == BLANK)
                return false;

            if (board[y][x] == putStone) {
                return true;
            }
            x = (x + vecX + MASU_NUM) % MASU_NUM;
            y = (y + vecY + MASU_NUM) % MASU_NUM;
        }
    }


    public void reverse(Undo undo, boolean tryAndError) {
        for (int i = 0; i < 8 ; i ++) {
            if (okPutDown[i]) {
                reverse(undo, dx[i],dy[i],tryAndError);
            }
        }
    }

    private void reverse(Undo undo, int vecX, int vecY, boolean tryAndError) {
        int putStone;
        int x = undo.x;
        int y = undo.y;

        if (isWhiteTurn) {
            putStone = WHITE_STONE;
        } else {
            putStone = BLACK_STONE;
        }

        x = (x + vecX + MASU_NUM) % MASU_NUM;
        y = (y + vecY + MASU_NUM) % MASU_NUM;
        while (board[y][x] != putStone) {

            board[y][x] = putStone;
            (undo.pos).push(new Point(x, y));
            if (!tryAndError) {
                update(getGraphics());
                sleep();
            }
            x = (x + vecX + MASU_NUM) % MASU_NUM;
            y = (y + vecY + MASU_NUM) % MASU_NUM;
        }
    }

    public void undoBoard(Undo undo) {
        Iterator<Point> iter = (undo.pos).iterator();
        while (iter.hasNext()) {
            Point cur = iter.next();
            board[cur.y][cur.x] *= -1;
        }
        board[undo.y][undo.x] = BLANK;
        nextTurn();
    }


    public void nextTurn() {
        isWhiteTurn = !isWhiteTurn;
        timerChange = true;
    }


    public int countCanPutDownStone() {
        int count = 0;
        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                if (canPutDown(x, y)) {
                    count++;
                }
            }
        }
        return count;
    }

    private void sleep() {
        try {
            Thread.sleep(SLEEP_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 中心に文字を表示する
     * @param g グラフィックスオブジェクト
     * @param s 表示する文字
     */
    public void drawTextCentering(Graphics g, String s) {
        Font f = new Font("SansSerif", Font.BOLD, 20);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.YELLOW);
        g.drawString(s, WIDTH / 2 - fm.stringWidth(s) / 2, HEIGHT / 2
                + fm.getDescent());
    }

    /**
     * ゲームの終了判定
     * @return ゲームを終了するか
     */
    public boolean endGame() {
        if (putNumber == END_NUMBER) {
            Counter counter;
            counter = countStone();

            if (counter.blackCount > 32) {
                gameState = YOU_WIN;
            } else if (counter.blackCount < 32) {
                gameState = YOU_LOSE;
            } else {
                gameState = DRAW;
            }
            repaint();
            return true;
        }
        return false;
    }

    public Counter countStone() {
        Counter counter = new Counter();
        for (int y = 0; y < MASU_NUM; y++) {
            for (int x = 0; x < MASU_NUM; x++) {
                if (board[y][x] == BLACK_STONE)
                    counter.blackCount++;
                if (board[y][x] == WHITE_STONE)
                    counter.whiteCount++;
            }
        }
        return counter;
    }

    private Point clickPlaceToRotatePlace(Point p) {
        return new Point((p.x + horizonSlided)%MASU_NUM,(p.y + verticleSlide)%MASU_NUM);
    }
    private Point rotatePlaceToclickPlace(Point p) {
        return new Point((p.x - horizonSlided + MASU_NUM) % MASU_NUM,(p.y - verticleSlide + MASU_NUM) % MASU_NUM);
    }

    public int getBoard(int x, int y) {
        return board[y][x];
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }
    @Override
    public void mouseEntered(MouseEvent e) {
    }
    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public void mouseReleased(MouseEvent e) {
    }
    @Override
    public void keyTyped(KeyEvent keyEvent) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        switch (key) {
            case KeyEvent.VK_UP:
                verticleSlide = (verticleSlide + 1 + MASU_NUM) % MASU_NUM;
                break;
            case KeyEvent.VK_DOWN :
                verticleSlide = (verticleSlide - 1 + MASU_NUM) % MASU_NUM;
                break;
            case KeyEvent.VK_LEFT:
                horizonSlided = (horizonSlided + 1 + MASU_NUM) % MASU_NUM;
                break;
            case KeyEvent.VK_RIGHT:
                horizonSlided = (horizonSlided - 1 + MASU_NUM) % MASU_NUM;
                break;
        }
        repaint();

    }
    @Override
    public void keyReleased(KeyEvent keyEvent) {
    }

    @Override
    public void run() {
        while (true) {
            if (gameState == PLAY ) {
                if (timerChange) {
                    timerChange = false;
                    if (isWhiteTurn) whiteStartTime = System.currentTimeMillis() / 1000;
                    else blackStartTime = System.currentTimeMillis() / 1000;
                }
                double curTime = System.currentTimeMillis() / 1000;
                if (isWhiteTurn) {
                    whiteTimer -= (curTime - whiteStartTime);
                    infoPanel.setWhiteTime(whiteTimer);
                    whiteStartTime = curTime;
                    if (whiteTimer < 0) gameState = YOU_WIN;
                } else {
                    blackTimer -= (curTime - blackStartTime);
                    infoPanel.setBlackTime(blackTimer);
                    blackStartTime = curTime;
                    if (blackTimer < 0) gameState = YOU_LOSE;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            repaint();
        }
    }

    @Override
    public void update(Graphics g) {
        paint(g);
    }
}