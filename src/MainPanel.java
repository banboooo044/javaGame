import java.awt.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.swing.*;

/**
 * ゲームの中心的な処理を行う
 */
public class MainPanel extends JPanel implements MouseListener ,KeyListener {

    /** マスの大きさ(pixel) */
    private static final int GS = 80;

    /** マスの数 */
    static final int MASU_NUM = 8;

    /** フレームの横幅 */
    private static final int WIDTH = GS * MASU_NUM;
    /** フレームの縦幅 */
    private static final int HEIGHT = WIDTH;

    /** 空きマスを表す */
    static final int BLANK = 0;

    /** 黒のマスを表す */
    static final int BLACK_STONE = 1;

    /** 白のマスを表す */
    static final int WHITE_STONE = -1;

    /** 休止時間 */
    private static final int SLEEP_TIME = 500;


    /** ゲームモード ( SOLO / COMP / COMP_HARD) */
    private int gameMode;

    /** プレイモードが一人用通常モードであることを表す */
    public static final int SOLO = 1;
    /** プレイモードが二人用モードであることを表す */
    public static final int COMP = 2;
    /** プレイモードが一人用難しいモードであることを表す */
    public static final int COMP_HARD = 3;


    /** 初期状態を表す. **/
    private static final int START = 0;
    /** ルーレットの表示を行なっている状態を表す*/
    private static final int ROULETTE = 1;
    /** ゲームをプレイ中である状態を表す */
    private static final int PLAY = 2;
    /** プレイヤー1が勝利した状態を表す */
    private static final int PLAYER1_WIN = 3;
    /** プレイヤー2またはコンピュータが勝利した状態を表す */
    private static final int PLAYER2_WIN= 4;
    /** 引き分けの状態を表す */
    private static final int DRAW = 5;

    /** ゲームの状態 ( START / PLAY / YOU_WIN / YOU_LOSE / DRAW ) */
    private int gameState;

    /** ルーレットのアニメーションが終わったかどうかを表す */
    private boolean finishRoulette;
    /** 別スレッドで動くタイマーアニメーションを停止させるフラグ*/
    boolean timerEnd = true;
    /** 別スレッドで動く盤面の平行移動アニメーションを停止させるフラグ*/
    boolean moveAnimeEnd = true;

    /** 盤面を表す */
    private int[][] board = new int[MASU_NUM][MASU_NUM];
    /** 隣接8マスへの遷移のx座標変化成分 */
    static final int[] dx = { 0 , 1, 0, -1+MASU_NUM, 1, -1+MASU_NUM, -1+MASU_NUM, 1 };
    /** 隣接8マスへの遷移のy座標変化成分 */
    static final int[] dy = { 1, 0, -1+MASU_NUM, 0, 1, -1+MASU_NUM, 1, -1+MASU_NUM };
    /** 隣接8マスの方向のうち,石が取れる向きはtrueとする */
    private boolean[] okPutDown = { false,false,false,false,false,false,false,false };
    /** 白のターンかを表すフラグ */
    boolean isWhiteTurn;
    /** プレイヤーの色 ( BLACK_STONE / WHITE_STONE ) */
    private int player;
    /** 平行移動している盤面の縦変化成分 */
    private int verticleSlide = 0;
    /** 平行移動している盤面の横変化成分 */
    private int horizonSlide = 0;

    /** 白の持ち時間の残り時間 */
    private double whiteTimer = 500000;
    /** 黒の待ち時間の残り時間 */
    private double blackTimer = 500000;
    /** 白のターンで直前に計測した時間 */
    private double whiteStartTime = 0.0;
    /** 黒のターンで直前に計測した時間 */
    private double blackStartTime = 0.0;
    /** ターンが交代したかどうかのフラグ */
    private boolean timerChange = true;
    /** AIが何色のターンであるかを表す*/
    private boolean aiFlag = true;

    /** コンピュータのターンであるかどうかのフラグ */
    boolean aiRun;

    /** 現在の白黒の石数や,残りの持ち時間の表示を行う */
    private InfoPanel infoPanel;

    /** タイマーアニメーションを行うためのスレッド */
    Thread timer;
    /** 盤面の平行移動アニメーションを行うためのスレッド */
    Thread moveAnime;

    /** バッファに書き込む用のグラフィクスオブジェクトを格納する */
    private Graphics dbg;
    /** 描写のバッファ */
    private Image dbImage = null;

