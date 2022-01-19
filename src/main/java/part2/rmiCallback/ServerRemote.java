package part2.rmiCallback;


import part2.common.Tile;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ServerRemote extends Remote {

    List<Tile> getTiles() throws RemoteException;

    void setTiles(List<Tile> newTiles) throws RemoteException;

    void registerClient(Client c) throws RemoteException;

    void unregisterClient(Client c) throws RemoteException;

    void notifyClients() throws RemoteException;

    Boolean isFinished() throws RemoteException;

    void setFinished() throws RemoteException;
}
