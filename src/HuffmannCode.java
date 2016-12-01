import HuffmannTree.HuffmannTreeNode;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Created by matth on 09.11.2015.
 */
public class HuffmannCode {

    /**
     * Variablen
     */
    int[] symbols;
    HashMap<Integer,Double> probabilities = null;


    /**
     * Konstruktor
     *
     * @param values
     */
    public HuffmannCode(int[] values) {

        this.symbols = values;
    }


    /**
     * Baum aus den Symbolen aufbauen und jedem Blatt mit Symbol seinen Huffmann-Code zuweisen
     *
     * @return
     */
    public HuffmannTreeNode encode() {

        List<HuffmannTreeNode> trees = new ArrayList<>();

        //
        // 1. Erstelle einen Baum f�r jedes Zeichen. Jeder der B�ume enth�lt anfangs
        //    nur ein Blatt, das Symbol
        //
        probabilities = calculatePropabilities(symbols);

        for(Map.Entry<Integer,Double> current : (Set<Map.Entry<Integer,Double>>) probabilities.entrySet()) {
            trees.add(new HuffmannTreeNode(current.getKey(), current.getValue(), null));
        }

        //
        // 2. Suche die beiden B�ume mit der geringsten Auftretenswahrscheinlichkeit.
        //    Ersetze diese 2 B�ume durch einen neuen "Baum", der sie an zwei
        //    Zweigen zusammenfasst
        //

        List<HuffmannTreeNode> resultTree = mergeAccordingToProbability(trees);

        resultTree.get(0).traverseAndEncode();
        return resultTree.get(0);
    }

    /**
     * Baum nach den Wahrscheinlichkeiten gewichtet aufbauen
     *
     * @param trees
     * @return
     */
    private List<HuffmannTreeNode> mergeAccordingToProbability (List<HuffmannTreeNode> trees) {

        HuffmannTreeNode newNode = null;
        HuffmannTreeNode last = null;
        HuffmannTreeNode secondToLast = null;

        while(trees.size()>1) {

            //
            //�ber das sortieren garantieren, dass die beiden am wenigsten wahrscheinlichen B�ume rechts landen
            //
            Collections.sort(trees, Collections.reverseOrder());

            //
            //Neuer Knoten kriegt als Value -1. Damit k�nnen alle Knoten von den Bl�ttern unterschieden werden.
            //
            //Wahrscheinlichkeiten der Kinderknoten werden im Elternknoten addiert
            //
            newNode = new HuffmannTreeNode(-1, trees.get(trees.size() - 1).getProbability() + trees.get(trees.size() - 2).getProbability(), null);

            last = trees.get(trees.size() - 1);
            secondToLast = trees.get(trees.size() - 2);

            //
            //Sicherstellen, dass der tiefste Baum immer rechts landet
            //
            if(secondToLast.getDepthOfTheTreeBelowThisNode() > last.getDepthOfTheTreeBelowThisNode()) {

                newNode.setLeftChild(last);
                newNode.setRightChild(secondToLast);
            }
            else {

                newNode.setLeftChild(secondToLast);
                newNode.setRightChild(last);
            }

            trees.remove(trees.size() - 2);
            trees.remove(trees.size() - 1);

            trees.add(newNode);

            //
            // 3. Wiederhole ab (2.) bis nur noch ein Baum �brig ist
            //
        }

        return trees;
    }


    /**
     * Wahrscheinlichkeiten berechnen f�r jedes Symbol
     *
     * @param symbols
     * @return
     */
    private HashMap calculatePropabilities(int[] symbols) {

        HashMap<Integer,Double> probabilities = new HashMap<Integer,Double>();

        int symbol;
        double count;

        for(int k = 0; k < symbols.length;k++) {

            symbol = symbols[k];
            count = 0;

            for (int i = 0; i < symbols.length; i++) {

                if (symbols[i] == symbol) {
                    count++;
                }
            }
            probabilities.put(symbol,count / symbols.length);
        }
        return probabilities;
    }

    /**
     * Methode, die eine Codierung nur aus Einsern bestehend entfernt
     *
     * @param treeOfInterest
     */
    public void removeCodeConsistingOfOnlyNumberOnes(HuffmannTreeNode treeOfInterest) {

        //
        //Traversieren...
        //
        if(treeOfInterest.getRightChild() != null) {

            removeCodeConsistingOfOnlyNumberOnes(treeOfInterest.getRightChild());

        } else {
            //
            //Falls wir die Funktion auf einem Baum aufrufen, der durch Tiefenbeschr�nkung rechts unten keinen Wert stehen hat.
            //
            if(treeOfInterest.getValueAtNodeToEncode() != -1) {
                moveBottomRightToLeftOfNewBottomRightNode(treeOfInterest);
            }
        }
    }