    /** ルーレットの乱数 */
    private int rouletteRnd;
    /** 盤面上の石数の合計 */
    int putDownCount;

    /**
     * マウス,キーボードを使用可能に設定.盤面やフラグの初期化.
     * @param infoPanel  現在の白黒の石数や,残りの持ち時間の表示を行う
     * @param gameMode ゲームモード ( SOLO / COMP / COMP_HARD)
     * @param rouletteRnd ルーレットの乱数
     */
    public MainPanel(InfoPanel infoPanel,int gameMode,int rouletteRnd) {
        // パネルのサイズを指定
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        addMouseListener(this);
        if (gameMode != COMP_HARD) {
            // 十字キーを使用可能に
            setFocusable(true);
            addKeyListener(this);
        }
        else {
            // 盤面の平行移動アニメーションをスタート
            moveAnime = new Thread(new MovingAnimation());
            moveAnime.start();
        }

        // 石の数の結果を表示するクラスのオブジェクト
        this.infoPanel = infoPanel;
        this.gameMode = gameMode;
        this.rouletteRnd = rouletteRnd;

        // 盤面の初期化
        initBoard();

        // メニュー画面を表示
        gameState = START;

        // フラグの初期化
        timerChange = true;
        finishRoulette = false;
        isWhiteTurn = false;
        aiRun = false;

        putDownCount = 0;

        // タイマーのアニメーションをスタート.
        timer = new Thread(new TimerAnimation());
        timer.start();
    }

    /**
     * 画面の更新を行う.
     */
    public void paint() {
        render();
        paintScreen();
    }

