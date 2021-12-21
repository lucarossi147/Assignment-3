package part2.rmiPooling.puzzle;

import part2.common.Tile;
import part2.common.TileButton;
import part2.rmiPooling.ServerRemote;
import part2.rmiPooling.Server;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class PuzzleBoard extends JFrame {

    final int delay = 1000; //Millisecond of delay of every update
    private List<Tile> tiles = new ArrayList<>();
    Registry registry;
    ServerRemote remoteInstance;
    final String OBJECT = "remoteInstance";
    Timer t;

    private final SelectionManager selectionManager = new SelectionManager();

    /**
     * Create a panel for client player.
     * Needs to call a connection initalizer method
     *
     * @param rows     of the puzzle
     * @param columns  of the puzzle
     * @param registry Registry where the puzzle is located
     */
    public PuzzleBoard(final int rows, final int columns, Registry registry) throws RemoteException {
        final JPanel board = initializePanel(rows, columns);

        connectAsClient(registry);

        paintPuzzle(board);

        t = initializeTimer(remoteInstance, board);
        t.start();
    }

    /**
     * Create a panel for the master player.
     * remoteInstance and registry are created by Client class.
     *
     * @param rows           of the puzzle
     * @param columns        of the puzzle
     * @param remoteInstance reference to the puzzle
     * @param registry       Registry where the puzzle is located
     */
    public PuzzleBoard(final int rows, final int columns, ServerRemote remoteInstance, Registry registry) throws RemoteException {
        final JPanel board = initializePanel(rows, columns);

        this.remoteInstance = remoteInstance;
        this.registry = registry;

        createTiles(rows, columns);
        paintPuzzle(board);
        remoteInstance.setTiles(tiles);

        t = initializeTimer(remoteInstance, board);
        t.start();
    }

    private Timer initializeTimer(ServerRemote remoteInstance, JPanel board) {
        return new Timer(delay, actionEvent -> {
            try {
                List<Tile> newTiles = remoteInstance.getTiles();
                if (tiles != newTiles && !newTiles.isEmpty()) {
                    tiles = newTiles;
                    paintPuzzle(board);
                    checkSolution();
                }
            } catch (UnmarshalException e){
                System.err.println("Unmarshalling error...");
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private JPanel initializePanel(int rows, int columns) {
        setTitle("Puzzle");
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                cleanRegistry();
            }
        });

        final JPanel board = new JPanel();
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        return board;
    }

    private void cleanRegistry() {
        try {
            Registry registry = LocateRegistry.getRegistry();
            registry.unbind(OBJECT);
        } catch (RemoteException y) {
            System.err.println("Remote exception on object destroy");
        } catch (NotBoundException nb) {
            System.err.println("Not bound exception on object destroy");
        }
    }

    private void createTiles(int rows, int columns) {
        final BufferedImage image;

        try {
            image = ImageIO.read(new File("src/main/java/part2/rmiPooling/park.jpg"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final int imageWidth = image.getWidth(null);
        final int imageHeight = image.getHeight(null);

        int position = 0;

        final List<Integer> randomPositions = new ArrayList<>();
        IntStream.range(0, rows * columns).forEach(randomPositions::add);
        Collections.shuffle(randomPositions);

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                final Image imagePortion = createImage(new FilteredImageSource(image.getSource(),
                        new CropImageFilter(j * imageWidth / columns,
                                i * imageHeight / rows,
                                (imageWidth / columns),
                                imageHeight / rows)));

                tiles.add(new Tile(imagePortion, position, randomPositions.get(position)));
                position++;
            }
        }
    }

    private void checkSolution() {
        if (tiles.stream().allMatch(Tile::isInRightPlace)) {
            JOptionPane.showMessageDialog(this, "Puzzle Completed!", "", JOptionPane.INFORMATION_MESSAGE);
            cleanRegistry();
            System.exit(0);
        }
    }

    private void paintPuzzle(final JPanel board) throws RemoteException {
        board.removeAll();
        List<Tile> tmp = remoteInstance.getTiles();
        if (!tmp.isEmpty()) {
            tiles = tmp;
        }
        Collections.sort(tiles);
        checkSolution();
        tiles.forEach(tile -> {
            final TileButton btn = new TileButton(tile);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener(actionListener -> selectionManager.selectTile(tile, () -> {
                try {
                    remoteInstance.setTiles(tiles);
                    paintPuzzle(board); //On swap performed
                    checkSolution();
                } catch (ConnectException e) {
                    try {
                        System.err.println("Master disconnected...");
                        if (registry.list().length == 0) {
                            System.err.println("I will regenerate a puzzle board.");
                            ServerRemote rm = new Server();
                            remoteInstance = (ServerRemote) UnicastRemoteObject.exportObject(rm, 0);
                            registry.rebind("remoteInstance", remoteInstance);
                        } else {
                            System.err.println("I will connect to new puzzle board...");
                            remoteInstance = (ServerRemote) registry.lookup(OBJECT);
                            remoteInstance.setTiles(tiles); //Setto le mie ultime tessere come quelle da distribuire
                        }
                        t.stop();
                        t = initializeTimer(remoteInstance, board);
                        t.start();
                    } catch (RemoteException x) {
                        System.err.println("Remote exception after master disconnection");
                    } catch (NotBoundException x) {
                        //Va bene, ho provato a riconettermi ma la partita era già finita...
                        //System.err.println("Not bound after master disconnection");
                    }
                } catch (AccessException e) {
                    System.err.println("Access exception on set tiles");
                } catch (RemoteException e) {
                    System.err.println("Remote exception on set tiles");
                }
            }, t));
        });
        pack();
    }



    private void connectAsClient(Registry registry) {
        try {
            this.registry = registry;
            this.remoteInstance = (ServerRemote) registry.lookup(OBJECT);
            this.tiles = remoteInstance.getTiles();
        } catch (ConnectException e){
            System.err.println("Connect exception on creation");
        } catch (AccessException e) {
            System.err.println("Access exception on creation");
            e.printStackTrace();
        } catch (RemoteException e) {
            System.err.println("Remote exception on creation");
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.err.println("Not bound exception on creation");
            e.printStackTrace();
        }
    }

}
