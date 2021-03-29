package exceptions;

import com.fileutils.specs2.models.UserSystemException;

public class UserExistException extends UserSystemException {
    public UserExistException(String username) {
        super("User "+ username + " exists");
    }
}