    /**
     * レンダリングを行う
     */
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
                else if (gameMode == COMP_HARD) drawTextCentering(dbg,"TORUS OTHELLO 1P VS CPU HARD");
                break;
            case ROULETTE:
                Graphics g = getGraphics();
                rouletteAnimation rn = new rouletteAnimation(g, dbg, WIDTH / 4, HEIGHT / 4, rouletteRnd);
                if (!finishRoulette) {
                    Thread roolet = new Thread(rn);
                    roolet.start();
                    try {
                        roolet.join();
                    } catch (InterruptedException e) {
                    }
                }
                else {
                    rn.drawRoulette((Graphics2D)dbg,rouletteRnd);
                }
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
                if (gameMode == SOLO || gameMode == COMP_HARD) drawTextCentering(dbg, "YOU WIN");
                else  drawTextCentering(dbg, "PLAYER1 WIN");
                break;
            case PLAYER2_WIN :
                drawStone(dbg);
                if (gameMode == SOLO || gameMode == COMP_HARD ) drawTextCentering(dbg, "YOU LOSE");
                else  drawTextCentering(dbg, "PLAYER2 WIN");
                break;
            case DRAW :
                drawStone(dbg);
                drawTextCentering(dbg, "DRAW");
                break;
        }
    }

    /**
     * バッファの内容を画面に表示する.
     */
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

    /**
     * マウスがクリックされた時のイベント処理.
     * @param e コンポーネントが定義するアクションが発生したことを示す意味上のイベント
     */
    public void mouseClicked(MouseEvent e) {
        if (aiRun) return;
        switch (gameState) {
            case START :
                if (gameMode == SOLO || gameMode == COMP_HARD) {
                    // ルーレットのアニメーションへ
                    gameState = ROULETTE;
                }
                else if (gameMode == COMP) {
                    // 対戦画面へ
                    gameState = PLAY;
                }
                break;

            case ROULETTE:
                if (finishRoulette) {
                    // 対戦画面へ
                    gameState = PLAY;
                    paint();
                    if (player == WHITE_STONE ) {
                        // コンピュータが先手の場合の処理.
                        aiFlag = false;
                        aiRun = true;
                        new AI(this,aiFlag);
                    }
                }
                break;
            case PLAY :
                if ( gameMode != COMP && ((isWhiteTurn && player == BLACK_STONE) || (!isWhiteTurn && player == WHITE_STONE))) return;
                // クリック場所(マス目)を取得
                Point click = clickPlaceToRotatePlace(new Point(e.getX() / GS,e.getY() / GS));
                int x = click.x;
                int y = click.y;
                // クリックした(x,y)で石を取ることができるか.
                if (canPutDown(x, y,isWhiteTurn)) { // 石を取れるので石を置く
                    Undo undo = new Undo(x, y);
                    // 石を置く
                    putDownStone(x, y);
                    // 挟んだ石をひっくり返す
                    reverse(undo);
                    putDownCount++;
                    // ターンを交代
                    nextTurn();

                    if (countCanPutDownStone() == 0) {
                        nextTurn();
                        if (countCanPutDownStone() == 0) {
                            endGame();
                            return;
                        }
                        if (gameMode == SOLO || gameMode == COMP_HARD) System.out.println("AI PASS!");
                        else if (isWhiteTurn) System.out.println("2P PASS!");
                        else System.out.println("1P PASS!");
                    }
                    else {
                        if (gameMode == SOLO || gameMode == COMP_HARD) {
                            aiRun = true;
                            new AI(this,aiFlag);
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
            }
        }
        board[3][3] = board[4][4] = WHITE_STONE;
        board[3][4] = board[4][3] = BLACK_STONE;
    }

    /** 盤面を描く */
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
            // 画面の更新
            paint();
            sleep();
        }
    }

    /**
     * (x,y) に石を置けるか判定
     * @param x 石をおく位置(x座標)
     * @param y 石をおく位置(y座標)
     * @param white 何色の石を置くか
     * @return 石を置けるかどうかの判定
     */
    public boolean canPutDown(int x, int y,boolean white) {
        // マス目外のクリック
        if ( x < 0 || x >= MASU_NUM || y < 0 || y >= MASU_NUM)
            return false;

        // マス目にすでに石がある
        if (board[y][x] != BLANK)
            return false;

        boolean canput = false;

        // マスを置けるか判定.
        for ( int i = 0;i < 8;i++ ) {
            if (canPutDown(x, y, dx[i], dy[i],white)) {
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
     * @param white 何色の石を置くか
     * @return 石を置けるかどうかの判定
     */
    boolean canPutDown(int x, int y, int vecX, int vecY,boolean white) {
        // 今の操作で置く石の色
        int putStone;
        if (white) {
            putStone = WHITE_STONE;
        } else {
            putStone = BLACK_STONE;
        }

        x = (x + vecX ) % MASU_NUM;
        y = (y + vecY ) % MASU_NUM;

        if (board[y][x] == putStone)
            return false;

        if (board[y][x] == BLANK)
            return false;

        x = (x + vecX ) % MASU_NUM;
        y = (y + vecY ) % MASU_NUM;

        while (true) {
            if (board[y][x] == BLANK)
                return false;

            if (board[y][x] == putStone) {
                return true;
            }
            x = (x + vecX ) % MASU_NUM;
            y = (y + vecY ) % MASU_NUM;
        }
    }

    /**
     * 直前に置いた石で挟まれた石を反転させる.
     * @param undo 反転させる石の位置を格納する用のメモリ
     */
    public void reverse(Undo undo) {
        for (int i = 0; i < 8 ; i ++) {
            if (okPutDown[i]) {
                reverse(undo, dx[i],dy[i]);
            }
        }
    }

    /**
     * ある一直線上で挟まれた石を反転させる.
     * @param undo 反転させる石の位置を格納する用のメモリ
     * @param vecX 対象の直線のX座標変化成分
     * @param vecY 対象の直線のY座標変化成分
     */
    private void reverse(Undo undo, int vecX, int vecY) {
        int putStone;
        int x = undo.x;
        int y = undo.y;

        if (isWhiteTurn) {
            putStone = WHITE_STONE;
        } else {
            putStone = BLACK_STONE;
        }

        x = (x + vecX ) % MASU_NUM;
        y = (y + vecY ) % MASU_NUM;
        while (board[y][x] != putStone) {
            board[y][x] = putStone;
            (undo.pos).push(new Point(x, y));
            if (!aiRun) {
                paint();
                sleep();
            }
            x = (x + vecX ) % MASU_NUM;
            y = (y + vecY ) % MASU_NUM;
        }
    }

    /**
     * 直前の手を打つ前の状態に戻す.
     * @param undo 直前に置かれた石の位置と反転した石の位置
     */
    public void undoBoard(Undo undo) {
        Iterator<Point> iter = (undo.pos).iterator();
        while (iter.hasNext()) {
            Point cur = iter.next();
            board[cur.y][cur.x] *= -1;
        }
        board[undo.y][undo.x] = BLANK;

        nextTurn();
    }

    /**
     * ターンを交代する処理
     */
    public void nextTurn() {
        isWhiteTurn = !isWhiteTurn;
        timerChange = true;
    }

    /**
     * 今打つ番のプレイヤーが石を置くことのできる位置数を数える
     * @return 今打つ番のプレイヤーが石を置くことのできる位置数
     */
    public int countCanPutDownStone() {
        int count = 0;
        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                if (canPutDown(x, y,isWhiteTurn)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * スレッドをある一定時間スリープさせる
     */
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
     * 下に文字を表示する
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
     * ゲームの終了処理
     */
    public void endGame() {
        Counter counter;
        counter = countStone();
        if (counter.blackCount < counter.whiteCount) {
            if (player == BLACK_STONE) gameState = PLAYER2_WIN;
            else gameState = PLAYER1_WIN;
        }
        else if (counter.blackCount > counter.whiteCount) {
            if (player == BLACK_STONE) gameState = PLAYER1_WIN;
            else gameState = PLAYER2_WIN;
        } else {
            gameState = DRAW;
        }
        if (!aiRun) paint();
    }

    /**
     * 現在の盤面上の黒石と白石の数をそれぞれカウント
     * @return 盤面上の黒石と白石の数
     */
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

    /**
     * 画面上のマス位置を二次元配列上のインデックスに変換する.
     * @param p 画面上のマス位置
     * @return 二次元配列上のインデックス
     */
    private Point clickPlaceToRotatePlace(Point p) {
        return new Point((p.x + horizonSlide + MASU_NUM)%MASU_NUM,(p.y + verticleSlide + MASU_NUM)%MASU_NUM);
    }

    /**
     * 二次元配列上のインデックスを画面上のマス位置に変換する
     * @param p 二次元配列上のインデックス
     * @return 画面上のマス位置
     */
    private Point rotatePlaceToclickPlace(Point p) {
        return new Point((p.x - horizonSlide + MASU_NUM) % MASU_NUM,(p.y - verticleSlide + MASU_NUM) % MASU_NUM);
    }

    /**
     * 盤面の状態を取得する
     * @param x 二次元配列の列番号
     * @param y 二次元配列の行番号
     * @return 盤面の状態( BLANK / BLACK_STONE / WHITE_STONE )
     */
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
    public void keyReleased(KeyEvent keyEvent) {
    }

    /**
     * 十字キーが押された時に,盤面の平行移動を行う処理.
     * @param e　コンポーネントが定義するアクションが発生したことを示す意味上のイベント
     */
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
                horizonSlide = (horizonSlide + 1 + MASU_NUM) % MASU_NUM;
                break;
            case KeyEvent.VK_RIGHT:
                horizonSlide = (horizonSlide - 1 + MASU_NUM) % MASU_NUM;
                break;
        }
        paint();
    }

    /**
     * タイマーのアニメーション処理を行う.
     */
    class TimerAnimation implements Runnable {
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
                            if (isWhiteTurn) whiteStartTime = System.currentTimeMillis();
                            else blackStartTime = System.currentTimeMillis();
                        }
                        double curTime = System.currentTimeMillis();
                        if (isWhiteTurn) {
                            whiteTimer -= (curTime - whiteStartTime);
                            infoPanel.setWhiteTime((int)whiteTimer/1000);
                            whiteStartTime = curTime;
                            if (whiteTimer < 0) gameState = PLAYER1_WIN;
                        } else {
                            blackTimer -= (curTime - blackStartTime);
                            infoPanel.setBlackTime((int)blackTimer/1000);
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

    /**
     * 盤面の平行アニメーション処理を行う.
     */
    class MovingAnimation implements Runnable {
        @Override
        public void run() {
            moveAnimeEnd = false;
            while (!moveAnimeEnd) {
                if (gameState == PLAY) {
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < MASU_NUM; i++) {
                        horizonSlide = (horizonSlide - 1 + MASU_NUM) % MASU_NUM;
                        paint();
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (gameState != PLAY) break;
                    }
                    if (gameState != PLAY) break;
                    for (int i = 0; i < MASU_NUM; i++) {
                        verticleSlide = (verticleSlide + 1 + MASU_NUM) % MASU_NUM;
                        paint();
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (gameState != PLAY) break;
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