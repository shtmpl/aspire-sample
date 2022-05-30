package me.sample;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class Resources {

    public static String slurpText(String resource) {
        return new String(slurpBytes(resource), StandardCharsets.UTF_8);
    }

    public static byte[] slurpBytes(String resource) {
        try {
            return Files.readAllBytes(path(resource));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static Path path(String resource) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        if (url == null) {
            throw new RuntimeException(String.format("No resource: %s", resource));
        }

        return Paths.get(url.getPath());
    }
}
