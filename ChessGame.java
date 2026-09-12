import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChessGame extends JFrame {

    // ==============================
    // ENUMS
    // ==============================

    enum PlayerColor {
        WHITE, BLACK
    }

    enum Type {
        KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN
    }

    // ==============================
    // PIECE CLASS
    // ==============================

    static class Piece {

        Type type;
        PlayerColor color;
        boolean moved;

        Piece(Type type, PlayerColor color) {
            this.type = type;
            this.color = color;
            this.moved = false;
        }

        Piece copy() {
            Piece p = new Piece(type, color);
            p.moved = moved;
            return p;
        }

        String symbol() {

            // White pieces
            if (color == PlayerColor.WHITE) {

                switch (type) {

                    case KING:
                        return "K";

                    case QUEEN:
                        return "Q";

                    case ROOK:
                        return "R";

                    case BISHOP:
                        return "B";

                    case KNIGHT:
                        return "N";

                    case PAWN:
                        return "P";
                }
            }

            // Black pieces
            else {

                switch (type) {

                    case KING:
                        return "k";

                    case QUEEN:
                        return "q";

                    case ROOK:
                        return "r";

                    case BISHOP:
                        return "b";

                    case KNIGHT:
                        return "n";

                    case PAWN:
                        return "p";
                }
            }

            return "";
        }
    }

    // ==============================
    // SNAPSHOT FOR UNDO
    // ==============================

    static class Snapshot {

        Piece[][] board = new Piece[8][8];

        PlayerColor turn;

        int enPassantRow;
        int enPassantCol;

        Snapshot(
                Piece[][] source,
                PlayerColor turn,
                int enPassantRow,
                int enPassantCol) {

            for (int row = 0; row < 8; row++) {

                for (int col = 0; col < 8; col++) {

                    if (source[row][col] == null) {

                        board[row][col] = null;

                    } else {

                        board[row][col] =
                                source[row][col].copy();
                    }
                }
            }

            this.turn = turn;
            this.enPassantRow = enPassantRow;
            this.enPassantCol = enPassantCol;
        }
    }

    // ==============================
    // VARIABLES
    // ==============================

    private Piece[][] board =
            new Piece[8][8];

    private PlayerColor turn =
            PlayerColor.WHITE;

    private int enPassantRow = -1;
    private int enPassantCol = -1;

    private JButton[][] squares =
            new JButton[8][8];

    private JLabel status =
            new JLabel();

    private JTextArea historyArea =
            new JTextArea();

    private List<String> moves =
            new ArrayList<>();

    private List<Snapshot> history =
            new ArrayList<>();

    private int selectedRow = -1;
    private int selectedCol = -1;

    private boolean gameOver = false;

    // ==============================
    // CONSTRUCTOR
    // ==============================

    public ChessGame() {

        setTitle("Java Chess Game");

        setSize(900, 700);

        setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        setLayout(
                new BorderLayout(10, 10)
        );

        createBoard();
        createSidePanel();

        newGame();

        setLocationRelativeTo(null);

        setVisible(true);
    }

    // ==============================
    // CREATE BOARD
    // ==============================

    private void createBoard() {

        JPanel boardPanel =
                new JPanel(
                        new GridLayout(8, 8)
                );

        for (int row = 0; row < 8; row++) {

            for (int col = 0; col < 8; col++) {

                JButton button =
                        new JButton();

                // Normal font - no special chess font
                button.setFont(
                        new Font(
                                "Arial",
                                Font.BOLD,
                                38
                        )
                );

                button.setFocusPainted(false);

                final int r = row;
                final int c = col;

                button.addActionListener(
                        e -> squareClicked(r, c)
                );

                squares[row][col] =
                        button;

                boardPanel.add(button);
            }
        }

        add(
                boardPanel,
                BorderLayout.CENTER
        );
    }

    // ==============================
    // SIDE PANEL
    // ==============================

    private void createSidePanel() {

        JPanel sidePanel =
                new JPanel(
                        new BorderLayout(5, 5)
                );

        sidePanel.setPreferredSize(
                new Dimension(250, 0)
        );

        status.setFont(
                new Font(
                        "Arial",
                        Font.BOLD,
                        18
                )
        );

        status.setHorizontalAlignment(
                SwingConstants.CENTER
        );

        sidePanel.add(
                status,
                BorderLayout.NORTH
        );

        historyArea.setEditable(false);

        historyArea.setFont(
                new Font(
                        "Arial",
                        Font.PLAIN,
                        14
                )
        );

        sidePanel.add(
                new JScrollPane(historyArea),
                BorderLayout.CENTER
        );

        JPanel buttonPanel =
                new JPanel(
                        new GridLayout(2, 1, 5, 5)
                );

        JButton undoButton =
                new JButton("Undo");

        JButton newGameButton =
                new JButton("New Game");

        undoButton.addActionListener(
                e -> undoMove()
        );

        newGameButton.addActionListener(
                e -> newGame()
        );

        buttonPanel.add(undoButton);
        buttonPanel.add(newGameButton);

        sidePanel.add(
                buttonPanel,
                BorderLayout.SOUTH
        );

        add(
                sidePanel,
                BorderLayout.EAST
        );
    }

    // ==============================
    // NEW GAME
    // ==============================

    private void newGame() {

        board = new Piece[8][8];

        turn = PlayerColor.WHITE;

        enPassantRow = -1;
        enPassantCol = -1;

        moves.clear();
        history.clear();

        selectedRow = -1;
        selectedCol = -1;

        gameOver = false;

        Type[] backRow = {

                Type.ROOK,
                Type.KNIGHT,
                Type.BISHOP,
                Type.QUEEN,
                Type.KING,
                Type.BISHOP,
                Type.KNIGHT,
                Type.ROOK
        };

        // BLACK

        for (int col = 0; col < 8; col++) {

            board[0][col] =
                    new Piece(
                            backRow[col],
                            PlayerColor.BLACK
                    );

            board[1][col] =
                    new Piece(
                            Type.PAWN,
                            PlayerColor.BLACK
                    );
        }

        // WHITE

        for (int col = 0; col < 8; col++) {

            board[6][col] =
                    new Piece(
                            Type.PAWN,
                            PlayerColor.WHITE
                    );

            board[7][col] =
                    new Piece(
                            backRow[col],
                            PlayerColor.WHITE
                    );
        }

        updateBoard();
    }

    // ==============================
    // CLICK SQUARE
    // ==============================

    private void squareClicked(
            int row,
            int col) {

        if (gameOver) {
            return;
        }

        // Select piece

        if (selectedRow == -1) {

            Piece piece =
                    board[row][col];

            if (piece != null &&
                    piece.color == turn) {

                selectedRow = row;
                selectedCol = col;

                updateBoard();

                squares[row][col]
                        .setBackground(
                                Color.YELLOW
                        );
            }

            return;
        }

        // Click same square

        if (selectedRow == row &&
                selectedCol == col) {

            selectedRow = -1;
            selectedCol = -1;

            updateBoard();

            return;
        }

        Piece movingPiece =
                board[selectedRow][selectedCol];

        Type promotion = null;

        // Pawn promotion

        if (movingPiece.type == Type.PAWN &&
                (row == 0 || row == 7)) {

            String[] choices = {
                    "Queen",
                    "Rook",
                    "Bishop",
                    "Knight"
            };

            int choice =
                    JOptionPane.showOptionDialog(
                            this,
                            "Choose promotion:",
                            "Pawn Promotion",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            choices,
                            choices[0]
                    );

            if (choice < 0) {
                return;
            }

            Type[] types = {

                    Type.QUEEN,
                    Type.ROOK,
                    Type.BISHOP,
                    Type.KNIGHT
            };

            promotion = types[choice];
        }

        // Move

        if (makeMove(
                selectedRow,
                selectedCol,
                row,
                col,
                promotion)) {

            selectedRow = -1;
            selectedCol = -1;

            updateBoard();

            checkGameState();

        } else {

            JOptionPane.showMessageDialog(
                    this,
                    "Invalid move!"
            );

            selectedRow = -1;
            selectedCol = -1;

            updateBoard();
        }
    }

    // ==============================
    // MAKE MOVE
    // ==============================

    private boolean makeMove(
            int fromRow,
            int fromCol,
            int toRow,
            int toCol,
            Type promotion) {

        Piece piece =
                board[fromRow][fromCol];

        if (piece == null) {
            return false;
        }

        if (piece.color != turn) {
            return false;
        }

        if (!legalMove(
                fromRow,
                fromCol,
                toRow,
                toCol)) {

            return false;
        }

        // Save for undo

        history.add(
                new Snapshot(
                        board,
                        turn,
                        enPassantRow,
                        enPassantCol
                )
        );

        Piece captured =
                board[toRow][toCol];

        // En passant

        boolean enPassant =
                piece.type == Type.PAWN &&
                fromCol != toCol &&
                captured == null;

        // Move

        board[toRow][toCol] =
                piece;

        board[fromRow][fromCol] =
                null;

        // Remove captured pawn

        if (enPassant) {

            int capturedRow =
                    toRow +
                            (piece.color ==
                                    PlayerColor.WHITE
                                    ? 1
                                    : -1);

            board[capturedRow][toCol] =
                    null;
        }

        // Castling

        if (piece.type == Type.KING &&
                Math.abs(
                        toCol - fromCol
                ) == 2) {

            int rookFrom =
                    toCol > fromCol
                            ? 7
                            : 0;

            int rookTo =
                    toCol > fromCol
                            ? 5
                            : 3;

            board[toRow][rookTo] =
                    board[toRow][rookFrom];

            board[toRow][rookFrom] =
                    null;

            if (board[toRow][rookTo]
                    != null) {

                board[toRow][rookTo]
                        .moved = true;
            }
        }

        piece.moved = true;

        // Promotion

        if (piece.type == Type.PAWN &&
                (toRow == 0 ||
                        toRow == 7)) {

            if (promotion == null) {
                piece.type = Type.QUEEN;
            } else {
                piece.type = promotion;
            }
        }

        // En passant target

        enPassantRow = -1;
        enPassantCol = -1;

        if (piece.type == Type.PAWN &&
                Math.abs(
                        toRow - fromRow
                ) == 2) {

            enPassantRow =
                    (fromRow + toRow) / 2;

            enPassantCol =
                    fromCol;
        }

        // Save move notation

        String notation =
                squareName(
                        fromRow,
                        fromCol
                )
                        + " -> "
                        + squareName(
                        toRow,
                        toCol
                );

        if (captured != null ||
                enPassant) {

            notation += " (capture)";
        }

        moves.add(notation);

        // Change turn

        turn = opposite(turn);

        return true;
    }

    // ==============================
    // LEGAL MOVE
    // ==============================

    private boolean legalMove(
            int fromRow,
            int fromCol,
            int toRow,
            int toCol) {

        if (!inBoard(
                fromRow,
                fromCol
        ) ||
                !inBoard(
                        toRow,
                        toCol
                )) {

            return false;
        }

        Piece piece =
                board[fromRow][fromCol];

        Piece target =
                board[toRow][toCol];

        if (piece == null) {
            return false;
        }

        if (piece.color != turn) {
            return false;
        }

        if (target != null &&
                target.color == piece.color) {

            return false;
        }

        if (!patternMove(
                fromRow,
                fromCol,
                toRow,
                toCol)) {

            return false;
        }

        // King cannot castle through check

        if (piece.type == Type.KING &&
                Math.abs(
                        toCol - fromCol
                ) == 2) {

            int step =
                    toCol > fromCol
                            ? 1
                            : -1;

            if (isSquareAttacked(
                    fromRow,
                    fromCol,
                    opposite(piece.color)
            )) {

                return false;
            }

            if (isSquareAttacked(
                    fromRow,
                    fromCol + step,
                    opposite(piece.color)
            )) {

                return false;
            }
        }

        // Simulate move

        Piece[][] copy =
                copyBoard();

        Piece moving =
                copy[fromRow][fromCol];

        copy[toRow][toCol] =
                moving;

        copy[fromRow][fromCol] =
                null;

        // En passant simulation

        if (piece.type == Type.PAWN &&
                fromCol != toCol &&
                target == null) {

            int capturedRow =
                    toRow +
                            (piece.color ==
                                    PlayerColor.WHITE
                                    ? 1
                                    : -1);

            copy[capturedRow][toCol] =
                    null;
        }

        // Castling simulation

        if (piece.type == Type.KING &&
                Math.abs(
                        toCol - fromCol
                ) == 2) {

            int rookFrom =
                    toCol > fromCol
                            ? 7
                            : 0;

            int rookTo =
                    toCol > fromCol
                            ? 5
                            : 3;

            copy[toRow][rookTo] =
                    copy[toRow][rookFrom];

            copy[toRow][rookFrom] =
                    null;
        }

        // Own king cannot remain in check

        return !isInCheck(
                copy,
                piece.color
        );
    }

    // ==============================
    // MOVEMENT RULES
    // ==============================

    private boolean patternMove(
            int fromRow,
            int fromCol,
            int toRow,
            int toCol) {

        Piece piece =
                board[fromRow][fromCol];

        Piece target =
                board[toRow][toCol];

        int rowDiff =
                toRow - fromRow;

        int colDiff =
                toCol - fromCol;

        int absRow =
                Math.abs(rowDiff);

        int absCol =
                Math.abs(colDiff);

        switch (piece.type) {

            case PAWN:

                int direction =
                        piece.color ==
                                PlayerColor.WHITE
                                ? -1
                                : 1;

                int startRow =
                        piece.color ==
                                PlayerColor.WHITE
                                ? 6
                                : 1;

                // One step

                if (colDiff == 0 &&
                        rowDiff == direction &&
                        target == null) {

                    return true;
                }

                // Two steps

                if (colDiff == 0 &&
                        rowDiff ==
                                2 * direction &&
                        fromRow == startRow &&
                        target == null &&
                        board[
                                fromRow + direction
                                ][fromCol] == null) {

                    return true;
                }

                // Capture

                if (absCol == 1 &&
                        rowDiff == direction &&
                        target != null &&
                        target.color !=
                                piece.color) {

                    return true;
                }

                // En passant

                if (absCol == 1 &&
                        rowDiff == direction &&
                        target == null &&
                        toRow == enPassantRow &&
                        toCol == enPassantCol) {

                    Piece beside =
                            board[fromRow][toCol];

                    return beside != null &&
                            beside.type == Type.PAWN &&
                            beside.color != piece.color;
                }

                return false;

            case KNIGHT:

                return
                        (absRow == 2 &&
                                absCol == 1)
                                ||
                        (absRow == 1 &&
                                absCol == 2);

            case BISHOP:

                return absRow == absCol &&
                        pathClear(
                                fromRow,
                                fromCol,
                                toRow,
                                toCol
                        );

            case ROOK:

                return
                        (rowDiff == 0 ||
                                colDiff == 0)
                                &&
                        pathClear(
                                fromRow,
                                fromCol,
                                toRow,
                                toCol
                        );

            case QUEEN:

                return
                        (rowDiff == 0 ||
                                colDiff == 0 ||
                                absRow == absCol)
                                &&
                        pathClear(
                                fromRow,
                                fromCol,
                                toRow,
                                toCol
                        );

            case KING:

                // Normal move

                if (absRow <= 1 &&
                        absCol <= 1) {

                    return true;
                }

                // Castling

                if (rowDiff == 0 &&
                        absCol == 2 &&
                        !piece.moved) {

                    int rookCol =
                            toCol > fromCol
                                    ? 7
                                    : 0;

                    Piece rook =
                            board[fromRow][rookCol];

                    if (rook == null ||
                            rook.type != Type.ROOK ||
                            rook.color != piece.color ||
                            rook.moved) {

                        return false;
                    }

                    int step =
                            toCol > fromCol
                                    ? 1
                                    : -1;

                    return
                            board[fromRow]
                                    [fromCol + step]
                                    == null
                                    &&
                            board[fromRow]
                                    [fromCol + 2 * step]
                                    == null;
                }

                return false;
        }

        return false;
    }

    // ==============================
    // PATH CLEAR
    // ==============================

    private boolean pathClear(
            int fromRow,
            int fromCol,
            int toRow,
            int toCol) {

        int rowStep =
                Integer.compare(
                        toRow,
                        fromRow
                );

        int colStep =
                Integer.compare(
                        toCol,
                        fromCol
                );

        int row =
                fromRow + rowStep;

        int col =
                fromCol + colStep;

        while (row != toRow ||
                col != toCol) {

            if (board[row][col] != null) {
                return false;
            }

            row += rowStep;
            col += colStep;
        }

        return true;
    }

    // ==============================
    // CHECK
    // ==============================

    private boolean isInCheck(
            Piece[][] position,
            PlayerColor color) {

        int kingRow = -1;
        int kingCol = -1;

        for (int row = 0; row < 8; row++) {

            for (int col = 0; col < 8; col++) {

                Piece piece =
                        position[row][col];

                if (piece != null &&
                        piece.type == Type.KING &&
                        piece.color == color) {

                    kingRow = row;
                    kingCol = col;
                }
            }
        }

        if (kingRow == -1) {
            return true;
        }

        return isSquareAttacked(
                position,
                kingRow,
                kingCol,
                opposite(color)
        );
    }

    // ==============================
    // SQUARE ATTACKED
    // ==============================

    private boolean isSquareAttacked(
            int row,
            int col,
            PlayerColor byColor) {

        return isSquareAttacked(
                board,
                row,
                col,
                byColor
        );
    }

    private boolean isSquareAttacked(
            Piece[][] position,
            int row,
            int col,
            PlayerColor byColor) {

        for (int r = 0; r < 8; r++) {

            for (int c = 0; c < 8; c++) {

                Piece piece =
                        position[r][c];

                if (piece == null ||
                        piece.color != byColor) {

                    continue;
                }

                int rowDiff =
                        row - r;

                int colDiff =
                        col - c;

                int absRow =
                        Math.abs(rowDiff);

                int absCol =
                        Math.abs(colDiff);

                switch (piece.type) {

                    case PAWN:

                        int direction =
                                byColor ==
                                        PlayerColor.WHITE
                                        ? -1
                                        : 1;

                        if (rowDiff == direction &&
                                absCol == 1) {

                            return true;
                        }

                        break;

                    case KNIGHT:

                        if (
                                (absRow == 2 &&
                                        absCol == 1)
                                        ||
                                (absRow == 1 &&
                                        absCol == 2)
                        ) {

                            return true;
                        }

                        break;

                    case KING:

                        if (absRow <= 1 &&
                                absCol <= 1) {

                            return true;
                        }

                        break;

                    case BISHOP:

                        if (absRow == absCol &&
                                clearOn(
                                        position,
                                        r,
                                        c,
                                        row,
                                        col
                                )) {

                            return true;
                        }

                        break;

                    case ROOK:

                        if (
                                (rowDiff == 0 ||
                                        colDiff == 0)
                                        &&
                                clearOn(
                                        position,
                                        r,
                                        c,
                                        row,
                                        col
                                )
                        ) {

                            return true;
                        }

                        break;

                    case QUEEN:

                        if (
                                (
                                        rowDiff == 0 ||
                                        colDiff == 0 ||
                                        absRow == absCol
                                )
                                        &&
                                clearOn(
                                        position,
                                        r,
                                        c,
                                        row,
                                        col
                                )
                        ) {

                            return true;
                        }

                        break;
                }
            }
        }

        return false;
    }

    // ==============================
    // CLEAR ON COPY
    // ==============================

    private boolean clearOn(
            Piece[][] position,
            int fromRow,
            int fromCol,
            int toRow,
            int toCol) {

        int rowStep =
                Integer.compare(
                        toRow,
                        fromRow
                );

        int colStep =
                Integer.compare(
                        toCol,
                        fromCol
                );

        int row =
                fromRow + rowStep;

        int col =
                fromCol + colStep;

        while (row != toRow ||
                col != toCol) {

            if (position[row][col] != null) {
                return false;
            }

            row += rowStep;
            col += colStep;
        }

        return true;
    }

    // ==============================
    // CHECKMATE / STALEMATE
    // ==============================

    private void checkGameState() {

        boolean check =
                isInCheck(
                        board,
                        turn
                );

        boolean hasMove =
                hasLegalMove(turn);

        if (!hasMove) {

            gameOver = true;

            if (check) {

                PlayerColor winner =
                        opposite(turn);

                status.setText(
                        winner +
                                " WINS - CHECKMATE!"
                );

                JOptionPane.showMessageDialog(
                        this,
                        winner +
                                " wins by checkmate!"
                );

            } else {

                status.setText(
                        "DRAW - STALEMATE"
                );

                JOptionPane.showMessageDialog(
                        this,
                        "Draw by stalemate!"
                );
            }

        } else if (check) {

            status.setText(
                    turn +
                            " IS IN CHECK!"
            );

        } else {

            status.setText(
                    turn +
                            "'s Turn"
            );
        }
    }

    // ==============================
    // FIND LEGAL MOVES
    // ==============================

    private boolean hasLegalMove(
            PlayerColor color) {

        PlayerColor oldTurn = turn;

        turn = color;

        for (int fromRow = 0;
             fromRow < 8;
             fromRow++) {

            for (int fromCol = 0;
                 fromCol < 8;
                 fromCol++) {

                Piece piece =
                        board[fromRow][fromCol];

                if (piece == null ||
                        piece.color != color) {

                    continue;
                }

                for (int toRow = 0;
                     toRow < 8;
                     toRow++) {

                    for (int toCol = 0;
                         toCol < 8;
                         toCol++) {

                        if (legalMove(
                                fromRow,
                                fromCol,
                                toRow,
                                toCol
                        )) {

                            turn = oldTurn;

                            return true;
                        }
                    }
                }
            }
        }

        turn = oldTurn;

        return false;
    }

    // ==============================
    // UNDO
    // ==============================

    private void undoMove() {

        if (history.isEmpty()) {
            return;
        }

        Snapshot snapshot =
                history.remove(
                        history.size() - 1
                );

        board =
                snapshot.board;

        turn =
                snapshot.turn;

        enPassantRow =
                snapshot.enPassantRow;

        enPassantCol =
                snapshot.enPassantCol;

        if (!moves.isEmpty()) {

            moves.remove(
                    moves.size() - 1
            );
        }

        selectedRow = -1;
        selectedCol = -1;

        gameOver = false;

        updateBoard();
    }

    // ==============================
    // UPDATE BOARD
    // ==============================

    private void updateBoard() {

        for (int row = 0; row < 8; row++) {

            for (int col = 0; col < 8; col++) {

                JButton button =
                        squares[row][col];

                Piece piece =
                        board[row][col];

                if (piece == null) {

                    button.setText("");

                } else {

                    button.setText(
                            piece.symbol()
                    );
                }

                // Chess colors

                if ((row + col) % 2 == 0) {

                    button.setBackground(
                            new Color(
                                    240,
                                    217,
                                    181
                            )
                    );

                } else {

                    button.setBackground(
                            new Color(
                                    181,
                                    136,
                                    99
                            )
                    );
                }
            }
        }

        // Selected square

        if (selectedRow != -1) {

            squares[
                    selectedRow
                    ][selectedCol]
                    .setBackground(
                            Color.YELLOW
                    );
        }

        // Move history

        StringBuilder text =
                new StringBuilder();

        for (int i = 0;
             i < moves.size();
             i++) {

            if (i % 2 == 0) {

                text.append(
                        (i / 2 + 1)
                );

                text.append(". ");
            }

            text.append(
                    moves.get(i)
            );

            text.append(" ");

            if (i % 2 == 1) {
                text.append("\n");
            }
        }

        historyArea.setText(
                text.toString()
        );

        if (!gameOver) {
            checkGameState();
        }
    }

    // ==============================
    // COPY BOARD
    // ==============================

    private Piece[][] copyBoard() {

        Piece[][] copy =
                new Piece[8][8];

        for (int row = 0; row < 8; row++) {

            for (int col = 0; col < 8; col++) {

                if (board[row][col] == null) {

                    copy[row][col] = null;

                } else {

                    copy[row][col] =
                            board[row][col].copy();
                }
            }
        }

        return copy;
    }

    // ==============================
    // HELPER METHODS
    // ==============================

    private static boolean inBoard(
            int row,
            int col) {

        return row >= 0 &&
                row < 8 &&
                col >= 0 &&
                col < 8;
    }

    private static PlayerColor opposite(
            PlayerColor color) {

        return color ==
                PlayerColor.WHITE
                ? PlayerColor.BLACK
                : PlayerColor.WHITE;
    }

    private static String squareName(
            int row,
            int col) {

        return ""
                + (char) ('a' + col)
                + (8 - row);
    }

    // ==============================
    // MAIN METHOD
    // ==============================

    public static void main(
            String[] args) {

        SwingUtilities.invokeLater(
                () -> new ChessGame()
        );
    }
}
