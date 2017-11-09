# Minesweeper

#### About
This is a multiplayer version of Minesweeper, where multiple users can play at the same time on the same Minesweeper board (in a command prompt). (To get a feel for single-player Minesweeper, see [this](http://minesweeperonline.com/).) In this game, a Minesweeper board is represented by a grid of squares, where each square may or may not contain a bomb. Depending on the players' inputs, each square is in 1 of 3 possible states: dug, flagged, or untouched. One difference from the traditional single-player Minesweeper is that in this multiplayer version, when a player digs an untouched square that contains a bomb, the game feeds back an alarming message but continues to allow the player to play.

#### How to play
To start the game: open a command prompt, go to the bin directory, and run the server (java minesweeper.GameServer). You can specify a port, game board size, or board file, but all of these are optional. The default port is 4444.

To play: connect to the server by using telnet (or PuTTY). First check to see if you have telnet by opening a command prompt and typing "telnet". If you are using Windows and telnet is not installed, install and use PuTTY instead. If you are using Mac and you have Homebrew, you can run ```brew install inetutils``` to install telnet. 
(Download PuTTY [here](https://www.chiark.greenend.org.uk/~sgtatham/putty/latest.html))

Once you have a network connection client: Go into the src directory, then type ```telnet localhost PORT``` (where PORT is 4444 or the port you specified earlier). This should establish a connection to the server for one player. To play multiple players, repeat this step in a different command prompt.

Once a player is connected to the game, the 6 commands that can be input are:
- help - provides instructions on what commands there are
- look - outputs the current state of the board
- dig X Y - uncovers what is in square (X,Y)
- flag X Y - places a flag on square X Y to indicate that there is a bomb
- deflag  X Y - unflags square X Y 
- bye - leaves the game
