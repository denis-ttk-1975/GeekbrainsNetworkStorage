import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;

public class FileMessage implements Serializable {

    private final String name;
    private final byte[] data;
    private final LocalDate createAt;
    private boolean end;

    public FileMessage(Path path) throws IOException {
        name = path.getFileName().toString();
        data = Files.readAllBytes(path);
        createAt = LocalDate.now();
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    public LocalDate getCreateAt() {
        return createAt;
    }

    @Override
    public String toString() {
        return "FileMessage{" +
                "name='" + name + '\'' +
                ", data=" + Arrays.toString(data) +
                ", createAt=" + createAt +
                '}';
    }
}
