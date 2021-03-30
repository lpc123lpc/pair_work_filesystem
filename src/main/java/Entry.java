public interface Entry {
    String getPath();
    String getName();
    void setPath(String path);
    void setFather(Dir father);
    Dir getFather();
    void setName(String name);
}
