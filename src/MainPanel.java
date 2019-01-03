
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel implements MouseListener {

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

    private boolean[] okPutDown = { false,false,false,false,false,false,false,false};

    private boolean isWhiteTurn;

    private int putNumber;



    private AI ai;

    private InfoPanel infoPanel;


    public MainPanel(InfoPanel infoPanel) {
        // パネルのサイズを指定
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);

        // 石の数の結果を表示するクラスのオブジェクト
        this.infoPanel = infoPanel;

        // 盤面の初期化
        initBoard();

        // AIの初期化
        ai = new AI(this);

        // メニュー画面を表示
        gameState = START;
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
                int x = e.getX() / GS;
                int y = e.getY() / GS;

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

        /** 最初は黒のターンから開始 */
        isWhiteTurn = false;
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
                if (board[y][x] == BLANK) {
                    continue;
                } else if (board[y][x] == BLACK_STONE) {
                    g.setColor(Color.BLACK);
                } else {
                    g.setColor(Color.WHITE);
                }
                g.fillOval(x * GS + 3, y * GS + 3, GS - 6, GS - 6);
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

        x += vecX;
        y += vecY;

        if (x < 0 || x >= MASU_NUM || y < 0 || y >= MASU_NUM)
            return false;

        if (board[y][x] == putStone)
            return false;

        if (board[y][x] == BLANK)
            return false;


        x += vecX;
        y += vecY;

        while (x >= 0 && x < MASU_NUM && y >= 0 && y < MASU_NUM) {

            if (board[y][x] == BLANK)
                return false;

            if (board[y][x] == putStone) {
                return true;
            }
            x += vecX;
            y += vecY;
        }

        return false;
    }


    public void reverse(Undo undo, boolean tryAndError) {

        for (int i = 0; i < 8 ; i ++) {
            if (okPutDown[i]) {
                reverse(undo, dx[i],dy[i],tryAndError);
            }
        }

        /*
        if (canPutDown(undo.x, undo.y, 1, 0))
            reverse(undo, 1, 0, tryAndError);
        if (canPutDown(undo.x, undo.y, 0, 1))
            reverse(undo, 0, 1, tryAndError);
        if (canPutDown(undo.x, undo.y, -1, 0))
            reverse(undo, -1, 0, tryAndError);
        if (canPutDown(undo.x, undo.y, 0, -1))
            reverse(undo, 0, -1, tryAndError);
        if (canPutDown(undo.x, undo.y, 1, 1))
            reverse(undo, 1, 1, tryAndError);
        if (canPutDown(undo.x, undo.y, -1, -1))
            reverse(undo, -1, -1, tryAndError);
        if (canPutDown(undo.x, undo.y, 1, -1))
            reverse(undo, 1, -1, tryAndError);
        if (canPutDown(undo.x, undo.y, -1, 1))
            reverse(undo, -1, 1, tryAndError);
        */
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


        x += vecX;
        y += vecY;
        while (board[y][x] != putStone) {

            board[y][x] = putStone;

            undo.pos[undo.count++] = new Point(x, y);
            if (!tryAndError) {

                update(getGraphics());

                sleep();
            }
            x += vecX;
            y += vecY;
        }
    }

    public void undoBoard(Undo undo) {
        int c = 0;

        while (undo.pos[c] != null) {

            int x = undo.pos[c].x;
            int y = undo.pos[c].y;

            board[y][x] *= -1;
            c++;
        }

        board[undo.y][undo.x] = BLANK;

        nextTurn();
    }


    public void nextTurn() {
        isWhiteTurn = !isWhiteTurn;
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


    public int getBoard(int x, int y) {
        return board[y][x];
    }
    
    public void mousePressed(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }
}