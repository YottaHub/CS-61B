package hw4.puzzle;

import edu.princeton.cs.algs4.MinPQ;

import java.util.*;

final class Solver {
    private final List<WorldState> path;
    private final int numOfMoves;

    /**
     * Constructor which solves the puzzle, computing
     * everything necessary for moves() and solution() to
     * not have to solve the problem again. Solves the
     * puzzle using the A* algorithm. Assumes a solution exists.
     */
    public Solver(WorldState initial) {
        SearchNode end = null;
        // previously encountered world states
        Set<SearchNode> history = new HashSet<>();
        // store all search nodes during this process
        MinPQ<SearchNode> moveSequence = new MinPQ<>(new cc());
        moveSequence.insert(new SearchNode(initial));
        // find the goal using A star tree search
        while (!moveSequence.isEmpty()) {
            while (history.contains(moveSequence.min())) {
                moveSequence.delMin();
            }
            // remove search node closest to the goal
            SearchNode sn = moveSequence.delMin();
            history.add(sn);
            if (sn.state.isGoal()) {
                end = sn;
                break;
            }
            // add neighbors into the storage
            for (WorldState nbr : sn.state.neighbors()) {
                SearchNode temp = new SearchNode(nbr, sn);
                if (!history.contains(temp)) {
                    moveSequence.insert(temp);
                }
            }
        }
        this.path = end.genPath();
        this.numOfMoves = end.actualMoves;
    }

    private static class cc implements Comparator<SearchNode> {
        @Override
        public int compare(SearchNode o1, SearchNode o2) {
            return Integer.compare(o1.expectedMoves, o2.expectedMoves);
        }
    }

    private static class SearchNode {
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
    }

    /**
     * Returns the minimum number of moves to solve the puzzle starting
     * at the initial WorldState.
     */
    public int moves() {
        return this.numOfMoves;
    }

    /**
     * Returns a sequence of WorldStates from the initial WorldState
     * to the solution.
     */
    public Iterable<WorldState> solution() {
        return this.path;
    }
}
