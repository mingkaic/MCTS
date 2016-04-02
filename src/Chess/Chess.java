package Chess;

import Synapse.MonteCarlo.AI;
import Synapse.MonteCarlo.MCMove;
import com.mgs.chess.core.Location;
import com.mgs.chess.core.Piece;
import com.mgs.chess.core.movement.Movement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Chess extends JFrame implements MouseListener, MouseMotionListener {
    static final int CELL_WIDTH = 100;

	JLayeredPane layeredPane;
	JPanel chessBoard;
	JLabel chessPiece;
	int xAdjustment;
	int yAdjustment;
    ChessState curState;
    Location origPos;
    List<ChessMove> potentialMoves = new ArrayList<>();

    private static class BotRunner implements Runnable {
        Chess   board;
        ChessState curState;
        AI bot;
        int id;

        public BotRunner(Chess board, ChessState rules, AI bot, int i) {
            this.board = board;
            curState = rules;
            this.bot = bot;
            id = i;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (curState) {
                    if (curState.hasMoves()) {
                        int player = curState.getPlayerId();
                        if (player == id) {
                            MCMove move = bot.doMove(curState.getLastMove());
                            board.graphicallyMove((ChessMove) move);
                        }
                    } else {
                        return;
                    }
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class WinChecker implements Runnable {
        ChessState curState;

        public WinChecker(ChessState state) {
            curState = state;
        }

        @Override
        public void run() {
            while (true) {
                synchronized (curState) {
                    if (false == curState.hasMoves()) {
                        String winning;
                        if (curState.getResult(1) == 1.0) {
                            winning = "Player 1 wins!";
                        } else if (curState.getResult(2) == 1.0) {
                            winning = "Player 2 wins!";
                        } else {
                            winning = "Both of you suck!";
                        }
                        System.out.println(winning);
                        return;
                    }
                }
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

	public Chess(ChessState rule){
        Dimension boardSize = new Dimension(CELL_WIDTH*8, CELL_WIDTH*8);
        layeredPane = new JLayeredPane();
        getContentPane().add(layeredPane);
        layeredPane.setPreferredSize(boardSize);
        layeredPane.addMouseListener(this);
        layeredPane.addMouseMotionListener(this);

        chessBoard = new JPanel();
        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
        chessBoard.setLayout( new GridLayout(8, 8) );
        chessBoard.setPreferredSize( boardSize );
        chessBoard.setBounds(0, 0, boardSize.width, boardSize.height);

        curState = rule;

        for (int i = 0; i < 64; i++) {
            JPanel square = new JPanel( new BorderLayout() );
            chessBoard.add( square );

            int row = (i / 8) % 2;
            if (row == 0)
                square.setBackground( i % 2 == 0 ? Color.gray : Color.white );
            else
                square.setBackground( i % 2 == 0 ? Color.white : Color.gray );
        }

        Piece pieces[] =
                {Piece.BLACK_ROOK, Piece.BLACK_KNIGHT, Piece.BLACK_BISHOP, Piece.BLACK_KING,
                Piece.BLACK_QUEEN, Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT, Piece.BLACK_ROOK,
                Piece.WHITE_ROOK, Piece.WHITE_KNIGHT, Piece.WHITE_BISHOP, Piece.WHITE_KING,
                Piece.WHITE_QUEEN, Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT, Piece.WHITE_ROOK};

        for (int i = 0; i < 8; i++) {
            addPiece(pieces[i], Location.valueOf(""+(char)('A'+i)+"8"));
        }
        for (int i = 0; i < 8; i++) {
            addPiece(Piece.BLACK_PAWN, Location.valueOf(""+(char)('A'+i)+"7"));
        }
        for (int i = 0; i < 8; i++) {
            addPiece(pieces[8+i], Location.valueOf(""+(char)('A'+i)+"1"));
        }
        for (int i = 0; i < 8; i++) {
            addPiece(Piece.WHITE_PAWN, Location.valueOf(""+(char)('A'+i)+"2"));
        }
	}

    private int[] PixelMap(Location l) {
        int coord[] = new int[2];

        coord[0] = l.getCoordinateX()*CELL_WIDTH-CELL_WIDTH/2;
        coord[1] = l.getCoordinateY()*CELL_WIDTH-CELL_WIDTH/2;

        return coord;
    }

    private Location PixelMap(int x, int y) {
        return Location.forCoordinates(x/CELL_WIDTH+1, y/CELL_WIDTH+1);
    }

    public void graphicallyMove(Movement move) {
        int o_pixels[] = PixelMap(move.getFrom());
        int n_pixels[] = PixelMap(move.getTo());

        Component orig = chessBoard.findComponentAt(o_pixels[0], o_pixels[1]);
        Component next = chessBoard.findComponentAt(n_pixels[0], n_pixels[1]);
        if (orig instanceof JPanel) {
            orig = (JLabel) chessPiece;
        }
        if (orig instanceof JPanel) return;
        Point parentLocation = orig.getParent().getLocation();
        JLabel cpiece = (JLabel) orig;
        cpiece.setLocation(
                o_pixels[0]+parentLocation.x - o_pixels[0],
                o_pixels[1]+parentLocation.y - o_pixels[1]);
        cpiece.setSize(cpiece.getWidth(), cpiece.getHeight());
        layeredPane.add(cpiece, JLayeredPane.DRAG_LAYER);

        cpiece.setVisible(false);
        if (next instanceof JLabel) {
            Container parent = next.getParent();
            parent.remove(0);
            parent.add(cpiece);
        } else {
            Container parent = (Container) next;
            parent.add(cpiece);
        }
        cpiece.setVisible(true);
    }

	public void addPiece(Piece p, Location l) {
		String imageLink = "images/chess/".concat(p.name().toLowerCase()).concat(".png");
		JLabel piece = new JLabel( new ImageIcon(imageLink) );
		JPanel panel = (JPanel)chessBoard.getComponent((l.getCoordinateY()-1)*8+l.getCoordinateX()-1);
		panel.add(piece);
        curState.setPiece(p, l);
	}

	public void mousePressed(MouseEvent e) {
        chessPiece = null;
        Component c =  chessBoard.findComponentAt(e.getX(), e.getY());
        if (c instanceof JPanel)
            return;
        Point parentLocation = c.getParent().getLocation();
        xAdjustment = parentLocation.x - e.getX();
        yAdjustment = parentLocation.y - e.getY();
        chessPiece = (JLabel)c;
        chessPiece.setLocation(e.getX() + xAdjustment, e.getY() + yAdjustment);
        chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
        layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);

        Location l = PixelMap(e.getX(), e.getY());
        List<MCMove> moves = curState.getMoves();
        potentialMoves.clear();
        for (MCMove move : moves) {
            ChessMove cm = (ChessMove) move;
            if (cm.getFrom() == l) {
                potentialMoves.add(cm);
            }
        }
        origPos = l;
	}

	public void mouseDragged(MouseEvent me) {
        if (chessPiece == null) return;
        chessPiece.setLocation(me.getX() + xAdjustment, me.getY() + yAdjustment);
	}

	public void mouseReleased(MouseEvent e) {
        Location l = PixelMap(e.getX(), e.getY());

        for (ChessMove move : potentialMoves) {
            if (move.getTo() == l) {
                graphicallyMove(move);
                curState.doMove(move);
                return;
            }
        }
        graphicallyMove(new Movement(Piece.WHITE_PAWN, origPos, origPos));
	}

	public void mouseClicked(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e) {}

    private static void ChessInit() {
        boolean player1Human = true;
        boolean player2Human = true;

        // AI settings
        int maxIt = 10000;
        int moveDepth = 100;

        ChessState rules = new ChessState();

        Chess board = new Chess(rules);
        board.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE );
        board.pack();
        board.setResizable(true);
        board.setLocationRelativeTo( null );
        board.setVisible(true);

        AI bot1 = null;
        AI bot2 = null;
        if (false == player1Human) {
            try {
                bot1 = new AI(rules, maxIt, moveDepth);
            } catch (Exception e) {
                System.out.println("report");
            }
        }
        if (false == player2Human) {
            try {
            bot2 = new AI(rules, maxIt, moveDepth);
            } catch (Exception e) {
                System.out.println("report");
            }
        }

        // something something swing
        /*if (null != bot1) {
            Runnable r1 = new BotRunner(board, rules, bot1, 1);
            Thread t1 = new Thread(r1);
            t1.run();
        }
        if (null != bot2) {
            Runnable r2 = new BotRunner(board, rules, bot2, 2);
            Thread t2 = new Thread(r2);
            t2.run();
        }
        /*Runnable r = new WinChecker(rules);
        Thread t = new Thread(r);
        t.run();*/
    }

    public static void main(String[] args) {
        try {
            ChessInit();
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
    }
}