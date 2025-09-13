package chess;
import javax.swing.text.Position;
import java.util.HashSet;

public class MoveGenerator {
    private final ChessBoard board;

    public MoveGenerator(ChessBoard board){
        this.board = board;
    }

    public HashSet<ChessMove> getBishopMoves(ChessPosition myPosition){
        int[][] directions = {
                {1,1}, {1,-1}, {-1,1}, {-1,-1}
        };
        HashSet<ChessMove> moves = new HashSet<>();
        for (int[] dir : directions){
            int drow = dir[0];
            int dcol = dir[1];
            ChessPosition current = myPosition;
            ChessPosition start = myPosition;

            while(true){
                ChessPosition next = new ChessPosition(current.getRow() + drow, current.getColumn() + dcol);
                current = next;
                if (!board.inBounds(current)) {
                    break;
                } else if (board.isEmpty(current)){
                    moves.add(new ChessMove(start, current, null));
                    continue;
                } else if (board.getPiece(current).getTeamColor() != board.getPiece(start).getTeamColor()) {
                    moves.add(new ChessMove(start, current, null));
                    break;
                } else{ break;}
            }
        }
        return moves;
    }
}
