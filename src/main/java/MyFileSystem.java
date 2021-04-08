import com.fileutils.specs2.models.FileSystem;
import com.fileutils.specs2.models.FileSystemException;
import exceptions.PathExistException;
import exceptions.PathInvalidException;

import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFileSystem implements FileSystem {

    private Dir root = new Dir("/", "/", 0, null, "root");
    private Dir nowDir = root;
    private Manager manager;

    public MyFileSystem() {
        root.setFather(root);
        manager = Manager.getInstance();
        manager.setCount(0);
        manager.setNowDir(root);
        manager.setRootPath("/");
    }

    private void update() throws FileSystemException {
        manager.update();
        if (manager.getFileSysFlag() == 1) {
            Dir tempEntry = findDir(manager.getRootPath());
            if (tempEntry == null) {
                nowDir = root;
            } else {
                nowDir = tempEntry;
            }
            manager.setNowDir(nowDir);
            manager.setFileSysFlag(0);
        }
    }

    public void rootChange(String path) throws FileSystemException {
        if (path.replaceAll("/+", "/").equals("/")) {
            throw new PathInvalidException(path);
        }
    }

    public void pathLenInvalid(String path) throws FileSystemException {
        if (path.length() > 4096) {
            throw new PathInvalidException(path);
        }
    }


    public String changeDirectory(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        if (path.replaceAll("/+", "/").equals("/")) {
            nowDir = root;
        } else {
            nowDir = findDir(path);
        }
        manager.setNowDir(nowDir);
        return nowDir.getPath();
    }

    public Dir findDir(String... paths) throws FileSystemException {
        String path = paths[0];
        String realPath = paths.length > 1 ? paths[1] : paths[0];
        Entry tempEntry = findEntry(path + "/", realPath);
        if (tempEntry instanceof Dir) {
            return (Dir) tempEntry;
        } else if (tempEntry instanceof SoftLink) {
            return findDir(((SoftLink) tempEntry).getPointPath());
        } else {
            throw new PathInvalidException(realPath);
        }
    }

    public String list(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        Dir targetDir;
        if (path.replaceAll("/+", "/").equals("/")) {
            targetDir = root;
        } else {
            targetDir = findDir(path);
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
        if (path.replaceAll("/+", "/").equals("/")) {
            throw new PathExistException(path);
        }
        String[] dirs = path.split("/+");
        Dir nowTempDir = root;
        int i;
        for (i = path.charAt(0) == '/' ? 1 : 0; i < dirs.length; ++i) {
            Dir loopDir = nowTempDir.getDir(dirs[i]);
            if (loopDir == null) {
                File looFile = nowTempDir.getFile(dirs[i]);
                if (looFile == null) {
                    // 此时表明 dir[i]不是一个已经存在的目录
                    if (i == dirs.length - 1) {
                        // mkdir
                        if (!nameIsValid(dirs[i])) {
                            throw new PathInvalidException(path);
                        } else {
                            result = nowTempDir.getPath() + "/" + dirs[i];
                            result = result.replaceAll("/+", "/");
                            nowTempDir.addDir(new Dir(dirs[i], result, manager.getCount(), nowTempDir, manager.getNowUser().getName()));
                            nowTempDir.setLastTime(manager.getCount());
                            break;
                        }
                    } else {
                        throw new PathInvalidException(path);
                    }
                } else if (looFile instanceof SoftLink) {
                    Entry loopEntry = findEntry(((SoftLink) looFile).getPointPath());
                    if (loopEntry == null) {
                        // 如果软连接对应的目录没有找到，一种情况时当前已经查询到路径最后一个，需要进行创建
                        // 一种情况时当前不在路径最后一个，此时软连接对应一个不存在目录，则需要进行抛出异常]
                        if (i == dirs.length - 1) {
                            // mkdir /
                            // 此时创建时，需要知道loopEntry的Father,以及需要被创建的name
                            // 由于 pointPath为一个绝对路径，可以直接使用创建链接时使用的方法
                            // 此时 name必合法 nowTempDir 一定不会包含同名子文件
                            String name = getName(((SoftLink) looFile).getPointPath());
                            nowTempDir = findDir(((SoftLink) looFile).getPointPath().substring(0,
                                    ((SoftLink) looFile).getPointPath().lastIndexOf("/")));
                            result = nowTempDir.getPath() + "/" + name;
                            result = result.replaceAll("/+", "/");
                            nowTempDir.addDir(new Dir(name, result, manager.getCount(), nowTempDir, manager.getNowUser().getName()));
                            nowTempDir.setLastTime(manager.getCount());
                            break;
                        } else {
                            throw new PathInvalidException(path);
                        }
                    } else if (loopEntry instanceof Dir) {
                        nowTempDir = (Dir) loopEntry;
                    } else {
                        throw new PathInvalidException(path);
                    }
                } else {
                    throw new PathInvalidException(path);
                }
            } else {
                nowTempDir = loopDir;
            }
        }
        if (i == dirs.length) {
            throw new PathExistException(path);
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

    public String getAbPath(String path) {

        if (path.replaceAll("/+", "/").equals("/")) {
            return "/";
        } else {
            Stack<String> stack = new Stack<>();
            for (String temp : path.split("/+")) {
                if (temp.equals("..") && stack.size() > 0) {
                    stack.pop();
                } else if (!temp.equals(".") && !temp.equals("..")) {
                    stack.push(temp);
                }
            }
            StringBuilder temp = new StringBuilder();
            while (stack.size() != 0) {
                temp = temp.insert(0, stack.pop());
                temp.insert(0, "/");
            }
            if (path.charAt(0) != '/') {
                return temp.toString().substring(1);
            } else {
                return temp.toString();
            }
        }
    }

    public String mkdirP(String path, Dir root) throws FileSystemException {
        String result;
        Dir nowTempDir = root;
        int i;
        //String realPath = getAbPath(path);
        String[] dirs = path.split("/+");
        for (i = path.charAt(0) == '/' ? 1 : 0; i < dirs.length; ++i) {
            Dir loopDir = nowTempDir.getDir(dirs[i]);
            if (loopDir == null) {
                File loopFile = nowTempDir.getFile(dirs[i]);
                if (loopFile == null) {
                    // 此时找不到需要进行创建，也就是dirs[i]不是一个已经存在
                    // 目录和已经存在的文件，需要进行创建目录
                    if (!nameIsValid(dirs[i])) {
                        throw new PathInvalidException(path);
                    } else {
                        result = nowTempDir.getPath() + "/" + dirs[i];
                        result = result.replaceAll("/+", "/");
                        loopDir = new Dir(dirs[i], result, manager.getCount(), nowTempDir, manager.getNowUser().getName());
                        nowTempDir.addDir(loopDir);
                        nowTempDir.setLastTime(manager.getCount());
                        // update nowTempDir
                        nowTempDir = loopDir;
                    }
                } else if (loopFile instanceof SoftLink) {
                    Entry loopEntry = findEntry(((SoftLink) loopFile).getPointPath());
                    if (loopEntry == null) {
                        // 找不到目标目录，需要进行创建
                        // 创建时，需要找到被创建目录的father和name
                        String name = getName(((SoftLink) loopFile).getPointPath());
                        nowTempDir = findDir(((SoftLink) loopFile).getPointPath()
                                .substring(0, ((SoftLink) loopFile).getPointPath().lastIndexOf("/")));
                        result = nowTempDir.getPath() + "/" + name;
                        result = result.replaceAll("/+", "/");
                        loopDir = new Dir(name, result, manager.getCount(), nowTempDir, manager.getNowUser().getName());
                        nowTempDir.addDir(loopDir);
                        nowTempDir.setLastTime(manager.getCount());
                        // update nowTempDir
                        nowTempDir = loopDir;
                    } else if (loopEntry instanceof Dir) {
                        // 目标路径存在，更新nowTempDir
                        nowTempDir = (Dir) loopEntry;
                    } else {
                        throw new PathInvalidException(path);
                    }
                } else {
                    throw new PathInvalidException(path);
                }
            } else {
                nowTempDir = loopDir;
            }
        }
        return nowTempDir.getPath();
    }


    public String removeRecursively(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        String rightPath = path.replaceAll("/+", "/");
        rootChange(rightPath);
        Entry targetDir = findEntry(path);
        if (!(targetDir instanceof Dir)) {
            throw new PathInvalidException(path);
        }
        Dir loopDir = nowDir;
        do {
            if (loopDir == targetDir) {
                throw new PathInvalidException(path);
            }
            loopDir = loopDir.getFather();
        } while (!loopDir.getName().equals("/"));
        targetDir.getFather().getSubDir().remove(targetDir.getName());
        targetDir.getFather().setLastTime(manager.getCount());
        return targetDir.getPath();
    }

    /**
     * @return String
     * 首先得到path对应的Entry，若不存在此Entry则显然抛出异常
     * 若Entry存在，对Entry类型进行讨论
     * 若Entry为SoftLink则不进行重定向，直接输出此SoftLink的information
     * 若Entry为HardLink则进行重定向，输出的为此HardLink指向的文件的information
     * 若Entry为普通Dir或File，则直接调用Info即可
     */

    public String information(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        Entry tempEntry = findEntry(path);
        String result;
        if (tempEntry == null) {
            throw new PathInvalidException(path);
        } else if (tempEntry instanceof HardLink) {
            result = ((HardLink) tempEntry).info();
        } else {
            result = tempEntry.info();
        }
        return result;
    }

    public Entry findEntry(String... paths) throws FileSystemException {
        String path = paths[0];
        String realPath = paths.length > 1 ? paths[1]: paths[0];
        Dir nowTempDir = path.charAt(0) == '/' ? root : nowDir;
        Dir loopDir = nowTempDir;
        String[] dirs = path.split("/+");
        int len = dirs.length;
        for (int i = 0; i < len - 1; ++i) {
            if (!dirs[i].equals("")) {
                nowTempDir = loopDir.getDir(dirs[i]);
                if (nowTempDir == null) {
                    File loopFile = loopDir.getFile(dirs[i]);
                    if (loopFile == null) {
                        throw new PathInvalidException(realPath);
                    } else if (loopFile instanceof HardLink) {
                        throw new PathInvalidException(realPath);
                    } else if (loopFile instanceof SoftLink) {
                        Entry loopEntry = findEntry(((SoftLink) loopFile).getPointPath());
                        if (loopEntry instanceof Dir) {
                            nowTempDir = (Dir) loopEntry;
                        } else {
                            throw new PathInvalidException(realPath);
                        }
                    } else {
                        throw new PathInvalidException(realPath);
                    }
                }
                loopDir = nowTempDir;
            }
        }

        // root file    info /   special just
        if (path.replaceAll("/+", "/").equals("/")) {
            return root;
        }
        Dir targetDir = nowTempDir.getDir(dirs[len - 1]);
        if (targetDir == null) {
            return nowTempDir.getFile(dirs[len - 1]);
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
        update();
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
            String realDesPath = desPath.charAt(0) == '/' ? desPath : nowDir.getPath() + "/" + desPath;
            Dir desEntryFather = findDir(realDesPath.substring(0, realDesPath.lastIndexOf('/')), desPath);
            SoftLink softLink = new SoftLink(name, (desEntryFather.getPath() + "/" + name).
                    replaceAll("/+", "/"),
                    manager.getCount(), desEntryFather, manager.getNowUser().getName());
            if (srcEntry instanceof SoftLink) {
                softLink.setPointPath(((SoftLink) srcEntry).getPointPath());
            } else if (srcEntry instanceof HardLink) {
                File file = findFile(((HardLink) srcEntry).getFile().getPath());
                softLink.setPointPath(srcEntry.getPath());
            } else {
                softLink.setPointPath(srcEntry.getPath());
            }
            desEntryFather.addFile(softLink);
            desEntryFather.setLastTime(manager.getCount());
            result = softLink.getPointPath();
        } else if (srcEntry.getPath().equals(desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (desEntry instanceof File) {
            throw new PathExistException(desPath);
        } else if (desEntry instanceof Dir) {
            if (isFather(srcEntry.getPath(), desEntry.getPath())) {
                throw new PathInvalidException(desPath);
            }
            Dir desDir = (Dir) desEntry;
            String srcName = srcEntry.getName();//srcPath不一定以文件名结尾，直接get
            if (desDir.containsDir(srcName) || desDir.containsFile(srcName)) {
                throw new PathExistException((desDir.getPath() + "/" + srcName));
            } else {
                SoftLink softLink = new SoftLink(srcName, (desDir.getPath() + "/" + srcName).
                        replaceAll("/+", "/"),
                        manager.getCount(), desDir, manager.getNowUser().getName());
                if (srcEntry instanceof SoftLink) {//如果是软连接，直接得到路径
                    softLink.setPointPath(((SoftLink) srcEntry).getPointPath());
                } else if (srcEntry instanceof HardLink) {//如果是硬链接，看硬链接指向得文件被删除没有，如果删除了指向硬链接
                    File file = findFile(((HardLink) srcEntry).getFile().getPath());
                    softLink.setPointPath(srcEntry.getPath());
                } else {//文件或者目录直接指向
                    softLink.setPointPath(srcEntry.getPath());
                }
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
            if (i >= desArray.length || !srcArray[i].equals(desArray[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String readLink(String desPath) throws FileSystemException {
        update();
        Entry link = findEntry(desPath);
        if (link instanceof SoftLink) {
            return ((SoftLink) link).getPointPath();
        } else {
            throw new PathInvalidException(desPath);
        }
    }

    @Override
    public String linkHard(String srcPath, String desPath) throws FileSystemException {
        update();
        Entry srcEntry = findEntry(srcPath);
        String result = "";
        if (srcEntry == null || srcEntry instanceof Dir) {
            throw new PathInvalidException(srcPath);
        }
        Entry desEntry = findEntry(desPath);
        if (desEntry == null) {
            // 如果硬链接文件对应路径不存在，则需要找到需要创建硬链接的Father和Name;
            String desName = getName(desPath);
            String realDesPath = desPath.charAt(0) == '/' ? desPath : nowDir.getPath() + "/" + desPath;
            Dir desFather = findDir(realDesPath.substring(0, realDesPath.lastIndexOf('/')), desPath);
            HardLink hardLink = new HardLink(desName, (desFather.getPath() + "/" + desName).replaceAll("/+", "/"),
                    manager.getCount(), desFather, manager.getNowUser().getName());
            if (srcEntry instanceof SoftLink) {
                Entry entry = findEntry(((SoftLink) srcEntry).getPointPath());
                if (entry == null || entry instanceof Dir) {//
                    throw new PathInvalidException(srcPath);
                } else {
                    hardLink.setFile((File) entry);
                }
            } else if (srcEntry instanceof HardLink) {
                hardLink.setFile(((HardLink) srcEntry).getFile());
            } else {
                hardLink.setFile((File) srcEntry);
            }
            desFather.addFile(hardLink);
            desFather.setLastTime(manager.getCount());
            result = hardLink.getPath();
        } else if (srcEntry.getPath().equals(desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (desEntry instanceof File) {
            throw new PathExistException(desPath);
        } else if (desEntry instanceof Dir) {
            Dir desDir = (Dir) desEntry;
            if (desDir.containsFile(srcEntry.getName()) || desDir.containsDir(srcEntry.getName())) {
                throw new PathExistException((desPath + "/" + srcEntry.getName()));
            }
            HardLink hardLink = new HardLink(srcEntry.getName(), (desDir.getPath() + "/" + srcEntry.getName()).
                    replaceAll("/+", "/"), manager.getCount(), desDir, manager.getNowUser().getName());
            if (srcEntry instanceof SoftLink) {
                Entry entry = findEntry(((SoftLink) srcEntry).getPointPath());
                if (entry == null || entry instanceof Dir) {//
                    throw new PathInvalidException(srcPath);
                } else {
                    hardLink.setFile((File) entry);
                }
            } else if (srcEntry instanceof HardLink) {
                hardLink.setFile(((HardLink) srcEntry).getFile());
            } else {
                hardLink.setFile((File) srcEntry);
            }
            desDir.addFile(hardLink);
            desDir.setLastTime(manager.getCount());
            result = hardLink.getPath();
        }
        return result;
    }

    @Override
    public void move(String srcPath, String desPath) throws FileSystemException {
        update();
        // move之后父目录的修改时间是否需要进行变化
        // 似乎每次进行move时都会删除srcEntry父目录的最后一次修改时间
        Entry srcEntry = findEntry(srcPath);
        if (srcEntry == null) {
            throw new PathInvalidException(srcPath);
        }
        Entry desEntry = findEntry(desPath);
        // srcPath的异常优先级高于 desPath的异常优先级。
        // 所以需要将后面的srcEntry的异常抛出提前到此处
        if (srcEntry instanceof Dir) {
            if (srcEntry.getPath().equals(nowDir.getPath()) || isFather(srcEntry.getPath(), nowDir.getPath())) {
                throw new PathInvalidException(srcPath);
            }
        }
        if (desEntry == null) {
            String name = getName(desPath);
            String realDesPath = desPath.charAt(0) == '/' ? desPath : nowDir.getPath() + "/" + desPath;
            Dir desFather = findDir(realDesPath.substring(0, realDesPath.lastIndexOf("/")), desPath);
            String newDesEntryPath = (desFather.getPath() + "/" + name).
                    replaceAll("/+", "/");
            if (isFather(srcEntry.getPath(), newDesEntryPath)) {
                // desEntry 为空，指导书有点点问题
                // 按照ln -s中所述，此异常的抛出只在dstEntry为目录且存在时
                // TODO
                throw new PathInvalidException(desPath);
            }
            srcEntry.setPath(newDesEntryPath, manager.getCount());
            if (srcEntry instanceof Dir) {
                srcEntry.getFather().getSubDir().remove(srcEntry.getName());
                srcEntry.setName(name);
                desFather.addDir((Dir) srcEntry);
            } else if (srcEntry instanceof File) {
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                srcEntry.setName(name);
                desFather.addFile((File) srcEntry);
            }
            // 更新srcEntry oldFather 最后一次修改时间
            srcEntry.getFather().setLastTime(manager.getCount());
            srcEntry.setFather(desFather);
            // 更新srcEntry newFather 最后一次修改时间
            desFather.setLastTime(manager.getCount());
        } else if (desEntry.getPath().equals(srcEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (isFather(srcEntry.getPath(), desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (desEntry instanceof Dir && srcEntry instanceof File) {
            // src -> File
            // des -> Entry
            if (((Dir) desEntry).containsDir(srcEntry.getName())) {
                throw new PathExistException((desPath + "/" + srcEntry.getName()));
            } else if (((Dir) desEntry).containsFile(srcEntry.getName())) {
                File srcFile = ((Dir) desEntry).getFile(srcEntry.getName());
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                onlyCopyFile((File) srcEntry, srcFile);
                // oldFather modifyTime
                srcEntry.getFather().setLastTime(manager.getCount());
            } else if (!((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    !((Dir) desEntry).containsFile(srcEntry.getName())) {
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                srcEntry.getFather().setLastTime(manager.getCount());
                srcEntry.setFather((Dir) desEntry);
                srcEntry.setPath((desEntry.getPath() + "/" + srcEntry.getName()).replaceAll("/+", "/"), manager.getCount());
                ((Dir) desEntry).addFile((File) srcEntry);
                ((Dir) desEntry).setLastTime(manager.getCount());
            }
        } else if (desEntry instanceof File) {
            if (srcEntry instanceof File) {
                srcEntry.getFather().setLastTime(manager.getCount());
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                // oldFather modifyTime
                onlyCopyFile((File) srcEntry, (File) desEntry);
                ((File) desEntry).setLastTime(manager.getCount());
            }
        }
        if (srcEntry instanceof Dir) {
            if (desEntry instanceof Dir) {
                // src -> Dir
                // des -> Dir
                if (((Dir) desEntry).containsFile(srcEntry.getName())) {
                    throw new PathExistException((desPath + "/" + srcEntry.getName()));
                }
                Dir tempDir = ((Dir) desEntry).getDir(srcEntry.getName());
                if (tempDir == null) {
                    if (!((Dir) desEntry).containsFile(srcEntry.getName())) {
                        srcEntry.getFather().getSubDir().remove(srcEntry.getName());
                        // oldFather modifyTime
                        srcEntry.getFather().setLastTime(manager.getCount());
                        srcEntry.setPath((desEntry.getPath() + "/" + srcEntry.getName()), manager.getCount());
                        ((Dir) desEntry).addDir((Dir) srcEntry);
                        srcEntry.setFather((Dir) desEntry);
                        // 考虑到父目录desEntry下子目录数量发生变化，需要更新其最后一次修改时间
                        ((Dir) desEntry).setLastTime(manager.getCount());
                    }
                } else if (tempDir.getDirCount() == 0) {
                    srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                    // oldFather modifyTime
                    srcEntry.getFather().setLastTime(manager.getCount());
                    srcEntry.setPath((desEntry.getPath() + "/" + srcEntry.getName()).
                            replaceAll("/+", "/"), manager.getCount());
                    for (Map.Entry<String, Dir> temp : ((Dir) srcEntry).getSubDir().entrySet()) {
                        if (!temp.getKey().equals(".") && !temp.getKey().equals("..")) {
                            tempDir.addDir(temp.getValue());
                            temp.getValue().setFather(tempDir);
                        }
                    }
                    for (File temp : ((Dir) srcEntry).getSubFile().values()) {
                        tempDir.addFile(temp);
                        temp.setFather(tempDir);
                    }
                    tempDir.setLastTime(manager.getCount());
                } else if (tempDir.getDirCount() != 0) {
                    throw new PathExistException((desPath + "/" + srcEntry.getName()));
                }
            } else if (desEntry instanceof File) {
                throw new PathExistException((desPath + "/" + srcEntry.getName()));
            }
        }

    }

    @Override
    public void copy(String srcPath, String desPath) throws FileSystemException {
        update();
        Entry srcEntry = findEntry(srcPath);//
        if (srcEntry == null) {
            throw new PathInvalidException(srcPath);
        }
        Entry desEntry = findEntry(desPath); // shi ji zhi xiang de lu jing yiyang  // src shi des de father
        if (desEntry == null) {
            String name = getName(desPath);
            String realDesPath = desPath.charAt(0) == '/' ? desPath : nowDir.getPath() + "/" + desPath;
            Dir desFather = findDir(realDesPath.substring(0, realDesPath.lastIndexOf("/")), desPath);
            String newDerEntryPath = (desFather.getPath() + "/" + name).
                    replaceAll("/+", "/");
            if (isFather(srcEntry.getPath(), newDerEntryPath)) {
                throw new PathInvalidException(desPath);
            }
            if (srcEntry instanceof File) {
                File desFile = new File(name, newDerEntryPath, manager.getCount(), desFather, manager.getNowUser().getName());
                desFile.write(((File) srcEntry).cat(), manager.getCount());
                desFather.addFile(desFile);
                desFather.setLastTime(manager.getCount());
            } else if (srcEntry instanceof Dir) {
                Dir desDir = new Dir(name, newDerEntryPath,
                        manager.getCount(), desFather, manager.getNowUser().getName());
                desDir.copy((Dir) srcEntry, manager.getCount(), manager.getNowUser().getName());
                desFather.addDir(desDir);
                desFather.setLastTime(manager.getCount());
            }
        } else if (desEntry.getPath().equals(srcEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (isFather(srcEntry.getPath(), desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (desEntry instanceof Dir && srcEntry instanceof File) {
            if (((Dir) desEntry).containsDir(srcEntry.getName())) {
                throw new PathExistException((desPath + "/" + srcEntry.getName()));
            } else if (((Dir) desEntry).containsFile(srcEntry.getName())) {
                onlyCopyFile((File) srcEntry, ((Dir) desEntry).getFile(srcEntry.getName()));
            } else if (!((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    !((Dir) desEntry).containsFile(srcEntry.getName())) {
                createAndCopyFile((File) srcEntry, (Dir) desEntry, srcEntry.getName());
            }
        } else if (srcEntry instanceof File && desEntry instanceof File) {
            onlyCopyFile((File) srcEntry, (File) desEntry);
        } else if (srcEntry instanceof Dir && desEntry instanceof Dir) {
            if ((((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    ((Dir) desEntry).getDir(srcEntry.getName()).getDirCount() > 0) ||
                    ((Dir) desEntry).containsFile(srcEntry.getName())) {
                throw new PathExistException((desPath + "/" + srcEntry.getName()));
            } else if ((((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    ((Dir) desEntry).getDir(srcEntry.getName()).getDirCount() == 0)) {
                ((Dir) desEntry).getDir(srcEntry.getName()).copy((Dir) srcEntry, manager.getCount(), manager.getNowUser().getName());
                ((Dir) desEntry).getDir(srcEntry.getName()).setLastTime(manager.getCount());
            } else {
                Dir createDir = new Dir(srcEntry.getName(), (desEntry.getPath() + "/" + srcEntry.getName()).
                        replaceAll("/+", "/"), manager.getCount(), (Dir) desEntry, manager.getNowUser().getName());
                createDir.copy((Dir) srcEntry, manager.getCount(), manager.getNowUser().getName());
                ((Dir) desEntry).setLastTime(manager.getCount());
                ((Dir) desEntry).addDir(createDir);
            }
        } else if (srcEntry instanceof Dir && desEntry instanceof File) {
            throw new PathExistException(desPath);
        }
    }

    public void onlyCopyFile(File srcFile, File desFile) throws FileSystemException {
        if (srcFile instanceof SoftLink) {
            SoftLink link = new SoftLink(desFile.getName(), desFile.getPath(), desFile.getCreateTime()
                    , desFile.getFather(), desFile.getCreateUser());
            desFile.getFather().addFile(link);
            link.setLastTime(manager.getCount());
        } else if (srcFile instanceof HardLink) {
            File srcLinkFile = ((HardLink) srcFile).getFile();
            if (desFile instanceof SoftLink) {
                HardLink link = new HardLink(desFile.getName(), desFile.getPath(), desFile.getCreateTime(),
                        desFile.getFather(), desFile.getCreateUser());
                link.setLastTime(manager.getCount());
                desFile.getFather().addFile(link);
                ;
            } else if (desFile instanceof HardLink) {
                File desLinkFile = ((HardLink) desFile).getFile();
                desLinkFile.write(srcLinkFile.cat(), manager.getCount());
            } else {
                desFile.write(srcLinkFile.cat(), manager.getCount());
            }
        } else {
            if (desFile instanceof SoftLink) {
                File file = new File(desFile.getName(), desFile.getPath(), desFile.getCreateTime(),
                        desFile.getFather(), desFile.getCreateUser());
                file.setLastTime(manager.getCount());
                desFile.getFather().addFile(file);
            } else if (desFile instanceof HardLink) {
                File desLinkFIle = ((HardLink) desFile).getFile();
                desLinkFIle.write(srcFile.cat(), manager.getCount());
            } else {
                desFile.write(srcFile.cat(), manager.getCount());
            }
        }
    }

    public void createAndCopyFile(File subFile, Dir father, String name) {
        if (subFile instanceof SoftLink) {
            SoftLink tempSoftLink = new SoftLink(name, (father.getPath() + "/" + subFile.getName()).
                    replaceAll("/+", "/"), manager.getCount(), father, manager.getNowUser().getName());
            tempSoftLink.setPointPath(((SoftLink) subFile).getPointPath());
            father.addFile(tempSoftLink);
        } else if (subFile instanceof HardLink) {
            HardLink tempHardLink = new HardLink(name, (father.getPath() + "/" + subFile.getName()).
                    replaceAll("/+", "/"), manager.getCount(), father, manager.getNowUser().getName());
            tempHardLink.setFile(((HardLink) subFile).getFile());
            father.addFile(tempHardLink);
        } else {
            File tempFile = new File(name, (father.getPath() + "/" + subFile.getName()).
                    replaceAll("/+", "/"), manager.getCount(), father, manager.getNowUser().getName());
            tempFile.write(subFile.cat(), manager.getCount());
            father.addFile(tempFile);
        }
    }


    // fuck file
    public File findFile(String path) throws FileSystemException {
        if (path.endsWith("/")) {
            throw new PathInvalidException(path);
        }
        Entry tempEntry = findEntry(path);
        if (tempEntry instanceof SoftLink) {
            File file = findFile(((SoftLink) tempEntry).getPointPath());
            if (file == null) {
                throw new PathInvalidException(((SoftLink) tempEntry).getPointPath());
            }
            return file;
        } else if (tempEntry instanceof HardLink) {
            return ((HardLink) tempEntry).getFile();
        } else if (tempEntry instanceof File) {
            return (File) tempEntry;
        }
        return null;
    }

    public String catFile(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        File file = findFile(path);
        if (file == null) {
            throw new PathInvalidException(path);
        }
        return file.cat();
    }

    public String removeFile(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        Entry file = findEntry(path);
        if (file == null || file instanceof Dir) {
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
        File file = findFile(path);
        if (file == null) {
            file = createFile(path);
            file.write(content, manager.getCount());
            file.getFather().setLastTime(manager.getCount());
        } else {
            file.write(content, manager.getCount());
        }
    }

    public void fileAppend(String path, String content) throws FileSystemException {
        File file = findFile(path);
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
        Dir nowTempDir;
        File result;
        rootChange(path);
        String[] dirs = path.split("/+");
        int len = dirs.length;
        String abPath = (path.charAt(0) == '/' ? path : nowDir.getPath() + "/" + path);
        nowTempDir = findDir(abPath.substring(0, abPath.lastIndexOf("/")), path);
        result = nowTempDir.getFile(dirs[len - 1]);
        if (result == null) {
            if (nowTempDir.getSubDir().containsKey(dirs[len - 1]) || !nameIsValid(dirs[len - 1])) {
                throw new PathInvalidException(path);
            }
            result = new File(dirs[len - 1], (nowTempDir.getPath() + "/" + dirs[len - 1])
                    .replaceAll("/+", "/"), manager.getCount(), nowTempDir, manager.getNowUser().getName());
            nowTempDir.addFile(result);
            nowTempDir.setLastTime(manager.getCount());
        }
        result.setLastTime(manager.getCount());
        return result;
    }
}
