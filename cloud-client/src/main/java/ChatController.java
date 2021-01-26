import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import model.ChatUnitMessage;
import model.UserConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatController implements Initializable {

    private static final Logger LOG = LoggerFactory.getLogger(ChatController.class);

    public ListView<String> listView;

    public TextField text;

    private ObjectEncoderOutputStream os;
    private ObjectDecoderInputStream is;

    public void sendMessage(ActionEvent event) throws IOException {
        String messageContent = text.getText();
        LocalDateTime sendAt = LocalDateTime.now();
        text.clear();
        os.writeObject(
                new ChatUnitMessage(
                        UserConstants.DEFAULT_SENDER_NAME,
                        messageContent,
                        sendAt)
        );
        os.flush();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            Socket socket = new Socket("localhost", 8189);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());

            new Thread(() -> {
                while (true) {
                    try {
                        ChatUnitMessage message = (ChatUnitMessage) is.readObject();
                        listView.getItems().add(message.toString());
                    } catch (Exception e) {
                        LOG.error("e = ", e);
                        break;
                    }
                }
            }).start();
        } catch (Exception e) {
            LOG.error("e = ", e);
        }
    }
}
