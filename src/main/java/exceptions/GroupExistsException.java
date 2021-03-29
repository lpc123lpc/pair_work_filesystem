package exceptions;

import com.fileutils.specs2.models.UserSystemException;

public class GroupExistsException extends UserSystemException {
    public GroupExistsException(String message) {
        super("Group " + message + " exists");
    }
}
