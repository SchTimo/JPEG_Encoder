import ColorModels.Pixel;

import javax.vecmath.GMatrix;

/**
 * Created by timo on 11.10.15.
 */
public class Picture {


    //region Variables

    private Pixel[][] allPixelsInPictureArray;

    private GMatrix channel1;
    private GMatrix channel2;
    private GMatrix channel3;


    private Block[][] blocksOfChannel1;
    private Block[][] blocksOfChannel2;
    private Block[][] blocksOfChannel3;


    private String magicNumber;

    //endregion


    //region Constructors

    public Picture() {
        this(20, 20);
    }

    public Picture(int numCols, int numRows) {

        allPixelsInPictureArray = new Pixel[numCols][numRows];

        channel1 = new GMatrix(numCols,numRows);
        channel2 = new GMatrix(numCols,numRows);
        channel3 = new GMatrix(numCols,numRows);
    }

    //endregion


    //region Getters & Setters

    public Pixel[][] getAllPixelsInPictureArray() {

        return allPixelsInPictureArray;
    }

    public void setAllPixelsInPictureArray(Pixel[][] allPixelsInPictureArray) {

        this.allPixelsInPictureArray = allPixelsInPictureArray;
    }

    public String getMagicNumber() {

        return magicNumber;
    }

    public GMatrix getChannel1() {
        return channel1;
    }

    public void setChannel1(GMatrix channel1) {
        this.channel1 = channel1;
    }

    public GMatrix getChannel2() {
        return channel2;
    }

    public void setChannel2(GMatrix channel2) {
        this.channel2 = channel2;
    }

    public GMatrix getChannel3() {
        return channel3;
    }

    public void setChannel3(GMatrix channel3) {
        this.channel3 = channel3;
    }

    /**
     * gets the given Pixel referenced by the given Coordinate or the last Pixel inside the valid area
     *
     * @param coordX given x-Coord
     * @param coordY given y-Coord
     * @return the ColorModels.RGBPixel for the given Coordinates
     */
    public Pixel getPixel(int coordX, int coordY) {

        //komplett drin
        if (coordX < allPixelsInPictureArray.length && coordY < allPixelsInPictureArray[0].length) {

            return allPixelsInPictureArray[coordX][coordY];
        }

        //nach links unten draussen
        else if (coordX < 0 && coordY >= allPixelsInPictureArray[0].length) {

            return allPixelsInPictureArray[0][allPixelsInPictureArray[0].length - 1];
        }

        //nach links draussen
        else if (coordX < 0 && coordY < allPixelsInPictureArray[0].length && coordY >= 0) {

            return allPixelsInPictureArray[0][coordY];
        }

        //links oben draussen
        else if (coordX < 0 && coordY < 0) {
            //Pixel an der Ecke oben links
            return allPixelsInPictureArray[0][0];
        }

        //oben draussen
        else if (coordX < allPixelsInPictureArray.length && coordY < 0 && coordX >= 0) {

            return allPixelsInPictureArray[coordX][0];
        }

        //rechts oben draussen
        else if (coordX >= allPixelsInPictureArray.length && coordY < 0) {

            return allPixelsInPictureArray[allPixelsInPictureArray.length - 1][0];
        }

        //nach rechts draussen
        else if (coordX >= allPixelsInPictureArray.length && coordY < allPixelsInPictureArray[0].length && coordY >= 0) {

            return allPixelsInPictureArray[allPixelsInPictureArray.length - 1][coordY];
        }

        //rechts unten draussen
        else if (coordX >= allPixelsInPictureArray.length && coordY >= allPixelsInPictureArray[0].length) {
            //Pixel an der Ecke unten Rechts
            return allPixelsInPictureArray[allPixelsInPictureArray.length - 1][allPixelsInPictureArray[0].length - 1];
        }

        //unten draussen
        else {

            return allPixelsInPictureArray[coordX][allPixelsInPictureArray[coordX].length - 1];
        }
    }

    /**
     * sets a ColorModels.RGBPixel-Value to the given Coordinate
     *
     * @param coordX given x-coordinate
     * @param coordY given y-coordinate
     * @param color  given ColorModels.RGBPixel-Value or YCbCr-Value
     */
    public void setPixel(int coordX, int coordY, Pixel color) {

        if (coordX >= 0 && coordX < allPixelsInPictureArray.length && coordY >= 0 && coordY < allPixelsInPictureArray[0].length) {

            this.allPixelsInPictureArray[coordX][coordY] = color;
        }
    }

    public void setMagicNumber(String magicNumber) {
        this.magicNumber = magicNumber;
    }

    public Block[][] getBlocksOfChannel1() {
        return blocksOfChannel1;
    }

    public void setBlocksOfChannel1(Block[][] blocksOfChannel1) {
        this.blocksOfChannel1 = blocksOfChannel1;
    }

    public Block[][] getBlocksOfChannel2() {
        return blocksOfChannel2;
    }

    public void setBlocksOfChannel2(Block[][] blocksOfChannel2) {
        this.blocksOfChannel2 = blocksOfChannel2;
    }

    public Block[][] getBlocksOfChannel3() {
        return blocksOfChannel3;
    }

    public void setBlocksOfChannel3(Block[][] blocksOfChannel3) {
        this.blocksOfChannel3 = blocksOfChannel3;
    }


    //endregion
}
