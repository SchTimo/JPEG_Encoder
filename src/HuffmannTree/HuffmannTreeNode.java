package HuffmannTree;

import java.util.*;

/**
 * Created by matth on 09.11.2015.
 */
public class HuffmannTreeNode implements Comparable<HuffmannTreeNode> {

    /**
     * Variablen
     */
    private static HashMap<String,Integer> codeToValueDictionary = new HashMap<>();
    private static HashMap<Integer,String> valueToCodeDictionary = new HashMap<>();

    private static HashMap<List<Boolean>,Integer> codeAsBoolsToValDictionary = new HashMap<>();
    private static HashMap<Integer,List<Boolean>> valueToCodeAsBoolsDictionary = new HashMap<>();

    private static LinkedList<HuffmannTreeNode> queue = new LinkedList<>();
    private static LinkedList<HuffmannTreeNode> queue2 = new LinkedList<>();

    private static LinkedList<HuffmannTreeNode> offCutNodes = new LinkedList<>();
    private static LinkedList<HuffmannTreeNode> nodesOnLevel = new LinkedList<>();

    private static LinkedList<HuffmannTreeNode> treeToLinkedAsList = null;

    private static int maxSubTreeLevel = 0;

    private int valueAtNodeToEncode;
    private String codeBits = "";
    private List<Boolean> codeBitsBoolList;
    private double probability;
    private int depthOfTheTreeBelowThisNode;
    private int depthLevelOfThisNodeInTree;

    private HuffmannTreeNode leftChild;
    private HuffmannTreeNode rightChild;
    private HuffmannTreeNode parent;

    /**
     * Konstruktor
     *
     * @param valueAtNodeToEncode
     * @param probability
     * @param parent
     */
    public HuffmannTreeNode(int valueAtNodeToEncode, double probability, HuffmannTreeNode parent) {

        this.codeBits = "";
        this.codeBitsBoolList = new ArrayList<>();
        this.valueAtNodeToEncode = valueAtNodeToEncode;

        this.parent = parent;

        this.probability = probability;
        this.depthOfTheTreeBelowThisNode = 0;
        this.depthLevelOfThisNodeInTree = 0;
    }


    /**
     * Baum traversieren und jedem Symbol seinen Huffmann-Code zuweisen
     * Wird nach dem Aufbau eines Huffmann-Baums aufgerufen
     */
    public void traverseAndEncode(){

        if(parent != null) {

            //
            //Code Bits entsprechend Position des Knotens im Baum setzen
            //
            this.codeBits = parent.codeBits + this.codeBits;

            LinkedList<Boolean> temp = new LinkedList<>();
            temp.addAll(parent.codeBitsBoolList);
            temp.addAll(this.codeBitsBoolList);

            this.codeBitsBoolList = temp;

            //
            //Position im Baum setzen
            //
            this.depthLevelOfThisNodeInTree = parent.depthLevelOfThisNodeInTree + 1;

        } else {

            this.codeBits = "";
            this.codeBitsBoolList = new ArrayList<Boolean>();
            this.depthLevelOfThisNodeInTree = 0;

            codeToValueDictionary = new HashMap<>();
            valueToCodeDictionary = new HashMap<>();

            codeAsBoolsToValDictionary = new HashMap<>();
            valueToCodeAsBoolsDictionary = new HashMap<>();
        }

        if(leftChild != null) {

            leftChild.codeBits = "0";
            leftChild.codeBitsBoolList = new ArrayList<Boolean>();
            leftChild.codeBitsBoolList.add(false);

            leftChild.traverseAndEncode();
        }

        if(rightChild != null) {

            rightChild.codeBits = "1";
            rightChild.codeBitsBoolList = new ArrayList<Boolean>();
            rightChild.codeBitsBoolList.add(true);

            rightChild.traverseAndEncode();
        }

        //
        //Für den bequemen Zugriff die Codierung der Symbole in Dictionarys füllen
        //
        if(this.valueAtNodeToEncode != -1) {

            HuffmannTreeNode.codeToValueDictionary.put(this.codeBits, this.valueAtNodeToEncode);
            HuffmannTreeNode.valueToCodeDictionary.put(this.valueAtNodeToEncode, this.codeBits);

            HuffmannTreeNode.codeAsBoolsToValDictionary.put(this.codeBitsBoolList,this.valueAtNodeToEncode);
            HuffmannTreeNode.valueToCodeAsBoolsDictionary.put(this.valueAtNodeToEncode, this.codeBitsBoolList);
        }
    }


