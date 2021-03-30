package exceptions;

import com.fileutils.specs2.models.FileSystemException;

public class PathExistException extends FileSystemException {
    public PathExistException(String message) {
        super("Path "+message+" exists");
    }
}