    /**
     * Methode, die den Knoten rechts unten durch einen neuen Knoten ersetzt.
     * Der ersetzte Knoten wandert an den neuen Knoten als linkes Blatt.
     *
     * @param treeOfInterest
     */
    private void moveBottomRightToLeftOfNewBottomRightNode(HuffmannTreeNode treeOfInterest) {

        //
        //An untersten Knoten einen neuen anh�ngen
        //
        HuffmannTreeNode newNode = new HuffmannTreeNode(-1,treeOfInterest.getProbability(),treeOfInterest.getParent());

        treeOfInterest.setLeftChild(newNode);
        treeOfInterest.setRightChild(null);

        //
        //Werte aus altem Knoten, jetzt der Elternknoten, ins neue Kind manuell kopieren, um fehlerhaftes Umh�ngen zu vermeiden
        //
        newNode.setCodeBits(treeOfInterest.getCodeBits() + "0");

        List<Boolean> list = treeOfInterest.getCodeBitsBoolList();
        list.add(false);
        newNode.setCodeBitsBoolList(list);

        newNode.setDepthLevelOfThisNodeInTree(treeOfInterest.getDepthLevelOfThisNodeInTree() + 1);

        newNode.setProbability(treeOfInterest.getProbability());
        newNode.setValueAtNodeToEncode(treeOfInterest.getValueAtNodeToEncode());

        //
        //Nachdem wir das ValueAtNode vom alten Knoten, jetzt Elternknoten, nicht mehr brauchen, bekommt er den Dummywert f�r Knoten statt Bl�tter
        //
        treeOfInterest.setValueAtNodeToEncode(-1);
    }


