package chess;
import javax.swing.text.Position;
import java.util.HashSet;

public class MoveGenerator {
    private final ChessBoard board;

    public MoveGenerator(ChessBoard board){
        this.board = board;
    }

    //function works for rook, bishop, and queen
    public HashSet<ChessMove> getSlidingMoves(ChessPosition myPosition, int[][] directions){
        HashSet<ChessMove> moves = new HashSet<>();
        for (int[] dir : directions){
            int drow = dir[0];
            int dcol = dir[1];
            ChessPosition current = myPosition;

            while(true){
                ChessPosition next = new ChessPosition(current.getRow() + drow, current.getColumn() + dcol);
                current = next;
                if (!board.inBounds(current)) {
                    break;
                } else if (board.isEmpty(current)){
                    moves.add(new ChessMove(myPosition, current, null));
                    continue;
                } else if (board.getPiece(current).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
                    moves.add(new ChessMove(myPosition, current, null));
                    break;
                } else{ break;}
            }
        }
        return moves;
    }
//    public HashSet<ChessMove> getRookMoves(ChessPosition myPosition){
//        int[][] directions = {
//                {1,0}, {0,1}, {-1,0}, {0,-1}
//        };
//        HashSet<ChessMove> moves = new HashSet<>();
//        for (int[] dir : directions){
//            int drow = dir[0];
//            int dcol = dir[1];
//            ChessPosition current = myPosition;
//
//            while(true){
//                ChessPosition next = new ChessPosition(current.getRow() + drow, current.getColumn() + dcol);
//                current = next;
//                if (!board.inBounds(current)) {
//                    break;
//                } else if (board.isEmpty(current)){
//                    moves.add(new ChessMove(myPosition, current, null));
//                    continue;
//                } else if (board.getPiece(current).getTeamColor() != board.getPiece(myPosition).getTeamColor()) {
//                    moves.add(new ChessMove(myPosition, current, null));
//                    break;
//                } else{ break;}
//            }
//        }
//        return moves;
//    }

}
