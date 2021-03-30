import com.fileutils.specs2.models.FileSystem;
import com.fileutils.specs2.models.FileSystemException;
import exceptions.PathExistException;
import exceptions.PathInvalidException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFileSystem implements FileSystem {

    private Dir root = new Dir("/", "/", 0, null);
    private Dir nowDir = root;
    private PathInvalidException pathInvalidException;
    private Manager manager;
    private String name;

    public MyFileSystem() {
        root.setFather(root);
        manager = Manager.getInstance();
        manager.setCount(0);
        manager.setNowDir(root);
        manager.setRootPath(root);
    }

    private void update() throws FileSystemException {
        manager.update();
        //
    }

    public void rootChange(String path) throws FileSystemException {
        if (path.equals("/")) {
            throw new PathInvalidException(path);
        }
    }

    private void pathLenInvalid(String path) throws FileSystemException {
        if (path.length() > 4096) {
            throw new PathInvalidException(path);
        }
    }


    public String changeDirectory(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        if (path.split("/+").length == 0) {
            nowDir = root;
        } else {
            if (path.charAt(0) == '/') {
                nowDir = findDir(root, path);
            } else {
                nowDir = findDir(nowDir, path);
            }
        }
        manager.setNowDir(nowDir);
        return nowDir.getPath();
    }

    // TODO
    public Dir findDir(Dir root, String path) throws PathInvalidException {
        Dir nowTempDir = null;
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
        Dir targetDir;
        if (path.split("/+").length == 0) {
            targetDir = root;
        } else {
            targetDir = findDir(path.charAt(0) == '/' ? root : nowDir, path);
        }
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
        // TODO
        targetDir.delete();
        targetDir.getFather().getSubDir().remove(targetDir.getName());
        targetDir.getFather().setLastTime(manager.getCount());
        return targetDir.getPath();
    }

    // TODO
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
        if (path.split("/+").length == 0) {
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

    public Entry findEntry(String path) throws FileSystemException {
        // 找到path对应的Entry对象
        // 当path最终对应的一个软连接或硬链接的文件，则不再进行重定向。
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
        if (path.equals("/")) {
            return root;
        }
        targetDir = nowTempDir.getDir(dirs[len - 1]);

        if (targetDir == null) {
            File targetFile = findFile(path.charAt(0) == '/' ? root : nowDir, path);// better ?
            if (targetFile == null) {
                throw new PathInvalidException(path);
            }
            return targetFile;
        } else {
            return targetDir;
        }
    }

    public String getName(String path) {
        // 当创建文件的时候，末尾必定是文件名
        String[] temp = path.split("/+");
        return temp[temp.length - 1];
    }

    @Override
    public String linkSoft(String srcPath, String desPath) throws FileSystemException {
        Entry srcEntry = findEntry(srcPath);
        String result = "";
        if (srcEntry == null) {
            throw new PathInvalidException(srcPath);
        }
        Entry desEntry = findEntry(desPath);
        if (desEntry == null) {
            if (desPath.endsWith("/")) {
                throw new PathInvalidException(desPath);
            }
            // desEntry对应路径文件不存在，需要先找到被创建链接文件的Father和他的name
            String name = getName(desPath);//当创建文件的时候，末尾必定是文件名
            Dir desEntryFather = findDir(desPath.charAt(0) == '/' ? root : nowDir,
                    desPath.substring(0, desPath.lastIndexOf('/')));

            SoftLink softLink = new SoftLink(name, (desEntryFather.getPath() + "/" + name).
                    replaceAll("/+", "/"),
                    manager.getCount(), desEntryFather, manager.getNowUser().getName());
            softLink.setPointPath(srcEntry.getPath());
            desEntryFather.addFile(softLink);
            desEntryFather.setLastTime(manager.getCount());
            result = softLink.getPointPath();
        } else if (srcEntry.getPath().equals(desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (desEntry instanceof File) {
            throw new PathExistException(desPath);
        } else if (desEntry instanceof Dir) {
            if (isFather(srcEntry.getPath(), (desEntry).getPath())) {
                throw new PathInvalidException(desPath);
            }
            Dir desDir = (Dir) desEntry;
            String srcName = srcEntry.getName();//srcPath不一定以文件名结尾，直接get
            if (desDir.containsDir(srcName) || desDir.containsFile(srcName)) {
                throw new PathExistException(desDir.getPath() + "/" + srcName);
            } else {
                SoftLink softLink = new SoftLink(srcName, (desDir.getPath() + "/" + srcName).
                        replaceAll("/+", "/"),
                        manager.getCount(), desDir, manager.getNowUser().getName());
                softLink.setPointPath(srcEntry.getPath());
                desDir.setLastTime(manager.getCount());
                desDir.addFile(softLink);
                result = softLink.getPointPath();
            }
        }
        return result;
    }

    public boolean isFather(String src, String des) {
        String[] desArray = des.split("/");
        String[] srcArray = src.split("/");

        for (int i = 0; i < srcArray.length; i++) {
            if (!srcArray[i].equals(desArray[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String readLink(String desPath) throws FileSystemException {
        File link = findFile(desPath.charAt(0) == '/' ? root : nowDir, desPath);
        if (link instanceof SoftLink) {
            return ((SoftLink) link).getPointPath();
        } else {
            throw new PathInvalidException(desPath);
        }
    }

    @Override
    public String linkHard(String srcPath, String desPath) throws FileSystemException {
        Entry srcEntry = findEntry(srcPath);
        String result = "";
        if (srcEntry == null || srcEntry instanceof Dir) {
            throw new PathInvalidException(srcPath);
        }
        Entry desEntry = findEntry(desPath);
        if (desEntry == null) {
            // 如果硬链接文件对应路径不存在，则需要找到需要创建硬链接的Father和Name;
            String desName = getName(desPath);
            Dir desFather = findDir(desPath.charAt(0) == '/' ? root : nowDir,
                    desPath.substring(0, desPath.lastIndexOf('/')));
            HardLink hardLink = new HardLink(desName, (desFather.getPath() + "/" + desName).replaceAll("/+", "/"),
                    manager.getCount(), desFather, manager.getNowUser().getName());
            hardLink.setFile((File) srcEntry);
            desFather.addFile(hardLink);
            desFather.setLastTime(manager.getCount());
            result = hardLink.getPath();
        } else if (srcEntry.getPath().equals(desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (desEntry instanceof File) {
            throw new PathExistException(desPath);
        }  else if (desEntry instanceof Dir) {
            Dir desDir = (Dir) desEntry;
            if (desDir.containsFile(srcEntry.getName()) || desDir.containsDir(srcEntry.getName())) {
                throw new PathExistException(desPath + "/" + srcEntry.getName());
            }
            HardLink hardLink = new HardLink(srcEntry.getName(), (desDir.getPath() + "/" + srcEntry.getName()).
                    replaceAll("/+", "/"),manager.getCount(), desDir, manager.getNowUser().getName());
            hardLink.setFile((File) srcEntry);
            desDir.addFile(hardLink);
            desDir.setLastTime(manager.getCount());
            result = hardLink.getPath();
        }
        return result;
    }

    @Override
    public void move(String srcPath, String desPath) throws FileSystemException {
        Entry srcEntry = findEntry(srcPath);
        if (srcEntry == null) {
            throw new PathInvalidException(srcPath);
        }
        Entry desEntry = findEntry(desPath);
        if (desEntry == null) {
            String name = getName(desPath);
            Dir desFather = findDir(desPath.charAt(0) == '/' ? root : nowDir, desPath.substring(0, desPath.lastIndexOf("/")));
            if (srcEntry instanceof Dir) {
                // 更改srcEntry name
                // 更改 srcEntry 的 path
                // 添加到desFather的子目录中
                // 从srcEntry的Father的子目录中删除，
                srcEntry.getFather().getSubDir().remove(srcEntry.getName());
                srcEntry.setName(name);
                srcEntry.setPath((desFather.getPath() + "/" + name).replaceAll("/+", "/"));
                srcEntry.setFather(desFather);
                desFather.addDir((Dir)srcEntry);

            } else if (srcEntry instanceof File) {
                // 更改 srcEntry的name
                // 更改 srcEntry的path
                //
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                srcEntry.setName(name);
                srcEntry.setFather(desFather);
                srcEntry.setPath((desFather.getPath() + "/" + name).replaceAll("/+", "/"));
                desFather.addFile((File)srcEntry);

            }
        } else if (desEntry.getPath().equals(srcEntry.getPath())) {
            throw new PathInvalidException(desPath);
        }  else if (isFather(srcEntry.getPath(), desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (desEntry instanceof Dir) {
            if (((Dir) desEntry).containsDir(srcEntry.getName())) {
                throw new PathExistException(desPath + "/" + srcPath);
            } else if (((Dir) desEntry).containsFile(srcEntry.getName())) {
                ((Dir) desEntry).addFile((File) srcEntry);
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                srcEntry.setFather((Dir)desEntry);
                srcEntry.setPath((desEntry.getPath() + "/" + srcEntry.getName()).replaceAll("/+", "/"));
                ((File) srcEntry).setLastTime(manager.getCount());
            } else if (!((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    !((Dir) desEntry).containsFile(srcEntry.getName())){
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                srcEntry.setFather((Dir)desEntry);
                srcEntry.setPath((desEntry.getPath() + "/" + name).replaceAll("/+", "/"));
                ((Dir)desEntry).addFile((File) srcEntry);
                ((Dir) desEntry).setLastTime(manager.getCount());
            }
        } else if (desEntry instanceof File) {
            if (srcEntry instanceof File) {
                // 此状况下 为 将srcEntry进行重命名为desEntry的name，然后移动到其父亲下。
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                srcEntry.setName(desEntry.getName());
                desEntry.getFather().addFile((File)srcEntry);
                srcEntry.setFather(desEntry.getFather());
                srcEntry.setPath((srcEntry.getFather().getPath() + "/" + srcEntry.getName()));
                ((File) srcEntry).setLastTime(manager.getCount());
            } else if (srcEntry instanceof Dir) {
                throw new PathExistException(desPath);
            }
        }
        if (srcEntry instanceof Dir) {
            if (srcEntry.getPath().equals(nowDir.getPath()) || isFather(srcEntry.getPath(), nowDir.getPath())) {
                throw new PathInvalidException(srcPath);
            }
            if (desEntry instanceof Dir) {

            }

        }

    }

    @Override
    public void copy(String s, String s1) throws FileSystemException {

    }


    // fuck file
    // TODO
    public File findFile(Dir root, String path) throws FileSystemException {
        if (path.endsWith("/")) {
            throw new PathInvalidException(path);
        }
        System.out.println(path);
        System.out.println(path.length());
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
                    .replaceAll("/+", "/"), manager.getCount(), nowTempDir,manager.getNowUser().getName());
            nowTempDir.addFile(result);
            nowTempDir.setLastTime(manager.getCount());
        }
        result.setLastTime(manager.getCount());
        return result;
    }
}
