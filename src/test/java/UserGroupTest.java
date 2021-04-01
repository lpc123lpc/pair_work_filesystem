import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UserGroupTest {
    private UserGroup group = new UserGroup("root");


    @Before
    public void setUp() throws Exception {
        User root = new User("root",1);
        group.addUser(root);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void addUser() {
    }


    @Test
    public void getName() {
        assertEquals("root",group.getName());
    }

    @Test
    public void delete() {
        group.delete("root");
        assertEquals(0,group.getUsers().size());
    }

    @Test
    public void getUsers() {
    }
}