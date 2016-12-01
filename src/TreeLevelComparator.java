import HuffmannTree.HuffmannTreeNode;

import java.util.Comparator;

/**
 * Created by matth on 07.12.2015.
 */
public class TreeLevelComparator implements Comparator<HuffmannTreeNode> {

    @Override
    public int compare(HuffmannTreeNode o1, HuffmannTreeNode o2) {

        if(o1.getDepthLevelOfThisNodeInTree() >= o2.getDepthLevelOfThisNodeInTree()) {
            return 1;
        }

        if(o1.getDepthLevelOfThisNodeInTree() < o2.getDepthLevelOfThisNodeInTree()) {
            return -1;
        }

        return 0;
    }
}
