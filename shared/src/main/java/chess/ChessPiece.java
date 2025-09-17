package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

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
        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> moves = new ArrayList<>();
        if (piece.getPieceType() == PieceType.BISHOP) {
            int new_row = myPosition.getRow();
            int new_col = myPosition.getColumn();
            while (new_row < 8 && new_col < 8) {
                moves.add(new ChessMove(new ChessPosition(myPosition.getRow(), myPosition.getColumn()), new ChessPosition(++new_row, ++new_col), null));
            }
            new_row = myPosition.getRow();
            new_col = myPosition.getColumn();
            while (new_row < 8 && new_col > 1) {
                moves.add(new ChessMove(new ChessPosition(myPosition.getRow(), myPosition.getColumn()), new ChessPosition(++new_row, --new_col), null));
            }
            new_row = myPosition.getRow();
            new_col = myPosition.getColumn();
            while (new_row > 1 && new_col > 1) {
                moves.add(new ChessMove(new ChessPosition(myPosition.getRow(), myPosition.getColumn()), new ChessPosition(--new_row, --new_col), null));
            }
            new_row = myPosition.getRow();
            new_col = myPosition.getColumn();
            while (new_row > 1 && new_col < 8) {
                moves.add(new ChessMove(new ChessPosition(myPosition.getRow(), myPosition.getColumn()), new ChessPosition(--new_row, ++new_col), null));
            }
        }
        return moves;
    }
}
