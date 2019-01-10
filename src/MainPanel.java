
import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;

public class MainPanel extends JPanel implements MouseListener ,KeyListener {

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

    public static final int SOLO = 1;
    public static final int COMP = 2;


    private static final int START = 0;
    private static final int ROULETTE = 1;
    private static final int PLAY = 2;
    private static final int PLAYER1_WIN = 3;
    private static final int PLAYER2_WIN= 4;
    private static final int DRAW = 5;

    private static final int[] dx = { 0 , 1, 0, -1, 1, -1, -1, 1 };
    private static final int[] dy = { 1, 0, -1, 0, 1, -1, 1, -1 };


    /** ゲームモード ( SOLO / COMP ) */
    private int gameMode;

    /** ゲームの状態 ( START / PLAY / YOU_WIN / YOU_LOSE / DRAW ) */
    private int gameState;

    private boolean finishRoulette;
    boolean timerEnd = true;


    /** 盤面 */
    private int[][] board = new int[MASU_NUM][MASU_NUM];

    private int[][] openScore = new int[MASU_NUM][MASU_NUM];

    private boolean[] okPutDown = { false,false,false,false,false,false,false,false };

    private boolean isWhiteTurn;

    private int putNumber;

    private int player;

    private int verticleSlide = 0;
    private int horizonSlided = 0;

    private double whiteTimer = 500;
    private double blackTimer = 500;
    private double whiteStartTime = 0.0;
    private double blackStartTime = 0.0;
    private boolean timerChange = true;
    private boolean aiFlag = true;

    private InfoPanel infoPanel;

    Thread timer;

    boolean aiRun;

    private Graphics dbg;
    private Image dbImage = null;

    private int rouletteRnd;


    public MainPanel(InfoPanel infoPanel,int gameMode,int rouletteRnd) {
        // パネルのサイズを指定
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        setFocusable(true);
        addKeyListener(this);


        // 石の数の結果を表示するクラスのオブジェクト
        this.infoPanel = infoPanel;

        this.gameMode = gameMode;
        this.rouletteRnd = rouletteRnd;

        // 盤面の初期化
        initBoard();

        // メニュー画面を表示
        gameState = START;
        // 最初は黒のターンから開始
        timerChange = true;
        finishRoulette = false;
        isWhiteTurn = false;
        putNumber = 0;
        aiRun = false;

        timer = new Thread(new TimerAnimation(this));
        timer.start();
        timer.setPriority(Thread.MAX_PRIORITY);
    }

    public void paint() {
        render();
        paintScreen();
    }

    private void render() {
        if (dbImage == null) {
            dbImage = createImage(WIDTH, HEIGHT);
            if (dbImage == null) {
                System.out.println("dbImage is null");
                return;
            } else {
                dbg = dbImage.getGraphics();
            }
        }
        // 盤面の表示
        drawBoard(dbg);
        switch (gameState) {
            case START:
                // 中心に OTHELLO と書く
                if (gameMode == SOLO) drawTextCentering(dbg, "TORUS OTHELLO : 1P VS CPU");
                else if (gameMode == COMP) drawTextCentering(dbg, "TORUS OTHELLO : 1P VS 2P");
                break;
            case ROULETTE:
                Graphics g = getGraphics();
                rouletteAnimation rn = new rouletteAnimation(g,dbg,WIDTH/4,HEIGHT/4,rouletteRnd);
                Thread roolet = new Thread(rn);
                roolet.start();
                try {
                    roolet.join();
                } catch (InterruptedException e) {}

                finishRoulette = true;
                String you;
                if(rn.getColor() == 0) {
                    you = "BLACK";
                    player = BLACK_STONE;
                }
                else {
                    you = "WHITE";
                    player = WHITE_STONE;
                }
                drawTextButtom(dbg, "You are " + you);
                break;
            case PLAY :
                // 石を描く
                drawStone(dbg);

                // 盤面の石のそれぞれの個数をカウント
                Counter counter = countStone();

                // 石の個数の表示を変更
                infoPanel.setBlackLabel(counter.blackCount);
                infoPanel.setWhiteLabel(counter.whiteCount);
                break;

            // 勝ち負け引き分け
            case PLAYER1_WIN :
                drawStone(dbg);
                if (gameMode == SOLO) drawTextCentering(dbg, "YOU WIN");
                else  drawTextCentering(dbg, "PLAYER1 WIN");
                break;
            case PLAYER2_WIN :
                drawStone(dbg);
                if (gameMode == SOLO) drawTextCentering(dbg, "YOU LOSE");
                else  drawTextCentering(dbg, "PLAYER2 WIN");
                break;
            case DRAW :
                drawStone(dbg);
                drawTextCentering(dbg, "DRAW");
                break;
        }
    }

