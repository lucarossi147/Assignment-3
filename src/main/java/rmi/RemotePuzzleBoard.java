package rmi;

import rmi.puzzle.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemotePuzzleBoard extends Remote {
    List<Tile> getBoard() throws RemoteException;
}
