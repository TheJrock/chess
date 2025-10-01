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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    public ChessBoard copy() {
        ChessBoard copy = new ChessBoard();
        for (int row = 1; row <= board.length; row++) {
            for (int col = 1; col <= board.length; col++) {
                ChessPosition position = new ChessPosition(row, col);
                copy.addPiece(position, getPiece(position));
            }
        }
        return copy;
    }

    private final ChessPiece[][] board = new ChessPiece[8][8];
    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        clearBoard();

        ChessPiece.PieceType[] backRank = {
                ChessPiece.PieceType.ROOK,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.QUEEN,
                ChessPiece.PieceType.KING,
                ChessPiece.PieceType.BISHOP,
                ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.ROOK
        };

        for (int column = 1; column <= 8; column++) {
            //Add White Pieces
            addPiece(new ChessPosition(1, column), new ChessPiece(ChessGame.TeamColor.WHITE, backRank[column-1]));
            addPiece(new ChessPosition(2, column), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            //Add Black Pieces
            addPiece(new ChessPosition(8, column), new ChessPiece(ChessGame.TeamColor.BLACK, backRank[column-1]));
            addPiece(new ChessPosition(7, column), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }

    private void clearBoard() {
        for (ChessPiece[] chessPieces : board) {
            Arrays.fill(chessPieces, null);
        }
    }

    @Override
    public String toString() {
        StringBuilder stringRep = new StringBuilder();
        for (int row = 8; row >= 1; row--) {
            stringRep.append(row);
            stringRep.append(" |");
            for (int column = 1; column <= 8; column++) {
                ChessPiece piece = getPiece(new ChessPosition(row, column));
                if (piece != null) {
                    stringRep.append(piece);
                } else {
                    stringRep.append(" ");
                }
                stringRep.append("|");
            }
            stringRep.append("\n");
        }
        stringRep.append("   a b c d e f g h");
        return stringRep.toString();
    }
}
