package main.StorageManager.B_Tree;

import main.Constants.Constant;
import main.Constants.Helper;
import main.StorageManager.MetaData.MetaAttribute;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class BPlusTree {
    private int rootIndex;
    private final int N;
    private final File file;
    private int numOfNodes;
    private final int pageSize;
    private final MetaAttribute metaAttribute;

    // for testing purposes
    static ArrayList<Node> nodes = new ArrayList<>();

    public BPlusTree(File file, MetaAttribute metaAttribute, int pageSize) {
        this.file = file;
        this.rootIndex = -1;
        this.numOfNodes = 0;
        this.metaAttribute = metaAttribute;
        this.pageSize = pageSize;
        this.N = calculateN();
    }

    public BPlusTree(File file, MetaAttribute metaAttribute, int numOfNodes, int rootIndex, int pageSize) {
        this.file = file;
        this.rootIndex = rootIndex;
        this.numOfNodes = numOfNodes;
        this.metaAttribute = metaAttribute;
        this.pageSize = pageSize;
        this.N = calculateN();
    }

    private Node getRoot() {
        Node rootNode;
        if (rootIndex == -1) {
            rootNode = new Node(metaAttribute, true, N, numOfNodes++, this);
            nodes.add(rootNode);
            rootIndex = rootNode.getIndex();
        } else {
            rootNode = nodes.get(rootIndex);
        }
        return rootNode;
    }

    public void updateRecordPointer(Object searchKey, int newPageId, int newRecordIndex) {
        Node root = getRoot();
        Node node = root.search(searchKey);
        ArrayList<Object> searchKeys = node.getSearchKeys();
        ArrayList<RecordPointer> recordPointers = node.getRecordPointers();
        for (int i = 0; i < searchKeys.size(); i++) {
            int compareValue = node.compareValues(searchKey, searchKeys.get(i));
            if (compareValue == 0) {
                RecordPointer recordPointer = recordPointers.get(i);
                recordPointer.setPageNumber(newPageId);
                recordPointer.setRecordIndex(newRecordIndex);
                return;
            }
        }
    }

    public Node search(Node root, Object searchKey) {
        return root.search(searchKey);
    }

    public RecordPointer findRecordPlacement(Object searchKey) {
        Node root = getRoot();
        Node nodeToInsert = search(root, searchKey);
        return nodeToInsert.findRecordPointer(nodeToInsert, searchKey);
    }

    public void insert(Object key, int pageNumber, int recordNumber) {
        Node rootNode = getRoot();
        Node nodeToInsert = search(rootNode, key);
        Node newRoot = nodeToInsert.insert(key, pageNumber, recordNumber);
        if (newRoot != null) {
            rootIndex = newRoot.getIndex();
        }
    }


    public static void putNode(Node node) {
        nodes.add(node);
    }

    public RecordPointer findRecordPointerForDeletion(Object key) {
        Node root = getRoot();
        Node nodeToDelete = search(root, key);
        RecordPointer recordPointer = nodeToDelete.findRecordPointerForDeletion(nodeToDelete, key);
        return recordPointer;
    }

    public void delete(Object key) {
        if (rootIndex == -1) {
            System.out.println("Table is empty! Nothing to delete!");
        } else {
            Node rootNode = nodes.get(rootIndex);
            Node node = rootNode.delete(key);
            if (node != null) {
                this.rootIndex = node.getIndex();
            }
        }
    }

    public void update(Object key, Object value) {
//        delete(key);
//        insert(value);
    }

    public static void insertNodeForTesting(Node node) {
        nodes.add(node);
    }

    public void setRootIndex(int index) {
        this.rootIndex = index;
    }

    public Node getNodeAtIndex(int index) {
        index += getBinarySize();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.getParentFile(), "r")) {
            randomAccessFile.seek(index);
            byte[] pageBytes = new byte[pageSize];
            randomAccessFile.readFully(pageBytes);
            return Node.deserialize(pageBytes, metaAttribute, N, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void persistNode(Node node) {
        int index = getBinarySize();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.getParentFile(), "w")) {
            randomAccessFile.seek(index);
            byte[] pageBytes = node.serialize();
            randomAccessFile.write(pageBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int getNextIndexAndIncrement() {
        return numOfNodes++;
    }

    private int calculateN() {
        Constant.DataType dataType = metaAttribute.getType();
        float maxSize = switch (dataType) {
            case INTEGER -> Constant.INTEGER_SIZE;
            case DOUBLE -> Constant.DOUBLE_SIZE;
            case BOOLEAN -> Constant.BOOLEAN_SIZE;
            default -> Constant.CHAR_SIZE * metaAttribute.getMaxLength();
        };

        return (int) Math.floor(pageSize / (maxSize)) - 1;
    }

    public static void printNodes() {
        for (Node n : nodes) {
            System.out.println(n);
        }
    }

    @Override
    public String toString() {
        if (rootIndex == -1) {
            return "No tree";
        }
        Node root = nodes.get(rootIndex);
        return root.toString();
    }

    /**
     * [numOfNodes(int), rootIndex(int)]
     *
     * @return
     */
    public byte[] serialize() {
        byte[] bytes = Helper.convertIntToByteArray(numOfNodes);
        bytes = Helper.concatenate(bytes, Helper.convertIntToByteArray(rootIndex));
        return bytes;
    }

    public static BPlusTree deserialize(byte[] bytes, File file, MetaAttribute metaAttribute, int pageSize) {
        int i = 0;
        int numOfNodes = Helper.convertByteArrayToInt(Arrays.copyOf(bytes, i += Constant.INTEGER_SIZE));
        int rootIndex = Helper.convertByteArrayToInt(Arrays.copyOfRange(bytes, i, i + Constant.INTEGER_SIZE));
        return new BPlusTree(file, metaAttribute, numOfNodes, rootIndex, pageSize);
    }

    public int getPageSize() {
        return pageSize;
    }

    private int getBinarySize() {
        return Constant.INTEGER_SIZE * 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BPlusTree bPlusTree = (BPlusTree) o;
        return rootIndex == bPlusTree.rootIndex && N == bPlusTree.N && numOfNodes == bPlusTree.numOfNodes && pageSize == bPlusTree.pageSize && Objects.equals(file, bPlusTree.file) && Objects.equals(metaAttribute, bPlusTree.metaAttribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootIndex, N, file, numOfNodes, pageSize, metaAttribute);
    }
}
