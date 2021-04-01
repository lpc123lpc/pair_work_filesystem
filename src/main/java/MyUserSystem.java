import com.fileutils.specs2.models.UserSystem;
import com.fileutils.specs2.models.UserSystemException;
import exceptions.GroupExistsException;
import exceptions.GroupInvalidException;
import exceptions.PermittionException;
import exceptions.UserExistException;
import exceptions.UserInvalidException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyUserSystem implements UserSystem {
    private User root = new User("root",1);
    private HashMap<String, UserGroup> groups = new HashMap<>();
    private HashMap<String, User> users = new HashMap<>();
    private Manager manager;

    public MyUserSystem(){
        UserGroup rootGroup = new UserGroup("root");
        rootGroup.addUser(root);
        root.setMainGroup(rootGroup);
        groups.put("root",rootGroup);
        manager = Manager.getInstance();
        manager.setNowUser(root);
    }

    private boolean nameIsInvalid(String name){
        if (name.length() > 128) {
            return true;
        }
        Pattern regex = Pattern.compile("[a-zA-Z._][a-zA-Z._]*");
        Matcher m = regex.matcher(name);
        return !m.matches();
    }

    @Override
    public void addUser(String username) throws UserSystemException {
        manager.update(); // upadte count
        if (manager.getNowUser().getPermission() > 1 || username.equals("root")) {
            throw new PermittionException();
        }
        if (nameIsInvalid(username)) {
            throw new UserInvalidException(username);
        }
        if (users.containsKey(username)){
            throw new UserExistException(username);
        }
        User user = new User(username,2);
        users.put(username,user);
        if (groups.containsKey(username)){
            groups.get(username).addUser(user);
            user.setMainGroup(groups.get(username));
        }
        else {
            UserGroup tempGroup = new UserGroup(username);
            tempGroup.addUser(user);
            user.setMainGroup(tempGroup);
            groups.put(username,tempGroup);
        }
    }

    @Override
    public void deleteUser(String username) throws UserSystemException {
        manager.update();
        if (manager.getNowUser().getPermission() > 1 || username.equals("root")) {
            throw new PermittionException();
        }
        if (!users.containsKey(username)) {
            throw new UserInvalidException(username);
        }
        for (Map.Entry<String, UserGroup> temp : users.get(username).getAdditionGroup().entrySet()) {
            temp.getValue().delete(username);
        }
        groups.get(username).getUsers().remove(username);
        if (groups.get(username).getUsers().size() == 0) {
            groups.remove(username);
        }
        users.remove(username);
    }

    @Override
    public void addGroup(String groupName) throws UserSystemException {
        manager.update();
        if (manager.getNowUser().getPermission() > 1 || groupName.equals("root")) {
            throw new PermittionException();
        }
        if (groups.containsKey(groupName)) {
            throw new GroupExistsException(groupName);
        }
        if (nameIsInvalid(groupName)) {
            throw new GroupInvalidException(groupName);
        }
        groups.put(groupName, new UserGroup(groupName));
    }

    @Override
    public void deleteGroup(String groupName) throws UserSystemException {
        manager.update();
        if (manager.getNowUser().getPermission() > 1 || groupName.equals("root")) {
            throw new PermittionException();
        }
        if (!groups.containsKey(groupName) ||  users.containsKey(groupName)) {
            throw new GroupInvalidException(groupName);
        }
        for (User temp : groups.get(groupName).getUsers().values()) {
            temp.delete(groupName);
        }
        groups.remove(groupName);
    }

    @Override
    public void addUserToGroup(String groupName, String username) throws UserSystemException {
        manager.update();
        if (manager.getNowUser().getPermission() > 1 || username.equals("root") || groupName.equals("root")) {
            throw new PermittionException();
        }
        if (!groups.containsKey(groupName)) {
            throw new GroupInvalidException(groupName);
        }
        if (!users.containsKey(username) || groups.get(groupName).getUsers().containsKey(username)) {
            throw new UserInvalidException(username);
        }
        groups.get(groupName).addUser(users.get(username));
        users.get(username).addGroup(groups.get(groupName));

    }

    @Override
    public String changeUser(String username) throws UserSystemException {
        manager.update();
        if (manager.getNowUser().getPermission() > 1) {
            throw new PermittionException();
        }
        if (username.equals("root")) {
            throw new PermittionException();
        }
        if (!users.containsKey(username)) {
            throw new UserInvalidException(username);
        } else {
            manager.setNowUser(users.get(username));
            manager.setRootPath(manager.getNowDir().getPath());
        }
        return manager.getNowUser().getName();
    }

    @Override
    public String exitUser() throws UserSystemException {
        manager.update();
        if (manager.getNowUser().getPermission() <= 1) {
            throw new PermittionException();
        }
        manager.setNowUser(root);
        manager.setFileSysFlag(1);
        return "exit";
    }

    @Override
    public String queryUser() throws UserSystemException {
        manager.update();
        return manager.getNowUser().getName();
    }
}
