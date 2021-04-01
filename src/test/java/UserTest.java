import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

import java.util.HashMap;

import static org.junit.Assert.*;

public class UserTest {
    private User root;
    private UserGroup rootGroup;
    private HashMap<String,UserGroup> additionGroup= new HashMap<>() ;
    @Before
    public void setUp() throws Exception {
        root = new User("root",1);
        rootGroup = new UserGroup("root");
        root.setMainGroup(rootGroup);
        UserGroup addGroup = new UserGroup("add");
        additionGroup.put("add",addGroup);
        root.addGroup(addGroup);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setMainGroup() {

        assertEquals(rootGroup,root.getMainGroup());
    }

    @Test
    public void getName() {
        assertEquals("root",root.getName());
    }

    @Test
    public void getAdditionGroup() {
        assertTrue(root.getAdditionGroup().containsKey("add"));
    }

    @Test
    public void getPermission() {
        assertEquals(1,root.getPermission());
    }

    @Test
    public void setName() {
        root.setName("test");
        assertEquals("test",root.getName());
    }

    @Test
    public void addGroup() {
    }

    @Test
    public void delete() {
        root.delete("add");
        assertFalse(root.getAdditionGroup().containsKey("add"));
    }
}