    private void paintScreen() {
        try {
            Graphics g = getGraphics();
            if ((g != null) && (dbImage != null)) {
                g.drawImage(dbImage, 0, 0, null);
            }
            Toolkit.getDefaultToolkit().sync();
            if (g != null) {
                g.dispose();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (aiRun) return;
        switch (gameState) {
            case START :
                //ルーレットへ遷移
                if (gameMode == SOLO) gameState = ROULETTE;
                else if (gameMode == COMP) gameState = PLAY;
                break;

            case ROULETTE:
                if (finishRoulette) {
                    gameState = PLAY;
                    paint();
                    if (player == WHITE_STONE ) {
                        aiFlag = false;
                        aiRun = true;
                        new AI(this,aiFlag);
                    }
                }
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
                    putDownStone(x, y);
                    // 挟んだ石をひっくり返す
                    reverse(undo);
                    // ゲームの終了判定
                    endGame();
                    // ターンを交代
                    nextTurn();

                    if ( gameMode == SOLO ) {
                        if (countCanPutDownStone() == 0) {
                            System.out.println("AI PASS!");
                            nextTurn();
                            return;
                        } else {
                            aiRun = true;
                            new AI(this,aiFlag);
                        }
                    }
                    else {
                        if (countCanPutDownStone() == 0) {
                            if (isWhiteTurn) System.out.println("2P PASS!");
                            else System.out.println("1P PASS!");
                            nextTurn();
                            return;
                        }
                    }
                }
                break;
            case DRAW :
                gameState = START;
                initBoard();
                break;
            default:
        }
        if (!aiRun) paint();
    }


    /** 盤面の初期化 */
    private void initBoard() {
        for (int y = 0; y < MASU_NUM; y++) {
            for (int x = 0; x < MASU_NUM; x++) {
                board[y][x] = BLANK;
                openScore[y][x] = 8;
            }
        }
        board[3][3] = board[4][4] = WHITE_STONE;
        board[3][4] = board[4][3] = BLACK_STONE;
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
    public void putDownStone(int x, int y) {
        int stone;
        if (isWhiteTurn) {
            stone = WHITE_STONE;
        } else {
            stone = BLACK_STONE;
        }

        board[y][x] = stone;
        if (!aiRun) {
            // 操作回数に1を足す
            putNumber++;
            // 画面の更新
            paint();
            sleep();
        }
        else {
            for (int yi : new int[] { 0 , 1, 0, -1} ) {
                for (int xi : new int[] { 1, 0, -1, 0} ) {
                    openScore[(y+yi+MASU_NUM)%MASU_NUM][(x+xi+MASU_NUM)%MASU_NUM] -= 1;
                }
            }
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


    public int reverse(Undo undo) {
        int openScoreValue = 0;
        for (int i = 0; i < 8 ; i ++) {
            if (okPutDown[i]) {
                openScoreValue += reverse(undo, dx[i],dy[i]);
            }
        }
        return openScoreValue;
    }

    private int reverse(Undo undo, int vecX, int vecY) {
        int putStone;
        int x = undo.x;
        int y = undo.y;
        int openScoreValue = 0;

        if (isWhiteTurn) {
            putStone = WHITE_STONE;
        } else {
            putStone = BLACK_STONE;
        }

        x = (x + vecX + MASU_NUM) % MASU_NUM;
        y = (y + vecY + MASU_NUM) % MASU_NUM;
        while (board[y][x] != putStone) {
            board[y][x] = putStone;
            openScoreValue += openScore[y][x];
            (undo.pos).push(new Point(x, y));
            if (!aiRun) {
                paint();
                sleep();
            }
            x = (x + vecX + MASU_NUM) % MASU_NUM;
            y = (y + vecY + MASU_NUM) % MASU_NUM;
        }
        return openScoreValue;
    }

    public void undoBoard(Undo undo) {
        Iterator<Point> iter = (undo.pos).iterator();
        while (iter.hasNext()) {
            Point cur = iter.next();
            board[cur.y][cur.x] *= -1;
        }
        board[undo.y][undo.x] = BLANK;

        for (int yi : new int[] { 0 , 1, 0, -1} ) {
            for (int xi : new int[] { 1, 0, -1, 0} ) {
                openScore[(undo.y+yi+MASU_NUM)%MASU_NUM][(undo.x+xi+MASU_NUM)%MASU_NUM] += 1;
            }
        }
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
     * 中心に文字を表示する
     * @param g グラフィックスオブジェクト
     * @param s 表示する文字
     */
    public void drawTextButtom(Graphics g, String s) {
        Font f = new Font("SansSerif", Font.BOLD, 40);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.YELLOW);
        g.drawString(s, WIDTH / 2 - fm.stringWidth(s) / 2, 4*HEIGHT / 5
                + fm.getDescent() );
    }

    /**
     * ゲームの終了判定
     * @return ゲームを終了するか
     */
    public boolean endGame() {
        if (putNumber == END_NUMBER) {
            Counter counter;
            counter = countStone();
            if ( (player == BLACK_STONE && counter.blackCount > 32) || (player == WHITE_STONE && counter.blackCount < 32)){
                gameState = PLAYER1_WIN;
            } else if ((player == BLACK_STONE && counter.blackCount < 32) || (player == WHITE_STONE && counter.blackCount > 32)) {
                gameState = PLAYER2_WIN;
            } else {
                gameState = DRAW;
            }
            if (!aiRun) paint();
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

    public int getOpenScore(int x,int y) {
        return openScore[y][x];
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
        if (aiRun) return;
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
        paint();
    }
    @Override
    public void keyReleased(KeyEvent keyEvent) {
    }


    class TimerAnimation implements Runnable {
        MainPanel mainPane;
        TimerAnimation(MainPanel mainPane) {
            this.mainPane = mainPane;
        }
        @Override
        public void run() {
            timerEnd = false;
            while (!timerEnd) {
                switch (gameState) {
                    case START:
                        paint();
                        break;
                    case PLAY:
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
                            if (whiteTimer < 0) gameState = PLAYER1_WIN;
                        } else {
                            blackTimer -= (curTime - blackStartTime);
                            infoPanel.setBlackTime(blackTimer);
                            blackStartTime = curTime;
                            if (blackTimer < 0) gameState = PLAYER2_WIN;
                        }
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}