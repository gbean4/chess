package chess;

import java.util.*;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn;
    private ChessBoard squares;

    public ChessGame() {
        squares = new ChessBoard();
        currentTurn = TeamColor.WHITE;

    }

    private ChessBoard deepCopy(ChessBoard original){
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row<9; row++){
            for (int col = 1; col< 9; col++){
                copy.addPiece(new ChessPosition(row, col), original.getPiece(new ChessPosition(row,col)));
            }
        }
        return copy;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        if (squares.isEmpty(startPosition)){
            return null;
        }
        Collection <ChessMove> rawMoves =  (squares.getPiece(startPosition).pieceMoves(squares, startPosition));
        HashSet<ChessMove> legalMoves = new HashSet<>();
        ChessBoard savedCopy = deepCopy(squares);

        for (ChessMove move: rawMoves){
            applyMove(move, squares);
            if (!isInCheck(currentTurn)){
                legalMoves.add(move);
            }
        }

        setBoard(savedCopy);
//        if (legalMoves.isEmpty()){
//            return null;
//        }
        return legalMoves;
    }

    public void applyMove(ChessMove move, ChessBoard board){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);
        board.addPiece(start, null);
        board.addPiece(end, piece);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (isInStalemate(currentTurn)) {
            throw new InvalidMoveException();
        }
        ChessPosition start = move.getStartPosition();
        Collection<ChessMove> legalMoves = validMoves(start);
        if (!legalMoves.contains(move)){
            throw new InvalidMoveException();
        }

        // make move now that it's good
        applyMove(move, squares);

        //passed and move made, now i can change color
        if (currentTurn == TeamColor.WHITE){
            currentTurn = TeamColor.BLACK;
        } else {
            currentTurn = TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = squares.getKingLocation(teamColor);
        for (int row = 1; row<9; row++){
            for (int col = 1; col< 9; col++){
                ChessPosition opPos = new ChessPosition(row, col);
                if (!squares.isEmpty(opPos) && squares.getPiece(opPos).getTeamColor()!=teamColor) {
                    Collection<ChessMove> opMoves = squares.getPiece(opPos).pieceMoves(squares, opPos);
                    for (ChessMove opMove : opMoves){
                        if (opMove.getEndPosition().equals(kingPos)){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)){
            return false;
        }
        for (int row = 1; row<9; row++){
            for (int col = 1; col< 9; col++){
                ChessPosition opPos = new ChessPosition(row, col);
                if (!squares.isEmpty(opPos) && squares.getPiece(opPos).getTeamColor()==teamColor) {
                    if(!validMoves(opPos).isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(currentTurn)){
            return false;
        }
        for (int row = 1; row<9; row++){
            for (int col = 1; col< 9; col++){
                ChessPosition opPos = new ChessPosition(row, col);
                if (!squares.isEmpty(opPos) && squares.getPiece(opPos).getTeamColor()==teamColor) {
                    if(!validMoves(opPos).isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        for (int row = 1; row<9; row++){
            for (int col = 1; col< 9; col++){
                squares.addPiece(new ChessPosition(row, col), board.getPiece(new ChessPosition(row,col)));
            }
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.squares;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return currentTurn == chessGame.currentTurn && Objects.equals(squares, chessGame.squares);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, squares);
    }
}
