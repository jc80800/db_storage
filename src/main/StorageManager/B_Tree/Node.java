package main.StorageManager.B_Tree;

import main.Constants.Constant.DataType;

import java.util.ArrayList;

public class Node {

    private final DataType dataType;
    private final boolean isLeaf;
    private Integer parentIndex;
    private final int index;
    private ArrayList<Object> searchKeys;
    private ArrayList<RecordPointer> recordPointers;
    private final int N;

    public Node(DataType dataType, boolean isLeaf, int N, int index) {
        this.dataType = dataType;
        this.isLeaf = isLeaf;
        this.searchKeys = new ArrayList<>();
        this.recordPointers = new ArrayList<>();
        this.N = N;
        this.parentIndex = null;
        this.index = index;
    }

    public Node(DataType dataType, boolean isLeaf, ArrayList<Object> searchKeys,
                ArrayList<RecordPointer> recordPointers, int N, int index) {
        this.dataType = dataType;
        this.isLeaf = isLeaf;
        this.searchKeys = searchKeys;
        this.recordPointers = recordPointers;
        this.N = N;
        this.parentIndex = null;
        this.index = index;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    private ArrayList<Node> splitRoot() {
        Node sibling = this.split();
        Node parent = new Node(dataType, false, N, BPlusTree.getNextIndexAndIncrement());
        RecordPointer left = new RecordPointer(-1, this.index);
        RecordPointer right = new RecordPointer(-1, sibling.index);
        parent.addElementByIndex(0, sibling.searchKeys.get(0), left, right);
        if (!isLeaf) {
            sibling.searchKeys.remove(0);
        }
        this.parentIndex = parent.index;
        sibling.parentIndex = parent.index;
        ArrayList<Node> result = new ArrayList<>();
        result.add(parent);
        result.add(sibling);
        return result;
    }

    private Node split() {
        int mid = searchKeys.size() / 2;
        int recordPointerMid = (isLeaf) ?
                recordPointers.size() / 2
                : (recordPointers.size() / 2) + 1;
        // construct new Node
        ArrayList<Object> newSearchKeys = new ArrayList<>(searchKeys.subList(mid, searchKeys.size()));
        ArrayList<RecordPointer> newRecordPointers = new ArrayList<>
                (recordPointers.subList(recordPointerMid, recordPointers.size()));
        Node newNode;
        if (isLeaf) {
            newNode = new Node(dataType, true, newSearchKeys, newRecordPointers, N,
                    BPlusTree.getNextIndexAndIncrement());
        } else {
            newNode = new Node(dataType, false, newSearchKeys, newRecordPointers, N,
                    BPlusTree.getNextIndexAndIncrement());
        }

        this.searchKeys = new ArrayList<>(searchKeys.subList(0, mid));
        this.recordPointers = new ArrayList<>(recordPointers.subList(0, recordPointerMid));
        return newNode;
    }

    private void addElementByIndex(int index, Object searchKey, RecordPointer recordPointer) {
        this.searchKeys.add(index, searchKey);
        if (isLeaf) {
            this.recordPointers.add(index, recordPointer);
        } else {
            this.recordPointers.add(index + 1, recordPointer);
        }
    }

    private void addElementByIndex(int index, Object searchKey, RecordPointer left, RecordPointer right) {
        this.searchKeys.add(index, searchKey);
        this.recordPointers.add(index, left);
        this.recordPointers.add(index + 1, right);
    }

    private ArrayList<Node> addNodeToParent(Node newNode) {
        Object searchKey = newNode.searchKeys.get(0);
        Node parent = BPlusTree.getNodeAtIndex(parentIndex);
        ArrayList<Node> result = new ArrayList<>();
        boolean added = false;
        for (int i = 0; i < parent.searchKeys.size(); i++) {
            int compareValue = compareValues(searchKey, parent.searchKeys.get(i));
            if (compareValue < 0) {
                parent.addElementByIndex(i, searchKey, new RecordPointer(-1, newNode.index));
                newNode.parentIndex = parent.index;
                if (parent.isRoot() && !newNode.isLeaf) {
                    newNode.searchKeys.remove(0);
                }
                result.add(newNode);
                added = true;
                break;
            }
        }
        if (!added) {
            parent.addElementByIndex(parent.searchKeys.size(), searchKey, new RecordPointer(-1, newNode.index));
            newNode.parentIndex = parent.index;
            if (parent.isRoot() && !newNode.isLeaf) {
                newNode.searchKeys.remove(0);
            }
            result.add(newNode);
        }
        if (parent.overflow()) {
            // if it's a root
            if (parent.isRoot()) {
                ArrayList<Node> ans = parent.splitRoot();
                this.parentIndex = ans.get(1).getIndex();
                newNode.parentIndex = ans.get(1).getIndex();
                result.addAll(ans);
            } else {
                Node parentSibling = parent.split();
                this.parentIndex = parentSibling.index;
                newNode.parentIndex = parentSibling.index;
                result.addAll(parent.addNodeToParent(parentSibling));
            }
        }
        return result;
    }

    public ArrayList<Node> insert(Object searchValue) {
        for (int i = 0; i < this.searchKeys.size(); i++) {
            int compareValue = compareValues(searchValue, this.searchKeys.get(i));
            if (isLeaf && compareValue == 0) {
                System.out.println("Primary Key already exist");
                return null;
            }
            if (compareValue < 0) {
                // if root as leaf (only one node)
                if (isRoot() && isLeaf) {
                    addElementByIndex(i, searchValue, new RecordPointer(0, (int) searchValue));
                    if (overflow()) {
                        return splitRoot();
                    }
                }
                // if leaf node
                else if (isLeaf) {
                    addElementByIndex(i, searchValue, new RecordPointer(0, (int) searchValue));
                    if (overflow()) {
                        Node newNode = split();
                        return addNodeToParent(newNode);
                    }
                }
                // if internal node
                else {
                    Node node = BPlusTree.getNodeAtIndex(recordPointers.get(i).getIndex());
                    return node.insert(searchValue);
                }
            }
        }
        // largest value
        // if it's root
        if (isRoot() && isLeaf) {
            addElementByIndex(searchKeys.size(), searchValue, new RecordPointer(0, (int) searchValue));
            if (overflow()) {
                return splitRoot();
            }
        }
        // if leaf node
        else if (isLeaf) {
            addElementByIndex(searchKeys.size(), searchValue, new RecordPointer(0, (int) searchValue));
            if (overflow()) {
                Node newNode = split();
                return addNodeToParent(newNode);
            }
        }
        // if internal node
        else {
            Node node = BPlusTree.getNodeAtIndex(recordPointers.get(searchKeys.size()).getIndex());
            return node.insert(searchValue);
        }
        return null;
    }

    private boolean overflow() {
        return this.searchKeys.size() == maxNum();
    }

    public void delete(Object searchValue){
        for(int i = 0; i < this.searchKeys.size(); i++) {
            int compareValue = compareValues(searchValue, this.searchKeys.get(i));
            if (compareValue == 0) {
                if(this.isLeaf){
                    this.recordPointers.remove(i);
                    this.searchKeys.remove(i);
                    // TODO check for deficiency
                    ArrayList<Node> siblings = getSiblings();
                    Node leftSibling = siblings.get(0);
                    Node rightSibling = siblings.get(1);


                } else {
                    getRecordPointerNode(i + 1).delete(searchValue);
                }

            } if (compareValue < 0){
                if(this.isLeaf){
                    System.out.println("Search Value doesn't exist");
                    return;
                }
                else{
                    getRecordPointerNode(i).delete(searchValue);
                }
            }
        }
        if(this.isLeaf){
            System.out.println("Search Value doesn't exist");
            return;
        } else {
            getRecordPointerNode(this.recordPointers.size() - 1).delete(searchValue);
        }
    }

    public void merge(){
        // TODO merge node if the size is too low
    }

    public void update(){
        // TODO not sure about parameter but update Node
    }

    public byte[] serialize(){
        // TODO
        return null;
    }

    public ArrayList<Node> getSiblings(){
        RecordPointer recordPointer = this.recordPointers.get(this.parentIndex);
        int recordPointerIndex = recordPointer.getIndex();
        Node parent = BPlusTree.getNodeAtIndex(recordPointerIndex);

        ArrayList<Node> result = new ArrayList<>();
        for (int i = 0; i < parent.recordPointers.size(); i++){
            if (parent.recordPointers.get(i).getIndex() == this.index){
                result.add(getRecordPointerNode(i - 1));
                result.add(getRecordPointerNode(i + 1));
            }
        }
        return result;
    }

    private Node getRecordPointerNode(int index){
        try {
            RecordPointer recordPointer = this.recordPointers.get(index);
            int recordPointerIndex = recordPointer.getIndex();
            return BPlusTree.getNodeAtIndex(recordPointerIndex);
        } catch (ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    private int maxNum() {
        if (isLeaf) {
            return N - 1;
        } else {
            return N;
        }
    }

    private int minNum() {
        if (isLeaf) {
            return (int) Math.ceil((double) (N - 1) / 2);
        }
        return (int) Math.ceil((double) N / 2);
    }

    public static Node deserialize(byte[] bytes) {
        // TODO
        return null;
    }

    public int compareValues(Object searchValue, Object compareValue) {
        return switch (dataType) {
            case INTEGER -> ((Integer) searchValue).compareTo((Integer) compareValue);
            case DOUBLE -> ((Double) searchValue).compareTo((Double) compareValue);
            case BOOLEAN -> ((Boolean) searchValue).compareTo((Boolean) compareValue);
            case VARCHAR -> ((String) searchValue).compareTo((String) compareValue);
            default -> ((Character) searchValue).compareTo((Character) compareValue);
        };
    }

    public boolean isRoot() {
        return parentIndex == null;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Node (");
        for (Object sk : searchKeys) {
            sb.append(" ").append(sk);
        }
        sb.append(" )").append(" Children: {");
        for (RecordPointer recordPointer : recordPointers) {
            if (recordPointer.getPageNumber() == -1) {
                Node node = BPlusTree.getNodeAtIndex(recordPointer.getIndex());
                sb.append(node.toString());
            }
        }
        sb.append("}");
        return sb.toString();
    }
}
