package SoundCloud;

import Common.FileHelpers;
import SoundCloud.UnexistentMusicException;
import SoundCloud.UnexistentUserException;
import SoundCloud.UserAlreadyExistsException;
import SoundCloud.User;
import SoundCloud.MusicMeta;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.Thread.sleep;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Library {
    private HashMap<Integer, MusicMeta> music;
    private HashMap<String, User> users;
    private HashMap<String, Integer> currentUserDownloads;
    private HashMap<String, Integer> requestUserDownloads;
    private Integer nmusics = 0;
    private int downloadCounter = 0;
    private int usedDownloadIds = 0;
    private int nDownloads = 0;
    private int maxDownloads = 10;
    private ReentrantLock downloadLock = new ReentrantLock();
    private Condition maxDownloadCondition;
    private Lock mLock = new ReentrantLock();
    private Lock uLock = new ReentrantLock();

    public Library() {
        this.users = new HashMap<String, User>();
        this.music = new HashMap<Integer, MusicMeta>();
        this.currentUserDownloads = new HashMap<String, Integer>();
        this.requestUserDownloads = new HashMap<String, Integer>();
        maxDownloadCondition = downloadLock.newCondition();
    }

    public void addUser(User u) throws UserAlreadyExistsException
    {
        this.uLock.lock();
        if(this.users.containsKey(u.getUserName()))
        {
            this.uLock.unlock();
            throw new UserAlreadyExistsException("Already exists");
        }
        
        this.users.put(u.getUserName(),u);
        this.currentUserDownloads.put(u.getUserName(), 0);
        this.requestUserDownloads.put(u.getUserName(), 0);
        
        this.uLock.unlock();
    }

    public User getUser(String email) throws UnexistentUserException
    {
        if(!this.users.containsKey(email))
        {
            throw new UnexistentUserException("UnexistentUser");
        }
        
        return this.users.get(email);
    }

    public int getNextId()
    {
        mLock.lock();
        int res = nmusics++;   
        mLock.unlock();
        
        return res;
    }
    
    public void addMusic(MusicMeta m){
        this.mLock.lock();
        music.put(m.getID(),m);
        this.mLock.unlock();
    }

    public MusicMeta getMusic(Integer key) throws UnexistentMusicException{
        if(!this.music.containsKey(key)) throw new UnexistentMusicException("Musica Inexistente");
        return this.music.get(key);
    }


    public String getMusicByTitle(String title) 
    {
        StringBuilder sb = new StringBuilder();

        this.music.forEach((k,v) ->
        {
            if(v.getTitle().equals(title))
            {
                sb.append(v.toString());
            }
        });
        return sb.toString(); 
    }
    
    public String getMusicByAuthor(String author) 
    {
        StringBuilder sb = new StringBuilder();

        this.music.forEach((k,v) ->
        {
            if(v.getAuthor().equals(author))
            {
                sb.append(v.toString());
            }
        });
        return sb.toString(); 
    }
    
    public String getMusicByYear(String year) 
    {
        StringBuilder sb = new StringBuilder();
        
        this.music.forEach((k,v) ->
        {
            if(v.getYear().equals(year))
            {
                sb.append(v.toString());
            }
        });
        return sb.toString(); 
    }
    
    public String getMusicByLabels(ArrayList<String> labels)
    {
        StringBuilder sb = new StringBuilder();
        HashMap<Integer,MusicMeta> retMusics = new HashMap<Integer,MusicMeta>();
        
        this.music.forEach((k,v) ->
        {
            if(v.hasLabels(labels))
            {
                retMusics.put(k,v);
            }
        });
        
        retMusics.forEach((k,v) ->
        {
            sb.append(v.toString());
        });

        return sb.toString();
    }

    public String getMusicByMinDownloads(Integer downloads)
    {
        StringBuilder sb = new StringBuilder();
        
        this.music.forEach((k,v) ->
        {
            if(v.getnDown() >= downloads)
            {
                sb.append(v);
            }
        });

        return sb.toString();
    }
    
    public void downloadMusic(int id, String userDownloading, PrintWriter destination, int transferRate) throws UnexistentMusicException, IOException
    {
        mLock.lock();
        if(!this.music.containsKey(id))
        {
            mLock.unlock();
            throw new UnexistentMusicException("UnexistentMusic");
        }
        
        MusicMeta m = this.music.get(id);
        m.download();
        
        mLock.unlock();
        
        downloadLock.lock();
        
        int downloadId = ++downloadCounter;
        this.requestUserDownloads.put(userDownloading, this.requestUserDownloads.get(userDownloading) + 1);
        
        int othersRequests = 0;
        
//        requestUserDownloads.forEach((k,v) -> 
//        {
//            if(!k.equals(userDownloading))
//            {
//                othersRequests += v;
//            }
//        });
        
        for(Map.Entry<String, Integer> entry : requestUserDownloads.entrySet()) 
        {
            if(!userDownloading.equals(entry.getKey()))
            {
                othersRequests += entry.getValue();
            }
        }
        
        //nDownloads >= maxDownloads || 
        // && currentUserDownloads.get(userDownloading) > 0)
        while(downloadId > (usedDownloadIds + (maxDownloads - nDownloads)) || 
                (downloadId <= (usedDownloadIds + (maxDownloads - nDownloads)) && currentUserDownloads.get(userDownloading) > 0 && othersRequests > (maxDownloads - nDownloads)))
        {
            try {
                maxDownloadCondition.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            for(Map.Entry<String, Integer> entry : requestUserDownloads.entrySet()) 
            {
                if(!userDownloading.equals(entry.getKey()))
                {
                    othersRequests += entry.getValue();
                }
            }
        }
        
        this.requestUserDownloads.put(userDownloading, this.requestUserDownloads.get(userDownloading) - 1);

        currentUserDownloads.put(userDownloading, currentUserDownloads.get(userDownloading) + 1);
        usedDownloadIds++;
        nDownloads++;
        
        downloadLock.unlock();
        
        try {
            sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Library.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        File downloadingFile = new File("ServerFiles/" + m.getID() + m.getTitle());

        destination.println("StartFile");
        destination.flush();

        long fileLength = downloadingFile.length();

        for(int i=0; i<fileLength; i+= transferRate)
        {
            String sendingMessage = FileHelpers.bytesToHex(FileHelpers.toByteArray(downloadingFile, i, transferRate));
            destination.println(sendingMessage);
            destination.flush();
        }
        
        destination.println("EndFile");
        destination.flush();
        
        downloadLock.lock();
        
        nDownloads--;
        currentUserDownloads.put(userDownloading, currentUserDownloads.get(userDownloading) - 1);

        maxDownloadCondition.signalAll();
        
        downloadLock.unlock();        
    }
}
