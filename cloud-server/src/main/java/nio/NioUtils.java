package nio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;

public class NioUtils {

    public static void main(String[] args) throws IOException {
        Path path = Paths.get("task.md");
        System.out.println(path);
        System.out.println(path.toAbsolutePath());
//        WatchService service;
//        WatchKey take = service.take(); // block
//        take.pollEvents().forEach(
//                event -> {
//                    WatchEvent.Kind<?> kind = event.kind();
//                    switch (kind.name()) {
//
//                    }
//                }
//        );


        Path writePath = Paths.get("out.txt");
        Files.write(writePath,
                "OK".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.APPEND);
        Files.copy(writePath,
                Paths.get("o.txt"),
                StandardCopyOption.REPLACE_EXISTING);
        Files.walkFileTree(Paths.get("./"), new HashSet<>(), 1,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println(file);
                        return super.visitFile(file, attrs);
                    }
                });

    }
}
