package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard implements Cloneable {

    private final ChessPiece[][] board;
    private List<ChessMove> moveHistory = new ArrayList<>();

    public ChessBoard() {
        board = new ChessPiece[8][8];
    }
    public ChessBoard(ChessBoard copy) {
        board = new ChessPiece[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = copy.board[row][col];
                board[row][col] = (piece == null) ? null : new ChessPiece(piece);
            }
        }
    }


    public void setBoard(ChessPiece[][] board) {
        for (int row = 0; row <= 7; row++) {
            System.arraycopy(board[row], 1, this.board[row], 1, 7);
        }
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

    public void addMove(ChessMove move) {
        moveHistory.add(move);
    }

    public List<ChessMove> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    public ChessMove getLastMove() {
        if (moveHistory.isEmpty()) {
            return null;
        }
        return moveHistory.getLast();
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
        for (ChessPiece[] row : board) {
            Arrays.fill(row, null);
        }
    }

    public ChessPosition findKing(ChessGame.TeamColor color) {
        for (int row = 1; row <= board.length; row++) {
            for (int col = 1; col <= board.length; col++) {
                ChessPiece piece = getPiece(new ChessPosition(row,col));
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == color) {
                    return new ChessPosition(row,col);
                }
            }
        }
        throw new RuntimeException("The King has gone missing");
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
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }
    @Override
    protected ChessBoard clone() {
        return new ChessBoard(this);
    }
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }
}
