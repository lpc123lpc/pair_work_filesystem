public class File {
    private String name;
    private String content;
    private String path;
    private Dir father;
    private int createTime;
    private int lastTime;

    File(String name,String path,int createTime,Dir father){
        this.name = name;
        this.content = "";
        this.path = path;
        this.father = father;
        this.createTime = createTime;
        this.lastTime = createTime;
    }

    public String getName() {
        return name;
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
        return path+": "+createTime+" "+lastTime+" "+content.length();
    }


}

//  /home/foo.txt