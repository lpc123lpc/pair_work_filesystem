import com.fileutils.specs2.models.FileSystem;
import com.fileutils.specs2.models.FileSystemException;
import exceptions.PathInvalidException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFileSystem implements FileSystem {

    private Dir root = new Dir("/", "/", 0, null);
    private Dir nowDir = root;
    private PathInvalidException pathInvalidException;
    private Manager manager;

    public MyFileSystem() {
        root.setFather(root);
        manager = Manager.getInstance();
        manager.setCount(0);
        manager.setNowDir(root);
        manager.setRootPath(root);
    }

    private void update() throws FileSystemException{
        manager.update();
        //
    }

    private void pathLenInvalid(String path) throws FileSystemException{
        if (path.length()>4096){
            throw new PathInvalidException(path);
        }
    }


    public String changeDirectory(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        if (path.charAt(0) == '/') {

            nowDir = findDir(root, path);
        } else {
            nowDir = findDir(nowDir, path);
        }
        manager.setNowDir(nowDir);
        return nowDir.getPath();
    }

    public Dir findDir(Dir root, String path) throws PathInvalidException {
        Dir nowTempDir = root;
        for (String temp : path.split("/+")) {
            if (!temp.equals("")) {
                nowTempDir = root.getDir(temp);
                if (nowTempDir == null) {
                    throw new PathInvalidException(path);
                }
                root = nowTempDir;
            }
        }
        return nowTempDir;
    }

    public String list(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        Dir targetDir = findDir(path.charAt(0) == '/' ? root : nowDir, path);
        return targetDir.ls();
    }

    public String makeDirectory(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
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
                        throw new PathInvalidException(path);
                    } else {
                        result = nowTempDir.getPath() + "/" + dirs[i];
                        result = result.replaceAll("/+", "/");
                        nowTempDir.addDir(new Dir(dirs[i], result, manager.getCount(), nowTempDir));
                        nowTempDir.setLastTime(manager.getCount());
                        break;
                    }
                } else {
                    throw new PathInvalidException(path);
                }
            }
            nowTempDir = loopDir;
        }
        if (i == dirs.length) {
            throw new PathInvalidException(path);
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
        pathLenInvalid(path);
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
                    throw new PathInvalidException(path);
                } else {
                    result = nowTempDir.getPath() + "/" + dirs[i];
                    result = result.replaceAll("/+", "/");
                    loopDir = new Dir(dirs[i], result, manager.getCount(), nowTempDir);
                    nowTempDir.addDir(loopDir);
                    nowTempDir.setLastTime(manager.getCount());
                }
            }
            nowTempDir = loopDir;
        }
        return nowTempDir.getPath();
    }

    public void rootChange(String path) throws FileSystemException{
        if (path.equals("/")) {
            throw new PathInvalidException(path);
        }
    }

    public String removeRecursively(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        String rightPath = path.replaceAll("/+", "/");
        rootChange(path);
        Dir targetDir = findDir(rightPath.charAt(0) == '/' ? root : nowDir, path);
        Dir loopDir = nowDir;
        while (!loopDir.getName().equals("/")) {
            if (loopDir == targetDir) {
                throw new PathInvalidException(path);
            }
            loopDir = loopDir.getFather();
        }
        if (targetDir.getName().equals("/")) {
            throw new PathInvalidException(path);
        } //if targetDir is root ,exception
        targetDir.delete();
        targetDir.getFather().getSubDir().remove(targetDir.getName());
        targetDir.getFather().setLastTime(manager.getCount());
        return targetDir.getPath();
    }

    public String information(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        Dir targetDir = null;
        Dir nowTempDir = path.charAt(0) == '/' ? root : nowDir;
        Dir temproot = nowTempDir;
        String[] dirs = path.split("/+");
        int len = dirs.length;
        for (int i = 0; i < len - 1; ++i) {
            if (!dirs[i].equals("")) {
                nowTempDir = temproot.getDir(dirs[i]);
                if (nowTempDir == null) {
                    throw new PathInvalidException(path);
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
                throw new PathInvalidException(path);
            }
            return targetFile.info();
        }

        return targetDir.info();
    }

    @Override
    public String linkSoft(String s, String s1) throws FileSystemException {
        return null;
    }

    @Override
    public String readLink(String s) throws FileSystemException {
        return null;
    }

    @Override
    public String linkHard(String s, String s1) throws FileSystemException {
        return null;
    }

    @Override
    public void move(String s, String s1) throws FileSystemException {

    }

    @Override
    public void copy(String s, String s1) throws FileSystemException {

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
                    throw new PathInvalidException(path);
                }
                root = nowTempDir;
            }
        }

        result = nowTempDir.getFile(dirs[len - 1]);
        return result;
    }

    public String catFile(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        File file = findFile(path.charAt(0) == '/' ? root : nowDir, path);
        if (file == null) {
            throw new PathInvalidException(path);
        }
        return file.cat();
    }

    public String removeFile(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        File file = findFile(path.charAt(0) == '/' ? root : nowDir, path);

        if (file == null) {
            throw new PathInvalidException(path);
        } else {
            Dir father = file.getFather();
            father.getSubFile().remove(file.getName());
            father.setLastTime(manager.getCount());
            return file.getPath();
        }
    }

    public void fileWrite(String path, String content) throws FileSystemException {
        update();
        pathLenInvalid(path);
        File file = findFile(path.charAt(0) == '/' ? root : nowDir, path);
        if (file == null) {
            file = createFile(path);
            file.write(content, manager.getCount());
            file.getFather().setLastTime(manager.getCount());
        } else {
            file.write(content, manager.getCount());
        }
    }

    public void fileAppend(String path, String content) throws FileSystemException {
        File file = findFile(path.charAt(0) == '/' ? root : nowDir, path);
        if (file == null) {
            file = createFile(path);
            fileWrite(path, content);
            file.getFather().setLastTime(manager.getCount());
        } else {
            update();
            pathLenInvalid(path);
            file.append(content, manager.getCount());
        }
    }

    public void touchFile(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
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
                    throw new PathInvalidException(path);
                }
                tempRoot = nowTempDir;
            }
        }

        result = nowTempDir.getFile(dirs[len - 1]);
        if (result == null) {
            if (nowTempDir.getSubDir().containsKey(dirs[len - 1]) || !nameIsValid(dirs[len - 1])) {
                throw new PathInvalidException(path);
            }
            result = new File(dirs[len - 1], (nowTempDir.getPath() + "/" + dirs[len - 1])
                    .replaceAll("/+", "/"), manager.getCount(), nowTempDir);
            nowTempDir.addFile(result);
            nowTempDir.setLastTime(manager.getCount());
        }
        result.setLastTime(manager.getCount());
        return result;
    }
}
