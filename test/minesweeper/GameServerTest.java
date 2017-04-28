/* Copyright (c) 2007-2017 MIT 6.005/6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Random;

import org.junit.Test;

import minesweeper.GameServer;

/**
 * Tests GameServer with various test boards.
 */
public class GameServerTest {
    /*
     * Testing Strategy:
     *  # clients: 1, >1
     *  client input:
     *      look
     *      dig
     *      flag
     *      deflag
     *  server output:
     *      board
     *      boom
     *      help
     *  
     *  concurrency tests:
     *      c1 digs, c2 digs same cell --> nothing changes
     *      c1 digs, c2 flags --> nothing changes
     *      c1 digs, c2 deflags --> nothing changes
     *      
     *      c1 flags, c2 flags --> nothing changes
     *      c1 flags, c2 digs --> nothing changes
     *      c1 flags, c2 deflags --> cell returns to untouched
     *      
     *      c1 deflags, c2 deflags --> cell remains untouched
     *      c1 deflags, c2 flags --> cell is flagged
     *      c1 deflags, c2 digs --> cell is unchanged
     */
    
    private static final String LOCALHOST = "127.0.0.1";
    private static final int PORT = 4000 + new Random().nextInt(1 << 15);
    private static final int MAX_CONNECTION_ATTEMPTS = 10;
    private static final String BOARDS_PKG = "minesweeper/boards/";

    /**
     * Start a GameServer with a board file from BOARDS_PKG.
     * 
     * @param boardFile board to load
     * @return thread running the server
     * @throws IOException if the board file cannot be found
     */
    private static Thread startGameServer(String boardFile, int port) throws IOException {
        final URL boardURL = ClassLoader.getSystemClassLoader().getResource(BOARDS_PKG + boardFile);
        if (boardURL == null) {
            throw new IOException("Failed to locate resource " + boardFile);
        }
        final String boardPath;
        try {
            boardPath = new File(boardURL.toURI()).getAbsolutePath();
        } catch (URISyntaxException urise) {
            throw new IOException("Invalid URL " + boardURL, urise);
        }
        final String[] args = new String[] {
                "--port", Integer.toString(port),
                "--file", boardPath
        };
        Thread serverThread = new Thread(() -> GameServer.main(args));
        serverThread.start();
        return serverThread;
    }

    /**
     * Connect to a GameServer and return the connected socket.
     * 
     * @param server abort connection attempts if the server thread dies
     * @return socket connected to the server
     * @throws IOException if the connection fails
     */
    private static Socket connectToGameServer(Thread server, int port) throws IOException {
        int attempts = 0;
        while (true) {
            try {
                Socket socket = new Socket(LOCALHOST, port);
                socket.setSoTimeout(3000);
                return socket;
            } catch (ConnectException ce) {
                if ( ! server.isAlive()) {
                    throw new IOException("Server thread not running");
                }
                if (++attempts > MAX_CONNECTION_ATTEMPTS) {
                    throw new IOException("Exceeded max connection attempts", ce);
                }
                try { Thread.sleep(attempts * 10); } catch (InterruptedException ie) { }
            }
        }
    }
    
    // covers 1 client
    @Test(timeout = 10000)
    public void publishedTest() throws IOException {
        int port = 4444;
        Thread thread = startGameServer("test_board_5", port);

        Socket socket = connectToGameServer(thread, port);

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        assertTrue("expected HELLO message", in.readLine().startsWith("Welcome"));
        
        out.println("");
        assertTrue("expected HELP message", in.readLine().startsWith("Please"));

        out.println("look");
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());

