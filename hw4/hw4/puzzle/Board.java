package hw4.puzzle;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Board implements WorldState {
    private static final int BLANK = 0;
    private final int N;
    private final int[][] tiles;

    /**
     * Constructs a board from an N-by-N array of tiles where
     * tiles[i][j] = tile at row i, column j
     */
    public Board(int[][] tiles) {
        this.N = tiles.length;
        this.tiles = new int[N][N];
        for (int i = 0; i < N; i++) {
            System.arraycopy(tiles[i], 0, this.tiles[i], 0, N);
        }
    }

    /**
     * Returns value of tile at row i, column j (or 0 if blank).
     */
    public int tileAt(int i, int j) {
        if (i < 0 || i >= N || j < 0 || j >= N) {
            throw new IndexOutOfBoundsException();
        }
        return this.tiles[i][j];
    }

    /**
     * Returns the board size N.
     */
    public int size() {
        return this.N;
    }

    /**
     * Returns the neighbors of the current board
     */
    @Override
    public Iterable<WorldState> neighbors() {
        Set<WorldState> nbrs = new HashSet<>();
        int bX = -1, bY = -1;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (tiles[i][j] == BLANK) {
                    bX = i;
                    bY = j;
                }
            }
        }
        if (bX + 1 < N) {
            nbrs.add(new Board(move(bX, bY, bX + 1, bY)));
        }
        if (bX - 1 >= 0) {
            nbrs.add(new Board(move(bX, bY, bX - 1, bY)));
        }
        if (bY + 1 < N) {
            nbrs.add(new Board(move(bX, bY, bX, bY + 1)));
        }
        if (bY - 1 >= 0) {
            nbrs.add(new Board(move(bX, bY, bX, bY - 1)));
        }
        return nbrs;
    }

    private int[][] move(int x, int y, int i, int j) {
        int[][] temp = new int[N][N];
        for (int m = 0; m < N; m++) {
            System.arraycopy(tiles[m], 0, temp[m], 0, N);
        }
        temp[x][y] = tileAt(i, j);
        temp[i][j] = BLANK;
        return temp;
    }

    /**
     * Hamming estimate: The number of tiles in the wrong position.
     */
    public int hamming() {
        int hd = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (tiles[i][j] != BLANK && tiles[i][j] != i * N + j + 1) {
                    hd += 1;
                }
            }
        }
        return hd;
    }

    /**
     * Manhattan estimate: The sum of the Manhattan distances
     * (sum of the vertical and horizontal distance) from the
     * tiles to their goal positions.
     */
    public int manhattan() {
        int md = 0;
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (tiles[i][j] != BLANK && tiles[i][j] != i * N + j + 1) {
                    // vertical distance
                    md += Math.abs((tiles[i][j] - 1) / N - i);
                    // horizontal distance
                    md += Math.abs((tiles[i][j] - 1) % N - j);
                }
            }
        }
        return md;
    }

    /**
     * Estimated distance to goal. This method should
     * simply return the results of manhattan() when
     * submitted to Gradescope.
     */
    @Override
    public int estimatedDistanceToGoal() {
        return manhattan();
    }

    /**
     * Returns true if this board's tile values are the same
     * position as y's
     */
    public boolean equals(Object y) {
        Board other = (Board) y;
        if (this.N != other.size()) {
            return false;
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (tiles[i][j] != other.tileAt(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the string representation of the board.
     * Uncomment this method.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(N + "\n");
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                s.append(String.format("%2d ", tileAt(i, j)));
            }
            s.append("\n");
        }
        s.append("\n");
        return s.toString();
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.tiles);
    }
}
