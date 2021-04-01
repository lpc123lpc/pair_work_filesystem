public class Manager {
    private int count;
    private User nowUser;
    private String rootPath; // absolute path
    private Dir nowDir; //
    private int fileSysFlag = 0;
    private static Manager instance = new Manager();

    private Manager(){}

    public static Manager getInstance(){
        return instance;
    }

    public void setCount(int count) {
        this.count = count;
    }
    // rm /home/target/..


    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setNowDir(Dir nowDir) {
        this.nowDir = nowDir;
    }

    public void setNowUser(User nowUser) {
        this.nowUser = nowUser;
    }

    public void setFileSysFlag(int fileSysFlag) {
        this.fileSysFlag = fileSysFlag;
    }

    public int getFileSysFlag() {
        return fileSysFlag;
    }

    public int getCount() {
        return count;
    }

    public Dir getNowDir() {
        return nowDir;
    }

    public String getRootPath() {
        return rootPath;
    }

    public User getNowUser() {
        return nowUser;
    }

    public void update(){
        count++;
    }
}
