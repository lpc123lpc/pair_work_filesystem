public class SoftLink extends File{

    private String pointPath;

    SoftLink(String name, String path, int createTime, Dir father, String createUser){
        super(name, path, createTime, father, createUser);
    }

    public void setPointPath(String pointPath) {
        this.pointPath = pointPath;
    }

    public String getPointPath() {
        return pointPath;
    }

    @Override
    public int getSize() {
        return 0;
    }
}
