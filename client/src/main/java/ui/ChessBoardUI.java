package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class ChessBoardUI {

    public static void renderBoard(ChessGame game, ChessGame.TeamColor perspective){
        ChessBoard board = game.getBoard();

        if (perspective == null){
            perspective = ChessGame.TeamColor.WHITE;
        }

        int[] rowRange = (perspective == ChessGame.TeamColor.WHITE) ? new int[]{7, -1, -1}: new int[]{0,8,1};
        int[] colRange = (perspective == ChessGame.TeamColor.WHITE) ? new int[]{1, 9, 1}: new int[]{8,0,-1};


        boardLabels(perspective);


        for (int row = rowRange[0]; row != rowRange[1]; row += rowRange[2]) {
            int rank = row + 1;
            System.out.print(RESET_TEXT_COLOR+ SET_BG_COLOR_LIGHT_GREY + " " + rank + " " + RESET_BG_COLOR);

            String bg;
            String color = "";
            String symbol = EMPTY;
            for (int col = colRange[0]; col != colRange[1]; col += colRange[2]) {
                boolean isDark = (row + col) % 2 == 0;
                bg = isDark ? SET_BG_COLOR_DARK_GREEN : SET_BG_COLOR_GREEN;

                ChessPosition pos = new ChessPosition(rank, col);
                ChessPiece piece = board.getPiece(pos);

                if (piece != null) {
                    symbol = getSymbol(piece);
                    color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                            ? SET_TEXT_COLOR_WHITE
                            : SET_TEXT_COLOR_BLACK;
                }
                System.out.print(bg + color + " " + symbol + " " + RESET_BG_COLOR+ RESET_TEXT_COLOR);
            }
            System.out.println(SET_BG_COLOR_LIGHT_GREY + " " + rank + " " + RESET_BG_COLOR);
        }
        boardLabels(perspective);
    }

    private static void boardLabels(ChessGame.TeamColor perspective) {
        System.out.print(RESET_TEXT_COLOR+SET_BG_COLOR_LIGHT_GREY + "   ");
        for (char file = (perspective == ChessGame.TeamColor.WHITE? 'a' : 'h');
             (perspective == ChessGame.TeamColor.WHITE ? file <= 'h' : file >='a');
             file += perspective == ChessGame.TeamColor.WHITE? 1 : (char) -1){
            System.out.print("  " + file + "  ");
        }
        System.out.println(RESET_BG_COLOR);
    }

    private static String getSymbol(ChessPiece piece){
        if (piece == null) {
            return EMPTY;
        }
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
