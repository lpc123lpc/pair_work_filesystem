import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Dir {
    private String name;
    private String path;
    private int createTime;
    private int lastTime;
    private int size = 0;
    private Dir father;
    private HashMap<String, Dir> subDir = new HashMap<String, Dir>();
    private HashMap<String, File> subFile = new HashMap<String, File>();

    Dir(String name, String path, int createTime, Dir father) {
        this.name = name;
        this.path = path;
        this.createTime = createTime;
        this.lastTime = createTime;
        this.father = father;
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
        return path + ": " + createTime + " " + lastTime + " " + getSize();
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

    @Override
    public String toString() {
        return path;
    }
}
