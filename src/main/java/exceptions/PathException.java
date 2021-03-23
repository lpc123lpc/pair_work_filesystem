package exceptions;


import com.fileutils.specs1.models.FileSystemException;

public class PathException extends FileSystemException {
    public PathException(String file) {
        super("Path " + file + " is invalid");
    }

}