    /**
     * Knoten im Baum ab einschließlich dem übergebenen Level unten abschneiden
     *
     * @param level
     */
    public void cutOffNodesWithLevel(int level) {

        if(this.depthLevelOfThisNodeInTree >= level) {

            if (this.parent != null && this.parent.leftChild != null) {
                offCutNodes.add(this.parent.leftChild);
                this.parent.leftChild = null;
            }

            if (this.parent != null && this.parent.rightChild != null) {
                offCutNodes.add(this.parent.rightChild);
                this.parent.rightChild = null;
            }
        }

        if(leftChild != null){
            leftChild.cutOffNodesWithLevel(level);
        }

        if(rightChild != null){
            rightChild.cutOffNodesWithLevel(level);
        }
    }


    /**
     * Methode, um aus einem Baum eine flache Liste zu erstellen
     *
     * @param flatTree
     */
    public void treeToLinkedList(LinkedList<HuffmannTreeNode> flatTree) {

        if(this.valueAtNodeToEncode != -1){

            flatTree.add(this);
        }

        if(this.leftChild != null) {
            this.leftChild.treeToLinkedList(flatTree);
        }

        if(this.rightChild != null) {
            this.rightChild.treeToLinkedList(flatTree);
        }
    }


    /**
     * Über den Baum iterieren und dabei nur die Tiefenposition der Knoten im Baum eintragen
     */
    public void setOnlyTreeDepthLevels() {

        if(parent != null) {

            this.depthLevelOfThisNodeInTree = parent.depthLevelOfThisNodeInTree + 1;

            if(this.depthLevelOfThisNodeInTree > HuffmannTreeNode.maxSubTreeLevel) {

                HuffmannTreeNode.maxSubTreeLevel = this.depthLevelOfThisNodeInTree;
            }

        }else{
            this.depthLevelOfThisNodeInTree = 0;
        }

        if(leftChild != null) {
            leftChild.setOnlyTreeDepthLevels();
        }

        if(rightChild != null) {
            rightChild.setOnlyTreeDepthLevels();
        }
    }



    //
    //Code zu einem Symbol durch Traversieren des Baums finden
    //
    public String find(int valueAtNodeToEncode) {

        String result = "";

        if(this.valueAtNodeToEncode == valueAtNodeToEncode) {
            return this.codeBits;
        }

        if(leftChild != null) {
            String tempRes = leftChild.find(valueAtNodeToEncode);
            if(!tempRes.equals(""))
                return tempRes;
        }

        if(rightChild != null) {
            String tempRes = rightChild.find(valueAtNodeToEncode);
            if(!tempRes.equals(""))
                return tempRes;
        }

        return "";
    }

    /**
     * Alle Knoten auf einer Ebene in der statischen Liste "nodesOnLevel" zur Verfügung stellen
     *
     * @param level
     */
    public void getAllNodesOnLevel(int level) {

        if(this.depthLevelOfThisNodeInTree == level) {

            nodesOnLevel.add(this);
        }

        if(leftChild != null){
            leftChild.getAllNodesOnLevel(level);
        }

        if(rightChild != null){
            rightChild.getAllNodesOnLevel(level);
        }
    }

    //
    //Ebeneninformationen der Knoten in Konsole ausgeben
    //
    public void printLevels() {
        if(parent != null) {
            System.out.println(this.depthOfTheTreeBelowThisNode + "; Parent war: " + this.parent.depthOfTheTreeBelowThisNode);
        }
        else { System.out.println(this.depthOfTheTreeBelowThisNode + " ");}

        if(leftChild != null)
            HuffmannTreeNode.queue.addLast(this.leftChild);

        if(rightChild != null)
            HuffmannTreeNode.queue.addLast(this.rightChild);

        HuffmannTreeNode newCurrent = HuffmannTreeNode.queue.pollFirst();

        if(newCurrent == null)
            return;

        newCurrent.printLevels();
    }


    /**
     * Detaillierte Informationen zu Lage und Inhalt der Knoten im Baum auf Konsole ausgeben
     */
    public void printTree() {
        if(parent != null)
            System.out.println("Level: " + this.depthLevelOfThisNodeInTree + ", Code: " + this.getCodeBits() + ", Bools: " + this.getCodeBitsBoolList().toString() + ", Value: " + this.getValueAtNodeToEncode() + "; Parent war auf: " + this.parent.depthLevelOfThisNodeInTree + ", ParentCode: " + this.parent.getCodeBits());

        else  System.out.println("Level: " + this.depthLevelOfThisNodeInTree + ", Code: " + this.getCodeBits() + ", Bools: " + this.getCodeBitsBoolList().toString() + ", Value: " + this.getValueAtNodeToEncode());

        if(leftChild != null)
            HuffmannTreeNode.queue2.addLast(this.leftChild);

        if(rightChild != null)
            HuffmannTreeNode.queue2.addLast(this.rightChild);

        HuffmannTreeNode newCurrent = HuffmannTreeNode.queue2.pollFirst();

        if(newCurrent == null)
            return;

        newCurrent.printTree();
    }


