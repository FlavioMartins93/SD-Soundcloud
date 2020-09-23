package Server;


import Common.FileHelpers;
import SoundCloud.Library;
import Server.Server;
import SoundCloud.UnexistentMusicException;
import SoundCloud.UnexistentUserException;
import SoundCloud.UserAlreadyExistsException;
import SoundCloud.User;
import SoundCloud.MusicMeta;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.Integer.parseInt;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import static java.lang.Integer.parseInt;

public class ServerWorker implements Runnable{
    private Socket clientSocket;
    private Library library;
    private User user;
    private Server server;
    private final int maxDebt = 500000;
    
    public ServerWorker(Socket cs, Library library){
        this.clientSocket = cs;
        this.library = library;
        this.user = null;
        this.server = server;
    }

    public String execute(String task, BufferedReader reader, PrintWriter output){
        //Divide uma String por espaços.
        String[] parser = task.trim().split("\\s+");

        String answer;

        switch (parser[0]){
            case "login":
            {
                String password = parser[2];
                
                try
                {
                    User u = this.library.getUser(parser[1]);
                    
                    if(u.getPassword().equals(password))
                    {
                        this.user=u;
                        answer = "OK";
                    }
                    else
                    {
                        answer = "Error 1"; //Invalid User
                    }
                }
                catch(UnexistentUserException ex)
                {
                    answer = "Error 1"; // Invalid User 
                }
                
                break;
            }
            case "logout":
            {
                this.user = null;
                answer = "OK";
                break;
            }
            case "fetch" : 
            {
                switch (parser[1]) {
                    case "music":
                    {
                        try
                        {
                            MusicMeta m = this.library.getMusic(parseInt(parser[2]));

                            File downloadingFile = new File("ServerFiles/" + m.getID() + m.getTitle() + "." + m.getExtension());

                            output.println("NameFile:" + m.getTitle() + "." + m.getExtension());
                            output.flush();

                            this.library.downloadMusic(parseInt(parser[2]), this.user.getUserName(), output, maxDebt);
                        }
                        catch(IOException ex)
                        {
                            answer = "Error 2"; //IO exception
                            break;
                        }
                        catch(UnexistentMusicException ex)
                        {
                            answer = "Error 3"; // Unexistent Music
                            break;
                        }
                        
                        answer = "OK";
                        break;
                    }  
                    case "user" :
                    {
                        User u;
                        try {
                            u = this.library.getUser(parser[2]);
                            answer = "Name:"+u.getUserName()+"Password:"+u.getPassword();
                        } catch (UnexistentUserException ex) {
                            answer = "Error 1"; // Invalid user 
                        }
                        break;
                    }

                    default: 
                    {
                        answer = "Error 4"; // invalid operation
                        break;
                    }
                }
                
                break;
            }
            case "add" : 
            {
                switch (parser[1]) 
                {
                    case "music":
                    {
                        ArrayList<String> labels = new ArrayList();
                        for(int i=6; i< parser.length; i++)
                        {
                            labels.add(parser[i]);
                        }
                        
                        MusicMeta addingMusic = new MusicMeta(parser[2], parser[3], parser[4], labels, parser[5], 0);
                        
                        try
                        {
                            String temp = reader.readLine();

                            if(temp.equals("StartFile"))
                            {
                                int musicId = this.library.getNextId();
                                addingMusic.setID(musicId);
                                String fileName = musicId + addingMusic.getTitle();

                                FileOutputStream out;
                                out = new FileOutputStream("ServerFiles/" + fileName);

                                while((temp = reader.readLine()) != null && !temp.equals("EndFile"))
                                {
                                    FileHelpers.writeBytesToFile(out, temp);
                                }

                                out.close();

                                if(temp.equals("EndFile"))
                                {
                                    this.library.addMusic(addingMusic);
                                    answer = "OK";
                                    Server.NotifyAllSockets("NOTIFY: Nova música: " + addingMusic.getTitle() + " - " + addingMusic.getAuthor(), clientSocket);
                                }
                                else
                                {
                                    answer = "Error 5"; // Error while uploading file
                                }
                            }
                            else
                            {
                                answer = "Error 8"; //Invalid content
                            }
                        }
                        catch(FileNotFoundException ex)
                        {
                            answer = "Error 7"; // file not found
                        }
                        catch(IOException ex)
                        {
                            answer = "Error 2"; // IO exception
                        }

                        break;
                    }
                    case "user":
                    {
                        try {
                            this.library.addUser(new User(parser[2],parser[3]));
                            answer = "OK";
                        } catch (UserAlreadyExistsException ex) {
                            answer = "Error 6"; // Already exists user
                        }
                        break;
                    }
                    default: 
                    {    
                        answer = "Error 4"; // invalid operation
                        break;
                    }
                }                
                break;
            }
                        case "ListMusics": 
            {
                switch(parser[1]) 
                {
                    case "id":
                    {
                        try {
                            MusicMeta m = this.library.getMusic((Integer) parseInt(parser[2]));
                            answer = "musicList\n" + m.toString() + "listEnd";
                        } catch (UnexistentMusicException ex){
                            answer = "NoMatch";
                        }
                        break;
                    }
                    case "title":
                    {
                        String musicList = this.library.getMusicByTitle(parser[2]);
                        if(musicList.equals("")) {
                            answer = "NoMatch";
                            return answer;
                        }
                        answer = "musicList\n" + musicList + "listEnd";
                        break;
                    }
                    case "author":
                    {
                        String musicList = this.library.getMusicByAuthor(parser[2]);
                        if(musicList.equals("")) {
                            answer = "NoMatch";
                            return answer;
                        }
                        answer = "musicList\n" + musicList + "listEnd";
                        break;
                    }
                    case "year":
                    {
                        String musicList = this.library.getMusicByYear(parser[2]);
                        if(musicList.equals("")) {
                            answer = "NoMatch";
                            return answer;
                        }
                        answer = "musicList\n" + musicList + "listEnd";
                        break;
                    }
                    case "labels":
                    {
                        ArrayList<String> labels = new ArrayList<String>();
                        for(int i=2;i<parser.length;i++) 
                        { 
                            labels.add(parser[i]);
                        }
                        String musicList = this.library.getMusicByLabels(labels);
                        if(musicList.equals("")) {
                            answer = "NoMatch";
                            return answer;
                        }
                        answer = "musicList\n" + musicList + "listEnd";
                        break;
                    }
                    case "minDownloads":
                    {
                        String musicList = this.library.getMusicByMinDownloads((Integer) parseInt(parser[2]));
                        if(musicList.equals("")) {
                            answer = "NoMatch";
                            return answer;
                        }
                        answer = "musicList\n" + musicList + "listEnd";
                        break;
                    }
                    default:{                        
                        answer = "Error 4";
                        break;
                    }
                }                
                break;           
            }
            default:
            {
                answer = "Error 4";
                break;
            }            
        }
        return answer;
    }

    
    public void run() {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter output = new PrintWriter((clientSocket.getOutputStream()));
            String temp = null;

            while (true && (temp = input.readLine())!= null) {
                String res = this.execute(temp, input, output);
                output.println(res);
                output.flush();
            }
            
            Server.RemoveSocket(clientSocket);
            clientSocket.shutdownOutput();                // Fecha o lado de escrita do socket **/
            clientSocket.shutdownInput();                 // Fecha o lado de leitura do socket **/
            clientSocket.close();                         // Fecha o socket **/
        }
        catch(IOException e){
            e.printStackTrace();
        }
        }
    }
