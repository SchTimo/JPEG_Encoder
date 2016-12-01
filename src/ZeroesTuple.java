import java.util.LinkedList;
import java.util.List;

/**
 * Created by matth on 17.01.2016.
 */
public class ZeroesTuple {

    int numberOfPrecedingZeroes = 0;
    Double val = 0.0;

    List<Boolean> bitRepVal = new LinkedList<Boolean>();
    int category = 0;

    List<Boolean> huffmannCodeList = new LinkedList<Boolean>();


    public ZeroesTuple(int c, double val) {
        numberOfPrecedingZeroes = c;
        this.val = val;
    }

}
