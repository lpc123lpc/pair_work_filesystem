import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileTest {

    private Dir root = new Dir("root","/root",0,null,"root");
    private File file = new File("file","/root/file",1,root, "shshhs");

    @Before
    public void setUp() throws Exception {
        root.setFather(root);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void getName() {
        assertEquals("file",file.getName());
    }

    @Test
    public void getFather() {
        assertEquals(root,file.getFather());
    }

    @Test
    public void getPath() {
        assertEquals("/root/file",file.getPath());
    }

    @Test
    public void getCreateTime() {
        assertEquals(1,file.getCreateTime());
    }

    @Test
    public void getLastTime() {
        assertEquals(1,file.getLastTime());
    }

    @Test
    public void setLastTime() {
        file.setLastTime(2);
        assertEquals(2,file.getLastTime());
    }

    @Test
    public void write() {
        String content = "aaaa@nb";
        file.write(content,2);
        content = content.replaceAll("@n","\n");
        assertEquals(content,file.cat());
    }

    @Test
    public void getSize() {
        String content = "aaaa@nb";
        file.write(content,2);
        assertEquals(6,file.getSize());
    }

    @Test
    public void cat() {
        String content = "aaaa@nb";
        file.write(content,2);
        content = content.replaceAll("@n","\n");
        assertEquals(content,file.cat());
    }

    @Test
    public void append() {
        String content = "aaaa@nb";
        file.write(content,2);
        content = content.replaceAll("@n","\n");
        file.append("bb",3);
        assertEquals(content+"bb",file.cat());
    }

    @Test
    public void info() {
        String act = "shshhs shshhs 1 1 0 1 /root/file";
        assertEquals(act,file.info());
    }
}