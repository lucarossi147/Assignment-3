package part2.rmiCallback;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client extends Remote {
    void onNotify() throws RemoteException;

    //void onMatchFinish() throws RemoteException;
}
