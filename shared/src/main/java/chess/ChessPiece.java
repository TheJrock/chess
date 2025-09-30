package chess;

import java.util.ArrayList;
import java.util.Collection;
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

    private static final int[][] KNIGHT_DIRECTIONS = {
            {2,1}, //Up and right
            {2,-1}, //Up and left
            {1,-2}, //Left and up
            {-1,-2}, //Left and down
            {-2,-1}, //Down and left
            {-2,1}, //Down and right
            {-1,2}, //Right and down
            {1,2} //Right and up
    };
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

    @Override
    public String toString() {
        String letterRep = switch (type) {
            case ROOK -> "R";
            case KNIGHT -> "N";
            case BISHOP -> "B";
            case QUEEN -> "Q";
            case KING -> "K";
            case PAWN -> "P";
        };
        return (pieceColor == ChessGame.TeamColor.WHITE ? letterRep : letterRep.toLowerCase());
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
        Collection<ChessMove> moves = new ArrayList<>();

        if (type != PieceType.PAWN) { //Logic for all pieces except pawns
            int[][] directions = switch (type) {
                case KING -> ALL_DIRECTIONS;
                case QUEEN -> ALL_DIRECTIONS;
                case BISHOP -> DIAGONAL_DIRECTIONS;
                case KNIGHT -> KNIGHT_DIRECTIONS;
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
                    if (type == PieceType.KING || type == PieceType.KNIGHT) {
                        break;
                    }
                }
            }
        } else { //Logic for Pawns only
            pawnMoves(board, myPosition, moves);
        }
        return moves;
    }

    private void pawnMoves(ChessBoard board, ChessPosition position, Collection<ChessMove> moves) {

        int direction = (pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promotionRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int enPassantRow = (pieceColor == ChessGame.TeamColor.WHITE) ? 5 : 4;

        int row = position.getRow();
        int col = position.getColumn();

        ChessPosition oneForward = new ChessPosition(row+direction, col);
        if (isOnBoard(oneForward) && board.getPiece(oneForward) == null) {
            addPawnMove(position, oneForward, promotionRow, moves);

            if (row == startRow) {
                ChessPosition twoForward = new ChessPosition(row + 2*direction, col);
                if (isOnBoard(twoForward) && board.getPiece(twoForward) == null) {
                    moves.add(new ChessMove(position, twoForward, null));
                }
            }
        }

        int[] captureColumns = {col-1, col+1};
        for (int column : captureColumns) {
            ChessPosition capturePosition = new ChessPosition(row+direction, column);
            if (isOnBoard(capturePosition)) {
                ChessPiece targetPiece = board.getPiece(capturePosition);
                if (targetPiece != null && targetPiece.getTeamColor() != pieceColor) {
                    addPawnMove(position, capturePosition, promotionRow, moves);
//                } else if (row == enPassantRow && targetPiece == null) {
//                    if ()
                }
            }
        }

//        int forward;
//        int initialRow;
//        int promotionRow;
//        if (pieceColor == ChessGame.TeamColor.BLACK) {
//            forward = -1;
//            initialRow = 7;
//            promotionRow = 1;
//        } else {
//            forward = 1;
//            initialRow = 2;
//            promotionRow = 8;
//        }
//
//        if (position.getRow() == promotionRow) {
//            return;
//        }
//
//        PieceType promotionPiece = null;
//        if (position.getRow()+forward == promotionRow) {
//            promotionPiece = PieceType.QUEEN;
//        }
//
//        ChessPosition forwardPosition = new ChessPosition(position.getRow()+forward, position.getColumn());
//        ChessPiece forwardPiece = board.getPiece(forwardPosition);
//        if (forwardPiece == null) {
//            moves.add(new ChessMove(position, forwardPosition, promotionPiece));
//            if (position.getRow() == initialRow) {
//                ChessPosition doubleForward = new ChessPosition(forwardPosition.getRow()+forward, position.getColumn());
//                ChessPiece doubleForwardPiece = board.getPiece(doubleForward);
//                if (doubleForwardPiece == null) {
//                    moves.add(new ChessMove(position, doubleForward, promotionPiece));
//                }
//            }
//        }
//
//        ChessPosition captureLeft = new ChessPosition(position.getRow()+forward, position.getColumn()-1);
//        ChessPosition captureRight = new ChessPosition(position.getRow()+forward, position.getColumn()+1);
//        ChessPiece leftPiece = board.getPiece(captureLeft);
//        ChessPiece rightPiece = board.getPiece(captureRight);
//        if (leftPiece != null && position.getColumn() != 1){
//            if (leftPiece.getTeamColor() != pieceColor) {
//                moves.add(new ChessMove(position, captureLeft, promotionPiece));
//            }
//        }
//        if (rightPiece != null && position.getColumn() != 8) {
//            if (rightPiece.getTeamColor() != pieceColor) {
//                moves.add(new ChessMove(position, captureRight, promotionPiece));
//            }
//        }
    }

    private boolean isOnBoard(ChessPosition position) {
        return position.getRow() >= 1 && position.getRow() <= 8 && position.getColumn() >= 1 && position.getColumn() <= 8;
    }

    private void addPawnMove(ChessPosition from, ChessPosition to, int promotionRow, Collection<ChessMove> moves) {
        if (to.getRow() == promotionRow) {
            moves.add(new ChessMove(from, to, PieceType.QUEEN));
            moves.add(new ChessMove(from, to, PieceType.ROOK));
            moves.add(new ChessMove(from, to, PieceType.BISHOP));
            moves.add(new ChessMove(from, to, PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}
