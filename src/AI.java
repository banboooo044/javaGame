public class AI implements Runnable {

    private static final int SEARCH_LEVEL = 7;

    private MainPanel panel;
    private boolean isWhite;

    public AI(MainPanel panel,boolean isWhite) {
        this.panel = panel;
        this.isWhite = isWhite;
        new Thread(this).start();
    }

    private int alphaBeta(boolean isWhite, int level, int alpha, int beta) {
        int value;

        int childValue;

        int bestX = 0;
        int bestY = 0;


        if (level == 0) {
            return score1() + score2(isWhite);
        }
        
        if (isWhite) {
            value = Integer.MAX_VALUE;
        } else {
            value = Integer.MIN_VALUE;
        }

        if (panel.countCanPutDownStone() == 0) {
            return score1() + score2(isWhite);
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
    

    private int score1() {
        int value = 0;
        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                value += panel.getBoard(x, y);
            }
        }
        return value;
    }

    private int score2(boolean isWhite) { //
        int value = 0;
        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                if (panel.canPutDown(x,y,isWhite)) value++;
            }
        }
        return value;
    }

    @Override
    public void run() {
        int temp = alphaBeta(isWhite, SEARCH_LEVEL, Integer.MIN_VALUE, Integer.MAX_VALUE);
        panel.aiRun = false;
        int x = temp % MainPanel.MASU_NUM;
        int y = temp / MainPanel.MASU_NUM;
        Undo undo = new Undo(x, y);
        panel.canPutDown(x,y,isWhite);
        panel.putDownStone(x, y);
        panel.reverse(undo);
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