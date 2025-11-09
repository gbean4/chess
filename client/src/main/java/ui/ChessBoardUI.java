package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static chess.ChessPiece.PieceType.*;
import static ui.EscapeSequences.*;

public class ChessBoardUI {

    public static void render(ChessGame game){
        ChessBoard board = game.getBoard();

        System.out.println("    a  b  c  d  e  f  g  h");
        System.out.print(SET_BG_COLOR_LIGHT_GREY + "8" + RESET_BG_COLOR);

        for (int row = 8; row>=1; row --){
            System.out.print(row + " | ");
            for (int col = 1; col<= 8; col++){
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                String symbol = getSymbol(piece);
                System.out.print(symbol + " ");
            }
            System.out.println("| " + row);
        }
        System.out.println("  --------------------------");
        System.out.println("    a  b  c  d  e  f  g  h");
    }

    private static String getSymbol(ChessPiece piece){
        if (piece == null) return EMPTY;
        String key = piece.getTeamColor() + "_" + piece.getPieceType();
        return switch (key){
            case "WHITE_KING" -> WHITE_KING;
            case "WHITE_QUEEN" -> WHITE_QUEEN;
            case "WHITE_BISHOP" -> WHITE_BISHOP;
            case "WHITE_KNIGHT" -> WHITE_KNIGHT;
            case "WHITE_ROOK" -> WHITE_ROOK;
            case "WHITE_PAWN" -> WHITE_PAWN;

            case "BLACK_KING" -> BLACK_KING;
            case "BLACK_QUEEN" -> BLACK_QUEEN;
            case "BLACK_BISHOP" -> BLACK_BISHOP;
            case "BLACK_KNIGHT" -> BLACK_KNIGHT;
            case "BLACK_ROOK" -> BLACK_ROOK;
            case "BLACK_PAWN" -> BLACK_PAWN;
            default -> EMPTY;
        };
    }
}
