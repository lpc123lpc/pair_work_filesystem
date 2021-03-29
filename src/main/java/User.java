import java.util.ArrayList;
import java.util.HashMap;

public class User {
    private String name;
    private HashMap<String,UserGroup> additionGroup= new HashMap<>() ;
    private UserGroup mainGroup;
    private int permission;

    public User(String name,int permission){
        this.name = name;
        this.permission = permission;
    }

    public void setMainGroup(UserGroup mainGroup) {
        this.mainGroup = mainGroup;
    }

    public String getName() {
        return name;
    }

    public HashMap<String,UserGroup> getAdditionGroup() {
        return additionGroup;
    }

    public int getPermission() {
        return permission;
    }

    public UserGroup getMainGroup() {
        return mainGroup;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addGroup(UserGroup group){
        additionGroup.put(group.getName(),group);
    }

    public void delete(String name){
        additionGroup.remove(name);
    }

}
