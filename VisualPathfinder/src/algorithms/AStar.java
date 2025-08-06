package VisualPathfinder.src.algorithms;

import VisualPathfinder.src.GridPanel;
import VisualPathfinder.src.utils.Node;

public class AStar implements AlgorithmStrategy {
    @Override
    public void solve(GridPanel gridPanel, int speed) {
        Node start = gridPanel.getStartNode();
        Node end = gridPanel.getEndNode();
        if (start == null || end == null) return;
        Node[][] grid = gridPanel.getGrid();
        int rows = grid.length, cols = grid[0].length;
        int[][] gScore = new int[rows][cols];
        int[][] fScore = new int[rows][cols];
        for (int[] row : gScore) java.util.Arrays.fill(row, Integer.MAX_VALUE);
        for (int[] row : fScore) java.util.Arrays.fill(row, Integer.MAX_VALUE);
        gScore[start.getRow()][start.getCol()] = 0;
        fScore[start.getRow()][start.getCol()] = heuristic(start, end);
        java.util.Map<Node, Node> parent = new java.util.HashMap<>();
        java.util.PriorityQueue<Node> openSet = new java.util.PriorityQueue<>(java.util.Comparator.comparingInt(n -> fScore[n.getRow()][n.getCol()]));
        openSet.add(start);
        boolean[][] visited = new boolean[rows][cols];
        boolean found = false;
        while (!openSet.isEmpty()) {
            Node curr = openSet.poll();
            if (visited[curr.getRow()][curr.getCol()]) continue;
            visited[curr.getRow()][curr.getCol()] = true;
            if (curr != start && curr != end) curr.setType(Node.Type.VISITED);
            if (curr == end) {
                found = true;
                break;
            }
            for (int[] d : new int[][]{{0,1},{1,0},{0,-1},{-1,0}}) {
                int nr = curr.getRow() + d[0];
                int nc = curr.getCol() + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    Node neighbor = grid[nr][nc];
                    if (neighbor.getType() != Node.Type.WALL) {
                        int tentativeG = gScore[curr.getRow()][curr.getCol()] + 1;
                        if (tentativeG < gScore[nr][nc]) {
                            gScore[nr][nc] = tentativeG;
                            fScore[nr][nc] = tentativeG + heuristic(neighbor, end);
                            parent.put(neighbor, curr);
                            openSet.add(neighbor);
                        }
                    }
                }
            }
            gridPanel.repaint();
            try { Thread.sleep(speed); } catch (InterruptedException ignored) {}
        }
        if (found) {
            Node curr = end;
            while (curr != null && curr != start) {
                if (curr != end) curr.setType(Node.Type.PATH);
                curr = parent.get(curr);
                gridPanel.repaint();
                try { Thread.sleep(speed); } catch (InterruptedException ignored) {}
            }
        }
    }

    private int heuristic(Node a, Node b) {
        return Math.abs(a.getRow() - b.getRow()) + Math.abs(a.getCol() - b.getCol());
    }
}
