public class HardLink extends File {
    private File file;

    HardLink(String name, String path, int createTime, Dir father, String createUser) {
        super(name, path, createTime, father, createUser);
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public int getSize() {
        return file.getSize();
    }

    @Override
    public String info() {
        return file.getCreateUser() + " " + file.getCreateUser() + " " + file.getCreateTime() + " " +
                file.getLastTime() + " " + file.getSize() + " " +  file.getFileCount()+ " " + getPath();
    }
}
