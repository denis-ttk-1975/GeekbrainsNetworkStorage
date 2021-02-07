package ru.geekbrains.cloudnetwork.common;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Library {
    /*
    * /auth_request±login±password
    * /auth_accept±nickname
    * /auth_denied
    * /broadcast±time±src±msg
    * /msg_format_error
    * /user_list±user1±user2±user3±....
    * /client_bcast±msg
    * /client_private±recipient±msg
    * /client_snooze
    * /
    * */

    public static final String DELIMITER = "±";
    public static final String AUTH_REQUEST = "/auth_request";
    public static final String AUTH_ACCEPT = "/auth_accept";
    public static final String AUTH_DENIED = "/auth_denied";
    public static final String MSG_FORMAT_ERROR = "/msg_format_error"; // если мы вдруг не поняли, что за сообщение и не смогли разобрать
    public static final String TYPE_BROADCAST = "/bcast"; // то есть сообщение, которое будет посылаться всем
    public static final String USER_LIST = "/user_list";
    public static final String TYPE_BCAST_CLIENT = "/client_bcast";
    public static final String FOLDERS_STRUCTURE = "/get_folder_struct";
    public static final String RENAME_FILE_REQUEST = "/rename_file";
    public static final String DELETE_FILE_REQUEST = "/delete_file";
    public static final String ADD_FOLDER_REQUEST = "/add_folder";
    public static final String UPLOAD_FILE_REQUEST = "/upload_file_request";
    public static final String UPLOAD_FILE_FROM_SERVER = "/upload_file_from_server";
    public static final String SEND_FILE_TO_SERVER = "/send_file_to_server";

    public static String getAuthRequest(String login, String password) {
        return AUTH_REQUEST + DELIMITER + login + DELIMITER + password;
    }

    public static String getAuthAccept(String nickname) {
        return AUTH_ACCEPT + DELIMITER + nickname;
    }

    public static String getFoldersStructure(String JSON) {
        return FOLDERS_STRUCTURE + DELIMITER + JSON;
    }

    public static String getAuthDenied() {
        return AUTH_DENIED;
    }

    public static String getMsgFormatError(String message) {
        return MSG_FORMAT_ERROR + DELIMITER + message;
    }

    public static String getTypeBroadcast(String src, String message) {
        return TYPE_BROADCAST + DELIMITER + System.currentTimeMillis() +
                DELIMITER + src + DELIMITER + message;
    }

    public static String getUserList(String users) {
        return USER_LIST + DELIMITER + users;
    }

    public static String getTypeClientBcast(String msg) {
        return TYPE_BCAST_CLIENT + DELIMITER + msg;
    }

    public static String getRenameFile(String path, String newPath) {
        return RENAME_FILE_REQUEST + DELIMITER + path + DELIMITER + newPath;
    }

    public static String getDeleteFile(String path) {
        return DELETE_FILE_REQUEST + DELIMITER + path;
    }

    public static String getFolder(String folderPath) {
        return ADD_FOLDER_REQUEST + DELIMITER + folderPath;
    }

    public static String getUploadRequestFile(String path) {
        return UPLOAD_FILE_REQUEST + DELIMITER + path;
    }

    public static String getUploadFile(Path filePath, ByteBuffer byteBuffer) {
        return UPLOAD_FILE_FROM_SERVER + DELIMITER + filePath.toFile().getName() + DELIMITER + byteBuffer.toString();
    }

    public static String getUploadFile(Path filePath, String byteBuffer) {
        return UPLOAD_FILE_FROM_SERVER + DELIMITER + filePath.toFile().getName() + DELIMITER + byteBuffer;
    }

    public static String getUploadFile(Path filePath, Optional<String> reduce) {
        return UPLOAD_FILE_FROM_SERVER + DELIMITER + filePath.toFile().getName() + DELIMITER + reduce.orElse("");
    }

    public static String getSendFileToServer(String path, Optional<String> content) {
        return SEND_FILE_TO_SERVER + DELIMITER + path + DELIMITER + content.orElse("");
    }

}
