import com.fileutils.specs1.models.FileSystem;
import com.fileutils.specs1.models.FileSystemException;
import exceptions.PathException;

import java.awt.print.PrinterAbortException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFileSystem implements FileSystem {
    private int count = 0;
    private Dir root = new Dir("/", "/", 0, null);
    private Dir nowDir = root;
    private PathException pathException;

    public MyFileSystem() {
        root.setFather(root);
    }

    private void update() {
        count++;
    }

    public String changeDirectory(String path) throws FileSystemException {
        update();
        if (path.charAt(0) == '/') {
            nowDir = findDir(root, path);
        } else {
            nowDir = findDir(nowDir, path);
        }
        return nowDir.getPath();
    }

    public Dir findDir(Dir root, String path) throws PathException {
        Dir nowTempDir = root;
        for (String temp : path.split("/+")) {
            if (!temp.equals("")) {
                nowTempDir = root.getDir(temp);
                if (nowTempDir == null) {
                    throw new PathException(path);
                }
                root = nowTempDir;
            }
        }
        return nowTempDir;
    }

    public String list(String path) throws FileSystemException {
        update();
        Dir targetDir = findDir(path.charAt(0) == '/' ? root : nowDir, path);
        return targetDir.ls();
    }

    public String makeDirectory(String path) throws FileSystemException {
        update();
        String result = null;
        if (path.charAt(0) == '/') {
            result = mkdir(path, root);
        } else {
            result = mkdir(path, nowDir);
        }
        return result;
    }

    public String mkdir(String path, Dir root) throws FileSystemException {
        String result = null;
        rootChange(path); /// mkdir /  path = "/" , path.split = [];
        String[] dirs = path.split("/+");
        Dir nowTempDir = root;
        int i = 0;
        for (i = path.charAt(0) == '/' ? 1 : 0; i < dirs.length; ++i) {
            Dir loopDir = nowTempDir.getDir(dirs[i]);
            if (loopDir == null) {
                if (i == dirs.length - 1) {
                    // mkdir /
                    if (nowTempDir.getFile(dirs[i]) != null || !nameIsValid(dirs[i])) {
                        throw new PathException(path);
                    } else {
                        result = nowTempDir.getPath() + "/" + dirs[i];
                        result = result.replaceAll("/+", "/");
                        nowTempDir.addDir(new Dir(dirs[i], result, count, nowTempDir));
                        nowTempDir.setLastTime(count);
                        break;
                    }
                } else {
                    throw new PathException(path);
                }
            }
            nowTempDir = loopDir;
        }
        if (i == dirs.length) {
            throw new PathException(path);
        }
        return result;
    }

    public boolean nameIsValid(String name) {
        // 合法返回true
        if (name.length() > 256) {
            return false;
        }
        Pattern regex = Pattern.compile("[a-zA-Z._][a-zA-Z0-9._]*");
        Matcher m = regex.matcher(name);
        return m.matches();

    }

    public String makeDirectoryRecursively(String path) throws FileSystemException {
        update();

        if (path.charAt(0) == '/') {
            return mkdirP(path, root);
        } else {
            return mkdirP(path, nowDir);
        }
    }

    public String mkdirP(String path, Dir root) throws FileSystemException {
        String result = null;
        String[] dirs = path.split("/+");
        Dir nowTempDir = root;
        int i = 0;
        for (i = path.charAt(0) == '/' ? 1 : 0; i < dirs.length; ++i) {
            Dir loopDir = nowTempDir.getDir(dirs[i]);
            if (loopDir == null) {
                if (nowTempDir.getFile(dirs[i]) != null || !nameIsValid(dirs[i])) {
                    throw new PathException(path);
                } else {
                    result = nowTempDir.getPath() + "/" + dirs[i];
                    result = result.replaceAll("/+", "/");
                    loopDir = new Dir(dirs[i], result, count, nowTempDir);
                    nowTempDir.addDir(loopDir);
                    nowTempDir.setLastTime(count);
                }
            }
            nowTempDir = loopDir;
        }
        return nowTempDir.getPath();
    }

    public void rootChange(String path) throws FileSystemException{
        if (path.equals("/")) {
            throw new PathException(path);
        }
    }

    public String removeRecursively(String path) throws FileSystemException {
        update();
        String rightPath = path.replaceAll("/+", "/");
        rootChange(path);
        Dir targetDir = findDir(rightPath.charAt(0) == '/' ? root : nowDir, path);
        Dir loopDir = nowDir;
        while (!loopDir.getName().equals("/")) {
            if (loopDir == targetDir) {
                throw new PathException(path);
            }
            loopDir = loopDir.getFather();
        }
        if (targetDir.getName().equals("/")) {
            throw new PathException(path);
        } //if targetDir is root ,exception
        targetDir.delete();
        targetDir.getFather().getSubDir().remove(targetDir.getName());
        targetDir.getFather().setLastTime(count);
        return targetDir.getPath();
    }

    public String information(String path) throws FileSystemException {
        update();
        Dir targetDir = null;
        Dir nowTempDir = path.charAt(0) == '/' ? root : nowDir;
        Dir temproot = nowTempDir;
        String[] dirs = path.split("/+");
        int len = dirs.length;
        for (int i = 0; i < len - 1; ++i) {
            if (!dirs[i].equals("")) {
                nowTempDir = temproot.getDir(dirs[i]);
                if (nowTempDir == null) {
                    throw new PathException(path);
                }
                temproot = nowTempDir;
            }
        }
        // root file    info /   special just
        if (path.equals("/")){
            return root.info();
        }
        targetDir = nowTempDir.getDir(dirs[len - 1]);

        if (targetDir == null) {
            File targetFile = findFile(path.charAt(0) == '/' ? root : nowDir, path);// better ?
            if (targetFile == null) {
                throw new PathException(path);
            }
            return targetFile.info();
        }

        return targetDir.info();
    }


    ////// hhhh  sbsbsbsbbsbsbsbsbs
    // fuck file
    public File findFile(Dir root, String path) throws FileSystemException {
        Dir nowTempDir = root;
        File result = null;
        rootChange(path);
        String[] dirs = path.split("/+");
        int len = dirs.length;
        for (int i = 0; i < len - 1; ++i) {
            if (!dirs[i].equals("")) {
                nowTempDir = root.getDir(dirs[i]);
                if (nowTempDir == null) {
                    throw new PathException(path);
                }
                root = nowTempDir;
            }
        }

        result = nowTempDir.getFile(dirs[len - 1]);
        return result;
    }

    public String catFile(String path) throws FileSystemException {
        update();
        File file = findFile(path.charAt(0) == '/' ? root : nowDir, path);
        if (file == null) {
            throw new PathException(path);
        }
        return file.cat();
    }

    public String removeFile(String path) throws FileSystemException {
        update();
        File file = findFile(path.charAt(0) == '/' ? root : nowDir, path);

        if (file == null) {
            throw new PathException(path);
        } else {
            Dir father = file.getFather();
            father.getSubFile().remove(file.getName());
            father.setLastTime(count);
            return file.getPath();
        }
    }

    public void fileWrite(String path, String content) throws FileSystemException {
        update();
        File file = findFile(path.charAt(0) == '/' ? root : nowDir, path);
        if (file == null) {
            file = createFile(path);
            file.write(content, count);
            file.getFather().setLastTime(count);
        } else {
            file.write(content, count);
        }
    }

    public void fileAppend(String path, String content) throws FileSystemException {
        update();
        File file = findFile(path.charAt(0) == '/' ? root : nowDir, path);
        if (file == null) {
            file = createFile(path);
            fileWrite(path, content);
            file.getFather().setLastTime(count);
        } else {
            file.append(content, count);
        }
    }

    public void touchFile(String path) throws FileSystemException {
        update();
        rootChange(path);
        createFile(path);
    }

    public File createFile(String path) throws FileSystemException {
        Dir nowTempDir = path.charAt(0) == '/' ? root : nowDir;
        File result;
        Dir tempRoot = nowTempDir;
        rootChange(path);
        String[] dirs = path.split("/+");
        int len = dirs.length;
        for (int i = 0; i < len - 1; ++i) {
            if (!dirs[i].equals("")) {
                nowTempDir = tempRoot.getDir(dirs[i]);
                if (nowTempDir == null) {
                    throw new PathException(path);
                }
                tempRoot = nowTempDir;
            }
        }

        result = nowTempDir.getFile(dirs[len - 1]);
        if (result == null) {
            if (nowTempDir.getSubDir().containsKey(dirs[len - 1]) || !nameIsValid(dirs[len - 1])) {
                throw new PathException(path);
            }
            result = new File(dirs[len - 1], (nowTempDir.getPath() + "/" + dirs[len - 1]).replaceAll("/+", "/"), count, nowTempDir);
            nowTempDir.addFile(result);
            nowTempDir.setLastTime(count);
        }
        result.setLastTime(count);
        return result;
    }

}
