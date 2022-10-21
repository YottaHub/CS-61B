package lab11.graphs;

import edu.princeton.cs.algs4.Stack;

/**
 *  @author Josh Hug
 */
public class MazeCycles extends MazeExplorer {
    /* Inherits public fields:
    public int[] distTo;
    public int[] edgeTo;
    public boolean[] marked;
    */
    private Maze maze;
    private int[] parent;

    public MazeCycles(Maze m) {
        super(m);
        maze = m;
        parent = new int[maze.V()];
    }

    @Override
    public void solve() {
        int s = maze.xyTo1D(1, 1);
        parent[s] = s;
        Stack<Integer> fringe = new Stack<>();
        fringe.push(s);
        while (!fringe.isEmpty()) {
            int v = fringe.pop();
            marked[v] = true;
            announce();
            for (int w : maze.adj(v)) {
                if (marked[w]) {
                    if (w != parent[v]) {
                        int c = v;
                        while (c != w) {
                            edgeTo[c] = parent[c];
                            c = parent[c];
                            announce();
                        }
                        return;
                    }
                } else {
                    fringe.push(w);
                    distTo[w] = distTo[v] + 1;
                    parent[w] = v;
                    announce();
                }
            }
        }
    }
}

