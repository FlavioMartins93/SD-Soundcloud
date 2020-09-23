package Client;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class ClientReceive implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    public ClientReceive(Socket c, PrintWriter p){
            this.client = c;
            this.out = p;
    }

    public void run(){
        String msg;
        try{
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            while((msg = in.readLine()) != null)
            {
                if(msg.startsWith("NOTIFY:"))
                {
                    System.out.println(msg.replace("NOTIFY:", ""));
                }
                else 
                {
                    out.println(msg);
                    out.flush();
                }
            }
        } catch (SocketException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
