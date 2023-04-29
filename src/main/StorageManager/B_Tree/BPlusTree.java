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
            persistNode(rootNode);
            rootIndex = rootNode.getIndex();
        } else {
            rootNode = getNodeAtIndex(rootIndex);
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
                persistNode(node);
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
            Node rootNode = getRoot();
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

    public void setRootIndex(int index) {
        this.rootIndex = index;
    }

    public Node getNodeAtIndex(int index) {
        int pointer = getBinarySize();
        pointer += index * pageSize;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "r")) {
            randomAccessFile.seek(pointer);
            byte[] pageBytes = new byte[pageSize];
            randomAccessFile.readFully(pageBytes);
            return Node.deserialize(pageBytes, metaAttribute, N, this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void persistNode(Node node) {
        int index = getBinarySize();
        index += node.getIndex() * pageSize;
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file.getPath(), "rw")) {
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
        int size = pageSize - (Constant.INTEGER_SIZE * 4 + Constant.BOOLEAN_SIZE);
        Constant.DataType dataType = metaAttribute.getType();
        float maxSize = switch (dataType) {
            case INTEGER -> Constant.INTEGER_SIZE;
            case DOUBLE -> Constant.DOUBLE_SIZE;
            case BOOLEAN -> Constant.BOOLEAN_SIZE;
            default -> Constant.CHAR_SIZE * metaAttribute.getMaxLength();
        };
        maxSize += RecordPointer.getBinarySize();
        return (int) Math.floor(size / (maxSize)) - 1;
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

    public static int BinarySize() {
        return Constant.INTEGER_SIZE * 2;
    }

    public File getFile() {
        return file;
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
