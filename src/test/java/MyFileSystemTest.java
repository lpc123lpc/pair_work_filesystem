import com.fileutils.specs2.models.FileSystemException;
import exceptions.PathExistException;
import exceptions.PathInvalidException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.IntConsumer;

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
            myFs.makeDirectory("/home/work");
            assertEquals("work ", myFs.list("/home"));
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.linkSoft("/home/work", "/slink");
            assertEquals("", myFs.list("/slink"));
        } catch (FileSystemException e) {
            e.printStackTrace();
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
    public void makeDirectory1() {
        try {
            myFs.makeDirectory("/home/work");
            myFs.linkSoft("/home/work", "/slink");
            myFs.removeRecursively("/home/work");
            myFs.makeDirectory("/slink/opt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.makeDirectory("/home/work");
            myFs.linkSoft("/home/work", "/home/slink");
            myFs.removeRecursively("/home/work");
            myFs.makeDirectory("/home/slink");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.makeDirectory("/slink/opt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }


        // /home/slink/test
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
    public void pathLenInvalid(){
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < 8192; i++) {
            temp.append("sb");
        }
        try {
            myFs.pathLenInvalid(temp.toString());
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
    }

    @Test
    public void makeDirectoryRecursively() {
        try {
            myFs.makeDirectoryRecursively("/testDir/lpc");
            myFs.linkSoft("testDir","soft");
            myFs.makeDirectoryRecursively("/soft/lpc/aaa");
            assertEquals("root root 7 7 0 0 /testDir/lpc/aaa",myFs.information("/soft/lpc/aaa"));
            myFs.removeRecursively("testDir");
            myFs.makeDirectoryRecursively("soft");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
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
        try {
            myFs.linkHard("test1.java","hardlink");
            myFs.information("hardlink");
        } catch (FileSystemException e) {
            //
        }
        try {
            myFs.linkSoft("test1.java","softlink");
            myFs.information("softlink");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void linkSoft() {

        // srcEntry == null
        try {
            myFs.linkSoft("/error", "/slink");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        // desEntry == null && desPath.endWith("/");
        try {
            myFs.linkSoft("/opt", "/slink/");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        // srcEntryPath == desEntryPath
        try {
            myFs.linkSoft("/opt", "/opt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        // desEntry instanceof File
        try {
            myFs.linkSoft("/opt", "/test1.java");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }
        // desEntry instanceof Dir && srcEntryPath is desEntryPath father
        try {
            myFs.makeDirectory("/home/work");
            myFs.linkSoft("/home", "/home/work");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.linkSoft("/home", "/slink");
            myFs.linkSoft("/slink", "/slink2");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        // srcEntry instanceof HardLink
        try {
            myFs.touchFile("/home/file1");
            myFs.linkHard("/home/file1", "/hLink");
            myFs.linkSoft("/hLink","/slink3");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.removeFile("/home/file1");
            myFs.linkSoft("/hLink","/slink4");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        //
        try {
            myFs.makeDirectory("/home/work/opt");
            myFs.makeDirectory("/usr");
            myFs.linkSoft("/opt", "/home/work");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }

        try {
            myFs.linkSoft("/usr", "/home/work");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }

        try {
            myFs.linkSoft("/slink", "/home/work");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            myFs.touchFile("/home/file1");
            myFs.linkSoft("/hLink", "/home/work");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            myFs.removeFile("/home/file1");
            myFs.removeFile("/home/work/hLink");
            myFs.linkSoft("/hLink", "/home/work");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

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
            assertEquals("root root 3 4 20 1 /hardlink",myFs.information("hardlink"));
        } catch (FileSystemException e) {
            //
        }
        try {
            myFs.linkHard("test1.java","hard1");
            myFs.linkHard("hard1","hard2");
            assertEquals("root root 3 4 20 1 /hard2",myFs.information("hard2"));
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
            assertEquals("root root 3 4 20 1 /opt/test1.java",myFs.information("/opt/test1.java"));
        } catch (FileSystemException e) {
            //
        }
        try{
            myFs.linkSoft("test1.java","soft");
            myFs.linkHard("soft","/opt");
            assertEquals("root root 3 4 20 1 /opt/soft",myFs.information("/opt/soft"));
        }   catch (FileSystemException e) {
            //
        }
    }

    @Test
    public void readLink() {
        try {
            myFs.linkSoft("/home", "/link");
            assertEquals("/home", myFs.readLink("/link"));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            myFs.readLink("/");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
    }

    @Test
    public void move() {
        // srcEntry = null
        try {
            myFs.move("/error", "/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        // srcEntry instanceof Dir && srcEntry == nowPath || srcEntry is noePath Father

        try {
            myFs.makeDirectory("/home/sb");
            myFs.move("/home", "/home/sb");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.move("/", "/error");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.move("/opt", "opt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.move("/", "/opt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.touchFile("/home/opt");
            myFs.move("/opt", "/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }

        try {
            myFs.touchFile("/home/opt");
            myFs.move("/opt", "/home/opt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }

    }
    @Test
    public void move1() {

        try {
            myFs.makeDirectory("/usr");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        try {
            myFs.move("/usr", "/usr/error");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.move("/usr", "/home/work");
            myFs.move("/test1.java", "/home/test1.java");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            myFs.touchFile("/home/test1");
            myFs.makeDirectory("/test1");
            myFs.move("/home/test1", "/");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }

        try {
            myFs.removeRecursively("/test1");
            myFs.touchFile("/test1");
            myFs.move("/home/test1", "/");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }

        try {
            myFs.removeFile("/test1");
            myFs.touchFile("/home/test1");
            myFs.move("/home/test1", "/");
        } catch (FileSystemException e) {
            e.printStackTrace();
            assertTrue(e instanceof PathInvalidException);
        }

        try {
            myFs.touchFile("/test2");
            myFs.move("/test2", "/test1");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void move2() {
        try {
            myFs.makeDirectory("/usr");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        try {
            myFs.touchFile("/home/usr");
            myFs.move("/usr", "/home/usr");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }
        try {
            myFs.removeFile("/home/usr");
            myFs.move("/usr", "/home");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            myFs.makeDirectory("/home/usr1");
            myFs.makeDirectory("/usr1");
            myFs.makeDirectory("/usr1/oo1");
            myFs.touchFile("/usr1/file1");
            myFs.move("/usr1", "/home");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }

        try {
            myFs.makeDirectory("/usr2");
            myFs.makeDirectory("/home/usr2");
            myFs.makeDirectory("/home/usr2/oo1");
            myFs.move("/usr2", "/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }

    }

    @Test
    public void copy() {
        try{
            myFs.copy("skaljf","test");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.copy("/","/home/test");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try{
            myFs.copy("test1.java","/home/test");
        } catch (FileSystemException e) {
            //
        }
        try {
            myFs.copy("/home","test");
        } catch (FileSystemException e) {
            //
        }
        try {
            myFs.copy("/home","/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.copy("/","/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathInvalidException);
        }
        try {
            myFs.copy("test1.java","/home");
        } catch (FileSystemException e) {
            //
        }
        try {
            myFs.copy("test1.java","/home");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }
        try {
            myFs.touchFile("file2");
            myFs.copy("test1.java","file2");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        try {
            myFs.touchFile("/opt/home");
            myFs.copy("/home","/opt");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }
        try {
            myFs.removeFile("/opt/home");
            myFs.makeDirectory("/opt/home");
            myFs.copy("/home","/opt");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        try {
            myFs.copy("/home","/opt/home");
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        try {
            myFs.copy("/home","test1.java");
        } catch (FileSystemException e) {
            assertTrue(e instanceof PathExistException);
        }

        try {
            myFs.linkSoft("test1.java","soft");
            myFs.copy("soft","home");
            assertEquals("soft test test1.java ",myFs.list("home"));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        try {
            myFs.removeFile("home/soft");
            myFs.copy("soft","home/cp");
            assertEquals("cp test test1.java ",myFs.list("home"));
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        try {
            myFs.linkHard("/test1.java","hardsrc");
            myFs.copy("hardsrc","/home/cp");
            assertEquals("root root 26 29 20 1 /home/cp",myFs.information("/home/cp"));
        } catch (FileSystemException e) {
            //
        }
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