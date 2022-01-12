package part2.rmiCallback.puzzle;

import part2.common.Tile;
import part2.common.TileButton;
import part2.rmiCallback.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PuzzleBoard extends JFrame {

    private List<Tile> tiles = new ArrayList<>();
    private Registry registry;
    private ServerRemote remoteInstance;
    private JPanel board;
    private ClientRemote callbackReference;

    private final SelectionManager selectionManager = new SelectionManager();

    /**
     * Create a panel for client player.
     * Needs to call a connection initalizer method
     *
     * @param rows     of the puzzle
     * @param columns  of the puzzle
     * @param registry Registry where the puzzle is located
     */
    public PuzzleBoard(int rows, int columns, Registry registry) throws RemoteException, NotBoundException {
        board = initializePanel(rows, columns);

        connectAsClient(registry);

        registerForCallback(remoteInstance);

        paintPuzzle(board);
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
    public PuzzleBoard(int rows, int columns, ServerRemote remoteInstance, Registry registry) throws RemoteException {
        board = initializePanel(rows, columns);
        createTiles(rows, columns);

        this.remoteInstance = remoteInstance;
        this.registry = registry;

        registerForCallback(remoteInstance);

        remoteInstance.setTiles(tiles);

        paintPuzzle(board);
    }

    private void registerForCallback(ServerRemote remoteInstance) throws RemoteException {
        //noinspection Convert2MethodRef
        Runnable r = (Runnable & Serializable) () -> updateBoard();

        //noinspection Convert2MethodRef
        //Runnable f = (Runnable & Serializable) () -> closeMatch();

        callbackReference = new ClientRemote(r);
        remoteInstance.registerClient(callbackReference);
    }

    private JPanel initializePanel(int rows, int columns) {
        setTitle("Puzzle");
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                cleanRegistry();
            }
        });

        board = new JPanel();
        board.setBorder(BorderFactory.createLineBorder(Color.gray));
        board.setLayout(new GridLayout(rows, columns, 0, 0));
        getContentPane().add(board, BorderLayout.CENTER);
        return board;
    }

    private void cleanRegistry() {
        try {
            Registry registry = LocateRegistry.getRegistry();
            remoteInstance.unregisterClient(callbackReference);
            registry.unbind(Main.instanceName);
        } catch (RemoteException | NotBoundException e) {
            //TODO potrebbe essere "normale" o accettabile
            //System.err.println("Remote exception on object destroy");
        }
    }

    private void paintPuzzle(JPanel board) throws RemoteException {
        board.removeAll();
        List<Tile> tmp = remoteInstance.getTiles();
        checkSolution();
        if (!tmp.isEmpty()) tiles = tmp;

        Collections.sort(tiles);
        for (Tile tile : tiles) {
            TileButton btn = new TileButton(tile);
            board.add(btn);
            btn.setBorder(BorderFactory.createLineBorder(Color.gray));
            btn.addActionListener((ActionEvent actionListener) ->
            selectionManager.selectTile(tile, () -> {
                try {
                    remoteInstance.setTiles(tiles);
                    paintPuzzle(board); //On swap performed
                    checkSolution();
                } catch (ConnectException e) {
                    manageDisconnection();
                } catch (RemoteException e) {
                    System.err.println("Remote exception on set tiles");
                }
            }));
        }
        pack();
    }

    private void manageDisconnection() {
        //System.out.println("Master disconnected...");
        try {
            if (0 == registry.list().length) { //Sono il primo a riconettersi, ricreo l'oggetto...
                System.out.println("I will regenerate a puzzle board.");
                Remote rm = new Server();
                remoteInstance = (ServerRemote) UnicastRemoteObject.exportObject(rm, 0);
                registry.rebind(Main.instanceName, remoteInstance);
            } else {
                System.out.println("I will connect to new puzzle board...");
                remoteInstance = (ServerRemote) registry.lookup(Main.instanceName);
            }
            registerForCallback(remoteInstance);
            remoteInstance.setTiles(tiles); //Setto le mie ultime tessere come quelle da distribuire
        } catch (RemoteException | NotBoundException x) {
            System.out.println("Exception after master disconnection");
        }
    }

    private void createTiles(int rows, int columns) {
        BufferedImage image;

        try {
            image = ImageIO.read(new File("src/main/java/part2/rmiCallback/park.jpg"));
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load image", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);

        List<Integer> randomPositions = new ArrayList<>();
        for (int i1 = 0; i1 < rows * columns; i1++) randomPositions.add(i1);
        Collections.shuffle(randomPositions);

        int position = 0;
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < columns; j++) {
                Image imagePortion = createImage(new FilteredImageSource(image.getSource(),
                        new CropImageFilter((j * imageWidth) / columns,
                                (i * imageHeight) / rows,
                                imageWidth / columns,
                                imageHeight / rows)));

                tiles.add(new Tile(imagePortion, position, randomPositions.get(position)));
                position++;
            }
    }

    private void checkSolution() {
        try {
            if (tiles.stream().allMatch(Tile::isInRightPlace)) {
                System.err.println("You finished the puzzle");
                remoteInstance.setFinished();
                closeMatch();
            } else if (remoteInstance.isFinished()){
                closeMatch();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void closeMatch() {
        //TODO rimane che se è il master a finire gli altri non si accorgono
        //TODO si blocca JOptionPane.showMessageDialog(board, "Puzzle Completed!", "Congrats", JOptionPane.INFORMATION_MESSAGE);
        //System.out.println("Match finished");
        cleanRegistry();
        System.exit(0);
    }

    private void connectAsClient(Registry registry) throws RemoteException, NotBoundException {
        this.registry = registry;
        remoteInstance = (ServerRemote) registry.lookup(Main.instanceName);
        tiles = remoteInstance.getTiles();
    }

    private void updateBoard() {
        try {
            if(remoteInstance.isFinished()){
                System.err.println("Other player finished the Puzzle...");
                closeMatch();
            }
            paintPuzzle(board);
        } catch (RemoteException e) {
            System.err.println("Error on update/callback");
        }
    }
}
