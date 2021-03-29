package exceptions;


import com.fileutils.specs2.models.FileSystemException;

public class PathInvalidException extends FileSystemException {
    public PathInvalidException(String file) {
        super("Path " + file + " is invalid");
    }

}
