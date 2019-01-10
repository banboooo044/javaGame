public class AI implements Runnable {

    private static final int SEARCH_LEVEL = 7;

    private MainPanel panel;
    private boolean flag;

    public AI(MainPanel panel,boolean flag) {
        this.panel = panel;
        this.flag = flag;
        new Thread(this).start();
    }

    private int alphaBeta(boolean flag, int level, int alpha, int beta) {
        int value;

        int childValue;

        int bestX = 0;
        int bestY = 0;


        if (level == 0) {
            return score1();
        }
        
        if (flag) {

            value = Integer.MIN_VALUE;
        } else {

            value = Integer.MAX_VALUE;
        }

        if (panel.countCanPutDownStone() == 0) {
            return score1();
        }
        

        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                if (panel.canPutDown(x, y)) {
                    Undo undo = new Undo(x, y);

                    panel.putDownStone(x, y);
                    panel.reverse(undo);
                    /*
                    if (flag) childValue = -panel.reverse(undo, true);
                    else childValue = panel.reverse(undo, true);
                    */
                    panel.nextTurn();

                    childValue = alphaBeta(!flag, level - 1, alpha, beta);
                    if (flag) {

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

        return -value;
    }

    private int score2() { //
        int value = 0;
        for (int y = 0; y < MainPanel.MASU_NUM; y++) {
            for (int x = 0; x < MainPanel.MASU_NUM; x++) {
                if (panel.canPutDown(x,y)) value++;
            }
        }
        return value;
    }

    @Override
    public void run() {
        int temp = alphaBeta(flag, SEARCH_LEVEL, Integer.MIN_VALUE, Integer.MAX_VALUE);
        panel.aiRun = false;
        int x = temp % MainPanel.MASU_NUM;
        int y = temp / MainPanel.MASU_NUM;
        Undo undo = new Undo(x, y);
        panel.canPutDown(x,y);
        panel.putDownStone(x, y);
        panel.reverse(undo);
        if (panel.endGame()) return;
        panel.nextTurn();
        if (panel.countCanPutDownStone() == 0) {
            System.out.println("Player PASS!");
            panel.nextTurn();
            panel.aiRun = true;
            new AI(panel,flag);
        }
    }
}