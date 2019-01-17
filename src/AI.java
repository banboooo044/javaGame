/**
 * オセロAI.戦略的に自動で手を打つ.
 */
public class AI implements Runnable {
    /** αβ法で先読みする手の数 */
    private static final int SEARCH_LEVEL = 6;
    /** ゲームの中心的な処理を行っているパネル */
    private MainPanel panel;
    /** AIが打つ石の色 */
    private boolean isWhite;

    /**
     * panelとisWhiteの初期化.探索を開始する.
     * @param panel ゲームの中心的な処理を行っているパネル
     * @param isWhite　AIが打つ石の色
     */
    public AI(MainPanel panel,boolean isWhite) {
        this.panel = panel;
        this.isWhite = isWhite;
        new Thread(this).start();
    }

    /**
     * αβ法による探索部分
     * @param isWhite AIが打つ石の色
     * @param level 現在の探索の深さ(再帰の深さ)
     * @param alpha アルファ値
     * @param beta ベータ値
     * @return 最適な石の置き位置
     */
    private int alphaBeta(boolean isWhite, int level, int alpha, int beta) {
        int value;

        int childValue;

        int bestX = 0;
        int bestY = 0;


        if (panel.putDownCount < 52 && level == 0) {
            return score1() + score2() / 2;
        }
        
        if (isWhite) {
            value = Integer.MAX_VALUE;
        } else {
            value = Integer.MIN_VALUE;
        }

        if (panel.countCanPutDownStone() == 0) {
            return score1();
        }
        

        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                if (panel.canPutDown(x, y ,isWhite)) {
                    Undo undo = new Undo(x, y);

                    panel.putDownStone(x, y);
                    panel.reverse(undo);
                    panel.nextTurn();

                    childValue = alphaBeta(!isWhite, level - 1, alpha, beta);
                    if (!isWhite) {
                        if (childValue > value) {
                            value = childValue;
                            alpha = value;
                            bestX = x;
                            bestY = y;
                        }
                        if (value > beta) {
                            panel.undoBoard(undo);
                            return value;
                        }
                    } else {
                        if (childValue < value) {
                            value = childValue;
                            beta = value;
                            bestX = x;
                            bestY = y;
                        }

                        if (value < alpha) {
                            panel.undoBoard(undo);
                            return value;
                        }
                    }

                    panel.undoBoard(undo);
                }
            }
        }

        if (level == SEARCH_LEVEL) {
            return bestX + bestY * MainPanel.MASU_NUM;
        } else {
            return value;
        }
    }
    
    /** (黒の石数 - 白の石数) の値を返す. */
    private int score1() {
        int value = 0;
        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                value += panel.getBoard(x, y);
            }
        }
        return value;
    }
    /** (黒の着手可能数 - 白の着手可能数) の値を返す. */
    private int score2() {
        int value = 0;
        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                // マス目にすでに石がある
                if (panel.getBoard(x,y) != MainPanel.BLANK)
                    continue;
                // マスを置けるか判定.
                for ( int i = 0;i < 8;i++ ) {
                    if (panel.canPutDown(x, y, MainPanel.dx[i], MainPanel.dy[i], false)) {
                        value++;
                        break;
                    }
                }
                for ( int i = 0;i < 8;i++ ) {
                    if (panel.canPutDown(x, y, MainPanel.dx[i], MainPanel.dy[i],true)){
                        value--;
                        break;
                    }
                }
            }
        }
        return value;
    }


    @Override
    public void run() {
        int temp = alphaBeta(isWhite, SEARCH_LEVEL, Integer.MIN_VALUE, Integer.MAX_VALUE);
        int x = temp % MainPanel.MASU_NUM;
        int y = temp / MainPanel.MASU_NUM;
        Undo undo = new Undo(x, y);
        panel.canPutDown(x,y,isWhite);
        panel.aiRun = false;
        panel.putDownStone(x, y);
        panel.reverse(undo);
        panel.putDownCount++;
        panel.nextTurn();
        if (panel.countCanPutDownStone() == 0) {
            panel.nextTurn();
            if (panel.countCanPutDownStone() == 0) {
                panel.endGame();
                return;
            }
            else {
                System.out.println("Player PASS!");
                panel.aiRun = true;
                new AI(panel, isWhite);
            }
        }
    }
}