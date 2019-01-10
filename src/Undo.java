import java.awt.*;
import java.util.Stack;

public class Undo {
    public int x;
    public int y;
    public int count;
    public Stack<Point> pos;
    
    public Undo(int x, int y) {
        this.x = x;
        this.y = y;
        count = 0;
        pos = new Stack<Point>();
    }
}
