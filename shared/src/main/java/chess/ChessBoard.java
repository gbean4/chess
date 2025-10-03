package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] squares = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()-1][position.getColumn()-1] = piece;
    }
    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()-1][position.getColumn()-1];
    }

    public ChessPosition getPieceLocation(ChessGame.TeamColor color, ChessPiece piece){
        for (int row = 1; row<9; row++){
            for (int col = 1; col< 9; col++){
                ChessPosition pos = new ChessPosition(row, col);
                if (getPiece(pos) == piece && getPiece(pos).getTeamColor() == color){
                    return pos;
                }
            }
        }
        return null;
    }

    public void clearBoard(){
        for (int row = 0; row<8; row++){
            for (int col = 0; col< 8; col++){
                 squares[row][col] = null;
            }
        }
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */

    public void resetBoard() {
        clearBoard();
        //add pawns
        for (int i = 1; i<9; i++){
            ChessPosition whitePosition = new ChessPosition(2, i);
            ChessPosition blackPosition = new ChessPosition(7, i);
            addPiece(whitePosition,new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(blackPosition,new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
        //add rooks
        for (int i : new int[] {1,8}){
            ChessPosition whitePosition = new ChessPosition(1, i);
            ChessPosition blackPosition = new ChessPosition(8, i);
            addPiece(whitePosition,new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
            addPiece(blackPosition,new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        }
        //add knights
        for (int i : new int[] {2,7}){
            ChessPosition whitePosition = new ChessPosition(1, i);
            ChessPosition blackPosition = new ChessPosition(8, i);
            addPiece(whitePosition,new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
            addPiece(blackPosition,new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        }
        //add bishops
        for (int i : new int[] {3,6}){
            ChessPosition whitePosition = new ChessPosition(1, i);
            ChessPosition blackPosition = new ChessPosition(8, i);
            addPiece(whitePosition,new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
            addPiece(blackPosition,new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        }
        //add queens
        addPiece(new ChessPosition(1, 4),new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8, 4),new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));

        //add kings
        addPiece(new ChessPosition(1, 5),new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8, 5),new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
    }

    public boolean isEmpty(ChessPosition position){
        return (squares[position.getRow()-1][position.getColumn()-1] == null);
    }

    public boolean inBounds(ChessPosition position){
        int row = position.getRow();
        int col = position.getColumn();
        return row>=1 && row<=8 && col>=1 && col<=8;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessBoard that)) {
            return false;
        }
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }
}
