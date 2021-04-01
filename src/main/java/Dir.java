import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Dir implements Entry{
    private String name;
    private String path;
    private int createTime;
    private int lastTime;
    private int size = 0;
    private int dirCount = 0;
    private Dir father;
    private String createUser;
    private HashMap<String, Dir> subDir = new HashMap<String, Dir>();
    private HashMap<String, File> subFile = new HashMap<String, File>();

    Dir(String name, String path, int createTime, Dir father,String createUser) {
        this.name = name;
        this.path = path;
        this.createTime = createTime;
        this.lastTime = createTime;
        this.father = father;
        this.createUser = createUser;
        size = 0;
        subDir.put(".", this);
        subDir.put("..", father);
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public int getDirCount() {
        return subDir.size() + subFile.size() - 2;
    }


    @Override
    public void setPath(String path, int lastTime) {
        this.path = path;
        setLastTime(lastTime);
        for (Map.Entry<String, Dir> temp : subDir.entrySet()) {
            if (!temp.getKey().equals(".") && !temp.getKey().equals("..")) {
                temp.getValue().setPath((path + "/" + temp.getValue().getName()).replaceAll("/+", "/"),
                        lastTime);
            }
        }
        for (File temp : subFile.values()) {
            temp.setPath((path + "/" + temp.getName()).replaceAll("/+", "/"), lastTime);
        }
    }

    public int getLastTime() {
        return lastTime;
    }

    public int getCreateTime() {
        return createTime;
    }

    public Dir getFather() {
        return father;
    }

    public HashMap<String, Dir> getSubDir() {
        return subDir;
    }

    public HashMap<String, File> getSubFile() {
        return subFile;
    }

    public void setFather(Dir father) {
        this.father = father;
        subDir.put("..", father);
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void delete() {
        for (Map.Entry<String, Dir> temp : subDir.entrySet()) {
            if (!temp.getKey().equals(".") && !temp.getKey().equals("..")) {
                temp.getValue().delete();
            }
        }
        subDir.clear();
        subFile.clear();
    }

    public String info() {
        return createUser + " " + createUser + " " + createTime + " " +
                lastTime + " " + getSize() + " " + getDirCount() + " " + path;
    }

    public String ls() {
        Set<String> fileSet = new TreeSet<>();
        fileSet.addAll(subFile.keySet());
        fileSet.addAll(subDir.keySet());
        StringBuilder out = new StringBuilder();
        for (String name : fileSet) {
            if (!name.equals(".") && !name.equals("..")) {
                out.append(name).append(" ");
            }

        }
        return out.toString();
    }

    public int getSize() {
        int size = 0;
        for (Map.Entry<String, Dir> temp : subDir.entrySet()) {
            if (!temp.getKey().equals(".") && !temp.getKey().equals("..")) {
                size += temp.getValue().getSize();
            }
        }
        for (File file : subFile.values()) {
            size += file.getSize();
        }
        return size;
    }

    public boolean containsFile(String name){
        return subFile.containsKey(name);
    }

    public boolean containsDir(String name){
        return subDir.containsKey(name);
    }


    public Dir getDir(String name) {
        return subDir.get(name);
    }

    public File getFile(String name) {
        return subFile.get(name);
    }

    public void addDir(Dir dir) {
        subDir.put(dir.getName(), dir);
    }

    public void addFile(File file) {
        subFile.put(file.getName(), file);
    }

    public void copy(Dir dir,int createTime,String createUser) {
        for (Map.Entry<String, Dir> temp : subDir.entrySet()) {
            if (!temp.getKey().equals(".") && !temp.getKey().equals("..")){
                Dir tempDir = new Dir(temp.getValue().getName(),(path+"/"+temp.getValue().getName()).
                        replaceAll("/+","/"),createTime,this,createUser);
                tempDir.copy(temp.getValue(),createTime,createUser);
                subDir.put(temp.getValue().getName(),tempDir);
            }
        }
        for (File subFile : dir.getSubFile().values()) {
            if (subFile instanceof SoftLink) {
                SoftLink tempSoftLink = new SoftLink(subFile.getName(), (path + "/" + subFile.getName()).
                        replaceAll("/+", "/"), createTime, this, createUser);
                tempSoftLink.setPointPath(((SoftLink) subFile).getPointPath());
                this.subFile.put(tempSoftLink.getName(),tempSoftLink);
            }
            else if (subFile instanceof HardLink) {
                HardLink tempHardLink = new HardLink(subFile.getName(), (path + "/" + subFile.getName()).
                        replaceAll("/+", "/"), createTime, this, createUser);
                tempHardLink.setFile(((HardLink) subFile).getFile());
                this.subFile.put(tempHardLink.getName(),tempHardLink);
            }
            else {
                File tempFile = new File(subFile.getName(), (path + "/" + subFile.getName()).
                        replaceAll("/+", "/"), createTime, this, createUser);
                tempFile.write(subFile.cat(), createTime);
                this.subFile.put(subFile.getName(), tempFile);
            }
        }
    }

    @Override
    public String toString() {
        return path;
    }
}
