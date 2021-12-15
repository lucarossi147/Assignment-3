package part2.rmiCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackRemote extends Remote {
    void update() throws RemoteException;
}
