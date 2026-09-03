package jua.compiler.utils;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class IOUtils {

    private static String userDirCache;
    private static Path userDirPath;

    public static CharBuffer readFileCharBuffer(File file, Charset charset) throws IOException {
        try (FileChannel channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            long size = channel.size();
            return charset.decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, size));
        }
    }

    public static Path relativize(Path p) {
        String userDir = System.getProperty("user.dir");
        if (!userDir.equals(userDirCache)) {
            userDirCache = userDir;
            userDirPath = Paths.get(userDir);
        }
        return userDirPath.relativize(p);
    }
}
