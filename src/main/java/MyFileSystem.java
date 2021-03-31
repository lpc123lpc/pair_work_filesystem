import com.fileutils.specs2.models.FileSystem;
import com.fileutils.specs2.models.FileSystemException;
import exceptions.PathExistException;
import exceptions.PathInvalidException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFileSystem implements FileSystem {

    private Dir root = new Dir("/", "/", 0, null,"root");
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
                nowDir = findDir(path);
            } else {
                nowDir = findDir(path);
            }
        }
        manager.setNowDir(nowDir);
        return nowDir.getPath();
    }

    // TODO
    public Dir findDir(String path) throws FileSystemException {
        Entry tempEntry = findEntry(path);
        if (tempEntry instanceof Dir) {
            return (Dir) tempEntry;
        }
        return null;
    }

    public String list(String path) throws FileSystemException {
        update();
        pathLenInvalid(path);
        Dir targetDir;
        if (path.split("/+").length == 0) {
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
                        nowTempDir.addDir(new Dir(dirs[i], result, manager.getCount(), nowTempDir,manager.getNowUser().getName()));
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
                    loopDir = new Dir(dirs[i], result, manager.getCount(), nowTempDir,manager.getNowUser().getName());
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
        Dir targetDir = findDir(path);
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
            File targetFile = findFile(path);// better ?
            if (targetFile == null) {
                throw new PathInvalidException(path);
            }
            return targetFile.info();
        }

        return targetDir.info();
    }

    public Entry findEntry(String path) throws FileSystemException {
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
                        throw new PathInvalidException(path);
                    } else if (loopFile instanceof HardLink){
                        throw new PathInvalidException(path);
                    } else if (loopFile instanceof SoftLink) {
                        Entry loopEntry = findEntry(((SoftLink) loopFile).getPointPath());
                        if (loopEntry instanceof Dir) {
                            nowTempDir = (Dir) loopEntry;
                        } else {
                            throw new PathInvalidException(path);
                        }
                    } else {
                        throw new PathInvalidException(path);
                    }
                }
                loopDir = nowTempDir;
            }
        }

        // root file    info /   special just
        if (path.equals("/")) {
            return root;
        }
        Dir targetDir = nowTempDir.getDir(dirs[len - 1]);
        if (targetDir == null) {
            File targetFile = nowTempDir.getFile(dirs[len - 1]);
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
            String realDesPath = desPath.charAt(0)=='/' ? desPath : nowDir.getPath() + "/" + desPath;
            Dir desEntryFather = findDir(realDesPath.substring(0, realDesPath.lastIndexOf('/')));
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
            if (i >= desArray.length ||!srcArray[i].equals(desArray[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String readLink(String desPath) throws FileSystemException {
        update();
        File link = findFile(desPath);
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
            String realDesPath = desPath.charAt(0)=='/' ? desPath : nowDir.getPath() + "/" + desPath;
            Dir desFather = findDir(realDesPath.substring(0, realDesPath.lastIndexOf('/')));
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
        update();
        // move之后父目录的修改时间是否需要进行变化
        // 似乎每次进行move时都会删除srcEntry父目录的最后一次修改时间
        Entry srcEntry = findEntry(srcPath);
        if (srcEntry == null) {
            throw new PathInvalidException(srcPath);
        }
        Entry desEntry = findEntry(desPath);
        if (desEntry == null) {
            String name = getName(desPath);
            String realDesPath = desPath.charAt(0)=='/' ? desPath : nowDir.getPath() + "/" + desPath;
            Dir desFather = findDir(realDesPath.substring(0, realDesPath.lastIndexOf("/")));
            // 更改srcEntry name
            // 更改 srcEntry 的 path
            // 添加到desFather的子目录中
            // 从srcEntry的Father的子目录中删除
            srcEntry.setPath((desFather.getPath() + "/" + name).replaceAll("/+", "/"));
            if (srcEntry instanceof Dir) {
                srcEntry.getFather().getSubDir().remove(srcEntry.getName());
                srcEntry.setName(name);
                desFather.addDir((Dir)srcEntry);
            } else if (srcEntry instanceof File) {
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                srcEntry.setName(name);
                desFather.addFile((File)srcEntry);
            }
            // 更新srcEntry oldFather 最后一次修改时间
            srcEntry.getFather().setLastTime(manager.getCount());
            srcEntry.setFather(desFather);
            // 更新srcEntry newFather 最后一次修改时间
            desFather.setLastTime(manager.getCount());
        } else if (desEntry.getPath().equals(srcEntry.getPath())) {
            throw new PathInvalidException(desPath);
        }  else if (isFather(srcEntry.getPath(), desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        } else if (desEntry instanceof Dir && srcEntry instanceof File) {
            if (((Dir) desEntry).containsDir(srcEntry.getName())) {
                throw new PathExistException(desPath + "/" + srcEntry.getName());
            } else if (((Dir) desEntry).containsFile(srcEntry.getName())) {
                File srcFile = ((Dir) desEntry).getFile(srcEntry.getName());
                ((Dir) desEntry).addFile((File) srcEntry);
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                // oldFather modifyTime
                onlyCopyFile((File) srcEntry, srcFile);
                srcEntry.getFather().setLastTime(manager.getCount());
            } else if (!((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    !((Dir) desEntry).containsFile(srcEntry.getName())){
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                // oldFather modifyTime
                srcEntry.getFather().setLastTime(manager.getCount());
                srcEntry.setFather((Dir)desEntry);
                srcEntry.setPath((desEntry.getPath() + "/" + name).replaceAll("/+", "/"));
                ((Dir)desEntry).addFile((File) srcEntry);
                ((Dir)desEntry).setLastTime(manager.getCount());
            }
        } else if (desEntry instanceof File) {
            if (srcEntry instanceof File) {
                srcEntry.getFather().setLastTime(manager.getCount());
                srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                // oldFather modifyTime
                onlyCopyFile((File)srcEntry, (File)desEntry);
                ((File) desEntry).setLastTime(manager.getCount());
            }
        }
        if (srcEntry instanceof Dir) {
            if (srcEntry.getPath().equals(nowDir.getPath()) || isFather(srcEntry.getPath(), nowDir.getPath())) {
                throw new PathInvalidException(srcPath);
            }
            if (desEntry instanceof Dir) {
                if (((Dir) desEntry).containsFile(srcEntry.getName())) {
                    throw new PathExistException(desPath + "/" + srcEntry.getName());
                }
                Dir tempDir = ((Dir) desEntry).getSubDir().get(srcEntry.getName());
                if (tempDir == null) {
                    srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                    // oldFather modifyTime
                    srcEntry.getFather().setLastTime(manager.getCount());
                    srcEntry.setPath((desEntry.getPath() + "/" + srcEntry.getName()).replaceAll("/+", "/"));
                    ((Dir) desEntry).addDir((Dir)srcEntry);
                    srcEntry.setFather((Dir) desEntry);
                    ((Dir) srcEntry).setLastTime(manager.getCount());
                    // 考虑到父目录desEntry下子目录数量发生变化，需要更新其最后一次修改时间
                    ((Dir) desEntry).setLastTime(manager.getCount());

                } else if (tempDir.getDirCount() == 0){
                    srcEntry.getFather().getSubFile().remove(srcEntry.getName());
                    // oldFather modifyTime
                    srcEntry.getFather().setLastTime(manager.getCount());
                    srcEntry.setPath((desEntry.getPath() + "/" + srcEntry.getName()).
                            replaceAll("/+", "/"));
                    for (Dir temp : ((Dir) srcEntry).getSubDir().values()) {
                        tempDir.addDir(temp);
                        temp.setFather(tempDir);
                    }
                    for (File temp : ((Dir) srcEntry).getSubFile().values()) {
                        tempDir.addFile(temp);
                        temp.setFather(tempDir);
                    }
                    //覆盖之后，子目录和文件数量发生变化时，更改最后一次修改时间
                    if (tempDir.getDirCount() > 0) {
                        tempDir.setLastTime(manager.getCount());
                    }
                } else if (tempDir.getDirCount() != 0) {
                    throw new PathExistException(desPath + "/" + srcEntry.getName());
                }
            } else if (desEntry instanceof File) {
                throw new PathExistException(desPath + "/" + srcEntry.getName());
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
            String realDesPath = desPath.charAt(0)=='/' ? desPath : nowDir.getPath() + "/" + desPath;
            Dir desFather = findDir(realDesPath.substring(0, realDesPath.lastIndexOf("/")));
            if (srcEntry instanceof File) {
                File desFile = new File(name,(desFather.getPath()+"/"+name).
                        replaceAll("/+","/"),manager.getCount(),desFather,manager.getNowUser().getName());
                desFile.write(((File) srcEntry).cat(),manager.getCount());
                desFather.addFile(desFile);
                desFather.setLastTime(manager.getCount());
            }
            else if (srcEntry instanceof Dir) {
                Dir desDir = new Dir(name,(desFather.getPath()+"/"+name).replaceAll("/+","/"),
                        manager.getCount(),desFather,manager.getNowUser().getName());
                desDir.copy((Dir)srcEntry,manager.getCount(),manager.getNowUser().getName());
                desFather.addDir(desDir);
                desFather.setLastTime(manager.getCount());
            }
        }
        else if (desEntry.getPath().equals(srcEntry.getPath())) {
            throw new PathInvalidException(desPath);
        }
        else if (isFather(srcEntry.getPath(), desEntry.getPath())) {
            throw new PathInvalidException(desPath);
        }
        else if (desEntry instanceof Dir && srcEntry instanceof File){
            if (((Dir) desEntry).containsDir(srcEntry.getName())) {
                throw new PathExistException(desPath + "/" + srcEntry.getName());
            } else if (((Dir) desEntry).containsFile(srcEntry.getName())) {
                onlyCopyFile((File)srcEntry,((Dir) desEntry).getFile(srcEntry.getName()));
            } else if (!((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    !((Dir) desEntry).containsFile(srcEntry.getName())){
                createAndCopyFile((File)srcEntry,(Dir)desEntry,srcEntry.getName());
            }
        }
        else if (srcEntry instanceof File && desEntry instanceof  File) {
            onlyCopyFile((File)srcEntry,(File)desEntry);
        }
        else if (srcEntry instanceof Dir && desEntry instanceof Dir) {
            if ((((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    ((Dir) desEntry).getDir(srcEntry.getName()).getDirCount() == 0 )||
                    ((Dir) desEntry).containsFile(srcEntry.getName()) ){
                throw new PathExistException(desPath+"/"+ srcEntry.getName());
            }
            else if ((((Dir) desEntry).containsDir(srcEntry.getName()) &&
                    ((Dir) desEntry).getDir(srcEntry.getName()).getDirCount() > 0 )){
                ((Dir) desEntry).getDir(srcEntry.getName()).copy((Dir)srcEntry,manager.getCount(),manager.getNowUser().getName());
                ((Dir) desEntry).getDir(srcEntry.getName()).setLastTime(manager.getCount());
            }
            else {
                Dir createDir = new Dir(srcEntry.getName(),(desEntry.getPath()+"/"+srcEntry.getName()).
                        replaceAll("/+","/"),manager.getCount(),(Dir)desEntry,manager.getNowUser().getName());
                createDir.copy((Dir)srcEntry,manager.getCount(),manager.getNowUser().getName());
                ((Dir) desEntry).setLastTime(manager.getCount());
                ((Dir) desEntry).addDir(createDir);
            }
        }
        else if (srcEntry instanceof Dir && desEntry instanceof File) {
            throw new PathExistException(desPath);
        }
    }

    public void onlyCopyFile(File srcFile,File desFile){
        if (srcFile instanceof SoftLink) {

        }
        else if (srcFile instanceof HardLink) {
            File srcLinkFile = ((HardLink) srcFile).getFile();
            if (desFile instanceof SoftLink) {

            }
            else if (desFile instanceof HardLink){
                File desLinkFile = ((HardLink) desFile).getFile();
                desLinkFile.write(srcLinkFile.cat(),manager.getCount());
            }
            else {
                desFile.write(srcLinkFile.cat(),manager.getCount());
            }
        }
        else {
            if (desFile instanceof SoftLink){
                createAndCopyFile(srcFile,desFile.getFather(),desFile.getName());
            }
            else if (desFile instanceof HardLink) {
                File desLinkFIle = ((HardLink) desFile).getFile();
                desLinkFIle.write(srcFile.cat(),manager.getCount());
            }
            else {
                desFile.write(srcFile.cat(),manager.getCount());
            }
        }
    }

    public void createAndCopyFile(File subFile, Dir father, String name){
        if (subFile instanceof SoftLink) {
            SoftLink tempSoftLink = new SoftLink(name, (father.getPath() + "/" + subFile.getName()).
                    replaceAll("/+", "/"), manager.getCount(), father, manager.getNowUser().getName());
            tempSoftLink.setPointPath(((SoftLink) subFile).getPointPath());
            father.addFile(tempSoftLink);
        }
        else if (subFile instanceof HardLink) {
            HardLink tempHardLink = new HardLink(name, (father.getPath() + "/" + subFile.getName()).
                    replaceAll("/+", "/"), manager.getCount(), father, manager.getNowUser().getName());
            tempHardLink.setFile(((HardLink) subFile).getFile());
            father.addFile(tempHardLink);
        }
        else {
            File tempFile = new File(name, (father.getPath() + "/" + subFile.getName()).
                    replaceAll("/+", "/"), manager.getCount(), father, manager.getNowUser().getName());
            tempFile.write(subFile.cat(), manager.getCount());
            father.addFile(tempFile);
        }
    }


    // fuck file
    // TODO
    public File findFile(String path) throws FileSystemException {
        if (path.endsWith("/")) {
            throw new PathInvalidException(path);
        }
        Entry tempEntry = findEntry(path);
        if (tempEntry instanceof File) {
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
        File file = findFile(path);

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
        nowTempDir = findDir(abPath.substring(0, abPath.lastIndexOf("/")));
        if (nowTempDir == null) {
            throw new PathInvalidException(path);
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
