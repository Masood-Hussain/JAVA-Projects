package VisualPathfinder.src.algorithms;

import VisualPathfinder.src.GridPanel;
import VisualPathfinder.src.utils.Node;

public class Dijkstra implements AlgorithmStrategy {
    @Override
    public void solve(GridPanel gridPanel, int speed) {
        Node start = gridPanel.getStartNode();
        Node end = gridPanel.getEndNode();
        if (start == null || end == null) return;
        Node[][] grid = gridPanel.getGrid();
        int rows = grid.length, cols = grid[0].length;
        int[][] dist = new int[rows][cols];
        for (int[] row : dist) java.util.Arrays.fill(row, Integer.MAX_VALUE);
        dist[start.getRow()][start.getCol()] = 0;
        java.util.Map<Node, Node> parent = new java.util.HashMap<>();
        java.util.PriorityQueue<Node> pq = new java.util.PriorityQueue<>(java.util.Comparator.comparingInt(n -> dist[n.getRow()][n.getCol()]));
        pq.add(start);
        boolean[][] visited = new boolean[rows][cols];
        boolean found = false;
        while (!pq.isEmpty()) {
            Node curr = pq.poll();
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
                        int newDist = dist[curr.getRow()][curr.getCol()] + 1;
                        if (newDist < dist[nr][nc]) {
                            dist[nr][nc] = newDist;
                            parent.put(neighbor, curr);
                            pq.add(neighbor);
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
}
