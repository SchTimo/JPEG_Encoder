import HuffmannTree.HuffmannTreeNode;

/**
 * Created by matth on 17.01.2016.
 */
public class HuffmanManager {

    public HuffmannTreeNode encode(int[] values) {
        /**
         * Die Werte nach Huffmann codieren
         */
        HuffmannCode h = new HuffmannCode(values);
        HuffmannTreeNode treeEncoded = h.encode();

        System.out.println("\n\nPrinting Positions before Cut:");
        treeEncoded.printTree();


        /**
         * Schauen, dass der Baum korrekt nach rechts wächst
         */
        HuffmannTreeNode balancedTree = h.weightsOnRightSide(treeEncoded);
        balancedTree.traverseAndEncode();

        balancedTree.printTree();


        /**
         * Auf Höhe maxDepth begrenzen
         */
        int maxDepth = 16;

        HuffmannTreeNode cutTree = h.guaranteeMaxDepthOfLevel(maxDepth, balancedTree);

        //System.out.println("\n\nPrinting Positions after Cut:");
        cutTree.printTree();


        /**
         * Schauen, dass kein Code nur aus Einsern besteht
         */
        h.removeCodeConsistingOfOnlyNumberOnes(cutTree);

        //System.out.println("\n\nPrinting Positions after Removing OnesOnlyCode:");
        cutTree.printTree();

        return cutTree;
    }
}
