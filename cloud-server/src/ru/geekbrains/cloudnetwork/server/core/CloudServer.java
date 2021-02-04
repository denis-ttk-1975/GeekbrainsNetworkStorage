package ru.geekbrains.cloudnetwork.server.core;

import ru.geekbrains.cloudnetwork.common.Library;
import ru.geekbrains.cloudnetwork.network.ServerSocketThread;
import ru.geekbrains.cloudnetwork.network.ServerSocketThreadListener;
import ru.geekbrains.cloudnetwork.network.SocketThread;
import ru.geekbrains.cloudnetwork.network.SocketThreadListener;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

public class CloudServer implements ServerSocketThreadListener, SocketThreadListener {
    private final DateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss: ");
    private final CloudServerListener listener;
    private final Vector<SocketThread> clients;
    private ServerSocketThread thread;

    public CloudServer(CloudServerListener listener) {
        this.listener = listener;
        this.clients = new Vector<>();
    }

    public void start(int port) {
        if (thread != null && thread.isAlive()) {
            putLog("Server already started");
        } else {
            thread = new ServerSocketThread(this, "Thread of server", port, 2000);
        }
    }

    public void stop() {
        if (thread == null || !thread.isAlive()) {
            putLog("Server is not running");
        } else {
            thread.interrupt();
        }
    }


    private void putLog(String msg) {
        msg = DATE_FORMAT.format(System.currentTimeMillis()) +
                Thread.currentThread().getName() + ": " + msg;
        listener.onChatServerMessage(msg);
    }

