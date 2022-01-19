package part2.rmiCallback;


import part2.common.Tile;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class Server implements ServerRemote {

    List<Tile> tiles = new ArrayList<>();
    List<Client> clients = new ArrayList<>();
    Boolean isFinished = false;

    @Override
    public List<Tile> getTiles() throws RemoteException {
        return tiles;
    }

    @Override
    public void setTiles(List<Tile> newTiles) throws RemoteException {
         tiles = newTiles;
         notifyClients();
    }

    @Override
    public void registerClient(Client c) throws RemoteException {
        if (!clients.contains(c)) {
            clients.add(c);
            System.out.println("[Remote-Object] Added a new client!");
        }
    }

    @Override
    public void unregisterClient(Client c) throws RemoteException {
        if (clients.remove(c)) {
            System.out.println("[Remote-Object] Removed client");
        } else {
            System.out.println("[Remote-Object] Unexistent client...");
        }
    }

    @Override
    public void notifyClients() throws RemoteException {
        for (Client cb : clients) {
            cb.onNotify();
        }
    }

    @Override
    public Boolean isFinished() throws RemoteException {
        return isFinished;
    }

    @Override
    public void setFinished() throws RemoteException {
        isFinished = true;
        if (clients.size() == 1){
            UnicastRemoteObject.unexportObject(this, true);
        }
    }

}
