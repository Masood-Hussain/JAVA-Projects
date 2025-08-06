package VisualPathfinder.src;

public enum AlgorithmType {
    BFS("BFS"),
    DFS("DFS"),
    DIJKSTRA("Dijkstra"),
    ASTAR("A*");

    private final String displayName;

    AlgorithmType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