    private void handleNonAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        if (arr.length != 3 || !arr[0].equals(Library.AUTH_REQUEST)) {
            client.msgFormatError(msg);
            return;
        }
        String login = arr[1];
        String password = arr[2];
        String nickname = SqlClient.getNickname(login, password);
        if (nickname == null) {
            /**
             * Task 2
             * 2. Добавить отключение неавторизованных пользователей по таймауту (120 сек. ждём после подключения клиента, и если он не авторизовался за это время, закрываем соединение).
             */
            client.sendMessage(Library.getAuthDenied());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    putLog("Invalid login attempt: " + login);
                    try {
                        sleep(120000);
                        //sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!client.isAuthorized()) {
                        client.authFail();
                    }
                }
            }).start();
            return;
        } else {
            ClientThread oldClient = null;
            if (client.isAuthorized()) {oldClient = findClientByNickname(nickname);}
            client.authAccept(nickname);
            client.sendMessage(Library.getFoldersStructure(getJSONFolderStructure(nickname)));
            if (oldClient == null) {
                //sendToAllAuthorizedClients(Library.getTypeBroadcast("Server", nickname + " connected"));
            }
            else {
                oldClient.reconnect();
                clients.remove(oldClient);
            }
        }
        //sendToAllAuthorizedClients(Library.getUserList(getUsers()));
    }

    private void handleAuthMessage(ClientThread client, String msg) {
        String[] arr = msg.split(Library.DELIMITER);
        String msgType = arr[0];
        switch (msgType) {
            case Library.TYPE_BCAST_CLIENT:
                sendToAllAuthorizedClients(Library.getTypeBroadcast(client.getNickname(), arr[1]));
                break;
            default:
                client.msgFormatError(msg);

        }
    }

    private void sendToAllAuthorizedClients(String msg) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread recipient = (ClientThread) clients.get(i);
            if (!recipient.isAuthorized()) continue;
            recipient.sendMessage(msg);
        }
    }

    private String getUsers() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            sb.append(client.getNickname()).append(Library.DELIMITER);
        }
        return sb.toString();
    }

    private synchronized ClientThread findClientByNickname(String nickname) {
        for (int i = 0; i < clients.size(); i++) {
            ClientThread client = (ClientThread) clients.get(i);
            if (!client.isAuthorized()) continue;
            if (client.getNickname().equals(nickname))
                return client;
        }
        return null;
    }

    private static Path serverPath = Paths.get("serverDir");

    private static String getJSONFolderStructure(String nickname) {
        StringBuilder result = new StringBuilder();
        try {
            result.append(String.format("{\"rootDir\":\"%s\",\"user\":\"%s\",\"content\":[%s]}",nickname,nickname,JSONFolderPart(serverPath.resolve(nickname))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result.toString().replaceAll("}\\{","},{");
    }

    public static String JSONFolderPart(Path p) throws IOException {
        StringBuilder result = new StringBuilder();
        Files.walkFileTree(p, new HashSet<>(), 1,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (file.toFile().isFile()) {
                            result.append(String.format("{\"type\":\"file\",\"name\":\"%s\"}",file.getFileName()));
                        }
                        else {
                            result.append(String.format("{\"type\":\"folder\",\"name\":\"%s\",\"content\":[%s]}",file.getFileName(),JSONFolderPart(file)));
                        }
                        return super.visitFile(file, attrs);
                    }
                });
        return result.toString();
    }

    public static void main(String[] args) throws IOException {
        String files = Files.list(serverPath.resolve("yuriy"))
                .map(path -> path.getFileName().toString())
                .collect(Collectors.joining(", "));

        List<File> fileList = Files.walk((serverPath.resolve("yuriy")))
                //.filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());
                //.collect(Collectors.toList());
        //String files2 = fileList.stream().collect(Collectors.joining(", "))

        Files.walkFileTree(serverPath.resolve("yuriy"), new HashSet<>(), 1,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println(file);
                        return super.visitFile(file, attrs);
                    }
                });

        System.out.println();
        System.out.println(files);
        System.out.println();
        System.out.println(fileList);
        System.out.println();

        System.out.println(getJSONFolderStructure("yuriy"));

    }

    /**
     * Server methods
     *
     * */

    @Override
    public void onServerStart(ServerSocketThread thread) {
        putLog("Server thread started");
        SqlClient.connect();
    }

    @Override
    public void onServerStop(ServerSocketThread thread) {
        putLog("Server thread stopped");
        SqlClient.disconnect();
        for (int i = 0; i < clients.size(); i++) {
            clients.get(i).close();
        }
    }

    @Override
    public void onServerSocketCreated(ServerSocketThread thread, ServerSocket server) {
        putLog("Server socket created");

    }

    @Override
    public void onServerTimeout(ServerSocketThread thread, ServerSocket server) {
//        putLog("Server timeout");

    }

    @Override
    public void onSocketAccepted(ServerSocketThread thread, ServerSocket server, Socket socket) {
        putLog("Client connected");
        String name = "SocketThread " + socket.getInetAddress() + ":" + socket.getPort();
        new ClientThread(this, name, socket);

    }

    @Override
    public void onServerException(ServerSocketThread thread, Throwable exception) {
        exception.printStackTrace();
    }

    /**
     * Socket methods
     *
     * */

    @Override
    public synchronized void onSocketStart(SocketThread thread, Socket socket) {
        putLog("Socket created");

    }

    @Override
    public synchronized void onSocketStop(SocketThread thread) {
        putLog("Socket stopped");
        clients.remove(thread);
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized() && !client.isReconnecting()) {
            sendToAllAuthorizedClients(Library.getTypeBroadcast("Server",
                    client.getNickname() + " disconnected"));
        }
        sendToAllAuthorizedClients(Library.getUserList(getUsers()));

    }

    @Override
    public synchronized void onSocketReady(SocketThread thread, Socket socket) {
        putLog("Socket ready");
        clients.add(thread);
    }

    @Override
    public synchronized void onReceiveString(SocketThread thread, Socket socket, String msg) {
        ClientThread client = (ClientThread) thread;
        if (client.isAuthorized())
            handleAuthMessage(client, msg);
        else
            handleNonAuthMessage(client, msg);
    }

    @Override
    public synchronized void onSocketException(SocketThread thread, Exception exception) {
        exception.printStackTrace();
    }

}
