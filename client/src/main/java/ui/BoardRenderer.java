package ui;

import chess.*;

public class BoardRenderer {

    private static final String LIGHT = "\u001B[47m";
    private static final String DARK  = "\u001B[46m";
    private static final String RESET = "\u001B[0m";

    public static String render(ChessBoard board, ChessGame.TeamColor perspective) {
        StringBuilder sb = new StringBuilder();

        int startRow = (perspective == ChessGame.TeamColor.WHITE) ? 8 : 1;
        int endRow   = (perspective == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int step     = (perspective == ChessGame.TeamColor.WHITE) ? -1 : 1;

        sb.append("   ");
        if (perspective == ChessGame.TeamColor.WHITE) {
            sb.append(" a  b  c  d  e  f  g  h\n");
        } else {
            sb.append(" h  g  f  e  d  c  b  a\n");
        }

        for (int r = startRow; r != endRow + step; r += step) {
            sb.append(" ").append(r).append(" ");
            for (int c = 1; c <= 8; c++) {
                int file = (perspective == ChessGame.TeamColor.WHITE) ? c : (9 - c);

                ChessPosition pos = new ChessPosition(r, file);
                ChessPiece piece = board.getPiece(pos);
                boolean isLight = ((r + file) % 2 == 0);

                sb.append(isLight ? LIGHT : DARK);

                if (piece == null) {
                    sb.append("   ");
                } else {
                    String letter = piece.getPieceType().toString().substring(0,1);
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        letter = letter.toUpperCase();
                    } else {
                        letter = letter.toLowerCase();
                    }
                    sb.append(" ").append(letter).append(" ");
                }

                sb.append(RESET);
            }
            sb.append(" ").append(r).append("\n");
        }

        sb.append("   ");
        if (perspective == ChessGame.TeamColor.WHITE) {
            sb.append(" a  b  c  d  e  f  g  h\n");
        } else {
            sb.append(" h  g  f  e  d  c  b  a\n");
        }

        return sb.toString();
    }

    public static String renderInitial(boolean white) {
        return (white) ? render(new ChessBoard(), ChessGame.TeamColor.WHITE) : render(new ChessBoard(), ChessGame.TeamColor.BLACK);
    }
}
