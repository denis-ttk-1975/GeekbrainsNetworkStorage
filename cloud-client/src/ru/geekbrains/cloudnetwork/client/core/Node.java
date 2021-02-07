package ru.geekbrains.cloudnetwork.client.core;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Node {
    static final String TYPE_FOLDER = "folder";
    static final String TYPE_FILE = "file";
    private String type;
    private String name;
    private String parentPath;
    private ArrayList<Node> childrenNodes = new ArrayList<>();

    public Node(String type, String name, String parentPath) {
        this.type = type;
        this.name = name;
        this.parentPath = parentPath;
    }

    void addChildrenNode(Node n) {
        childrenNodes.add(n);
    }

    public ArrayList<Node> getChildNodes() {
        return childrenNodes;
    }

    public boolean isChildrenNodePresent() {
        return childrenNodes.size()>0?true:false;
    }

    public boolean isFile() {
        return type.equals(Node.TYPE_FILE);
    }

    public boolean isFolder() {
        return type.equals(Node.TYPE_FOLDER);
    }

    public String getNodePath() {
        return parentPath+(parentPath.equals("")?"":"\\")+name;
    }

    public String getParentPath() {
        return parentPath;
    }

    public Object[][] getFileLeaves () {
        int fileCount = (int) childrenNodes.stream().filter(node -> node.isFile()).count();
        Object[][] result = new Node[fileCount][1];
        final int[] i = {0};
        childrenNodes.stream().filter(node -> node.isFile()).forEach(node -> result[i[0]++][0] = node);
        return result;
    }

    public void fillFilesList(DefaultTableModel tableModel) {
        for (int i = tableModel.getRowCount() - 1; i >= 0; i--) {
            tableModel.removeRow(i);
        }
        childrenNodes.stream().filter(node -> node.isFile()).forEach(node -> tableModel.addRow(new Node[]{node, node}));
    }

    @Override
    public String toString() {
        return name;
    }

}
