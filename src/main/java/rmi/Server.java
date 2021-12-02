package rmi;

import rmi.puzzle.PuzzleBoard;
import rmi.puzzle.Tile;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class Server {

    private final PuzzleBoard board;

    private Server(){
        String path = "src/main/java/rmi/park.jpg";
        board = new PuzzleBoard(3,3, path);
    }

    public static void main(String[] args) throws RemoteException {
        RemotePuzzleBoard puzzle = new RemotePuzzleBoardImpl();

        try {
            Registry reg = LocateRegistry.getRegistry();
            RemotePuzzleBoard stub = (RemotePuzzleBoard) UnicastRemoteObject.exportObject(puzzle, 0);

            reg.bind("Test", stub);

            System.err.println("Server ready");
        } catch (RemoteException e){
            System.out.println("Remote exception");
            e.printStackTrace();
        } catch (AlreadyBoundException e){
            System.out.println("Already bound exception");
            e.printStackTrace();
        }
    }
}
