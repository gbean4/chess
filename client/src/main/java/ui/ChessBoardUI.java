package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class ChessBoardUI {

    public static void renderBoard(ChessGame game){
        ChessBoard board = game.getBoard();

        System.out.print(SET_BG_COLOR_LIGHT_GREY + "    a  b  c  d  e  f  g  h" + RESET_BG_COLOR);

        for (int row = 0; row<8; row ++) {
            int rank = 8 - row;
            System.out.print(SET_BG_COLOR_LIGHT_GREY + " " + rank + " " + RESET_BG_COLOR);

            String bg;
            String color = null;
            String symbol = null;
            for (int col = 1; col <= 8; col++) {
                boolean isDark = (row + col) % 2 == 0;
                bg = isDark ? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;

                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null) {
                    symbol = getSymbol(piece);
                    color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE ?
                            SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK);
                }
                System.out.print(bg + color + " " + symbol + " " + RESET_BG_COLOR+ RESET_TEXT_COLOR);
            }
            System.out.println(SET_BG_COLOR_LIGHT_GREY + " " + rank + " " + RESET_BG_COLOR);
        }
        System.out.println(SET_BG_COLOR_LIGHT_GREY + "    a  b  c  d  e  f  g  h" + RESET_BG_COLOR);
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
