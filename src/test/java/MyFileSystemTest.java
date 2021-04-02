import com.fileutils.specs2.models.FileSystemException;
import exceptions.PathExistException;
import exceptions.PathInvalidException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MyFileSystemTest {
    private MyFileSystem myFs = new MyFileSystem();

    @Before
    public void setUp() throws Exception {
        Manager.getInstance().setNowUser(new User("root", 1));
        myFs.makeDirectory("/home");
        myFs.makeDirectory("/opt");
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
            assertEquals("/", myFs.changeDirectory(".."));
            assertEquals("/", myFs.changeDirectory("///"));
            assertEquals("/home", myFs.changeDirectory("home"));
            myFs.changeDirectory("/errorPath");
        } catch (Exception e) {
            assertTrue(e instanceof PathInvalidException);
        }
    }

    @Test
    public void list() {
        try {
            assertEquals("home opt test1.java ", myFs.list("/"));
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
    }

    @Test
    public void makeDirectory() {
        try {
            assertEquals("/mkdir", myFs.makeDirectory("mkdir"));
            assertEquals("/home/work", myFs.makeDirectory("/home/work"));
            myFs.makeDirectory("/home/work"); // dir exists
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }
        try {
            myFs.touchFile("testFile");
            myFs.makeDirectory("testFile");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.changeDirectory("home");
            assertEquals("/home/test", myFs.makeDirectory("test"));
            myFs.makeDirectory("/home/sb/sb/test");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.makeDirectory("/0");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            assertEquals("/home/work",myFs.linkSoft("/home/work", "/slink"));
            assertEquals("/home/work",myFs.removeRecursively("/home/work"));
            myFs.changeDirectory("/");
            assertEquals("/home/work", myFs.makeDirectory("/slink"));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            assertEquals("/home/work",myFs.removeRecursively("/home/work"));
            myFs.touchFile("/home/work");
            myFs.changeDirectory("/");
            assertEquals("/home/work", myFs.makeDirectory("/slink"));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            assertEquals("/home/work",myFs.removeRecursively("/home/work"));
            myFs.changeDirectory("/");
            assertEquals("/home/work", myFs.makeDirectory("/slink"));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            myFs.linkSoft("/opt", "/home/work/slink");
            assertEquals("/home/work", myFs.makeDirectory("/slink/slink"));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            assertEquals("/home/work",myFs.removeRecursively("/home/work"));
            assertEquals("/home/work", myFs.makeDirectory("/slink/slink"));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        try {
            myFs.touchFile("/opt/file");
            myFs.makeDirectory("/opt/file");
        } catch (FileSystemException e) {
            e.printStackTrace();
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
            assertTrue(e instanceof PathInvalidException);
        }
    }


    @Test
    public void removeRecursively() {
        try {
            myFs.removeRecursively("/");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.makeDirectoryRecursively("/home/test/sb");
            assertEquals("/home", myFs.removeRecursively("/home"));
            myFs.makeDirectoryRecursively("/home/test/sb");
            myFs.changeDirectory("/home/test");
            myFs.removeRecursively("/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
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
            assertEquals("root root 6 6 0 0 /home/test/sb", myFs.information("/home/test/sb"));
            myFs.information("/home/kk/sb");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.touchFile("/home/test/sb/test.txt");
            assertEquals("root root 9 9 0 1 /home/test/sb/test.txt", myFs.information("/home/test/sb/test.txt"));
            myFs.information("/home/test/sb/test.txt1");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
    }

    @Test
    public void linkSoft() {

    }

    @Test
    public void linkHard() throws FileSystemException {
        try {
            myFs.linkHard("test1.javaklsjd","linkHard");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.linkSoft("/home","test");
            myFs.linkHard("test","linkHard");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.linkSoft("test1.java","softLink");
            myFs.linkHard("softLink","hardlink");
            assertEquals("root root 3 4 20 1 /test1.java",myFs.information("hardlink"));
        } catch (FileSystemException e) {
            //
        }
        try {
            myFs.linkHard("test1.java","hard1");
            myFs.linkHard("hard1","hard2");
            assertEquals("root root 3 4 20 1 /test1.java",myFs.information("hardlink"));
        } catch (FileSystemException e) {
            //
        }
        try {
            myFs.linkHard("test1.java","test1.java");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.touchFile("testLink");
            myFs.linkHard("test1.java","testLink");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }
        try {
            myFs.touchFile("/home/test1.java");
            myFs.linkHard("test1.java","/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }
        try{
            myFs.linkHard("test1.java","opt");
            assertEquals("root root 3 4 20 1 /test1.java",myFs.information("/opt/test1.java"));
        } catch (FileSystemException e) {
            //
        }
        try{
            myFs.linkSoft("test1.java","soft");
            myFs.linkHard("soft","/opt");
            assertEquals("root root 3 4 20 1 /test1.java",myFs.information("/opt/soft"));
        }   catch (FileSystemException e) {
            //
        }
    }

    @Test
    public void readLink() {

    }

    @Test
    public void move() {

    }

    @Test
    public void copy() {

    }

    @Test
    public void onlyCopyFile() {

    }

    @Test
    public void createAndCopyFile() {

    }

    @Test
    public void catFile() {
        try {
            assertEquals("public class Main\n{}", myFs.catFile("test1.java"));
            myFs.catFile("tes");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
    }

    @Test
    public void removeFile() {
        try {
            assertEquals("/test1.java", myFs.removeFile("test1.java"));
            myFs.removeFile("te");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
    }

    @Test
    public void fileWrite() {
        try {
            myFs.fileWrite("/test2.java", "public");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            myFs.fileWrite("/jj/test.java", "public");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void fileAppend() {
        try {
            myFs.fileAppend("/test2.java", "public");

            myFs.fileAppend("/te", "dsa");
        } catch (FileSystemException e) {

        }

        try {
            myFs.fileAppend("/test2.java", " class");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
    }

    @Test
    public void touchFile() {
        try {
            myFs.touchFile("/jj/tes.txt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.touchFile("/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.touchFile("/error/p");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

    }


}