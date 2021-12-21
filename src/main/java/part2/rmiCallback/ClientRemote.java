package part2.rmiCallback;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ClientRemote extends UnicastRemoteObject implements Client{

    Runnable r;

    //Runnable finish;

 /*   public ClientRemote(Runnable r, Runnable finish) throws RemoteException {
        super();
        this.r = r;
        this.finish = finish;
    }*/

    public ClientRemote(Runnable r) throws RemoteException {
        super();
        this.r = r;
    }

    @Override
    public void onNotify() throws RemoteException {
        r.run();
    }

    /*@Override
    public void onMatchFinish() throws RemoteException {
        System.out.println("On finish");
        finish.run();
    }*/
}
