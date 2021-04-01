public interface Entry {
    String getPath();
    String getName();
    void setPath(String path, int lastTime);
    void setFather(Dir father);
    Dir getFather();
    String info();
    void setName(String name);
}
