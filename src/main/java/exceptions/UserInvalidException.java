package exceptions;

import com.fileutils.specs2.models.UserSystemException;

public class UserInvalidException extends UserSystemException {
    public UserInvalidException(String message) {
        super("User " + message + " is invalid");
    }
}
