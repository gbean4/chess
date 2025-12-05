package ui;

import chess.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class ChessBoardUI {

    public static void renderBoard(ChessGame game, ChessGame.TeamColor perspective, ChessPosition highlightPos, Set<ChessPosition> highlightSquares){
        ChessBoard board = game.getBoard();

        if (perspective == null){
            perspective = ChessGame.TeamColor.WHITE;
        }
        int rowStart = (perspective == ChessGame.TeamColor.WHITE? 7: 0);
        int rowEnd = (perspective == ChessGame.TeamColor.WHITE? -1: 8);
        int rowStep = (perspective == ChessGame.TeamColor.WHITE? -1: 1);

        int colStart = (perspective == ChessGame.TeamColor.WHITE? 1: 8);
        int colEnd = (perspective == ChessGame.TeamColor.WHITE? 9: 0);
        int colStep = (perspective == ChessGame.TeamColor.WHITE? 1: -1);

        boardLabels(perspective);


        for (int row = rowStart; row != rowEnd; row += rowStep) {
            int rank = row + 1;
            System.out.print(RESET_TEXT_COLOR+ SET_BG_COLOR_LIGHT_GREY + " " + rank + " " + RESET_BG_COLOR);

            for (int col = colStart; col != colEnd; col += colStep) {
                String bg;
                String color = RESET_TEXT_COLOR;
                String symbol = EMPTY;

                boolean isDark = ((row + col) % 2 == 0);
                bg = isDark ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;

                ChessPosition pos = new ChessPosition(rank, col);

                if (highlightPos != null && pos.equals(highlightPos)){
                    bg = SET_BG_COLOR_YELLOW;
                }

                if (highlightSquares != null && highlightSquares.contains(pos) && isDark){
                    bg = isDark? SET_BG_COLOR_GREEN : SET_BG_COLOR_DARK_GREEN;
                }
                ChessPiece piece = board.getPiece(pos);
                if (piece != null) {
                    color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                            ? SET_TEXT_COLOR_MAGENTA
                            : SET_TEXT_COLOR_RED;
                    symbol = getSymbol(piece);
                }
                System.out.print(bg + color + " " + symbol + " " + RESET_BG_COLOR+ RESET_TEXT_COLOR);
            }
            System.out.println(SET_BG_COLOR_LIGHT_GREY + " " + rank + " " + RESET_BG_COLOR);
        }
        boardLabels(perspective);
    }

    public static Set<ChessPosition> getHighlightSquares(ChessGame game, ChessPosition pos) {
        Set<ChessPosition> highlights = new HashSet<>();
        if (pos == null){
            return highlights;
        }
        try{
            Collection<ChessMove> moves = game.validMoves(pos);
            for (ChessMove move: moves){
                highlights.add(move.getEndPosition());
            }
        } catch (Exception ignored){}
        return highlights;
    }

    private static void boardLabels(ChessGame.TeamColor perspective) {
        System.out.print(RESET_TEXT_COLOR+SET_BG_COLOR_LIGHT_GREY + "   ");
        for (char file = (perspective == ChessGame.TeamColor.WHITE? 'a' : 'h');
             (perspective == ChessGame.TeamColor.WHITE ? file <= 'h' : file >='a');
             file += perspective == ChessGame.TeamColor.WHITE? 1 : (char) -1){
            System.out.print("  " + file + "   ");
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
