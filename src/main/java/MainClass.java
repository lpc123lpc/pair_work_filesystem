import com.fileutils.specs2.AppRunner;

import java.util.HashMap;

public class MainClass {
    public static void main(String[] args) throws Exception {

        AppRunner runner = AppRunner.newInstance(MyFileSystem.class,MyUserSystem.class);
        runner.run(args);

    }
}