    /**
     * Methode, die f�r den �bergebenen OldResultTree eine maximal Tiefe bis einschlie�lich zum �bergebenen Level garantiert
     *
     * @param level
     * @param oldResultTree
     * @return
     */
    public HuffmannTreeNode guaranteeMaxDepthOfLevel(int level, HuffmannTreeNode oldResultTree){

        level -= 1;
        //
        //Variable f�r das Ergebnis
        //
        HuffmannTreeNode newResultTree = null;

        //
        //Die Knoten einschlie�lich dem Level, f�r das garantiert werden soll, abschneiden und aus der Static List der Knoten-Klasse holen
        //
        oldResultTree.cutOffNodesWithLevel(level);
        LinkedList<HuffmannTreeNode> offCutNodes = HuffmannTreeNode.getOffCutNodes();


        //
        //In offCutNodes sind nun die unten abgeschnittenen Baumteile drin, d.h. ganze B�ume mit Kindern. Diese erstmal flachklopfen...
        //
        LinkedList<HuffmannTreeNode> flatTree = new LinkedList<HuffmannTreeNode>();

        for (HuffmannTreeNode current : offCutNodes) {

            current.treeToLinkedList(flatTree);
        }

        //
        //...und aus der flachen Liste dann einen neuen Huffmann-Baum aufbauen
        //
        List<HuffmannTreeNode> listForBinaryCutOffNodesTree = new ArrayList<HuffmannTreeNode>();

        for(HuffmannTreeNode current : flatTree) {

           listForBinaryCutOffNodesTree.add(new HuffmannTreeNode(current.getValueAtNodeToEncode(), probabilities.get(current.getValueAtNodeToEncode()), null));
        }

        List<HuffmannTreeNode> binarySubTree = mergeAccordingToProbability(listForBinaryCutOffNodesTree);

        //
        //Auf dem gewonnenen Sub-Baum aus den abgeschnittenen Knoten noch die Tiefe feststellen.
        //Diese kann nach dem Setzen der Levels �ber HuffmannTreeNode.getMaxSubTreeLevel() statisch abgerufen werden
        //
        binarySubTree.get(0).setOnlyTreeDepthLevels();

        //
        // Die Ebene bestimmen, auf dem der Sub-Baum in den alten Baum eingebaut werden soll
        //
        int insertLevel = level - HuffmannTreeNode.getMaxSubTreeLevel() - 1;

        //
        //Sicher ist sicher...
        //
        if(insertLevel < 0){

            insertLevel = 0;
        }

        //
        //Alle Knoten des alten Baums, die auf dem ermittelten Level liegen, holen und sortieren,
        //weil wir den mit der geringsten Wahrscheinlichkeit suchen
        //
        oldResultTree.getAllNodesOnLevel(insertLevel);

        LinkedList<HuffmannTreeNode> nodesOnLevel = HuffmannTreeNode.getNodesOnLevel();

        Collections.sort(nodesOnLevel);


        //
        //Und nun den Sub-Baum in den alten einbauen.
        //M�glichkeit eins: Neuer Kopf f�r alten Baum und Sub-Baum, falls als Level der oberste Knoten ermittelt wurde
        //
        if(nodesOnLevel.get(0).getParent() == null) {

            HuffmannTreeNode newNode = new HuffmannTreeNode(-1,0.0,null);

            if(oldResultTree.getDepthOfTheTreeBelowThisNode() > binarySubTree.get(0).getDepthOfTheTreeBelowThisNode()){

                newNode.setRightChild(oldResultTree);
                newNode.setLeftChild(binarySubTree.get(0));

            }else{

                newNode.setLeftChild(oldResultTree);
                newNode.setRightChild(binarySubTree.get(0));
            }

            newResultTree = newNode;

        //
        //Falls der Knoten zum Einh�ngen ein linkes Kind ist:
        //
        }else if(nodesOnLevel.get(0).getParent().getLeftChild() == nodesOnLevel.get(0)) {

            HuffmannTreeNode oldParent = nodesOnLevel.get(0).getParent();

            HuffmannTreeNode newNode = new HuffmannTreeNode(-1,0.0,nodesOnLevel.get(0).getParent());
            newNode.setLeftChild(nodesOnLevel.get(0));
            newNode.setRightChild(binarySubTree.get(0));

            oldParent.setLeftChild(newNode);

            newResultTree = oldParent;

        //
        //Falls der Knoten zum Einh�ngen ein rechtes Kind ist
        //
        }else if(nodesOnLevel.get(0).getParent().getRightChild() == nodesOnLevel.get(0)) {

            HuffmannTreeNode oldParent = nodesOnLevel.get(0).getParent();

            HuffmannTreeNode newNode = new HuffmannTreeNode(-1,0.0,nodesOnLevel.get(0).getParent());
            newNode.setLeftChild(nodesOnLevel.get(0));
            newNode.setRightChild(binarySubTree.get(0));

            oldParent.setRightChild(newNode);

            newResultTree = oldParent;
        }

        //
        //F�r den Ergebnisbaum noch die Ebeneninformation zu jedem Knoten abspeichern
        //
        newResultTree.setOnlyTreeDepthLevels();

        //
        //Huffmann-Codes den Symbolen zuordnen. Die statischen Dictionaries vorher leeren.
        //
        resetDictionaries();
        newResultTree.traverseAndEncode();

        return newResultTree;
    }

    /**
     * Methode zum resetten der Dictionaries der TreeNode. Da diese f�r die ganze Klasse stehen,
     * kann dies notwendig sein bei Erzeugen eines neuen Baums.
     */
    private void resetDictionaries() {

        HuffmannTreeNode.setCodeAsBoolsToValDictionary(new HashMap<>());
        HuffmannTreeNode.setCodeToValueDictionary(new HashMap<>());

        HuffmannTreeNode.setValueToCodeAsBoolsDictionary(new HashMap<>());
        HuffmannTreeNode.setValueToCodeDictionary(new HashMap<>());
    }

    /**
     * Liefert einen Baum zur�ck, bei dem garantiert die minimale H�he jedes rechten Unterbaums gr��er gleich
     * der maximalen H�he jedes linken Unterbaums ist
     *
     * @param insertTree
     */
    public HuffmannTreeNode weightsOnRightSide(HuffmannTreeNode insertTree) {

        LinkedList<HuffmannTreeNode> sorted = new LinkedList<>();

        LinkedList<HuffmannTreeNode> valueNodes = new LinkedList<HuffmannTreeNode>();
        insertTree.treeToLinkedList(valueNodes);

        Collections.sort(valueNodes,new TreeLevelComparator());

        HuffmannTreeNode head = new HuffmannTreeNode(-1,0.0,null);

        head.buildCorrectTree(valueNodes);

        return head;
    }
}
