/* Copyright (c) 2007-2017 MIT 6.005/6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TODO: Specification
 * Creates a mutable Minesweeper board, where each cell (i,j) either contains a bomb
 * or does not contain a bomb. All cells in the board start off "untouched" until
 * a user "digs" or "flags" that cell. The user can also "deflag" a flagged cell.
 * 
 */
public class GameBoard {
    
    // board, where each key is a cell (i,j), and the value at each key is an int[]
    // keys are in String form "i,j"
    // values are int[] of size 3, where:
    //      int[0] represents bomb status (0 for no bomb, 1 for bomb)
    //      int[1] represents how many neighbors have a bomb
    //      int[2] represents status (0 for untouched, 1 for flagged, 2 for dug)
    private final int numCols;
    private final int numRows;
    private final Map<String, int[]> board = new HashMap<>();
    
    /*
     * Abstraction function:
     *  AF(sizeX, sizeY) = Minesweeper board with sizeX number of columns and sizeY
     *  number of rows.
     * Rep invariant:
     *  numCols > 0
     *  numRows > 0
     *  board.size() = sizeX * sizeY
     * Rep exposure:
     *  numRows and numCols are both final and primitive data types
     *  board is never returned in any of the public methods
     * Thread safety:
     *  TODO
     */
    
    // TODO: Specify, test, and implement in problem 2
    
    /**
     * Checks that the board size is always exactly equal to sizeX * sizeY
     */
    private void checkRep() {
        assert numCols > 0;
        assert numRows > 0;
        assert board.size() == numCols * numRows;
    }
    
    /**
     * Constructs a Minesweeper board of size sizeX * sizeY.
     * @param sizeX number of columns in the board
     * @param sizeY number of rows in the board
     */
    public GameBoard(int sizeX, int sizeY) {
        numCols = sizeX;
        numRows = sizeY;
        Random rand = new Random();
        
        // add entries to the board
        for (int x=0; x < sizeX; x++) {
            for (int y=0; y < sizeY; y++) {
                int[] status = {0, 0, 0};
                status[0] = rand.nextInt(2);        // bomb status: 1, no bomb: 0
                board.put(x+","+y, status);
                System.out.println(Arrays.toString(board.get(x+","+y)));
            }
        }
        // increment count of bombs in neighbors
        for (int x=0; x < sizeX; x++) {
            for (int y=0; y < sizeY; y++) {
                if (board.get(x+","+y)[0]==1) {
                    incrementNeighbors(x, y);
                    System.out.println(Arrays.toString(board.get(x+","+y)));
                }
            }
        }
//        System.err.println("Printing board");
//        System.err.println(board.toString());
        checkRep();
    }
    
    /**
     * Finishes setup of the game board.
     * For the neighbors of cell (x,y), where cell (x,y) contains a bomb,
     * increment the count-of-neighbors-with-bombs count in the neighbors.
     * @param x column of the cell with a bomb
     * @param y row of the cell with a bomb
     */
    private void incrementNeighbors(int x, int y) {
        for (int i=x-1; i <= x+1; i++) {
            for (int j=y-1; j <= j+1; j++) {
                if (i!=x && j!=y && board.containsKey(i+","+j)) {
                    board.get(i+","+j)[1] += 1;
                }
            }
        }
    }
    
    
    
    /**
     * Digs cell (i,j) of the board.
     * If (i,j) is not a valid cell, nothing happens.
     * If (i,j) has been flagged or dug, nothing happens.
     * If (i,j) is untouched, the status of the cell becomes 'dug'.
     * If (i,j) contains a bomb, the bomb is removed from the board.
     * If none of the neighboring cells contain bombs, the status of untouched neighbors
     * will be changed to "dug" as well.
     * 
     * @param i column of the cell to be dug
     * @param j row of the cell to be dug
     * @return the type of message ("BOARD" or "BOOM"). "BOOM" is returned if
     * cell (i,j) contains a bomb.
     */
    public String dig(int i, int j) {
        // if not valid or not untouched, return BOARD
        if (!board.containsKey(i+","+j) || board.get(i+","+j)[2]!=0) {
            return "BOARD";
        }
        int[] status = board.get(i+","+j);
        // if untouched, change to dug
        if (status[2]==0) {
            status[2] = 2;      // change status to 2 for 'dug'
        }
        // if contains a bomb, return BOOM message, remove bomb, update count of neighbors
        if (status[0]==1) {
            status[0] = 0;
            decrementNeighbors(i, j);
            return "BOOM";
        }
        // if has no neighbor cells with bombs, change untouched neighbors to dug, and recurse this step for those neighbors
        digUntouchedNeighbors(i, j);
        
        return "BOARD";
    }
    
    /**
     * For the neighbors of cell (x,y), decrement the 
     * count-of-neighbors-with-bombs count in its neighbors.
     * @param x column of the cell whose neighbors will be decremented
     * @param y row of the cell whose neighbors will be decremented
     */
    private void decrementNeighbors(int x, int y) {
        for (int i=x-1; i <= x+1; i++) {
            for (int j=y-1; j <= j+1; j++) {
                if (i!=x && j!=y && board.containsKey(i+","+j)) {
                    board.get(i+","+j)[1] -= 1;
                }
            }
        }
    }
    
    /**
     * Digs untouched neighbors if the cell (x,y) contains no neighbors
     * with bombs. Recurses on untouched neighbors.
     * @param x column of cell
     * @param y row of cell
     */
    private void digUntouchedNeighbors(int x, int y) {
        int[] status = board.get(x+","+y);
        if (status[1]==0) {
            for (int i=x-1; i <= x+1; i++) {
                for (int j=y-1; j <= j+1; j++) {
                    if (i!=x && j!=y && board.containsKey(i+","+j) && board.get(i+","+j)[2]==0) {
                        board.get(i+","+j)[2] = 2;
                        digUntouchedNeighbors(i, j);
                    }
                }
            } 
        }
    }
    
    /**
     * Flags a cell (i,j) on the board.
     * @param i column of the cell to be flagged
     * @param j row of the cell to be flagged
     * @return "BOARD"
     */
    public String flag(int i, int j) {
        if (board.containsKey(i+","+j) && board.get(i+","+j)[2]==0) {
            board.get(i+","+j)[2] = 1;  // flag
        }
        return "BOARD";
    }
    
    /**
     * Deflags a cell (i,j) on the board.
     * @param i column of the cell to be deflagged
     * @param j row of the cell to be deflagged
     * @return "BOARD"
     */
    public String deflag(int i, int j) {
        if (board.containsKey(i+","+j) && board.get(i+","+j)[2]==1) {
            board.get(i+","+j)[2] = 0;  // untouched
        }
        return "BOARD";
    }
    
    @Override
    public String toString() {
        List<String> cells = new ArrayList<>(board.keySet());
        String s = "";
        for (String cell: cells) {
            s = s.concat(cell + ": " + Arrays.toString(board.get(cell)) + "\n");
        }
        return s;
    }
}
