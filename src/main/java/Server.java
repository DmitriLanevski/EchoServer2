import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by lanev_000 on 3.03.2016.
 */
public class Server implements Runnable{

    private Globals limitations;
    private int socketAdress;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean serverOpened = false;

    public Server(Globals limitations) {
        this.limitations = limitations;
    }

    public Integer minAvailableSocket(){
        for (Integer i = limitations.getMinConnection(); i <= limitations.getMaxConnection(); i++ ){
            if (!limitations.getOpenedConnections().contains(i)){
                return i;
            }
        }
        return -1;
    }

    public void run(){
        socketAdress = minAvailableSocket();
        if (socketAdress > 0) {
            try {
                System.out.println("Connection " + socketAdress + " is waiting for client.");
                limitations.getOpenedConnections().add(socketAdress);
                serverSocket = new ServerSocket(socketAdress);
                serverSocket.setSoTimeout(5000);
                clientSocket = serverSocket.accept();
                dis = new DataInputStream(clientSocket.getInputStream());
                dos = new DataOutputStream(clientSocket.getOutputStream());
                serverOpened = true;
            } catch (SocketTimeoutException e) {
                System.out.println("Connection " + socketAdress + " timeout. Opening new connection.");
                if (serverSocket != null) {
                    try {
                        limitations.getOpenedConnections().remove(new Integer(socketAdress));
                        serverSocket.close();
                    } catch (IOException e1) {
                        System.out.println("Server socket could not be closed.");
                    }
                }
                serverOpened = false;
                Thread nextServerConnection = new Thread(new Server(limitations));
                nextServerConnection.start();
            } catch (IOException e) {
                if (serverSocket != null) {
                    try {
                        limitations.getOpenedConnections().remove(new Integer(socketAdress));
                        serverSocket.close();
                    } catch (IOException e1) {
                        System.out.println("Server socket could not be closed.");
                    }
                }
                //throw new RuntimeException(e);
            }

            if (serverOpened) {
                System.out.println("Connection " + socketAdress + " successfully connected to client.");
                System.out.println("Connection " + minAvailableSocket() + " will be opened for next client.");
                Thread nextServerConnection = new Thread(new Server(limitations));
                nextServerConnection.start();
            }

            while (serverOpened) {
                try {
                    String buf = dis.readUTF();
                    dos.writeUTF(buf);
                    if (buf.equals("Close")) {
                        System.out.println("Connection " + socketAdress + " is closed.");
                        limitations.getOpenedConnections().remove(new Integer(socketAdress));
                        dis.close();
                        dos.close();
                        clientSocket.close();
                        serverSocket.close();
                        break;
                    }
                } catch (IOException e) {
                    if (serverSocket != null) {
                        try {
                            System.out.println("Connection " + socketAdress + " will be closed.");
                            limitations.getOpenedConnections().remove(new Integer(socketAdress));
                            dis.close();
                            dos.close();
                            clientSocket.close();
                            serverSocket.close();
                            break;
                        } catch (IOException e1) {
                            System.out.println("Server socket could not be closed.");
                        }
                    }
                    //throw new RuntimeException(e);
                }
            }
        }
        else {
            System.out.println("Connections limit " + limitations.getConnectionLimit() + " is reached.");
            try {
                Thread.sleep(10000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Thread tryOnceAgain = new Thread(new Server(limitations));
            tryOnceAgain.start();
        }
    }

    public static void main(String[] args) throws Exception{
        Globals limitations = new Globals();
        Thread ServerConnection = new Thread(new Server(limitations));
        ServerConnection.start();
    }
}
