/* Copyright (c) 2007-2017 MIT 6.005/6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * TODO: Specification, clarify rules for user on digging, flagging, deflagging; indicate thread safety
 * 
 * 
 * Creates a mutable, thread-safe Minesweeper board, where each cell (i,j) either contains a bomb
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
    private final Map<String, int[]> board = Collections.synchronizedMap(new HashMap<>());
    
    /*
     * Abstraction function:
     *  AF(sizeX, sizeY) = Minesweeper board with sizeX number of columns and sizeY
     *                     number of rows.
     * Rep invariant:
     *  numCols > 0
     *  numRows > 0
     *  board.size() = sizeX * sizeY
     * Rep exposure:
     *  numRows and numCols are both final and primitive data types
     *  board is never returned in any of the public methods
     * Thread safety:
     *  board is wrapped in a thread-safe wrapper
     *  each instance method that accesses the board has a lock to ensure that calls in the
     *      method are atomic
     */
    
    // TODO: Specify, test, and implement in problem 2
    
    /**
     * Checks that the board size is always exactly equal to sizeX * sizeY
     */
    private void checkRep() {
        assert numCols > 0 && numRows > 0;
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
                int randomInt = rand.nextInt(4);        // could generate 0, 1, 2, or 3
                if (randomInt == 0) {
                    status[0] = 1;        // bomb status: 1, no bomb: 0
                }
                board.put(x+","+y, status);
//                System.out.println(Arrays.toString(board.get(x+","+y)));
            }
        }
        // increment count of bombs in neighbors
        for (int x=0; x < sizeX; x++) {
            for (int y=0; y < sizeY; y++) {
                if (board.get(x+","+y)[0]==1) {
                    incrementNeighbors(x, y);
                }
            }
        }
        checkRep();
