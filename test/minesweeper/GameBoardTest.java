/* Copyright (c) 2007-2017 MIT 6.005/6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * Tests methods of the GameBoard class.
 */
public class GameBoardTest {
    
    /* Testing strategy
     * gameBoard():
     *  inputs:
     *      sizeX: <=0, >0
     *      sizeY: <=0, >0
     *      file
     *  outputs:
     *      nothing (board is correct)
     *      assertion error
     *  
     * dig():
     *  inputs:
     *      invalid cell
     *      valid cell:
     *          dug
     *          flagged
     *          untouched, no bomb
     *          untouched, with bomb
     *  outputs:
     *      "BOMB"
     *      "BOARD"
     * 
     * flag():
     *  inputs:
     *      invalid cell
     *      valid cell
     *          flagged
     *          dug
     *          untouched
     *  outputs:
     *      "BOARD"
     * 
     * deflag():
     *  inputs:
     *      invalid cell
     *      valid cell
     *          flagged
     *          dug
     *          untouched
     *  outputs:
     *      "BOARD"
     * 
     * getStatus():
     *  inputs:
     *      invalid cell
     *      valid cell
     *          flagged
     *          dug
     *          untouched
     *  outputs:
     *      "invalid cell"
     *      "flagged"
     *      "dug"
     *      "untouched"
     * 
     * getCols():
     *  input: valid GameBoard
     *  
     * getRows():
     *  input: valid GameBoard
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // TESTS FOR gameBoard()
    // covers invalid sizeX and sizeY
    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidXandY() {
        new GameBoard(0, 0);
    }
    
    // covers invalid sizeX
    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidX() {
        new GameBoard(0, 3);
    }
    
    // covers invalid sizeY
    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidY() {
        new GameBoard(3, 0);
    }
    
    // covers inputting a file
    @Test
    public void testGameBoardFromFile() {
        File f = new File("test/minesweeper/boards/test5.txt");
        try {
            GameBoard g = new GameBoard(f);
            String output = "- - - - -\n" + "- - - - -\n" + "- - - - -\n" + "- - - - -\n" + "- - - - -";
            assertEquals(output, g.toString());
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    // TESTS FOR dig()    
    // covers invalid cell
    @Test
    public void testDigInvalidCell() {
        GameBoard g = new GameBoard(3, 1);
        assertEquals("digging invalid cell returns BOARD", "BOARD", g.dig(3, 0));
    }
    
    // covers valid cell that is already dug
    @Test
    public void testDigAlreadyDugCell() {
        GameBoard g = new GameBoard(3, 1);
        g.dig(2, 0);
        assertEquals("digging already dug cell returns BOARD", "BOARD", g.dig(2, 0));
        assertTrue("status of already dug cell is unchanged", g.getStatus(2, 0).equals("dug"));
    }
    
    // covers valid cell that is flagged
    @Test
    public void testDigAlreadyFlaggedCell() {
        GameBoard g = new GameBoard(3, 1);
        g.flag(2, 0);
        assertEquals("digging already flagged cell returns BOARD", "BOARD", g.dig(2, 0));
        assertTrue("status of already flagged cell is unchanged", g.getStatus(2, 0).equals("flagged"));
    }
    
    // covers valid cell that is untouched and contains a bomb
    @Test
    public void testDigUntouchedWithBomb() {
        File f = new File("test/minesweeper/boards/test_board_5");
        try {
            GameBoard g = new GameBoard(f);
            assertEquals("digging untouched cell returns BOOM", "BOOM", g.dig(4, 1));
            assertTrue("status of untouched cell is now 'dug'", g.getStatus(4, 1).equals("dug"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    // covers valid cell that is untouched and does not contain a bomb, has a neighbor with a bomb
    @Test
    public void testDigUntouchedNoBomb() {
        File f = new File("test/minesweeper/boards/test_board_5");
        try {
            GameBoard g = new GameBoard(f);
            assertEquals("digging untouched cell returns BOARD", "BOARD", g.dig(5, 1));
            assertTrue("status of untouched cell is now 'dug'", g.getStatus(5, 1).equals("dug"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    // covers valid cell that does not have a bomb or neighbors with bombs, has an untouched neighbor
    @Test
    public void testDigUntouchedNoBombNoNeighborsWithBomb() {
        File f = new File("test/minesweeper/boards/test_board_5");
        try {
            GameBoard g = new GameBoard(f);
            assertEquals("digging untouched cell returns BOARD", "BOARD", g.dig(2, 1));
            assertTrue("status of untouched cell is now 'dug'", g.getStatus(2, 1).equals("dug"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    // TESTS FOR flag()
    // covers invalid cell
    @Test
    public void testFlagInvalidCell() {
        GameBoard g = new GameBoard(3, 1);
        assertEquals("flagging invalid cell returns BOARD", "BOARD", g.flag(3, 0));
        assertTrue("status of invalid cell", g.getStatus(3, 0).equals("invalid cell"));
    }
    
    // covers valid cell that is dug
    @Test
    public void testFlagAlreadyDugCell() {
        GameBoard g = new GameBoard(3, 1);
        g.dig(2, 0);
        assertEquals("flagging already dug cell returns BOARD", "BOARD", g.flag(2, 0));
        assertTrue("status of already dug cell is unchanged", g.getStatus(2, 0).equals("dug"));
    }
    
    // covers valid cell that is flagged
    @Test
    public void testFlagAlreadyFlaggedCell() {
        GameBoard g = new GameBoard(3, 1);
        g.flag(2, 0);
        assertEquals("flagging already flagged cell returns BOARD", "BOARD", g.flag(2, 0));
        assertTrue("status of already flagged cell is unchanged", g.getStatus(2, 0).equals("flagged"));
    }
    
    // covers valid cell that is untouched
    @Test
    public void testFlagUntouchedCell() {
        GameBoard g = new GameBoard(3, 1);
        assertEquals("flagging untouched cell returns BOARD", "BOARD", g.flag(2, 0));
        assertTrue("status of untouched cell is now flagged", g.getStatus(2, 0).equals("flagged"));
    }
    
    
    // TESTS FOR deflag()
    // covers invalid cell
    @Test
    public void testDeflagInvalidCell() {
        GameBoard g = new GameBoard(3, 1);
        assertEquals("deflagging invalid cell returns BOARD", "BOARD", g.deflag(3, 0));
        assertTrue("status of invalid cell", g.getStatus(3, 0).equals("invalid cell"));
    }
    
    // covers valid cell that is dug
    @Test
    public void testDeflagAlreadyDugCell() {
        GameBoard g = new GameBoard(3, 1);
        g.dig(2, 0);
        assertEquals("deflagging already dug cell returns BOARD", "BOARD", g.deflag(2, 0));
        assertTrue("status of already dug cell is unchanged", g.getStatus(2, 0).equals("dug"));
    }
    
    // covers valid cell that is flagged
    @Test
    public void testDeflagAlreadyFlaggedCell() {
        GameBoard g = new GameBoard(3, 1);
        g.flag(2, 0);
        assertEquals("deflagging already flagged cell returns BOARD", "BOARD", g.deflag(2, 0));
        assertTrue("status of already flagged cell is now untouched", g.getStatus(2, 0).equals("untouched"));
    }
    
    // covers valid cell that is untouched
    @Test
    public void testDeflagUntouchedCell() {
        GameBoard g = new GameBoard(3, 1);
        assertEquals("deflagging untouched cell returns BOARD", "BOARD", g.deflag(2, 0));
        assertTrue("status of untouched cell is unchanged", g.getStatus(2, 0).equals("untouched"));
    }
    
    // TESTS FOR getStatus()
    // covers invalid cell
    @Test
    public void testGetStatusInvalidCell() {
        GameBoard g = new GameBoard(3, 1);
        assertEquals("getting status of invalid cell returns 'invalid cell'", "invalid cell", g.getStatus(3, 0));
    }
    
    // covers valid cell that is dug
    @Test
    public void testGetStatusAlreadyDugCell() {
        GameBoard g = new GameBoard(3, 1);
        g.dig(2, 0);
        assertEquals("getting status of already dug cell returns 'dug'", "dug", g.getStatus(2, 0));
    }
  
    // covers valid cell that is flagged
    @Test
    public void testGetStatusAlreadyFlaggedCell() {
        GameBoard g = new GameBoard(3, 1);
        g.flag(2, 0);
        assertEquals("getting status of already flagged cell returns 'flagged'", "flagged", g.getStatus(2, 0));
    }
  
    // covers valid cell that is untouched
    @Test
    public void testGetStatusUntouchedCell() {
        GameBoard g = new GameBoard(3, 1);
        assertEquals("getting status of untouched cell returns 'untouched'", "untouched", g.getStatus(2, 0));
    }
    
    // TESTS FOR getCols()
    // covers valid GameBoard
    @Test
    public void testGetCols() {
        GameBoard g = new GameBoard(5, 3);
        assertEquals("game board contains 5 columns", 5, g.getCols());
    }
    
    // TESTS FOR getRows()
    @Test
    public void testGetRows() {
        GameBoard g = new GameBoard(5, 3);
        assertEquals("game board contains 3 rows", 3, g.getRows());
    }
}
