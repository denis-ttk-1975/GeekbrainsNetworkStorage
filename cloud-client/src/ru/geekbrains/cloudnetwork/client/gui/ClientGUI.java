package ru.geekbrains.cloudnetwork.client.gui;

import ru.geekbrains.cloudnetwork.client.core.JsonTransformUtils;
import ru.geekbrains.cloudnetwork.client.core.Node;
import ru.geekbrains.cloudnetwork.common.Library;
import ru.geekbrains.cloudnetwork.network.SocketThread;
import ru.geekbrains.cloudnetwork.network.SocketThreadListener;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Scanner;

public class ClientGUI extends JFrame implements ActionListener, TableModelListener, Thread.UncaughtExceptionHandler, SocketThreadListener {
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
    private final JButton btnBrowse = new JButton("Browse...");
    private final JButton btnAddFile = new JButton("Add file");

    private final JTextField tfFolderName = new JTextField();
    private final JButton btnDeleteFolder = new JButton("Delete folder");
    private final JButton btnAddFolder = new JButton("Add Folder");

    private final JButton btnUploadFile = new JButton("Upload File");
    private final JButton btnDeleteFile = new JButton("Delete File");
    private JTable jTabFiles;
    private JTree jTreeFolders;
    private JScrollPane scrollDir;


    private final JList<String> userList = new JList<>();
    private boolean shownIoErrors = false;
    private SocketThread socketThread;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private final String WINDOW_TITLE = "Network Storage";

    private static Path uploadPath = Paths.get("uploadDir");

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

        scrollDir = new JScrollPane();
        scrollDir.setPreferredSize(new Dimension(WIDTH/2, 0));

        panelLeft.add(scrollDir);

        Object[] columnsHeader = new String[] {"Node Link", "Name"};
        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.setColumnIdentifiers(columnsHeader);
        jTabFiles = new JTable(tableModel);

        TableColumnModel columnModel = jTabFiles.getColumnModel();
        Enumeration<TableColumn> e = columnModel.getColumns();
        TableColumn column = e.nextElement();
        column.setMinWidth(0);
        column.setMaxWidth(0);
        tableModel.addTableModelListener(this::tableChanged);

        JScrollPane scrollFiles = new JScrollPane(jTabFiles);
        scrollFiles.setPreferredSize(new Dimension(WIDTH/2, 0));

        cbAlwaysOnTop.addActionListener(this);
        btnAddFile.addActionListener(this);
        tfFilePath.addActionListener(this);
        tfFilePath.setEditable(false);
        btnAddFolder.addActionListener(this);
        btnDeleteFolder.addActionListener(this);
        btnLogin.addActionListener(this);
        btnDisconnect.addActionListener(this);
        btnDeleteFile.addActionListener(this);
        btnUploadFile.addActionListener(this);
        btnBrowse.addActionListener(this);
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

        panelBottomRight.add(btnUploadFile);
        panelBottomRight.add(btnDeleteFile);

        panelToolbar.add(panelBottomLeft);
        panelToolbar.add(panelBottomRight);

        JPanel panel;
        panel = new JPanel(new BorderLayout());
        panel.add(btnDeleteFolder, BorderLayout.WEST);
        panel.add(tfFolderName, BorderLayout.CENTER);
        panel.add(btnAddFolder, BorderLayout.EAST);
        panelToolbar.add(panel);

        panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JPanel());
        panel.add(btnDisconnect);
        panelToolbar.add(panel);
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
    public void tableChanged(TableModelEvent e) {
        try {
            int idx = jTabFiles.getSelectedRow();
            DefaultTableModel tableModel = (DefaultTableModel) e.getSource();
            if (idx >= 0) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jTreeFolders.getSelectionPath().getLastPathComponent();
                Node n = (Node) treeNode.getUserObject();
                String path = n.getNodePath() + "\\" + tableModel.getValueAt(idx, 0);
                String newPath = n.getNodePath() + "\\" + tableModel.getValueAt(idx, 1).toString();
                renameFile(path, newPath);
            }
        } catch (Exception exception) {

        }
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
        } else if (src == btnDeleteFile) {
            deleteFile();
        }
        else if (src == btnDeleteFolder) {
            deleteFolder();
        }
        else if (src == btnAddFolder) {
            addFolder();
        }
        else if (src == btnUploadFile) {
            uploadFile();
        }
        else if (src == btnBrowse) {
            openFileChooser();
        }
        else {
            throw new RuntimeException("Unknown source: " + src);
        }
    }

    private void openFileChooser() {
        final JFileChooser fc = new JFileChooser();
        fc.showOpenDialog(this);
        tfFilePath.setText(fc.getSelectedFile().getAbsolutePath());
    }

    private void deleteFile() {
        try {
            int idx = jTabFiles.getSelectedRow();
            DefaultTableModel tableModel = (DefaultTableModel) jTabFiles.getModel();
            if (idx >= 0) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jTreeFolders.getSelectionPath().getLastPathComponent();
                Node n = (Node) treeNode.getUserObject();
                String path = n.getNodePath() + "\\" + tableModel.getValueAt(idx, 0);
                socketThread.sendMessage(Library.getDeleteFile(path));
            }
        } catch (Exception exception) {

        }
    }

    private void uploadFile() {
        try {
            int idx = jTabFiles.getSelectedRow();
            DefaultTableModel tableModel = (DefaultTableModel) jTabFiles.getModel();
            if (idx >= 0) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jTreeFolders.getSelectionPath().getLastPathComponent();
                Node n = (Node) treeNode.getUserObject();
                String path = n.getNodePath() + "\\" + tableModel.getValueAt(idx, 0);
                socketThread.sendMessage(Library.getUploadRequestFile(path));
            }
        } catch (Exception exception) {

        }
    }

    private void addFolder() {

        if ("".equals(tfFolderName.getText())) return;

        try {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jTreeFolders.getSelectionPath().getLastPathComponent();
            Node n = (Node) treeNode.getUserObject();
            String filePath = n.getNodePath() + "\\" +  tfFolderName.getText();

            tfFolderName.setText(null);
            tfFolderName.grabFocus();
            socketThread.sendMessage(Library.getFolder(filePath));
        } catch (Exception exception) {

        }
    }

    private void deleteFolder() {
        try {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jTreeFolders.getSelectionPath().getLastPathComponent();
            Node n = (Node) treeNode.getUserObject();
            String path = n.getNodePath();
            socketThread.sendMessage(Library.getDeleteFile(path));
        } catch (Exception exception) {

        }
    }

    private void renameFile(String path, String newPath) {
        if ("".equals(newPath)) return;
        socketThread.sendMessage(Library.getRenameFile(path, newPath));
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
        Path localFilePath = Paths.get(tfFilePath.getText());
        if ("".equals(localFilePath)) return;
        tfFilePath.setText(null);
        tfFilePath.grabFocus();

        try {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) jTreeFolders.getSelectionPath().getLastPathComponent();
            Node n = (Node) treeNode.getUserObject();
            String filePath = n.getNodePath() + "\\" +  localFilePath.toFile().getName();
            socketThread.sendMessage(Library.getSendFileToServer(
                    filePath,
                    Files.readAllLines(localFilePath).stream().reduce((s, s2) -> s+"\n"+s2))
            );
        } catch (Exception exception) {

        }

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
                clearFolderPanels();
                initFolderStructure(arr[1]);
                break;
            case Library.UPLOAD_FILE_FROM_SERVER:
                uploadFileFromServer(arr[1],arr[2]);
                break;
            default:
                throw new RuntimeException("Unknown message type: " + msg);
        }
    }

    private void uploadFileFromServer(String fileName, String byteBufString) {
        try {
            Files.write(uploadPath.resolve(fileName),byteBufString.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
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
        refreshFolderTree();
    }

    private void clearFolderPanels() {
        jTreeFolders = null;//new JTree();
        refreshFolderTree();
        DefaultTableModel tableModel = ((DefaultTableModel) jTabFiles.getModel());
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }
    }

    private void refreshFolderTree() {
        panelLeft.remove(scrollDir);
        scrollDir = new JScrollPane(jTreeFolders);
        panelLeft.add(scrollDir);
        if (jTreeFolders!=null) jTreeFolders.updateUI();
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
        clearFolderPanels();
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
