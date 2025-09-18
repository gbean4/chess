package chess;
import java.util.HashSet;

import static chess.ChessPiece.PieceType.*;

public record MoveGenerator(ChessBoard board) {

    //function works for rook, bishop, king, knight, and queen
    public HashSet<ChessMove> getSlidingMoves(ChessPosition myPosition, int[][] directions) {
        HashSet<ChessMove> moves = new HashSet<>();
        for (int[] dir : directions) {
            int dRow = dir[0];
            int dCol = dir[1];
            ChessPosition current = myPosition;

            while (true) {
                current = new ChessPosition(current.getRow() + dRow, current.getColumn() + dCol);
                if (!board.inBounds(current)) {
                    break;
                } else if (board.isEmpty(current)) {
                    moves.add(new ChessMove(myPosition, current, null));
                    if (board.getPiece(myPosition).getPieceType() == KING ||
                            board.getPiece(myPosition).getPieceType() == KNIGHT) {
                        break;
                    }
                } else if (board.getPiece(current).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, current, null));
                    break;
                } else {
                    break;
                }
            }
        }
        return moves;
    }

    private void addPawnMoves(HashSet<ChessMove> moves, ChessPosition from, ChessPosition to, ChessGame.TeamColor color) {
        if (color == ChessGame.TeamColor.WHITE && to.getRow() == 8 || color == ChessGame.TeamColor.BLACK && to.getRow() == 1) {
            ChessPiece.PieceType[] promotions = {
                    QUEEN, ROOK, BISHOP, KNIGHT
            };

            for (ChessPiece.PieceType promotion : promotions) {
                moves.add(new ChessMove(from, to, promotion));
            }
        } else {
            moves.add(new ChessMove(from, to, null));
        }

    }

    public HashSet<ChessMove> getPawnMoves(ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();
        ChessPiece pawn = board.getPiece(myPosition);
        //if it's white it moves up the board, black it moves down
        int direction = (pawn.getTeamColor() == ChessGame.TeamColor.WHITE) ? 1 : -1;

        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        //Logic: make variable for next position forward. If it's empty and in bounds, it's added
        // then check if the current position is 2nd row (white) or 7th (black), if yes, check if it can move again
        // then looking diagonally, if there's an enemy piece there, then add it

        ChessPosition oneForward = new ChessPosition(row + direction, col);
        if (board.inBounds(oneForward) && board.isEmpty(oneForward)) {
            addPawnMoves(moves, myPosition, oneForward, pawn.getTeamColor());
            if (pawn.getTeamColor() == ChessGame.TeamColor.WHITE && row == 2 || pawn.getTeamColor() == ChessGame.TeamColor.BLACK && row == 7) {
                ChessPosition twoForward = new ChessPosition(row + 2 * direction, col);
                if (board.inBounds(twoForward) && board.isEmpty(twoForward)) {
                    addPawnMoves(moves, myPosition, twoForward, pawn.getTeamColor());
                }
            }
        }
        int[][] diagonalDir = {{direction, 1}, {direction, -1}};
        for (int[] dir : diagonalDir) {
            ChessPosition diagonal = new ChessPosition(row + dir[0], col + dir[1]);
            if (board.inBounds(diagonal) && !board.isEmpty(diagonal) && board.getPiece(diagonal).getTeamColor() != pawn.getTeamColor()) {
                addPawnMoves(moves, myPosition, diagonal, pawn.getTeamColor());
            }
        }
        return moves;
    }
}
