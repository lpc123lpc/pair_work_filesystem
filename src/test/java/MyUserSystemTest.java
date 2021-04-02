import com.fileutils.specs2.models.UserSystem;
import com.fileutils.specs2.models.UserSystemException;
import exceptions.GroupExistsException;
import exceptions.GroupInvalidException;
import exceptions.PermittionException;
import exceptions.UserExistException;
import exceptions.UserInvalidException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class MyUserSystemTest {
    private User root;
    private User common =  new User("common",2);
    private Manager manager;
    private MyUserSystem userSystem = new MyUserSystem();

    @Before
    public void setUp() throws Exception {
        manager = Manager.getInstance();
        root = manager.getNowUser();
        Dir nowDir = new Dir("root","/",0,null,"root");
        manager.setNowDir(nowDir);
        userSystem.addUser("common");
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
        try{
            manager.setNowUser(root);
            userSystem.addUser("root");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }
        try {
            manager.setNowUser(root);
            userSystem.addUser("?sadkj;");
        } catch (UserSystemException e) {
            assertTrue(e instanceof UserInvalidException);
        }
        try {
            manager.setNowUser(root);
            userSystem.addUser("1111111111111111111111111111111111111111111111111111111111111111111111" +
                    "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                    "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                    "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                    "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                    "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                    "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111" +
                    "11111111111111111111111111111111111111111111111111");
        } catch (UserSystemException e) {
            assertTrue(e instanceof UserInvalidException);
        }
        try {
            manager.setNowUser(root);
            userSystem.addUser("common");
        } catch (UserSystemException e) {
            assertTrue(e instanceof UserExistException);
        }
        try {
            userSystem.deleteUser("common");
            userSystem.addUser("common");
        } catch (UserSystemException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteUser() {
        try{
            manager.setNowUser(common);
            userSystem.addUser("test");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }
        try{
            manager.setNowUser(root);
            userSystem.addUser("root");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }
        try {
            manager.setNowUser(root);
            userSystem.deleteUser("lkasjd");
        } catch (UserSystemException e) {
            assertTrue(e instanceof UserInvalidException);
        }
    }

    @Test
    public void addGroup() {
        try{
            manager.setNowUser(common);
            userSystem.addGroup("test");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }
        try{
            manager.setNowUser(root);
            userSystem.addGroup("root");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }
        try {
            manager.setNowUser(root);
            userSystem.addGroup("common");
        } catch (UserSystemException e) {
            assertTrue(e instanceof GroupExistsException);
        }
        try {
            manager.setNowUser(root);
            userSystem.addGroup("?sla;dj");
        } catch (UserSystemException e) {
            assertTrue(e instanceof GroupInvalidException);
        }
        try {
            manager.setNowUser(root);
            userSystem.addGroup("test");
        } catch (UserSystemException e) {
            //jkh
        }
    }

    @Test
    public void deleteGroup() {
        try{
            manager.setNowUser(common);
            userSystem.deleteGroup("test");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }
        try{
            manager.setNowUser(root);
            userSystem.deleteGroup("root");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }
        try {
            manager.setNowUser(root);
            userSystem.deleteGroup("alskjd");
        } catch (UserSystemException e) {
            assertTrue(e instanceof GroupInvalidException);
        }
        try {
            manager.setNowUser(root);
            userSystem.deleteGroup("common");
        } catch (UserSystemException e) {
            assertTrue(e instanceof GroupInvalidException);
        }
        try {
            manager.setNowUser(root);
            userSystem.addGroup("test");
            userSystem.deleteGroup("test");
        } catch (UserSystemException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void addUserToGroup() {
        try{
            manager.setNowUser(common);
            userSystem.addUserToGroup("common","root");
        }catch (UserSystemException e){
            assertTrue(e instanceof PermittionException);
        }
        try{
            manager.setNowUser(root);
            userSystem.addUserToGroup("test","common");
        }catch (UserSystemException e){
            assertTrue(e instanceof GroupInvalidException);
        }
        try {
            manager.setNowUser(root);
            userSystem.addUserToGroup("common","lkasjd");
        } catch (UserSystemException e) {
            assertTrue(e instanceof UserInvalidException);
        }
        try {
            userSystem.addUser("test");
            userSystem.addUserToGroup("common","test");
        } catch (UserSystemException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void changeUser() {
        try {
            userSystem.changeUser("common");
            userSystem.changeUser("root");
        } catch (UserSystemException e) {
            assertTrue(e instanceof PermittionException);
        }
        try {
            userSystem.changeUser("root");
        } catch (UserSystemException e) {
            assertTrue(e instanceof PermittionException);
        }
        try {
            userSystem.exitUser();
            userSystem.changeUser("test");
        } catch (UserSystemException e) {
            assertTrue(e instanceof UserInvalidException);
        }
        try {
            assertEquals("common",userSystem.changeUser("common"));;
        } catch (UserSystemException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void exitUser() {

        try{
            manager.setNowUser(root);
            userSystem.exitUser();
        } catch (UserSystemException e) {
            e.printStackTrace();
        }
        try{
            userSystem.changeUser("common");
            userSystem.exitUser();
            assertEquals(root,manager.getNowUser());
        } catch (UserSystemException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void queryUser() {
        try {
            assertEquals("root",userSystem.queryUser());
        } catch (UserSystemException e) {
            e.printStackTrace();
        }

    }
}