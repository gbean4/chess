package ui;

import chess.ChessBoard;
import chess.ChessGame;

public class ChessBoardUI {

    public static void render(ChessGame game){
        ChessBoard board = game.getBoard();

        System.out.println("    a  b  c  d  e  f  g  h");
        System.out.println("  --------------------------");
    }
}