    /**
     * Komparator
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(HuffmannTreeNode o) {
        if(probability >= o.getProbability()) {
            return 1;
        }
        if(probability < o.getProbability()) {
            return -1;
        }
        return 0;
    }


    /**
     * Rechtes Kind setzen
     *
     * @param rightChild
     */
    public void setRightChild(HuffmannTreeNode rightChild) {

        if(rightChild == null) {
            rightChild = null;
            return;
        }

        rightChild.parent = this;
        this.rightChild = rightChild;

        //
        //Code setzen
        //
        this.rightChild.codeBits = "1";
        this.rightChild.codeBitsBoolList.add(true);

        //
        //Tiefe setzen
        //
        if(this.leftChild != null && (this.leftChild.depthOfTheTreeBelowThisNode >= rightChild.depthOfTheTreeBelowThisNode)) {

            this.depthOfTheTreeBelowThisNode = this.leftChild.depthOfTheTreeBelowThisNode;

        } else {

            this.depthOfTheTreeBelowThisNode = rightChild.depthOfTheTreeBelowThisNode;
        }
        this.depthOfTheTreeBelowThisNode++;
    }


    /**
     * Linkes Kind setzen
     *
     * @param leftChild
     */
    public void setLeftChild(HuffmannTreeNode leftChild) {

        if(leftChild == null) {
            this.leftChild = null;
            return;
        }

        leftChild.parent = this;
        this.leftChild = leftChild;

        //
        //Code setzen
        //
        this.leftChild.codeBits = "0";
        this.leftChild.codeBitsBoolList.add(false);

        //
        //Tiefe setzen
        //
        if(this.rightChild != null && (this.rightChild.depthOfTheTreeBelowThisNode >= leftChild.depthOfTheTreeBelowThisNode)) {

            this.depthOfTheTreeBelowThisNode = this.rightChild.depthOfTheTreeBelowThisNode;

        }else {

            this.depthOfTheTreeBelowThisNode = leftChild.depthOfTheTreeBelowThisNode;
        }
        this.depthOfTheTreeBelowThisNode++;
    }

    /**
     * Baut aus dem echten Huffmann-Baum einen korrekten, der nur nach rechts wächst
     *
     * @param map
     */
    public void buildCorrectTree(LinkedList<HuffmannTreeNode> map) {

        if (!map.isEmpty()) {

            if (this.leftChild == null) {

                //
                //Sind wir genau eine Ebene über der gewünschten Ebene, dann fügen wir ein
                //
                if (this.getDepthLevelOfThisNodeInTree() + 1 == map.peekFirst().getDepthLevelOfThisNodeInTree()) {

                    this.setLeftChild(map.pollFirst());
                    this.getLeftChild().setDepthLevelOfThisNodeInTree(this.getDepthLevelOfThisNodeInTree() + 1);

                    //
                    //Hier stellen wir sicher, dass neue Knoten nur erzeugt werden, wenn wir auch wirklich noch über der
                    //Ebene sind, wo der Knoten eingefügt werden muss
                    //
                } else if(this.getDepthLevelOfThisNodeInTree() <= map.peekFirst().getDepthLevelOfThisNodeInTree()){

                    this.setLeftChild(new HuffmannTreeNode(-1, 0.0, this));
                    this.getLeftChild().setDepthLevelOfThisNodeInTree(this.getDepthLevelOfThisNodeInTree() + 1);

                    this.getLeftChild().buildCorrectTree(map);
                }
            }

            if (this.rightChild == null) {

                //
                //Sind wir genau eine Ebene über der gewünschten Ebene, dann fügen wir ein
                //
                if (this.getDepthLevelOfThisNodeInTree() + 1 == map.peekFirst().getDepthLevelOfThisNodeInTree()) {

                    this.setRightChild(map.pollFirst());
                    this.getRightChild().setDepthLevelOfThisNodeInTree(this.getDepthLevelOfThisNodeInTree() + 1);

                    //
                    //Hier stellen wir sicher, dass neue Knoten nur erzeugt werden, wenn wir auch wirklich noch über der
                    //Ebene sind, wo der Knoten eingefügt werden muss
                    //
                } else if (this.getDepthLevelOfThisNodeInTree() <= map.peekFirst().getDepthLevelOfThisNodeInTree()) {

                    this.setRightChild(new HuffmannTreeNode(-1, 0.0, this));
                    this.getRightChild().setDepthLevelOfThisNodeInTree(this.getDepthLevelOfThisNodeInTree() + 1);

                    this.getRightChild().buildCorrectTree(map);
                }
            }

            if(this.getParent() != null) {
                this.getParent().buildCorrectTree(map);
            }
        }
    }


