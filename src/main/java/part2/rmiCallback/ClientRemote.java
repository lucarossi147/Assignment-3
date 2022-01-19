package part2.rmiCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientRemote extends UnicastRemoteObject implements Client{

    Runnable r;

    public ClientRemote(Runnable r) throws RemoteException {
        super();
        this.r = r;
    }

    @Override
    public void onNotify() throws RemoteException {
        r.run();
    }
}
