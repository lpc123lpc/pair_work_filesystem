import com.fileutils.specs1.AppRunner;

public class MainClass {
    public static void main(String[] args) throws Exception {
        AppRunner runner = AppRunner.newInstance(MyFileSystem.class);
        runner.run(args);
    }
}