        out.println("dig 3 1");
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - 1 - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());
        assertEquals("- - - - - - -", in.readLine());

        out.println("dig 4 1");
        assertEquals("BOOM!", in.readLine());

        out.println("look");
        assertEquals("             ", in.readLine());
        assertEquals("             ", in.readLine());
        assertEquals("             ", in.readLine());
        assertEquals("             ", in.readLine());
        assertEquals("             ", in.readLine());
        assertEquals("1 1          ", in.readLine());
        assertEquals("- 1          ", in.readLine());

        out.println("bye");
        socket.close();
    }
    
    // covers all scenarios, 4 clients
    @Test(timeout = 10000)
    public void testTest5Combined() throws IOException {
        int port = 4001;
        Thread thread = startGameServer("test5.txt", port);

        Socket socket = connectToGameServer(thread, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        Socket socket2 = connectToGameServer(thread, port);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
        
        Socket socket3 = connectToGameServer(thread, port);
        BufferedReader in3 = new BufferedReader(new InputStreamReader(socket3.getInputStream()));
        PrintWriter out3 = new PrintWriter(socket3.getOutputStream(), true);
        
        Socket socket4 = connectToGameServer(thread, port);
        BufferedReader in4 = new BufferedReader(new InputStreamReader(socket4.getInputStream()));
        PrintWriter out4 = new PrintWriter(socket4.getOutputStream(), true);

        assertTrue("expected HELLO message", in.readLine().startsWith("Welcome"));
        assertTrue("expected HELLO message", in2.readLine().startsWith("Welcome"));
        assertTrue("expected HELLO message", in3.readLine().startsWith("Welcome"));
        assertTrue("expected HELLO message", in4.readLine().startsWith("Welcome"));

        out.println("look");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        
        // c1 digs untouched cell
        out.println("dig 1 0");
        assertEquals("- 3 - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        
        // c2 digs same cell, gets same output
        out2.println("dig 1 0");
        assertEquals("- 3 - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        
        // c3 flags same cell, gets same output
        out3.println("flag 1 0");
        assertEquals("- 3 - - -", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        
        // c4 deflags same cell, gets same output
        out4.println("deflag 1 0");
        assertEquals("- 3 - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        
        // c3 digs bomb
        out3.println("dig 1 4");
        assertEquals("BOOM!", in3.readLine());
        
        // c1 deflags untouched cell, remains unchanged
        out.println("deflag 2 4");
        assertEquals("- 3 - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- 1 - - -", in.readLine());
        
        // c2 deflags same cell
        out2.println("deflag 2 4");
        assertEquals("- 3 - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- 1 - - -", in2.readLine());
        
        // c3 digs same cell, recursively digs
        out3.println("dig 2 4");
        assertEquals("- 3 - - -", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        assertEquals("- 2 1 3 -", in3.readLine());
        assertEquals("- 1   3 -", in3.readLine());
        assertEquals("- 1   2 -", in3.readLine());
        
        // c4 deflags untouched cell
        out4.println("deflag 4 2");
        assertEquals("- 3 - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- 2 1 3 -", in4.readLine());
        assertEquals("- 1   3 -", in4.readLine());
        assertEquals("- 1   2 -", in4.readLine());
        
        // c2 flags same cell
        out2.println("flag 4 2");
        assertEquals("- 3 - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- 2 1 3 F", in2.readLine());
        assertEquals("- 1   3 -", in2.readLine());
        assertEquals("- 1   2 -", in2.readLine());
        
        // c4 deflags same cell
        out4.println("deflag 4 2");
        assertEquals("- 3 - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- 2 1 3 -", in4.readLine());
        assertEquals("- 1   3 -", in4.readLine());
        assertEquals("- 1   2 -", in4.readLine());
        
        // c3 digs same cell
        out3.println("dig 4 2");
        assertEquals("BOOM!", in3.readLine());
        
        // c1 looks
        out.println("look");
        assertEquals("- 3 - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- 2 1 2 2", in.readLine());
        assertEquals("- 1   2 -", in.readLine());
        assertEquals("- 1   2 -", in.readLine());
        
        // c2 digs untouched cell
        out2.println("dig 0 4");
        assertEquals("BOOM!", in2.readLine());
        
        // c3 looks
        out3.println("look");
        assertEquals("- 3 - - -", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        assertEquals("2 2 1 2 2", in3.readLine());
        assertEquals("      2 -", in3.readLine());
        assertEquals("      2 -", in3.readLine());
        
        out.println("bye");
        out2.println("bye");
        out3.println("bye");
        out4.println("bye");
        socket.close();
        socket2.close();
        socket3.close();
        socket4.close();
    }
    
    @Test
    public void testTest5Flag() throws IOException {
        int port = 4002;
        Thread thread = startGameServer("test5.txt", port);

        Socket socket = connectToGameServer(thread, port);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        Socket socket2 = connectToGameServer(thread, port);
        BufferedReader in2 = new BufferedReader(new InputStreamReader(socket2.getInputStream()));
        PrintWriter out2 = new PrintWriter(socket2.getOutputStream(), true);
        
        Socket socket3 = connectToGameServer(thread, port);
        BufferedReader in3 = new BufferedReader(new InputStreamReader(socket3.getInputStream()));
        PrintWriter out3 = new PrintWriter(socket3.getOutputStream(), true);
        
        Socket socket4 = connectToGameServer(thread, port);
        BufferedReader in4 = new BufferedReader(new InputStreamReader(socket4.getInputStream()));
        PrintWriter out4 = new PrintWriter(socket4.getOutputStream(), true);

        assertTrue("expected HELLO message", in.readLine().startsWith("Welcome"));
        assertTrue("expected HELLO message", in2.readLine().startsWith("Welcome"));
        assertTrue("expected HELLO message", in3.readLine().startsWith("Welcome"));
        assertTrue("expected HELLO message", in4.readLine().startsWith("Welcome"));
        
        out.println("look");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        
        // c1 flags untouched cell
        out.println("flag 4 3");
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - -", in.readLine());
        assertEquals("- - - - F", in.readLine());
        assertEquals("- - - - -", in.readLine());
        
        // c2 flags same cell, gets same output
        out2.println("flag 4 3");
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        assertEquals("- - - - F", in2.readLine());
        assertEquals("- - - - -", in2.readLine());
        
        // c3 digs flagged cell, nothing happens
        out3.println("dig 4 3");
        assertEquals("- - - - -", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        assertEquals("- - - - F", in3.readLine());
        assertEquals("- - - - -", in3.readLine());
        
        // c4 deflags the same cell, cell becomes untouched
        out4.println("deflag 4 3");
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        assertEquals("- - - - -", in4.readLine());
        
        out.println("bye");
        out2.println("bye");
        out3.println("bye");
        out4.println("bye");
        socket.close();
        socket2.close();
        socket3.close();
        socket4.close();
    }
}
