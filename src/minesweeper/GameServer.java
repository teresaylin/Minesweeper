/* Copyright (c) 2007-2017 MIT 6.005/6.031 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.io.*;
import java.net.*;
import java.util.*;

import minesweeper.GameBoard;

/**
 * Multi-player Minesweeper server.
 * 
 * <p>PS4 instructions: you MUST NOT change the specs of main() or runGameServer(),
 *                      or the implementation of main().
 */
public class GameServer {

    /** Default server port. */
    private static final int DEFAULT_PORT = 4444;
    /** Default board size. */
    private static final int DEFAULT_SIZE = 12;

    /** Socket for receiving client connections. */
    private final ServerSocket serverSocket;
    
    /** Minesweeper board. */
    private final GameBoard board;

    // TODO: Abstraction function, rep invariant, rep exposure

    // Thread safety for instance of GameServer
    //   Threads and data are kept safe because data are confined to each thread.
    //   handleConnection() does not modify any shared variables within threads.

    // Thread safety for system started by main()
    //   TODO: Problem 5

    /**
     * Make a new game server that listens for connections on port.
     * 
     * @param port port number, requires 0 <= port <= 65535
     * @throws IOException if an error occurs opening the server socket
     */
    public GameServer(int port, GameBoard board) throws IOException {
        serverSocket = new ServerSocket(port);
        this.board = board;
    }

    /**
     * Run the server, listening for and handling client connections.
     * Never returns, unless an exception is thrown.
     * 
     * @throws IOException if an error occurs waiting for a connection
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            final Socket socket = serverSocket.accept();
            
            // create a thread for each client
            Thread handler = new Thread(new Runnable() {
                public void run() {
                    try {
                        try {
                            handleConnection(socket);
                        } finally {
                            socket.close();
                        }
                    } catch (IOException ioe) {
                        // both handleConnection and socket.close() can throw an IOException
                        ioe.printStackTrace(); // but do not stop serving
                    }
                }
            });
            handler.start();
            
            // handle a single client
//            try {
//                handleConnection(socket);
//            } catch (IOException ioe) {
//                ioe.printStackTrace(); // but do not stop serving
//            } finally {
//                socket.close();
//            }
        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket socket where the client is connected
     * @throws IOException if the connection encounters an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        // TODO change welcome message
        out.println("Welcome to Minesweeper. Players: " + 1 + " including you. Board: "
          + board.getCols() + " columns by " + board.getRows() + " rows. Type 'help' for help.\n");

        try {
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                String output = handleRequest(line);
                if (output != null) {
                    // TODO: improve the spec of handleRequest to avoid using null
                    out.println(output);
                }
            }
        } finally {
            out.close();
            in.close();
        }
    }

    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client, or null if none
     */
    private String handleRequest(String input) {
        String regex = "(look)|(help)|(bye)|"
                     + "(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|(deflag -?\\d+ -?\\d+)";
        if ( ! input.matches(regex)) {
            // invalid input
            // TODO: Problem 5
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            // 'look' request
            // TODO: Problem 5
        } else if (tokens[0].equals("help")) {
            // 'help' request
            // TODO: Problem 5
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            // TODO: Problem 5
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                // TODO: Problem 5
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request
                // TODO: Problem 5
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request
                // TODO: Problem 5
            }
        }
        // TODO: should never reach here, make sure to return in every case above
        throw new UnsupportedOperationException();
    }

    /**
     * Start a game server using the given arguments.
     * 
     * <br> Usage:
     * <pre>
     *      minesweeper.GameServer [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]
     * </pre>
     * 
     * <p>  PORT is an optional integer in the range 0 to 65535 inclusive, specifying the port the server
     *      should be listening on for incoming connections.
     * <br> E.g. "--port 1234" starts the server listening on port 1234.
     * 
     * <p>  SIZE_X and SIZE_Y are optional positive integer arguments, specifying that a random board of size
     *      SIZE_X*SIZE_Y should be generated.
     * <br> E.g. "--size 42,58" starts the server initialized with a random board of size 42 x 58.
     * 
     * <p>  FILE is an optional argument specifying a file pathname where a board has been stored. If this
     *      argument is given, the stored board should be loaded as the starting board.
     * <br> E.g. "--file boardfile.txt" starts the server initialized with the board stored in
     *      boardfile.txt.
     * 
     * <p>  The board file format, for use with the "--file" option, is specified by the following grammar:
     * <pre>
     *      FILE ::= BOARD LINE+
     *      BOARD ::= X SPACE Y NEWLINE
     *      LINE ::= (VALUE SPACE)* VALUE NEWLINE
     *      VALUE ::= "0" | "1"
     *      X ::= INT
     *      Y ::= INT
     *      SPACE ::= " "
     *      NEWLINE ::= "\n" | "\r" "\n"?
     *      INT ::= [0-9]+
     * </pre>
     *      The file must contain Y LINEs where each LINE contains X VALUEs.
     *      1 indicates a bomb, 0 indicates no bomb.
     * 
     * <p>  If neither --file nor --size is given, generate a random board of size 12 x 12.
     * 
     * <p>  Note that --file and --size may not be specified simultaneously.
     * 
     * @param args arguments as described
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        int sizeX = DEFAULT_SIZE;
        int sizeY = DEFAULT_SIZE;
        Optional<File> file = Optional.empty();

        Queue<String> arguments = new LinkedList<>(Arrays.asList(args));
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                    } else if (flag.equals("--size")) {
                        String[] sizes = arguments.remove().split(",");
                        sizeX = Integer.parseInt(sizes[0]);
                        sizeY = Integer.parseInt(sizes[1]);
                        file = Optional.empty();
                    } else if (flag.equals("--file")) {
                        sizeX = -1;
                        sizeY = -1;
                        file = Optional.of(new File(arguments.remove()));
                        if ( ! file.get().isFile()) {
                            throw new IllegalArgumentException("file not found: \"" + file.get() + "\"");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: GameServer [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]");
            return;
        }

        try {
            runGameServer(file, sizeX, sizeY, port);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Start a new GameServer running on the specified port, with either a random new board or a
     * board loaded from a file.
     * 
     * @param file if file.isPresent(), start with a board loaded from the specified file,
     *             according to the input file format defined in the documentation for main(..)
     * @param sizeX if (!file.isPresent()), start with a random board with width sizeX
     *              (and require sizeX > 0)
     * @param sizeY if (!file.isPresent()), start with a random board with height sizeY
     *              (and require sizeY > 0)
     * @param port the network port on which the server should listen, requires 0 <= port <= 65535
     * @throws IOException if a network error occurs
     */
    public static void runGameServer(Optional<File> file, int sizeX, int sizeY, int port) throws IOException {
        
        // TODO: Continue implementation here in problem 4
        GameBoard board;
        // If file is passed in as an argument
        if (file.isPresent()) {
            // pass file into GameBoard
            board = new GameBoard(file.get());
        } else {
            // Random new board
            assert sizeX > 0 && sizeY > 0;
            board = new GameBoard(sizeX, sizeY);
        }
        
        // Start server
        GameServer server = new GameServer(port, board);
        server.serve();
    }
}
