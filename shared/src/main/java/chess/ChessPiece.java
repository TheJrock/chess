package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    private static final int[][] DIAGONAL_DIRECTIONS = {
            {1,1}, //Up and right
            {1,-1}, //Up and left
            {-1,-1}, //Down and left
            {-1,1} //Down and right
    };
    private static final int[][] STRAIGHT_DIRECTIONS = {
            {1,0}, //Up
            {0,-1}, //Left
            {-1,0}, //Down
            {0,1} //Right
    };
    private static final int[][] ALL_DIRECTIONS;

    static {
        ALL_DIRECTIONS = new int[STRAIGHT_DIRECTIONS.length + DIAGONAL_DIRECTIONS.length][2];
        int i = 0;
        for (int[] direction : STRAIGHT_DIRECTIONS) {
            ALL_DIRECTIONS[i++] = direction;
        }
        for (int[] direction : DIAGONAL_DIRECTIONS) {
            ALL_DIRECTIONS[i++] = direction;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece piece = (ChessPiece) o;
        return pieceColor == piece.pieceColor && type == piece.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
//        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> moves = new ArrayList<>();

        int[][] directions = switch (type) {
            case KING -> ALL_DIRECTIONS;
            case QUEEN -> ALL_DIRECTIONS;
            case BISHOP -> DIAGONAL_DIRECTIONS;
            case ROOK -> STRAIGHT_DIRECTIONS;
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
        for (int[] direction : directions) {
            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            while (true) {
                row += direction[0];
                col += direction[1];

                if (row < 1 || row > 8 || col < 1 || col > 8) {
                    break;
                }

                ChessPosition newPosition = new ChessPosition(row, col);
                ChessPiece targetPiece = board.getPiece(newPosition);

                if (targetPiece != null) {
                    if (targetPiece.getTeamColor() != pieceColor) {
                        moves.add(new ChessMove(myPosition, newPosition, null));
                    }
                    break;
                }

                moves.add(new ChessMove(myPosition, newPosition, null));
                if (type == PieceType.KING) {
                    break;
                }
            }
        }
        return moves;
    }
}
