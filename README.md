# Welcome to the 2016 Connect Four Atomic Games Challenge!

Thanks very much for taking on the Connect Four challenge. We hope you have a lot of fun with it.

The connect-four.jar application provides a playing field for competing Connect Four AI implementations.

It accepts options to run two AIs against each other, or to host an AI for a remote game (this is used during the tournament).

## Running

The simplest way to run the game board is to launch it with the defaults:

java -jar connect-four.jar

Once it's up and running, you can connect to a webpage to view the state of the game:

http://localhost:5000/

You can run the game with the -h option to view the available parameters:

java -jar connect-four.jar -h

Most of the time during your AI development, you'll probably want to run the default random AI versus your AI executable. You can do this by passing:

java -jar connect-four.jar --p1-location [path to your executable]

## Implementing an AI

The game board manages the state of the game and computes victory conditions. When it decides to invoke your AI, it will pass information about the board state, how much time you're allowed, and which player your AI represents.

Your AI is expected to return its selected move as the return code when it exits. An AI must return a valid move within the given time or it will forfeit the game. Your move should simply be the index of the column you'd like to play in (so a number from 0-6).

When your AI is invoked, the board state is passed as a JSON string after the -b label. The player is passed as either "player-one" or "player-two" after the -p label. Time allowed is passed after the -t label and is specified in milliseconds. A typical invocation might look like:

your-ai.sh -b "...board json..." -p "player-one" -t "7000"

Where the board JSON structure will look like:
"[[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0]]"

at the start of the game. After four turns, it could look like:
"[[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[0,0,0,0,0,0,0],[2,2,0,0,0,0,0],[1,1,0,0,0,0,0]]"

So, basically it's an array of arrays, where 0 indicates an open cell and a 1 or 2 represents a piece played by player 1 or player 2.

Again, the AI is expected to return its move as the exit code, and this should be an integer index from 0-6 representing the column you'd like to drop a piece into.

Remember than an invalid move will result in losing the game. It's also expected that your AI will return within the amount of time allowed. Exceeding the timeout will result in losing the game.

Good luck!

Hints:
- Focus first on simply picking a language and tools you and your teammate will be productive with, and planning how you'll approach the day.
- We strongly suggest using some form of version control so that you can roll back to a working version if you need to at the end of the day.
- This type of problem lends itself very well to automated tests. Tests may save you a lot of time throughout the day.
- Start by parsing the arguments passed to your executable, and returning a random valid move.
- Once you've created a "random" player, you can look for winning moves, block your opponent's winning moves, or build in other optimizations.
- Advanced solutions will likely use some form of minimax, minimax with alpha-beta pruning, or a monte carlo tree search solution.

Here are some links to algorithms that may or may not be useful:

- Minimax:
http://neverstopbuilding.com/minimax
https://en.wikipedia.org/wiki/Minimax
https://www.youtube.com/watch?v=6ELUvkSkCts

- Minimax with alpha-beta pruning:
https://en.wikipedia.org/wiki/Alpha–beta_pruning
http://web.cs.ucla.edu/~rosen/161/notes/alphabeta.html

- Monte Carlo tree search:
https://en.wikipedia.org/wiki/Monte_Carlo_tree_search
http://sander.landofsand.com/publications/Monte-Carlo_Tree_Search_-_A_New_Framework_for_Game_AI.pdf

- Principal Variation Search:
https://en.wikipedia.org/wiki/Principal_variation_search
http://chessprogramming.wikispaces.com/Principal+Variation+Search

- MTD-f
https://askeplaat.wordpress.com/534-2/mtdf-algorithm/
https://en.wikipedia.org/wiki/MTD-f
