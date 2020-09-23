package SoundCloud;


import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class MusicMeta {
    private Integer ID;
    private String title;
    private String author;
    private String year;
    private String extension;
    private Integer nDown;
    private ArrayList<String> labels;

    public MusicMeta(String title, String author, String year, ArrayList<String> labels, String extension, Integer ndown){
        this.title=title;
        this.author=author;
        this.year=year;
        this.nDown=ndown;
        this.labels = labels;
        this.extension = extension;
        this.ID=0;
    }

    public String getTitle(){
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getYear() {
        return this.year;
    }

    public String getExtension()
    {
        return this.extension;
    }
    
    public Integer getnDown() {
        return this.nDown;
    }

    public Integer getID() {
        return this.ID;
    }

    public ArrayList<String> getLabels()
    {
        return this.labels;
    }
    
    public void setID(Integer ID) {
        this.ID = ID;
    }
    
    public int getDownloadNumber()
    {
        return this.nDown;
    }
    
    public void download()
    {
        nDown++;
    }

    public boolean hasLabels(ArrayList<String> labels)
    {
        for(int i=0; i<labels.size(); i++) {
            if(this.labels.contains(labels.get(i))) return true;
        }
        return false;
    }

    public String toString() {
        return 
                "ID: " + ID +
                ", título: '" + title + 
                ", intérprete: '" + author + 
                ", ano: " + year +
                ", etiquetas: " + labels + 
                ", número de downloads: " + nDown + "\n";
    }
}
