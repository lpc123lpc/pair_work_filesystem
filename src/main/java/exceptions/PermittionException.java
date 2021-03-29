package exceptions;

import com.fileutils.specs2.models.UserSystemException;

public class PermittionException extends UserSystemException {
    public PermittionException() {
        super("Operation is not permitted");
    }
}
