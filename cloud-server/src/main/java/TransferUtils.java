import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class TransferUtils {

    private static final String LOG = "vendor-partner.log";

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException {
//
//        OutputStream os = new FileOutputStream("123.txt");
//        for (int i = 0; i < 30; i++) {
//            os.write(65);
//        }

//        while (true) {
//            Thread.sleep(1000);
//        }

        //new TransferUtils().copy(LOG, LOG + "_copy");

        TransferUtils utils = new TransferUtils();
        utils.writeObject(new FileMessage(Paths.get("task.md")), "object_data.ololo");
        System.out.println(utils.readObject("object_data.ololo"));
        FileMessage fileMessage = utils.readObject("object_data.ololo");
        Files.write(Paths.get("task_ser_copy.md"), fileMessage.getData(), StandardOpenOption.CREATE_NEW);
        // 1, 2, 3
        // [1, 2, 3, 0, 0, 0] -> disk
        // os.flush();
    }

    public FileMessage readObject(String src) throws IOException, ClassNotFoundException {
        ObjectInputStream is = new ObjectInputStream(new FileInputStream(src));
        return (FileMessage) is.readObject();
    }

    public void writeObject(FileMessage fileMessage, String dst) throws IOException {
        ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(dst));
        os.writeObject(fileMessage);
        os.close();
    }

    public void copy(String src, String dst) throws IOException {
        InputStream is = new FileInputStream(getClass().getResource(src).getPath());
        OutputStream os = new FileOutputStream(dst);
        int read;
        byte[] buffer = new byte[1024];
        while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
        }
        os.close();
        is.close();
    }
}
