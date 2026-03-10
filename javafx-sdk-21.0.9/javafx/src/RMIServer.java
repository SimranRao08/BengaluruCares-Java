import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

interface CareServer extends Remote {
    void joinHub(CareClient client, String userName) throws RemoteException;
    void postUpdate(String update) throws RemoteException;
    void leaveHub(CareClient client, String userName) throws RemoteException;
}

interface CareClient extends Remote {
    void notifyUpdate(String message) throws RemoteException;
}

public class RMIServer extends UnicastRemoteObject implements CareServer {
    private List<CareClient> activeUsers;

    protected RMIServer() throws RemoteException {
        super();
        activeUsers = new ArrayList<>();
    }

    @Override
    public synchronized void joinHub(CareClient client, String userName) throws RemoteException {
        activeUsers.add(client);
        System.out.println("[LOG]: " + userName + " connected.");
        postUpdate("[SYSTEM]: " + userName + " is now active!");
    }

    @Override
    public synchronized void postUpdate(String update) throws RemoteException {
        for (int i = activeUsers.size() - 1; i >= 0; i--) {
            try {
                activeUsers.get(i).notifyUpdate(update);
            } catch (RemoteException e) {
                activeUsers.remove(i);
            }
        }
    }

    @Override
    public synchronized void leaveHub(CareClient client, String userName) throws RemoteException {
        activeUsers.remove(client);
        postUpdate("[SYSTEM]: " + userName + " has left the hub.");
    }

    public static void main(String[] args) {
        try {
            LocateRegistry.createRegistry(1099);
            RMIServer hub = new RMIServer();
            Naming.rebind("BengaluruCaresHub", hub);
            System.out.println(">>> Server Running...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}