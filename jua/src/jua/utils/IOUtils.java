package jua.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class IOUtils {

    public static char[] readCharsFromFile(File file) throws IOException {
        try (InputStream fileInputStream = Files.newInputStream(file.toPath())) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(fileInputStream.available());
            byte[] buffer = new byte[1024];
            int nRead;

            while ((nRead = fileInputStream.read(buffer, 0, 1024)) != -1) {
                byteArrayOutputStream.write(buffer, 0, nRead);
            }

            return byteArrayOutputStream.toString().toCharArray();
        }
    }

    private IOUtils() { Assert.error(); }
}
