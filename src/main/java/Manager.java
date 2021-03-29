public class Manager {
    private int count;
    private User nowUser;
    private Dir rootPath; // absolute path
    private Dir nowDir; //
    private static Manager instance = new Manager();


    private Manager(){}

    public static Manager getInstance(){
        return instance;
    }

    public void setCount(int count) {
        this.count = count;
    }
    // rm /home/target/..


    public void setRootPath(Dir rootPath) {
        this.rootPath = rootPath;
    }

    public void setNowDir(Dir nowDir) {
        this.nowDir = nowDir;
    }

    public void setNowUser(User nowUser) {
        this.nowUser = nowUser;
    }

    public int getCount() {
        return count;
    }

    public Dir getNowDir() {
        return nowDir;
    }

    public Dir getRootPath() {
        return rootPath;
    }

    public User getNowUser() {
        return nowUser;
    }

    public void update(){
        count++;
    }
}
