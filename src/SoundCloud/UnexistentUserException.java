package SoundCloud;


public class UnexistentUserException extends Exception {
    public UnexistentUserException(String errorMessage){
        super(errorMessage);
    }
}
