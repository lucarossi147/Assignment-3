package part2.rmiCallback;


import part2.rmiCallback.puzzle.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteManager extends Remote {

    List<Tile> getTiles() throws RemoteException;

    void setTiles(List<Tile> newTiles) throws RemoteException;

}
