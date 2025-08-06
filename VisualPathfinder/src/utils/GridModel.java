package VisualPathfinder.src.utils;

public class GridModel {
    private Node[][] grid;
    private int rows, cols;

    public GridModel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        grid = new Node[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new Node(r, c);
            }
        }
    }

    public Node[][] getGrid() { return grid; }
    public int getRows() { return rows; }
    public int getCols() { return cols; }
}
