package ru.geekbrains.cloudnetwork.client.gui;

import ru.geekbrains.cloudnetwork.client.core.JsonTransformUtils;
import ru.geekbrains.cloudnetwork.client.core.Node;
import ru.geekbrains.cloudnetwork.common.Library;
import ru.geekbrains.cloudnetwork.network.SocketThread;
import ru.geekbrains.cloudnetwork.network.SocketThreadListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class ClientGUI extends JFrame implements ActionListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;

    private final JTextArea log = new JTextArea();
    private final JPanel panelTop = new JPanel(new GridLayout(2, 3));

    private final JTextField tfIPAddress = new JTextField("127.0.0.1");
    //private final JTextField tfIPAddress = new JTextField("95.84.209.91");
    private final JTextField tfPort = new JTextField("8189");
    private final JCheckBox cbAlwaysOnTop = new JCheckBox("Always on top");
    private final JTextField tfLogin = new JTextField("yuriy");
    private final JPasswordField tfPassword = new JPasswordField("12345");
    private final JButton btnLogin = new JButton("Login");

    private final JPanel panelToolbar = new JPanel(new GridLayout(2, 2));
    private final JPanel panelLeft = new JPanel(new GridLayout(1, 1));;
    private final JPanel panelBottom = new JPanel(new GridLayout(2, 1));
    private final JPanel panelMessage = new JPanel(new GridLayout(1, 1));
    private final JPanel panelBottomLeft = new JPanel(new BorderLayout());
    private final JPanel panelBottomRight = new JPanel(new GridLayout(1, 2));
    private final JButton btnDisconnect = new JButton("<html><b>Disconnect</b></html>");
    private final JTextField tfFilePath = new JTextField();
    private final JButton btnAddFile = new JButton("Add file");
    private final JButton btnDownload = new JButton("Upload");
    private final JButton btnDelete = new JButton("Delete");
    private final JButton btnBrowse = new JButton("Browse...");
    private JTable jTabFiles;
    private JTree jTreeFolders;
    private JScrollPane scrollDir;


    private final JList<String> userList = new JList<>();
    private boolean shownIoErrors = false;
    private SocketThread socketThread;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private final String WINDOW_TITLE = "Network Storage";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() { // Event Dispatching Thread
                new ClientGUI();
            }
        });
    }

    private ClientGUI() {
        Thread.setDefaultUncaughtExceptionHandler(this);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setSize(WIDTH, HEIGHT);
        setTitle(WINDOW_TITLE);
        log.setEditable(false);
        log.setLineWrap(true);

//        try {
//            jTreeFolders = JsonTransformUtils.getTestInstance().getFolderTree();
//
//            jTreeFolders.addTreeSelectionListener(e -> {
//                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
//                Node n = (Node) treeNode.getUserObject();
//                System.out.println(n.getNodePath());
//
//                n.fillFilesList(((DefaultTableModel) jTabFiles.getModel()));
//
//                jTabFiles.updateUI();
//
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        scrollDir = new JScrollPane(jTreeFolders);
        scrollDir = new JScrollPane();
        scrollDir.setPreferredSize(new Dimension(WIDTH/2, 0));

        panelLeft.add(scrollDir);

        Object[] columnsHeader = new String[] {"Name"};
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(columnsHeader);
        jTabFiles = new JTable(tableModel);
        JScrollPane scrollFiles = new JScrollPane(jTabFiles);
        scrollFiles.setPreferredSize(new Dimension(WIDTH/2, 0));

        cbAlwaysOnTop.addActionListener(this);
        btnAddFile.addActionListener(this);
        tfFilePath.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
        panelToolbar.setVisible(false);

        panelTop.add(tfIPAddress);
        panelTop.add(tfPort);
        panelTop.add(cbAlwaysOnTop);
        panelTop.add(tfLogin);
        panelTop.add(tfPassword);
        panelTop.add(btnLogin);

        panelBottomLeft.add(btnBrowse, BorderLayout.WEST);
        panelBottomLeft.add(tfFilePath, BorderLayout.CENTER);
        panelBottomLeft.add(btnAddFile, BorderLayout.EAST);

        panelBottomRight.add(btnDownload);
        panelBottomRight.add(btnDelete);

        panelToolbar.add(panelBottomLeft);
        panelToolbar.add(panelBottomRight);
        panelToolbar.add(btnDisconnect);
        //panelToolbar.setPreferredSize(new Dimension(WIDTH, HEIGHT/10));

        panelMessage.add(log);
        //panelMessage.setPreferredSize(new Dimension(WIDTH, HEIGHT/20));

        panelBottom.add(panelToolbar);
        //panelBottom.add(panelMessage);
        JScrollPane scrollMessage = new JScrollPane(panelMessage);
        scrollMessage.setHorizontalScrollBar(null);
        panelBottom.add(scrollMessage);
        panelBottom.setPreferredSize(new Dimension(WIDTH, HEIGHT/5));

        add(panelLeft, BorderLayout.CENTER);
        add(scrollFiles, BorderLayout.EAST);
        add(panelTop, BorderLayout.NORTH);
        add(panelBottom, BorderLayout.SOUTH);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == cbAlwaysOnTop) {
            setAlwaysOnTop(cbAlwaysOnTop.isSelected());
        } else if (src == btnAddFile || src == tfFilePath) {
            sendFile();
        } else if (src == btnLogin) {
            connect();
        } else if (src == btnDisconnect) {
            socketThread.close();
        } else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    private void connect() {
        if (socketThread!=null) {
            if (!socketThread.getSocket().isClosed()){
                sendCredentials(socketThread);
                return;
            }
        }
        try {
            Socket socket = new Socket(tfIPAddress.getText(), Integer.parseInt(tfPort.getText()));
            socketThread = new SocketThread(this, "Client", socket);
        } catch (IOException e) {
            showException(Thread.currentThread(), e);
        }
    }

    private void sendCredentials(SocketThread thread) {
        panelToolbar.setVisible(true);
        panelTop.setVisible(false);
        String login = tfLogin.getText();
        String password = new String(tfPassword.getPassword());
        thread.sendMessage(Library.getAuthRequest(login, password));
    }

    private void sendFile() {
        String msg = tfFilePath.getText();
        if ("".equals(msg)) return;
        tfFilePath.setText(null);
        tfFilePath.grabFocus();
        socketThread.sendMessage(Library.getTypeClientBcast(msg)); //TODO: поменять на отправку файла
    }

    private void putLog(String msg) {
        if ("".equals(msg)) return;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                //log.setText(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());
            }
        });
    }

    private void showException(Thread t, Throwable e) {
        String msg;
        StackTraceElement[] ste = e.getStackTrace();
        if (ste.length == 0)
            msg = "Empty Stacktrace";
        else {
            msg = String.format("Exception in \"%s\" %s: %s\n\tat %s",
                    t.getName(), e.getClass().getCanonicalName(), e.getMessage(), ste[0]);
            JOptionPane.showMessageDialog(this, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(null, msg, "Exception", JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
    }

    private void handleMessage(String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.AUTH_ACCEPT:
                setTitle(WINDOW_TITLE + " entered with nickname: " + arr[1]);
                break;
            case Library.AUTH_DENIED:
                panelTop.setVisible(true);
                panelToolbar.setVisible(false);
                putLog("Authorization failed");
                break;
            case Library.MSG_FORMAT_ERROR:
                putLog(msg);
                socketThread.close();
                break;
            case Library.TYPE_BROADCAST:
                putLog(DATE_FORMAT.format(Long.parseLong(arr[1])) +
                        arr[2] + ": " + arr[3]);
                break;
            case Library.USER_LIST:
                msg = msg.substring(Library.USER_LIST.length() + Library.DELIMITER.length());
                String[] usersArray = msg.split(Library.DELIMITER);
                Arrays.sort(usersArray);
                userList.setListData(usersArray);
                break;
            case Library.FOLDERS_STRUCTURE:
                initFolderStructure(arr[1]);
                break;
            default:
                throw new RuntimeException("Unknown message type: " + msg);
        }
    }

    private void initFolderStructure(String JSON) {

        jTreeFolders = new JsonTransformUtils(JSON).getFolderTree();

        jTreeFolders.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
            Node n = (Node) treeNode.getUserObject();

            n.fillFilesList(((DefaultTableModel) jTabFiles.getModel()));

            jTabFiles.updateUI();

        });
        panelLeft.remove(scrollDir);
        scrollDir = new JScrollPane(jTreeFolders);
        panelLeft.add(scrollDir);
        jTreeFolders.updateUI();
        scrollDir.updateUI();
        panelLeft.updateUI();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        showException(t, e);
        System.exit(1);
    }


    /**
     * Socket thread listener methods
     * */

    @Override
    public void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Start");
    }

    @Override
    public void onSocketStop(SocketThread thread) {
        panelToolbar.setVisible(false);
        panelTop.setVisible(true);
        setTitle(WINDOW_TITLE);
        userList.setListData(new String[0]);
        thread.interrupt();
    }

    @Override
    public void onSocketReady(SocketThread thread, Socket socket) {
        sendCredentials(thread);
    }

    @Override
    public void onReceiveString(SocketThread thread, Socket socket, String msg) {
        handleMessage(msg);
    }

    @Override
    public void onSocketException(SocketThread thread, Exception exception) {
        showException(thread, exception);
    }
}