//        System.out.println("Printing board");
//        System.out.println(this);
        
    }
    
    /**
     * TODO
     * 
     * @param file
     * @throws IOException
     */
    public GameBoard(final File file) throws IOException {
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String[] size = reader.readLine().split("\\s+");
        this.numCols = Integer.parseInt(size[0]);
        this.numRows = Integer.parseInt(size[1]);
        
        // populate board
        for (int row = 0; row < numRows; row++) {
            String line = reader.readLine();
            String[] cells = line.split("\\s+");
            for (int col = 0; col < numCols; col++) {
                int[] status = {0, 0, 0};
                status[0] = Integer.parseInt(cells[col]);       // 0 if no bomb, 1 if has bomb
                board.put(col+","+row, status);
            }
        }
        reader.close();
        
        // change neighbor counts
        for (int x=0; x < numCols; x++) {
            for (int y=0; y < numRows; y++) {
                if (board.get(x+","+y)[0]==1) {
                    incrementNeighbors(x, y);
                }
            }
        }
        
        checkRep();
//        System.out.println(this);
    }
    
    /**
     * Used for incrementing/decrementing the count of neighbors with bombs.
     * For the neighbors of cell (x,y), update the count-of-neighbors-with-bombs count 
     * in its neighbors by delta.
     * @param x column of the cell with a bomb
     * @param y row of the cell with a bomb
     * @param delta how much to add to each neighbor's count-of-neighbors-with-bombs
     */
    private synchronized void updateNeighbors(int x, int y, int delta) {
        for (int i=x-1; i <= x+1; i++) {
            for (int j=y-1; j <= y+1; j++) {
                if (!(i==x && j==y) && board.containsKey(i+","+j)) {
                    board.get(i+","+j)[1] += delta;
                    assert board.get(i+","+j)[1] <= 8 && board.get(i+","+j)[1] >= 0;
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
    public synchronized String dig(int i, int j) {
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
//            updateNeighbors(i, j, -1);
//            System.out.println("neighbors updated");
            digUntouchedNeighbors(i, j);
            System.out.println("dug untouched neighbors 1");
            return "BOOM";
        }
        // if has no neighbor cells with bombs, change untouched neighbors to dug, and recurse this step for those neighbors
        digUntouchedNeighbors(i, j);
        System.out.println("dug untouched neighbors 2");
        
        return "BOARD";
    }

    /**
     * Finishes setup of the game board.
     * For the neighbors of cell (x,y), where cell (x,y) contains a bomb,
     * increment the count-of-neighbors-with-bombs count in the neighbors.
     * @param x column of the cell with a bomb
     * @param y row of the cell with a bomb
     */
    private synchronized void incrementNeighbors(int x, int y) {
        for (int i=x-1; i <= x+1; i++) {
            for (int j=y-1; j <= y+1; j++) {
                if (!(i==x && j==y) && board.containsKey(i+","+j)) {
                    board.get(i+","+j)[1] += 1;
                }
            }
        }
    }
    
    /**
     * For the neighbors of cell (x,y), decrement the 
     * count-of-neighbors-with-bombs count in its neighbors.
     * @param x column of the cell whose neighbors will be decremented
     * @param y row of the cell whose neighbors will be decremented
     */
    private synchronized void decrementNeighbors(int x, int y) {
        for (int i=x-1; i <= x+1; i++) {
            for (int j=y-1; j <= y+1; j++) {
                if (!(i==x && j==y) && board.containsKey(i+","+j)) {
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
    private synchronized void digUntouchedNeighbors(int x, int y) {
        int[] status = board.get(x+","+y);
        if (status[1]==0) {
            for (int i=x-1; i <= x+1; i++) {
                for (int j=y-1; j <= y+1; j++) {
                    if (!(i==x && j==y) && board.containsKey(i+","+j) && board.get(i+","+j)[2]==0) {
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
    public synchronized String flag(int i, int j) {
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
    public synchronized String deflag(int i, int j) {
        if (board.containsKey(i+","+j) && board.get(i+","+j)[2]==1) {
            board.get(i+","+j)[2] = 0;  // untouched
        }
        return "BOARD";
    }
    
    /**
     * Gets the status of a specific cell (i, j) in the board.
     * @param i column of the cell
     * @param j row of the cell
     * @return "untouched", "flagged", or "dug", according to the specification for GameBoard
     */
    public synchronized String getStatus(int i, int j) {
        if (board.containsKey(i+","+j)) {
            // 0 for untouched, 1 for flagged, 2 for dug
            int status = board.get(i+","+j)[2];
            switch(status) {
            case 0: return "untouched";
            case 1: return "flagged";
            case 2: return "dug";
            default: throw new AssertionError("status is not untouched, flagged, or dug; should never reach here");
            }
        } else {
            return "invalid cell";
        }
    }
    
    /**
     * Gets the number of rows (must be positive) in the board.
     * @return number of rows
     */
    public int getRows() {
        return numRows;
    }
    
    /**
     * Gets the number of columns (must be positive) in the board.
     * @return number of columns
     */
    public int getCols() {
        return numCols;
    }
    
    /**
     * Returns a string representation of the board.
     * TODO edit this description
     * 
     * “-” for squares with state untouched.
     * “F” for squares with state flagged.
     * “ ” (space) for squares with state dug and 0 neighbors that have a bomb.
     * integer COUNT in range [1-8] for squares with state dug and COUNT neighbors that have a bomb.
     */
    @Override
    public synchronized String toString() {
//        List<String> cells = new ArrayList<>(board.keySet());
        String s = "";
//        for (String cell: cells) {
//            s = s.concat(cell + ": " + Arrays.toString(board.get(cell)) + "\n");
//        }
//        return s;
        
        for (int row=0; row<numRows; row++) {
            String line = "";
            for (int col=0; col<numCols; col++) {
                if (board.get(col+","+row)[2]==0) {
                    //untouched
                    line = line.concat("- ");
                } else if (board.get(col+","+row)[2]==1) {
                    //flagged
                    line = line.concat("F ");
                } else {
                    // dug
                    if (board.get(col+","+row)[1]==0) {
                        line = line.concat("  ");
                    } else {
                        line = line.concat(board.get(col+","+row)[1] + " ");
                    }
                }
//                if (board.get(col+","+row)[0]==1) {
//                    line = line.concat("1 ");
//                } else {
//                    line = line.concat("0 ");
//                }
            }
            line = line.substring(0, line.length()-1);
            line = line.concat("\n");
            s = s.concat(line);
        }
        s = s.substring(0, s.length()-1);
        return s;
    }
}
