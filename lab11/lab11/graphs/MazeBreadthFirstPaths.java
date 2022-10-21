package lab11.graphs;


import java.util.LinkedList;
import java.util.Queue;

/**
 *  @author Josh Hug
 */
public class MazeBreadthFirstPaths extends MazeExplorer {
    /* Inherits public fields:
    public int[] distTo;
    public int[] edgeTo;
    public boolean[] marked;
    */
    private Maze maze;
    private int s;
    private int t;

    public MazeBreadthFirstPaths(Maze m, int sourceX, int sourceY, int targetX, int targetY) {
        super(m);
        maze = m;
        s = maze.xyTo1D(sourceX, sourceY);
        t = maze.xyTo1D(targetX, targetY);
        distTo[s] = 0;
        edgeTo[s] = s;
    }

    /** Conducts a breadth first search of the maze starting at the source. */
    private void bfs() {
        Queue<Integer> fringe = new LinkedList<>();
        fringe.add(s);
        while (!fringe.isEmpty()) {
            int c = fringe.poll();
            marked[c] = true;
            announce();
            if (c == t) {
                return;
            }
            for (int w : maze.adj(c)) {
                if (!marked[w]) {
                    distTo[w] = distTo[c] + 1;
                    edgeTo[w] = c;
                    fringe.add(w);
                    announce();
                }
            }
        }

    }


    @Override
    public void solve() {
        bfs();
    }
}

