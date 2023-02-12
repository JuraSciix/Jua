package jua.utils;

import java.io.*;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class IOUtils {

    private static String userDirCache;
    private static Path userDirPath;

    public static char[] readFileCharBuffer(File file, Charset charset) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException();
        }
        long len64 = file.length();
        if (len64 == 0L) {
            return new char[0];
        }
        if (len64 > 0x7fffffffL) {
            throw new OutOfMemoryError("File is too big");
        }
        byte[] buffer = new byte[(int) len64];
        try (InputStream reader = Files.newInputStream(file.toPath())) {
            int c = reader.read(buffer);
            if (c < 0) {
                throw new AssertionError(c);
            }
        }
        return new String(buffer, charset).toCharArray();
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