    /**
     * Standard-Getter und -Setter
     *
     * @return
     */
    public HuffmannTreeNode getLeftChild() {
        return leftChild;
    }

    public int getDepthOfTheTreeBelowThisNode() {
        return depthOfTheTreeBelowThisNode;
    }

    public void setDepthOfTheTreeBelowThisNode(int depthOfTheTreeBelowThisNode) {
        this.depthOfTheTreeBelowThisNode = depthOfTheTreeBelowThisNode;
    }

    public HuffmannTreeNode getRightChild() {
        return rightChild;
    }

    public HuffmannTreeNode getParent() {
        return parent;
    }

    public void setParent(HuffmannTreeNode parent) {
        this.parent = parent;
    }

    public int getValueAtNodeToEncode() {
        return valueAtNodeToEncode;
    }

    public void setValueAtNodeToEncode(int valueAtNodeToEncode) {
        this.valueAtNodeToEncode = valueAtNodeToEncode;
    }

    public String getCodeBits() {
        return codeBits;
    }

    public void setCodeBits(String codeBits) {
        this.codeBits = codeBits;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }

    public List<Boolean> getCodeBitsBoolList() {
        return codeBitsBoolList;
    }

    public void setCodeBitsBoolList(List<Boolean> codeBitsBoolList) {
        this.codeBitsBoolList = codeBitsBoolList;
    }

    public int getDepthLevelOfThisNodeInTree() {
        return depthLevelOfThisNodeInTree;
    }

    public void setDepthLevelOfThisNodeInTree(int depthLevelOfThisNodeInTree) {
        this.depthLevelOfThisNodeInTree = depthLevelOfThisNodeInTree;
    }

    public static HashMap<String, Integer> getCodeToValueDictionary() {
        return codeToValueDictionary;
    }

    public static void setCodeToValueDictionary(HashMap<String, Integer> codeToValueDictionary) {
        HuffmannTreeNode.codeToValueDictionary = codeToValueDictionary;
    }

    public static HashMap<Integer, String> getValueToCodeDictionary() {
        return valueToCodeDictionary;
    }

    public static void setValueToCodeDictionary(HashMap<Integer, String> valueToCodeDictionary) {
        HuffmannTreeNode.valueToCodeDictionary = valueToCodeDictionary;
    }

    public static HashMap<List<Boolean>, Integer> getCodeAsBoolsToValDictionary() {
        return codeAsBoolsToValDictionary;
    }

    public static void setCodeAsBoolsToValDictionary(HashMap<List<Boolean>, Integer> codeAsBoolsToValDictionary) {
        HuffmannTreeNode.codeAsBoolsToValDictionary = codeAsBoolsToValDictionary;
    }

    public static HashMap<Integer, List<Boolean>> getValueToCodeAsBoolsDictionary() {
        return valueToCodeAsBoolsDictionary;
    }

    public static void setValueToCodeAsBoolsDictionary(HashMap<Integer, List<Boolean>> valueToCodeAsBoolsDictionary) {
        HuffmannTreeNode.valueToCodeAsBoolsDictionary = valueToCodeAsBoolsDictionary;
    }

    public static LinkedList<HuffmannTreeNode> getQueue() {
        return queue;
    }

    public static void setQueue(LinkedList<HuffmannTreeNode> queue) {
        HuffmannTreeNode.queue = queue;
    }

    public static LinkedList<HuffmannTreeNode> getOffCutNodes() {
        return offCutNodes;
    }

    public static void setOffCutNodes(LinkedList<HuffmannTreeNode> offCutNodes) {
        HuffmannTreeNode.offCutNodes = offCutNodes;
    }

    public static LinkedList<HuffmannTreeNode> getQueue2() {
        return queue2;
    }

    public static void setQueue2(LinkedList<HuffmannTreeNode> queue2) {
        HuffmannTreeNode.queue2 = queue2;
    }

    public static LinkedList<HuffmannTreeNode> getNodesOnLevel() {
        return nodesOnLevel;
    }

    public static void setNodesOnLevel(LinkedList<HuffmannTreeNode> nodesOnLevel) {
        HuffmannTreeNode.nodesOnLevel = nodesOnLevel;
    }

    public static int getMaxSubTreeLevel() {
        return maxSubTreeLevel;
    }

    public static void setMaxSubTreeLevel(int maxSubTreeLevel) {
        HuffmannTreeNode.maxSubTreeLevel = maxSubTreeLevel;
    }
}
