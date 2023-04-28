package main.StorageManager.B_Tree;

import main.Constants.Constant.DataType;
import main.StorageManager.Data.Page;

import java.util.ArrayList;

public class Node {

    private final DataType dataType;
    private final boolean isLeaf;
    private Integer parentIndex;
    private final int index;
    private ArrayList<Object> searchKeys;
    private ArrayList<RecordPointer> recordPointers;
    private final int N;
    private BPlusTree bPlusTree = null;

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
                ArrayList<RecordPointer> recordPointers, int N, Integer parentIndex, int index) {
        this.dataType = dataType;
        this.isLeaf = isLeaf;
        this.searchKeys = searchKeys;
        this.recordPointers = recordPointers;
        this.N = N;
        this.parentIndex = parentIndex;
        this.index = index;
    }

    public Node(DataType dataType, boolean isLeaf, int N, int index, BPlusTree bPlusTree) {
        this.dataType = dataType;
        this.isLeaf = isLeaf;
        this.searchKeys = new ArrayList<>();
        this.recordPointers = new ArrayList<>();
        this.N = N;
        this.parentIndex = null;
        this.index = index;
        this.bPlusTree = null;
    }

    private Node splitRoot() {
        Node sibling = this.split();
        Node parent = new Node(dataType, false, N, BPlusTree.getNextIndexAndIncrement());
        RecordPointer left = new RecordPointer(-1, this.index);
        RecordPointer right = new RecordPointer(-1, sibling.index);
        parent.addElementByIndex(0, sibling.searchKeys.get(0), left, right);
        this.parentIndex = parent.index;
        sibling.parentIndex = parent.index;
        BPlusTree.putNode(parent);
        return parent;
    }

    private Node splitRoot(Node leftChild, Node rightChild) {
        Node sibling = this.split();
        Node parent = new Node(dataType, false, N, BPlusTree.getNextIndexAndIncrement());
        RecordPointer left = new RecordPointer(-1, this.index);
        RecordPointer right = new RecordPointer(-1, sibling.index);
        Object elementLiftedUp = sibling.searchKeys.get(0);
        parent.addElementByIndex(0, sibling.searchKeys.get(0), left, right);
        sibling.searchKeys.remove(0);
        this.parentIndex = parent.index;
        sibling.parentIndex = parent.index;
        int compareResult = compareValues(rightChild.searchKeys.get(rightChild.searchKeys.size() - 1), elementLiftedUp);
        if (compareResult >= 0) {
            leftChild.parentIndex = sibling.index;
            rightChild.parentIndex = sibling.index;
        }
        BPlusTree.putNode(parent);
        return parent;
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
            newNode = new Node(dataType, true, newSearchKeys, newRecordPointers, N, parentIndex,
                    BPlusTree.getNextIndexAndIncrement());
        } else {
            newNode = new Node(dataType, false, newSearchKeys, newRecordPointers, N, parentIndex,
                    BPlusTree.getNextIndexAndIncrement());
        }
        BPlusTree.putNode(newNode);

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

    private Node addNodeToParent(Node newNode) {
        Object searchKey = newNode.searchKeys.get(0);
        Node parent = BPlusTree.getNodeAtIndex(parentIndex);
        boolean added = false;
        for (int i = 0; i < parent.searchKeys.size(); i++) {
            int compareValue = compareValues(searchKey, parent.searchKeys.get(i));
            if (compareValue < 0) {
                parent.addElementByIndex(i, searchKey, new RecordPointer(-1, newNode.index));
                newNode.parentIndex = parent.index;
                if (parent.isRoot() && !newNode.isLeaf) {
                    newNode.searchKeys.remove(0);
                }
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
        }
        if (parent.overflow()) {
            // if it's a root
            if (parent.isRoot()) {
                return parent.splitRoot(this, newNode);
            } else {
                Node parentSibling = parent.split();
                this.parentIndex = parentSibling.index;
                newNode.parentIndex = parentSibling.index;
                return parent.addNodeToParent(parentSibling);
            }
        }
        return null;
    }

    public Node insert(Object searchValue) {
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
                return null;
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
                if (this.isLeaf){
                    this.recordPointers.remove(i);
                    this.searchKeys.remove(i);
                    if(minNum() > searchKeys.size()){
                        // try to burrow, else merge
                        handleDeficiency();
                    }
                }
                else {
                    getRecordPointerNode(i + 1).delete(searchValue);
                }
                return;
            }
            else if (compareValue < 0){
                if(this.isLeaf){
                    System.out.println("Search Value doesn't exist");
                }
                else{
                    getRecordPointerNode(i).delete(searchValue);
                }
                return;
            }
        }
        if(this.isLeaf){
            System.out.println("Search Value doesn't exist");
        } else {
            getRecordPointerNode(this.recordPointers.size() - 1).delete(searchValue);
        }
    }

    private void handleDeficiency(){
        ArrayList<Node> siblings = getSiblings();
        Node leftSibling = siblings.get(0);
        Node rightSibling = siblings.get(1);
        Node parentNode = BPlusTree.getNodeAtIndex(parentIndex);
        if(!burrow(parentNode, leftSibling, rightSibling)){
            merge(parentNode, leftSibling, rightSibling);
        }
    }

    public boolean burrow(Node parentNode, Node leftSibling, Node rightSibling) {
        //borrowing from siblings
        if (isLeaf) {
            if (leftSibling != null && leftSibling.searchKeys.size() > leftSibling.minNum()) {
                Object borrow = leftSibling.searchKeys.remove(leftSibling.searchKeys.size() - 1);
                searchKeys.add(0, borrow);

                parentNode.searchKeys.set(getIndexRelativetoParent() - 1, borrow);
                return true;

            } else if (rightSibling != null && rightSibling.searchKeys.size() > rightSibling.minNum()) {
                Object borrow = rightSibling.searchKeys.remove(0);
                searchKeys.add(borrow);
                parentNode.searchKeys.set(getIndexRelativetoParent(), rightSibling.searchKeys.get(0));
                return true;
            }
        } else {
            // Internal Node
            int relativeIndex = getIndexRelativetoParent();
            if (leftSibling != null && leftSibling.searchKeys.size() > leftSibling.minNum()) {
                Object borrow = parentNode.searchKeys.get(relativeIndex - 1);
                Object transfer = leftSibling.searchKeys.remove(leftSibling.searchKeys.size() - 1);

                this.searchKeys.add(0, borrow);
                parentNode.searchKeys.set(relativeIndex - 1, transfer);
                RecordPointer recordPointer = leftSibling.recordPointers.remove(leftSibling.recordPointers.size() - 1);
                this.recordPointers.add(0, recordPointer);
                return true;
            } else if (rightSibling != null && rightSibling.searchKeys.size() > rightSibling.minNum()){
                Object borrow = parentNode.searchKeys.get(relativeIndex);
                Object transfer = rightSibling.searchKeys.remove(0);

                this.searchKeys.add(borrow);
                parentNode.searchKeys.set(relativeIndex, transfer);
                RecordPointer recordPointer = rightSibling.recordPointers.remove(0);
                this.recordPointers.add(recordPointer);
                return true;
            }
        }
        return false;
    }

    public void merge(Node parentNode, Node leftSibling, Node rightSibling){
        int relativeIndex = getIndexRelativetoParent();
        int parentIndexToDelete;

        // Record Pointer to delete / replace
        int rp1;
        int rp2;
        if (isLeaf) {
            // if merging to the left
            if (leftSibling != null) {
                parentIndexToDelete = relativeIndex - 1;
                rp1 = relativeIndex;
                rp2 = relativeIndex - 1;
                for (int i = leftSibling.searchKeys.size() - 1; i >= 0; i--){
                    this.searchKeys.add(0, leftSibling.searchKeys.get(i));
                }

            } else {
                // Merging to the right
                parentIndexToDelete = relativeIndex;
                rp1 = relativeIndex;
                rp2 = relativeIndex + 1;
                this.searchKeys.addAll(rightSibling.searchKeys);
            }

            // delete the SK for parent Node
            parentNode.searchKeys.remove(parentIndexToDelete);
            parentNode.recordPointers.remove(rp1);
            parentNode.recordPointers.remove(rp2);

            int lesserIndex = Math.min(rp1, rp2);

            // TODO might be array out of bounds for lesserIndex
            parentNode.recordPointers.add(lesserIndex, new RecordPointer(-1, this.index));

            // TODO Check if parentNode is sufficient
        } else {
            // Internal Node
            if (leftSibling != null) {
                parentIndexToDelete = relativeIndex - 1;
                rp1 = relativeIndex;
                rp2 = relativeIndex - 1;

                // Drag the parent Value down
                Object parentValue = parentNode.searchKeys.remove(parentIndexToDelete);

                for (int i = leftSibling.searchKeys.size() - 1; i >= 0; i--){
                    this.searchKeys.add(0, leftSibling.searchKeys.get(i));
                }
                this.searchKeys.add(parentValue);

                for (int i = leftSibling.recordPointers.size() - 1; i >= 0; i--){
                    this.recordPointers.add(0, leftSibling.recordPointers.get(i));
                }

            } else {
                // Merging to the right
                parentIndexToDelete = relativeIndex;
                rp1 = relativeIndex;
                rp2 = relativeIndex + 1;

                // Drag the parent Value down
                Object parentValue = parentNode.searchKeys.remove(parentIndexToDelete);

                this.searchKeys.addAll(rightSibling.searchKeys);
                this.searchKeys.add(0, parentValue);

                this.recordPointers.addAll(rightSibling.recordPointers);
            }
            parentNode.recordPointers.remove(rp1);
            parentNode.recordPointers.remove(rp2);

            int lesserIndex = Math.min(rp1, rp2);

            // TODO might be array out of bounds for lesserIndex
            parentNode.recordPointers.add(lesserIndex, new RecordPointer(-1, this.index));

        }
        if (parentNode.searchKeys.size() < parentNode.minNum()) {
            if(!parentNode.isRoot()) {
                parentNode.handleDeficiency();
            } else {
                if (parentNode.searchKeys.size() == 0){
                    this.bPlusTree = parentNode.bPlusTree;
                    this.bPlusTree.setRootIndex(this.index);
                    this.parentIndex = null;
                }
            }
        }
    }

    public void update(){
        // TODO not sure about parameter but update Node
    }

    public byte[] serialize(){
        // TODO
        return null;
    }

    public ArrayList<Node> getSiblings(){
        Node parent = BPlusTree.getNodeAtIndex(this.parentIndex);
        ArrayList<Node> result = new ArrayList<>();
        for (int i = 0; i < parent.recordPointers.size(); i++){
            if (parent.recordPointers.get(i).getIndex() == this.index){
                result.add(parent.getRecordPointerNode(i - 1));
                result.add(parent.getRecordPointerNode(i + 1));
            }
        }
        return result;
    }

    public int getIndexRelativetoParent(){
        Node parent = BPlusTree.getNodeAtIndex(this.parentIndex);
        ArrayList<Node> result = new ArrayList<>();
        for (int i = 0; i < parent.recordPointers.size(); i++){
            if (parent.recordPointers.get(i).getIndex() == this.index){
                return i;
            }
        }

        return -1;
    }

    private Node getRecordPointerNode(int index){
        try {
            RecordPointer recordPointer = this.recordPointers.get(index);
            int recordPointerIndex = recordPointer.getIndex();
            return BPlusTree.getNodeAtIndex(recordPointerIndex);
        } catch (IndexOutOfBoundsException e){
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
        if(!this.isLeaf) {
            for (RecordPointer recordPointer : recordPointers) {
                if (recordPointer.getPageNumber() == -1) {
                    Node node = BPlusTree.getNodeAtIndex(recordPointer.getIndex());
                    if (node != null) {
                        sb.append(node.toString());
                    }
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public void insertValuesForTesting(int values){
        this.searchKeys.add(values);
    }

    public void setParentIndexForTesting(int index){
        this.parentIndex = index;
    }

    public void setRecordPointers(int index){
        this.recordPointers.add(new RecordPointer(-1, index));
    }
}
