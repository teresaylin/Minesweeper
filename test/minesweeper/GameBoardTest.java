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
     */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // TESTS FOR gameBoard()
    // covers invalid sizeX and sizeY
//    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidXandY() {
        GameBoard g = new GameBoard(0, 0);
    }
    
    // covers invalid sizeX
//    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidX() {
        GameBoard g = new GameBoard(0, 3);
    }
    
    // covers invalid sizeY
//    @Test(expected=AssertionError.class)
    public void testGameBoardInvalidY() {
        GameBoard g = new GameBoard(3, 0);
    }
    
    //TODO test valid gameBoards?
    
    // TESTS FOR dig()
    @Test
    public void testDig() {
        GameBoard g = new GameBoard(3, 1);
    }
    
    // covers invalid cell
//    @Test
    public void testDigInvalidCell() {
        GameBoard g = new GameBoard(3, 1);
        assertEquals("digging invalid cell returns BOARD", "BOARD", g.dig(3, 0));
    }
    
    // covers valid cell that is already dug
//    @Test
    public void testDigAlreadyDugCell() {
        GameBoard g = new GameBoard(3, 1);
        g.dig(2, 0);
        assertEquals("digging already dug cell returns BOARD", "BOARD", g.dig(2, 0));
    }
    
    // covers valid cell that is flagged
//    @Test
    public void testDigAlreadyFlaggedCell() {
        GameBoard g = new GameBoard(3, 1);
        g.flag(2, 0);
        assertEquals("digging already dug cell returns BOARD", "BOARD", g.dig(2, 0));
    }
    
    // TODO covers valid cell that is untouched and contains a bomb
    
    // TODO covers valid cell that is untouched and does not contain a bomb
    
    // TESTS FOR flag()
    
    
    
    // TESTS FOR deflag()
    
}
