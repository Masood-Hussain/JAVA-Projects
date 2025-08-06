# Visual Pathfinder

A Java Swing application for visualizing pathfinding algorithms on a grid. Supports BFS, DFS, Dijkstra, and A* algorithms.

## Features
- Interactive grid for setting start, end, walls, and erasing cells
- Algorithm selector: BFS, DFS, Dijkstra, A*
- Adjustable visualization speed
- Real-time path and visited node visualization

## How to Run
1. Make sure you have Java (JDK 8+) installed.
2. Navigate to the `VisualPathfinder/src` directory.
3. Compile all Java files:
   ```bash
   javac VisualPathfinder/src/*.java VisualPathfinder/src/algorithms/*.java VisualPathfinder/src/utils/*.java
   ```
4. Run the application:
   ```bash
   java VisualPathfinder.src.Main
   ```

## Usage
- Use the left panel to select edit mode (Set Start, Set End, Draw Wall, Erase).
- Choose an algorithm from the dropdown.
- Adjust the speed slider for visualization speed.
- Click "Start" to run the selected algorithm.
- Click "Reset" to clear the grid.

## Project Structure
```
VisualPathfinder/
  src/
    Main.java
    ControlPanel.java
    GridPanel.java
    AlgorithmType.java
    algorithms/
      AlgorithmStrategy.java
      BFS.java
      DFS.java
      Dijkstra.java
      AStar.java
    utils/
      Node.java
      Constants.java
      GridModel.java
```

## License
MIT
