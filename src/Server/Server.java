package Server;


import SoundCloud.Library;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static ArrayList<Socket> clients = new ArrayList();
    
    public static void main(String[] args) throws Exception{
        File directory = new File("ServerFiles/");
        if (! directory.exists()){
            directory.mkdir();
        }
        Library db = new Library();
        ServerSocket serverSocket = new ServerSocket(12345);
        while(true){

            Socket clientSocket = serverSocket.accept();
            clients.add(clientSocket);
            Thread cliente = new Thread(new ServerWorker(clientSocket,db));

            cliente.start();
        }
    }
    
    public static void NotifyAllSockets(String message, Socket notSendToThis)
    {
        for(Socket s : clients)
        {
            if(notSendToThis == null || s != notSendToThis)
            {
                try {
                    PrintWriter output = new PrintWriter((s.getOutputStream()));
                    output.println(message);
                    output.flush();
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static void RemoveSocket(Socket disconnected)
    {
        clients.remove(disconnected);
    }
}
