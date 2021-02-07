package ru.geekbrains.cloudnetwork.client.core;


import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class JsonTransformUtils {
    private Node root;
    private String user;
    JSONObject json;

    public static JsonTransformUtils getTestInstance() throws IOException {
        File file = new File(JsonTransformUtils.class.getClassLoader().getResource("user_storage_test.json").getFile());
        return new JsonTransformUtils(Files.readString(file.toPath()));
    }

    public static void main(String[] args) {
        try {
            getTestInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JsonTransformUtils(String JSON) {
        //System.out.println(JSON);
        json = new JSONObject(JSON);
        user = json.getString("user");
        root = new Node(Node.TYPE_FOLDER,json.getString("rootDir"),"");
        fillTree(root, json.getJSONArray("content"));
    }

    void fillTree(Node root, JSONArray content) {
        if (content!=null) {
            for (int i = 0; i < content.length(); i++) {
                JSONObject leaf = (JSONObject) content.get(i);
                Node n = new Node(leaf.getString("type"), leaf.getString("name"),root.getNodePath());
                root.addChildrenNode(n);
                if (leaf.getString("type").equals(Node.TYPE_FOLDER)) {
                    if (leaf.has("content")) {
                        fillTree(n, leaf.getJSONArray("content")); }
                }
            };
        }
    }

    public JTree getFolderTree() {

        DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(root);
        fillJTree(root, rootTreeNode);
        DefaultTreeModel treeModel = new DefaultTreeModel(rootTreeNode, true);
        return new JTree(treeModel);
    }

    void fillJTree(Node root, DefaultMutableTreeNode rootTreeNode){
        root.getChildNodes().stream().filter(node -> node.isFolder()).forEach(node -> {
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
            rootTreeNode.add(treeNode);
            fillJTree(node, treeNode);
        });
    }

}
