public class File implements Entry{
    private String name;
    private String content;
    private String path;
    private Dir father;
    private int createTime;
    private int lastTime;
    private int fileCount = 1;
    private String createUser;

    File(String name,String path,int createTime, Dir father, String createUser){
        this.name = name;
        this.content = "";
        this.path = path;
        this.father = father;
        this.createTime = createTime;
        this.lastTime = createTime;
        this.createUser = createUser;
    }

    File(){}

    public String getName() {
        return name;
    }

    @Override
    public void setPath(String path, int lastTime) {
        this.path = path;
        setLastTime(lastTime);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFather(Dir father) {
        this.father = father;
    }

    public Dir getFather() {
        return father;
    }

    public String getPath() {
        return path;
    }

    public int getCreateTime() {
        return createTime;
    }

    public int getLastTime() {
        return lastTime;
    }

    public String getCreateUser() {
        return createUser;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setLastTime(int lastTime) {
        this.lastTime = lastTime;
    }



    public void write(String content,int lastTime){
        this.lastTime = lastTime;
        this.content = content.replaceAll("@n","\n");;
    }



    public int getSize(){
        return this.content.length();
    }

    public String cat(){
        return this.content;
    }

    public void append(String content,int lastTime){
        this.lastTime = lastTime;

        this.content = (this.content + content).replaceAll("@n","\n");;
    }

    public String info(){
        return createUser + " " + createUser + " " + createTime + " " +
                lastTime + " " + getSize() + " " + fileCount + " " + path;
    }


}

//  /home/foo.txt