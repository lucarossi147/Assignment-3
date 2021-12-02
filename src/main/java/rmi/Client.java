package rmi;

import rmi.puzzle.Tile;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class Client {
    public static void main(String[] args) throws RemoteException {
        Registry reg = LocateRegistry.getRegistry();
        try {
            RemotePuzzleBoard t = (RemotePuzzleBoard) reg.lookup("Test");
            List<Tile> tiles = t.getBoard();
            System.out.println("Ok");
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
