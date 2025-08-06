package VisualPathfinder.src.utils;

public class Node {
    public enum Type { EMPTY, WALL, START, END, VISITED, PATH }
    private int row, col;
    private Type type;

    public Node(int row, int col) {
        this.row = row;
        this.col = col;
        this.type = Type.EMPTY;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
}
