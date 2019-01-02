
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel implements MouseListener {


    private static final int GS = 32;

    public static final int MASU = 8;

    private static final int WIDTH = GS * MASU;
    private static final int HEIGHT = WIDTH;

    private static final int BLANK = 0;

    private static final int BLACK_STONE = 1;

    private static final int WHITE_STONE = -1;

    private static final int SLEEP_TIME = 500;

    private static final int END_NUMBER = 60;

    private static final int START = 0;
    private static final int PLAY = 1;
    private static final int YOU_WIN = 2;
    private static final int YOU_LOSE = 3;
    private static final int DRAW = 4;


    private int[][] board = new int[MASU][MASU];

    private boolean flagForWhite;

    private int putNumber;


    private int gameState;

    private AI ai;

    private InfoPanel infoPanel;


    public MainPanel(InfoPanel infoPanel) {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.infoPanel = infoPanel;


        initBoard();

        ai = new AI(this);
        addMouseListener(this);

        gameState = START;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBoard(g);
        switch (gameState) {
            case START :
                drawTextCentering(g, "OTHELLO");
                break;
            case PLAY :
                drawStone(g);
                Counter counter = countStone();
                infoPanel.setBlackLabel(counter.blackCount);
                infoPanel.setWhiteLabel(counter.whiteCount);
                break;
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
                gameState = PLAY;
                break;
            case PLAY :
                int x = e.getX() / GS;
                int y = e.getY() / GS;

                if (canPutDown(x, y)) {
                    Undo undo = new Undo(x, y);
                    putDownStone(x, y, false);
                    reverse(undo, false);
                    endGame();
                    nextTurn();
                    if (countCanPutDownStone() == 0) {
                        System.out.println("AI PASS!");
                        nextTurn();
                        return;
                    } else {
                        ai.compute();
                    }
                }
                break;
            case YOU_WIN :
            case YOU_LOSE :
            case DRAW :
                gameState = START;
                initBoard();
                break;
        }
        repaint();
    }


    private void initBoard() {
        for (int y = 0; y < MASU; y++) {
            for (int x = 0; x < MASU; x++) {
                board[y][x] = BLANK;
            }
        }
        board[3][3] = board[4][4] = WHITE_STONE;
        board[3][4] = board[4][3] = BLACK_STONE;

        flagForWhite = false;
        putNumber = 0;
    }

    private void drawBoard(Graphics g) {
        g.setColor(new Color(0, 128, 128));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        for (int y = 0; y < MASU; y++) {
            for (int x = 0; x < MASU; x++) {
                g.setColor(Color.BLACK);
                g.drawRect(x * GS, y * GS, GS, GS);
            }
        }
    }

    private void drawStone(Graphics g) {
        for (int y = 0; y < MASU; y++) {
            for (int x = 0; x < MASU; x++) {
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

    public void putDownStone(int x, int y, boolean tryAndError) {
        int stone;

        if (flagForWhite) {
            stone = WHITE_STONE;
        } else {
            stone = BLACK_STONE;
        }

        board[y][x] = stone;
        if (!tryAndError) {
            putNumber++;
            update(getGraphics());
            sleep();
        }
    }


    public boolean canPutDown(int x, int y) {

        if (x >= MASU || y >= MASU)
            return false;

        if (board[y][x] != BLANK)
            return false;

        if (canPutDown(x, y, 1, 0))
            return true; // �E
        if (canPutDown(x, y, 0, 1))
            return true; // ��
        if (canPutDown(x, y, -1, 0))
            return true; // ��
        if (canPutDown(x, y, 0, -1))
            return true; // ��
        if (canPutDown(x, y, 1, 1))
            return true; // �E��
        if (canPutDown(x, y, -1, -1))
            return true; // ����
        if (canPutDown(x, y, 1, -1))
            return true; // �E��
        if (canPutDown(x, y, -1, 1))
            return true;


        return false;
    }


    private boolean canPutDown(int x, int y, int vecX, int vecY) {
        int putStone;


        if (flagForWhite) {
            putStone = WHITE_STONE;
        } else {
            putStone = BLACK_STONE;
        }


        x += vecX;
        y += vecY;

        if (x < 0 || x >= MASU || y < 0 || y >= MASU)
            return false;

        if (board[y][x] == putStone)
            return false;

        if (board[y][x] == BLANK)
            return false;


        x += vecX;
        y += vecY;

        while (x >= 0 && x < MASU && y >= 0 && y < MASU) {

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
    }


    private void reverse(Undo undo, int vecX, int vecY, boolean tryAndError) {
        int putStone;
        int x = undo.x;
        int y = undo.y;

        if (flagForWhite) {
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
        flagForWhite = !flagForWhite;
    }


    public int countCanPutDownStone() {
        int count = 0;
        
        for (int y = 0; y < MainPanel.MASU; y++) {
            for (int x = 0; x < MainPanel.MASU; x++) {
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

    public void drawTextCentering(Graphics g, String s) {
        Font f = new Font("SansSerif", Font.BOLD, 20);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Color.YELLOW);
        g.drawString(s, WIDTH / 2 - fm.stringWidth(s) / 2, HEIGHT / 2
                + fm.getDescent());
    }


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

        for (int y = 0; y < MASU; y++) {
            for (int x = 0; x < MASU; x++) {
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