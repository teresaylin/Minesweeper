/* Copyright (c) 2007-2017 MIT 6.005/6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a mutable, thread-safe Minesweeper board, where each cell (i,j) - where i is the x coordinate 
 * (column), and j is the y coordinate (row) - either contains a bomb or does not contain a bomb. 
 * (0,0) indicates the top left corner of the board.
 * 
 * All cells in the board start off "untouched" until a user "digs" or "flags" that cell. 
 * The user can also "deflag" a flagged cell, which returns the cell back to the "untouched" state.
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
    private static final double BOMB_PROBABILITY = 0.25;
    
    /*
     * Abstraction function:
     *  AF(numCols, numRows, board) = Minesweeper board with numCols number of columns and numRows
     *                                number of rows, where each coordinate is mapped to a status 
     *                                array, representing if it contains a bomb, how many neighbors
     *                                have bombs, and if it is untouched/flagged/dug.
     * Rep invariant:
     *  numCols > 0
     *  numRows > 0
     *  board.size() = numCols * numRows
     * Rep exposure:
     *  numRows and numCols are both final and primitive data types
     *  board is never returned in any of the public methods
     * Thread safety:
     *  board is wrapped in a thread-safe wrapper
     *  each instance method that accesses the board has a lock to ensure that calls in the
     *      method are atomic
     */
    
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
        
        // add entries to the board
        for (int x=0; x < sizeX; x++) {
            for (int y=0; y < sizeY; y++) {
                int[] status = {0, 0, 0};
                double random = Math.random();            // could generate any decimal >= 0 and < 1
                if (random < BOMB_PROBABILITY) {
                    status[0] = 1;                        // bomb status: bomb: 1, no bomb: 0
                }
                board.put(x+","+y, status);
            }
        }
        // increment count of bombs in neighbors
        for (int x=0; x < sizeX; x++) {
            for (int y=0; y < sizeY; y++) {
                if (board.get(x+","+y)[0]==1) {
                    updateNeighbors(x, y, 1);
                }
            }
        }
        checkRep();
        
    }
    
    /**
     * Constructs a GameBoard from a file.
     * 
     * @param file The board file to be translated into a GameBoard.
     * The first line of this board file must specify "numCols [SPACE] numRows".
     * After the first line, there must be exactly numRows number of rows and
     * numCols number of columns. Each cell is represented by a number (0 or 1)
     * to represent whether or not that cell contains a bomb (1 means bomb, 0
     * means no bomb). Each line ends in a newline.
     * 
     * @throws IOException if the file cannot be located
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
                    updateNeighbors(x, y, 1);
                }
            }
        }
        
        checkRep();
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
            updateNeighbors(i, j, -1);
            digUntouchedNeighbors(i, j);
            return "BOOM";
        }
        // if has no neighbor cells with bombs, change untouched neighbors to dug, and recurse this step for those neighbors
        digUntouchedNeighbors(i, j);
        return "BOARD";
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
     * Returns the string representation of the current state of the board. At each cell, the 
     * following may be printed:
     * 
     * “-” for squares with state 'untouched'.
     * “F” for squares with state 'flagged'.
     * “ ” (space) for squares with state 'dug' and 0 neighbors that have a bomb.
     * integer COUNT in range [1-8] for squares with state 'dug' and COUNT neighbors that have a bomb.
     */
    @Override
    public synchronized String toString() {
        String s = "";
        
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
            }
            line = line.substring(0, line.length()-1);
            line = line.concat("\n");
            s = s.concat(line);
        }
        s = s.substring(0, s.length()-1);
        return s;
    }
}
