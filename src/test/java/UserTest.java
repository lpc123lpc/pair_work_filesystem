import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;

import static org.junit.Assert.*;

public class UserTest {
    private User root;
    private UserGroup rootGroup;
    @Before
    public void setUp() throws Exception {
        root = new User("root",1);
        rootGroup = new UserGroup("root");

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void setMainGroup() {
        root.setMainGroup(rootGroup);
        assertEquals(rootGroup,root.getMainGroup());
    }

    @Test
    public void getName() {
        assertEquals("root",root.getName());
    }

    @Test
    public void getAdditionGroup() {
    }

    @Test
    public void getPermission() {
    }

    @Test
    public void setName() {
    }

    @Test
    public void addGroup() {
    }

    @Test
    public void delete() {
    }
}