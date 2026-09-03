package jua.compiler.utils;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class IOUtils {
    private IOUtils() {
        throw new AssertionError();
    }

    public static CharBuffer readFileCharBuffer(File file, Charset charset) throws IOException {
        return readPathCharBuffer(file.toPath(), charset);
    }

    public static CharBuffer readPathCharBuffer(Path path, Charset charset) throws IOException {
        try (FileChannel channel = FileChannel.open(path, StandardOpenOption.READ)) {
            long size = channel.size();
            return charset.decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, size));
        }
    }
}
