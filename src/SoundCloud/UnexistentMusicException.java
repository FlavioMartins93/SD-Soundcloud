package SoundCloud;

public class UnexistentMusicException extends Exception {
    public UnexistentMusicException(String errorMessage){
        super(errorMessage);
    }
}
