package ColorModels;

import javax.vecmath.GVector;
import javax.vecmath.Vector3f;

/**
 * Created by matth on 18.10.2015.
 */
public class Pixel {

    float item1;
    float item2;
    float item3;

    public Pixel() {
        this(0.0f,0.0f,0.0f);
    }

    public Pixel(float item1, float item2, float item3) {
        this.item1 = item1;
        this.item2 = item2;
        this.item3 = item3;
    }

    //region Getters & Setters

    public GVector getColorData() {
        return new GVector(new Vector3f(item1,item2,item3));
    }

    public float getItem1() {
        return item1;
    }

    public void setItem1(float item1) {
        this.item1 = item1;
    }

    public float getItem2() {
        return item2;
    }

    public void setItem2(float item2) {
        this.item2 = item2;
    }

    public float getItem3() {
        return item3;
    }

    public void setItem3(float item3) {
        this.item3 = item3;
    }

    //endregion

    //region Methoden
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("X: " + String.valueOf(this.getItem1()));
        sb.append("Y: " + String.valueOf(this.getItem2()));
        sb.append("Z: " + String.valueOf(this.getItem3()));

        return sb.toString();
    }
    //endregion
}
