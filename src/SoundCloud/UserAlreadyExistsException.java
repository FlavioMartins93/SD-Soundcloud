package SoundCloud;

public class UserAlreadyExistsException extends Exception {
    public UserAlreadyExistsException(String errorMessage){
        super(errorMessage);
    }
}
