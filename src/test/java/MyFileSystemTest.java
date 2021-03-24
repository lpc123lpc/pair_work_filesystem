import com.fileutils.specs1.models.FileSystemException;
import exceptions.PathException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyFileSystemTest {
    private MyFileSystem myFs = new MyFileSystem();

    @Before
    public void setUp() throws Exception {
        myFs.makeDirectory("/home");
        myFs.touchFile("test1.java");
        myFs.fileWrite("test1.java", "public class Main@n{}");
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void changeDirectory() {
        try {
            assertEquals("/home", myFs.changeDirectory("/home"));
            assertEquals("/home", myFs.changeDirectory("home"));
            myFs.changeDirectory("/errorPath");
        } catch (Exception e) {
            assertTrue(e instanceof PathException);
        }
    }

    @Test
    public void list() {
        try {
            assertEquals("home test1.java ", myFs.list("/"));
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
    }

    @Test
    public void makeDirectory() {
        try {
            assertEquals("/home/work", myFs.makeDirectory("/home/work"));
            myFs.makeDirectory("/home/work"); // dir exists
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
        try {
            myFs.touchFile("testFile");
            myFs.makeDirectory("testFile");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }

        try {
            myFs.changeDirectory("home");
            assertEquals("/home/test", myFs.makeDirectory("test"));
            myFs.makeDirectory("/home/sb/sb/test");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
    }

    @Test
    public void nameIsValid() {
        assertFalse(myFs.nameIsValid("qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq" +
                "qqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqqq"));
    }

    @Test
    public void makeDirectoryRecursively() {
        try {
            myFs.makeDirectoryRecursively("/home/work/sb/hhh");
            myFs.makeDirectoryRecursively("home/work/sb1/hhh1");
            myFs.makeDirectoryRecursively("home/work/sb1/0");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
    }


    @Test
    public void removeRecursively() {
        try {
            myFs.removeRecursively("/");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }

        try {
            myFs.makeDirectoryRecursively("/home/test/sb");
            assertEquals("/home", myFs.removeRecursively("/home"));
            myFs.makeDirectoryRecursively("/home/test/sb");
            myFs.changeDirectory("/home/test");
            myFs.removeRecursively("/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }


    }

    @Test
    public void information() {
        System.out.println("begin");
        try {
            System.out.println("begin");
            myFs.makeDirectory("/hhh");
            myFs.makeDirectoryRecursively("/home/test/sb");
            System.out.println("testing");
            assertEquals("/home/test/sb: 5 5 0", myFs.information("/home/test/sb"));
            myFs.information("/home/kk/sb");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
        try {
            myFs.touchFile("/home/test/sb/test.txt");
            assertEquals("/home/test/sb/test.txt: 8 8 0", myFs.information("/home/test/sb/test.txt"));
            myFs.information("/home/test/sb/test.txt1");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
    }

    @Test
    public void catFile() {
        try {
            assertEquals("public class Main\n{}", myFs.catFile("test1.java"));
            myFs.catFile("tes");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }

    }

    @Test
    public void removeFile() {
        try {
            assertEquals("/test1.java", myFs.removeFile("test1.java"));
            myFs.removeFile("te");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
    }

    @Test
    public void fileWrite() {
        try {
            myFs.fileWrite("/test2.java", "public");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fileAppend() {
        try {
            myFs.fileAppend("/test2.java", "public");
            myFs.fileAppend("/test2.java", " class");
            myFs.fileAppend("/te", "dsa");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
    }

    @Test
    public void touchFile() {
        try {
            myFs.touchFile("/jj/tes.txt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }
        try {
            myFs.touchFile("/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathException);
        }

    }

}