import com.fileutils.specs2.models.UserSystem;
import com.fileutils.specs2.models.UserSystemException;
import exceptions.PermittionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class MyUserSystemTest {
    private User root = new User("root",1);
    private User common =  new User("common",2);
    private Manager manager;
    private MyUserSystem userSystem = new MyUserSystem();

    @Before
    public void setUp() throws Exception {
        UserGroup rootGroup = new UserGroup("root");
        root.setMainGroup(rootGroup);
        manager = Manager.getInstance();
        manager.setNowUser(root);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addUser() {
        try{
            manager.setNowUser(common);
            userSystem.addUser("test");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }

    }

    @Test
    public void deleteUser() {
    }

    @Test
    public void addGroup() {
    }

    @Test
    public void deleteGroup() {
    }

    @Test
    public void addUserToGroup() {
    }

    @Test
    public void changeUser() {
    }

    @Test
    public void exitUser() {
    }

    @Test
    public void queryUser() {
    }
}