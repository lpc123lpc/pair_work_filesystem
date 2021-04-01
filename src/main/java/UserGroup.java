import java.util.HashMap;

public class UserGroup {
    private String name;
    private HashMap<String,User> users = new HashMap<>();

    public UserGroup(String name){
        this.name = name;
    }

    public void addUser(User user){
        users.put(user.getName(),user);
    }


    public String getName() {
        return name;
    }

    public void delete(String name){
        users.remove(name);
    }

    public HashMap<String, User> getUsers() {
        return users;
    }
}
