package exceptions;

import com.fileutils.specs2.models.UserSystemException;

public class GroupInvalidException extends UserSystemException {

    public GroupInvalidException(String message) {
        super("Group " + message + " is invalid");
    }
}
