package hw4.puzzle;

import edu.princeton.cs.algs4.MinPQ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Solver {
    private final List<WorldState> path;
    private final int minMoves;

    /**
     * Constructor which solves the puzzle, computing
     * everything necessary for moves() and solution() to
     * not have to solve the problem again. Solves the
     * puzzle using the A* algorithm. Assumes a solution exists.
     */
    public Solver(WorldState initial) {
        // store all search nodes during this process
        MinPQ<SearchNode> moveSequence = new MinPQ<>();
        moveSequence.insert(new SearchNode(initial));
        // find the goal using A star tree search
        while (!moveSequence.isEmpty()) {
            if (moveSequence.min().state.isGoal()) {
                break;
            }
            // remove search node closest to the goal
            SearchNode sn = moveSequence.delMin();
            // add neighbors into the storage
            for (WorldState nbr : sn.state.neighbors()) {
                if (sn.prev == null || !sn.prev.state.equals(nbr)) {
                    moveSequence.insert(new SearchNode(nbr, sn));
                }
            }
        }
        this.path = moveSequence.min().genPath();
        this.minMoves = this.path.size() - 1;
    }


    public static class SearchNode implements Comparable<SearchNode> {
        public WorldState state;
        public int expectedMoves;
        public int actualMoves;
        public SearchNode prev;

        public SearchNode(WorldState ws) {
            state = ws;
            actualMoves = 0;
            expectedMoves = 0;
            prev = null;
        }

        public SearchNode(WorldState ws, SearchNode sn) {
            state = ws;
            actualMoves = sn.actualMoves + 1;
            expectedMoves = sn.actualMoves + ws.estimatedDistanceToGoal();
            prev = sn;
        }

        public List<WorldState> genPath() {
            SearchNode cur = this;
            List<WorldState> path = new ArrayList<>();
            while (cur != null) {
                path.add(cur.state);
                cur = cur.prev;
            }
            Collections.reverse(path);
            return path;
        }

        @Override
        public int hashCode() {
            if (prev == null) {
                return state.hashCode();
            }
            return state.hashCode() & prev.hashCode();
        }

        @Override
        public int compareTo(SearchNode o) {
            return this.expectedMoves - o.expectedMoves;
        }
    }

    /**
     * Returns the minimum number of moves to solve the puzzle starting
     * at the initial WorldState.
     */
    public int moves() {
        return this.minMoves;
    }

    /**
     * Returns a sequence of WorldStates from the initial WorldState
     * to the solution.
     */
    public Iterable<WorldState> solution() {
        return this.path;
    }
}
