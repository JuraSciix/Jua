package jua.util;

import sun.nio.cs.ThreadLocalCoders;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

            return ThreadLocalCoders.decoderFor(StandardCharsets.UTF_8)
                    .decode(ByteBuffer.wrap(buffer)).array();
        }
    }

    private IOUtils() {
        throw new UnsupportedOperationException();
    }
}
