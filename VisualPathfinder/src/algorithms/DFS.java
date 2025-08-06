package VisualPathfinder.src.algorithms;

import VisualPathfinder.src.GridPanel;
import VisualPathfinder.src.utils.Node;

public class DFS implements AlgorithmStrategy {
    @Override
    public void solve(GridPanel gridPanel, int speed) {
        Node start = gridPanel.getStartNode();
        Node end = gridPanel.getEndNode();
        if (start == null || end == null) return;
        Node[][] grid = gridPanel.getGrid();
        int rows = grid.length, cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        java.util.Map<Node, Node> parent = new java.util.HashMap<>();
        java.util.Stack<Node> stack = new java.util.Stack<>();
        stack.push(start);
        visited[start.getRow()][start.getCol()] = true;
        boolean found = false;
        while (!stack.isEmpty()) {
            Node curr = stack.pop();
            if (curr != start && curr != end) {
                curr.setType(Node.Type.VISITED);
            }
            if (curr == end) {
                found = true;
                break;
            }
            for (int[] d : new int[][]{{0,1},{1,0},{0,-1},{-1,0}}) {
                int nr = curr.getRow() + d[0];
                int nc = curr.getCol() + d[1];
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                    Node neighbor = grid[nr][nc];
                    if (!visited[nr][nc] && neighbor.getType() != Node.Type.WALL) {
                        visited[nr][nc] = true;
                        parent.put(neighbor, curr);
                        stack.push(neighbor);
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
}
