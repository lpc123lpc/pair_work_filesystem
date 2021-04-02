import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class DirTest {

    private Dir root = new Dir("root","/root",0,null, "root");
    private Dir test = new Dir("test","/root/test",0,root,"root");
    private Dir test2 = new Dir("test2","/root/test2",0,root,"root");
    private File testFile = new File("testFile","/root/testFile",0,root, "sb");
    private File testFile2 = new File("testFile2","/root/testFile2",0,root, "snb");
    private HashMap<String,Dir> testMap =new HashMap<>();
    private HashMap<String,File> testFileMAp = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        root.setFather(root);
        root.getSubDir().put("test",test);
        root.getSubDir().put("test2",test2);
        root.getSubFile().put("testFile",testFile);
        root.getSubFile().put("testFile2",testFile2);

        testMap.put("test",test);
        testMap.put("test2",test2);

        testFileMAp.put("testFile",testFile);
        testFileMAp.put("testFile2",testFile2);
    }

    @After
    public void tearDown() throws Exception {
        assertEquals(root.toString(), root.getPath());
    }

    @Test
    public void getPath() {
        assertEquals("/root",root.getPath());
    }

    @Test
    public void getName() {
        assertEquals("root",root.getName());
    }

    @Test
    public void getLastTime() {
        assertEquals(0,root.getLastTime());
        root.setLastTime(7);
        assertEquals(7,root.getLastTime());
    }

    @Test
    public void getCreateTime() {
        assertEquals(0,root.getCreateTime());
    }

    @Test
    public void getFather() {
        assertEquals(root,root.getFather());
    }

    @Test
    public void getSubDir() {
        HashMap<String,Dir> dirMap = root.getSubDir();
        for (String name : dirMap.keySet()) {
            if (!name.equals(".") && !name.equals("..")){
                Dir temp = dirMap.get(name);
                Dir test = testMap.get(name);
                assertEquals(temp,test);
            }
        }

    }

    @Test
    public void getSubFile() {
        HashMap<String,File> fileMap = root.getSubFile();
        for (String name : fileMap.keySet()) {
            File temp = fileMap.get(name);
            File test = testFileMAp.get(name);
            assertEquals(temp,test);
        }
    }

    @Test
    public void setFather() {
        test.setFather(test2);
        assertEquals(test.getFather(),test2);
    }

    @Test
    public void setLastTime() {
        root.setLastTime(4);
        assertEquals(4,root.getLastTime());
    }

    @Test
    public void setName() {
        test.setName("sb");
        assertEquals("sb",test.getName());
    }

    @Test
    public void delete() {
        root.delete();
        assertEquals(0,root.getSubDir().size());
        assertEquals(0,root.getSubFile().size());
    }

    @Test
    public void info() {
        assertEquals("root root 0 0 0 4 /root",root.info());
    }

    @Test
    public void ls() {
        assertEquals("test test2 testFile testFile2 ",root.ls());
    }

    @Test
    public void getSize() {
        assertEquals(0,root.getSize());
    }

    @Test
    public void getDir() {
        assertEquals(test,root.getDir("test"));
    }

    @Test
    public void getFile() {
        assertEquals(testFile,root.getFile("testFile"));
    }

    @Test
    public void addDir() {
        Dir dir = new Dir("dir","/root/dir",0,root,"root");
        root.addDir(dir);
        testMap.put("dir",dir);
        HashMap<String,Dir> DirMap = root.getSubDir();
        for (String name : DirMap.keySet()) {
            if (!name.equals(".") && !name.equals("..")){
                Dir temp = DirMap.get(name);
                Dir test = testMap.get(name);
                assertEquals(temp,test);
            }
        }
    }

    @Test
    public void addFile() {
        File testFile3 = new File("testFile3","/root/testFile3",0,root, "sb");
        root.addFile(testFile3);
        testFileMAp.put("testFile3",testFile3);
        HashMap<String,File> fileMap = root.getSubFile();
        for (String name : fileMap.keySet()) {
            File temp = fileMap.get(name);
            File test = testFileMAp.get(name);
            assertEquals(temp,test);
        }
    }


}