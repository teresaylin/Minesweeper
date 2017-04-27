/* Copyright (c) 2007-2017 MIT 6.005/6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * TODO: Description
 */
public class GameBoardTest {
    private static final String TEST_BOARD_5 = "minesweeper/boards/test_board_5";
    
    /* TODO: Testing strategy
     * gameBoard():
     *  inputs:
     *      sizeX: <=0, >0
     *      sizeY: <=0, >0
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
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // TESTS FOR gameBoard()
    // covers invalid sizeX and sizeY
    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidXandY() {
        GameBoard g = new GameBoard(0, 0);
    }
    
    // covers invalid sizeX
    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidX() {
        GameBoard g = new GameBoard(0, 3);
    }
    
    // covers invalid sizeY
    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidY() {
        GameBoard g = new GameBoard(3, 0);
    }
    
    //TODO test valid gameBoards?
    
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
    
    // TODO covers valid cell that is untouched and contains a bomb
//    @Test
//    public void testDigUntouchedWithBomb() {
//        GameBoard g = new GameBoard(3, 1);
//        g.flag(2, 0);
//        assertEquals("digging already flagged cell returns BOARD", "BOARD", g.dig(2, 0));
//        assertTrue("status of already flagged cell is unchanged", g.getStatus(2, 0).equals("flagged"));
//        System.out.println(g);
//    }
    
    // TODO covers valid cell that is untouched and does not contain a bomb
    
    
    // TODO covers valid cell that does not have a bomb or neighbors with bombs, has an untouched neighbor
    
    
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
}
