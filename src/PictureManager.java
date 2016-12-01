import ColorModels.Pixel;
import Exceptions.InvalidPPMFileException;
import Exceptions.NotP3Exception;
import Exceptions.WrongColorMaxValueException;
import HuffmannTree.HuffmannTreeNode;
import IOUtils.DCT;
import IOUtils.KeyValuePair;
import sun.awt.image.ImageWatched;

import javax.vecmath.GMatrix;
import javax.vecmath.GVector;
import javax.vecmath.Vector3f;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * Created by timo on 11.10.15.
 */
public class PictureManager {

    private final int N = 8;
    private final int Y = 0;
    private final int Cb = 1;
    private final int Cr = 2;

    private double[] channelYLuminance = new double[]
            {1.0, 1.0, 1.0, 1.0, 1.0, 2.0, 3.0, 3.0,
                    1.0, 1.0, 1.0, 1.0, 1.0, 3.0, 3.0, 3.0,
                    1.0, 1.0, 1.0, 1.0, 2.0, 3.0, 3.0, 3.0,
                    1.0, 1.0, 1.0, 2.0, 3.0, 4.0, 4.0, 3.0,
                    1.0, 1.0, 2.0, 3.0, 3.0, 5.0, 5.0, 4.0,
                    1.0, 2.0, 3.0, 3.0, 4.0, 5.0, 5.0, 4.0,
                    2.0, 3.0, 4.0, 4.0, 5.0, 6.0, 6.0, 5.0,
                    4.0, 4.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0};

    private double[] channelCbCrChrominance = new double[]
            {1.0, 1.0, 1.0, 2.0, 5.0, 5.0, 5.0, 5.0,
                    1.0, 1.0, 1.0, 3.0, 5.0, 5.0, 5.0, 5.0,
                    1.0, 1.0, 3.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                    2.0, 3.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                    5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                    5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                    5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,
                    5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0,};

    private GMatrix luminance = new GMatrix(N, N, channelYLuminance);
    private GMatrix chromminance = new GMatrix(N, N, channelCbCrChrominance);

    //region Variables

    /**
     * "Matrix" for converting RGB to YCbCr
     */
    private GVector topToYCbCr = new GVector(new Vector3f(0.299f, 0.587f, 0.114f));
    private GVector middleToYCbCr = new GVector(new Vector3f(-0.1687f, -0.3312f, 0.5f));
    private GVector bottomToYCbCr = new GVector(new Vector3f(0.5f, -0.4186f, -0.0813f));


    /**
     * "Matrix" for converting YCbCr to RGB
     */
    private GVector topToRGB = new GVector(new Vector3f(0.99986f, 0.0000115073f, 1.40209f));
    private GVector middleToRGB = new GVector(new Vector3f(1.00704f, -0.346535f, -0.719129f));
    private GVector bottomToRGB = new GVector(new Vector3f(0.964133f, 1.78432f, 0.0254785f));

    public Picture quantizeChannels(Picture pictureToEncode) {

        GMatrix luminanceMatrix = new GMatrix(N, N, channelYLuminance);
        GMatrix chrominanceMatrix = new GMatrix(N, N, channelCbCrChrominance);

        pictureToEncode.setChannel1(quantizeChannel(pictureToEncode.getChannel1(), luminanceMatrix));
        pictureToEncode.setChannel2(quantizeChannel(pictureToEncode.getChannel2(), chrominanceMatrix));
        pictureToEncode.setChannel3(quantizeChannel(pictureToEncode.getChannel3(), chrominanceMatrix));

        return pictureToEncode;
    }

    /**
     * Quantisiert Kanal
     *
     * @param colorChannelToEncode
     * @param quantizationTable
     * @return
     */
    private GMatrix quantizeChannel(GMatrix colorChannelToEncode, GMatrix quantizationTable) {

        GMatrix temp = new GMatrix(N, N);

        for (int row = 0; row < colorChannelToEncode.getNumRow(); row += N) {
            for (int col = 0; col < colorChannelToEncode.getNumCol(); col += N) {

                colorChannelToEncode.copySubMatrix(row, col, N, N, 0, 0, temp);

                temp = quantize8Block(temp, quantizationTable);

                temp.copySubMatrix(0, 0, N, N, row, col, colorChannelToEncode);
            }
        }

        return colorChannelToEncode;
    }

    /**
     * Quantisiert 8er-Block
     *
     * @param input8Block
     * @param quantizationTable
     * @return
     */
    private GMatrix quantize8Block(GMatrix input8Block, GMatrix quantizationTable) {

        double quantizedVal = 0.0;

        for (int row = 0; row < N; row++) {
            for (int col = 0; col < N; col++) {

                quantizedVal = input8Block.getElement(row, col) / quantizationTable.getElement(row, col);
                input8Block.setElement(row, col, Math.round(quantizedVal));
            }
        }

        return input8Block;
    }


    private LinkedList<Double> walkZigZag8Block(GMatrix temp) {

        int[] walk = {0, 1, 8, 16, 9, 2, 3, 10,
                17, 24, 32, 25, 18, 11, 4, 5,
                12, 19, 26, 33, 40, 48, 41, 34,
                27, 20, 13, 6, 7, 14, 21, 28,
                35, 42, 49, 56, 57, 50, 43, 36,
                29, 22, 15, 23, 30, 37, 44, 51,
                58, 59, 52, 45, 38, 31, 39, 46,
                53, 60, 61, 54, 47, 55, 62, 63};

        LinkedList<Double> result = new LinkedList<Double>();

        for (int i = 0; i < walk.length; i++) {
            result.add(temp.getElement(walk[i] / N, walk[i] % N));
        }

        return result;
    }

    //endregion


    //region Constructors

    /**
     * Inner-private class, which first inits when calling by Picturemanager itself
     */
    private static final class InstanceHolder {

        // Die Initialisierung von Klassenvariablen geschieht nur einmal und ist threadsafe
        static final PictureManager INSTANCE = new PictureManager();
    }

    /**
     * private Constructor which avoids creating of an instance from extern
     */
    private PictureManager() {

    }

    /**
     * A not synchronized getter
     */
    public static PictureManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    //endregion


    //region Methods

    /**
     * Komprimiert einen oder mehrere Farbkanaele eines Bildes.
     *
     * @param picture zu komprimierendes Bild
     * @param mode    Modus, 1 = Subsampling, 2 = Mittelwertberechnung
     * @param stride  Komressionsfaktor
     * @return komprimiertes Bild
     */
    public Picture subsamplePictureChannelsCbCr(Picture picture, int mode, int stride) {

        switch (mode) {
            case 1:
                picture.setChannel2(subsample(picture.getChannel2(), stride));
                picture.setChannel3(subsample(picture.getChannel3(), stride));
                break;
            case 2:
                picture.setChannel2(locallyAverage(picture.getChannel2(), stride));
                picture.setChannel3(locallyAverage(picture.getChannel3(), stride));
                break;
            default:
                break;
        }

        return picture;
    }

    /**
     * Bild kanalweise Arai-Codieren
     *
     * @param picture Bild mit allen Kanälen codiert
     * @return
     */
    public Picture encodeBlocks(Picture picture) {

        picture.setBlocksOfChannel1(encodeBlocksOfSingleChannel(picture.getChannel1(), Y));
        picture.setBlocksOfChannel2(encodeBlocksOfSingleChannel(picture.getChannel2(), Cb));
        picture.setBlocksOfChannel3(encodeBlocksOfSingleChannel(picture.getChannel3(), Cr));

        return picture;
    }

    /**
     * Einen einzelnen Farbkanal Arai-codieren
     *
     * @param colorChannelToEncode kodierter Kanal
     * @return
     */
    private Block[][] encodeBlocksOfSingleChannel(GMatrix colorChannelToEncode, int channelCode) {

        DCT dct = new DCT();
        GMatrix temp = new GMatrix(N, N);
        Block current;
        Block[][] blocksOfChannel = new Block[colorChannelToEncode.getNumRow() / N][colorChannelToEncode.getNumCol() / N];

        for (int row = 0; row < colorChannelToEncode.getNumRow(); row += N) {
            for (int col = 0; col < colorChannelToEncode.getNumCol(); col += N) {

                colorChannelToEncode.copySubMatrix(row, col, N, N, 0, 0, temp);

                //TODO
                current = processBlock(dct.processArai(temp), channelCode);

                blocksOfChannel[row][col] = current;
            }
        }

        blocksOfChannel = processDCComponents(blocksOfChannel);

        return blocksOfChannel;
    }


    private Block[][] processDCComponents(Block[][] inputBlocks) {

        double dcLast = 0.0;
        Block current = null;
        LinkedList<Double> DCs = new LinkedList<>();

        for (int row = 0; row < inputBlocks.length; row ++) {
            for (int col = 0; col < inputBlocks[0].length; col ++) {

                current = inputBlocks[row][col];
                current.dc -= dcLast;

                DCs.add(current.dc);

                dcLast = current.dc;
            }
        }

        LinkedList<ZeroesTuple> zeroesTuples = determineTupleList(DCs);
        zeroesTuples = determineBinaryRepresentation(zeroesTuples);
        LinkedList<Boolean> result = encodeHuffmann(zeroesTuples);

        //DC und AC zusammenschweißen
        for (int row = 0; row < inputBlocks.length; row ++) {
            for (int col = 0; col < inputBlocks[0].length; col ++) {

                current = inputBlocks[row][col];
                current.dcTuples = result;

                current.ACDCCombinedList.addAll(current.dcTuples);
                current.ACDCCombinedList.addAll(current.acTuples);
            }
        }

        return inputBlocks;
    }

    private Block processBlock(GMatrix araiedBlock, int channelCode) {

        Block result = new Block();

        //Quantisieren
        switch (channelCode) {
            case 0:
                araiedBlock = quantize8Block(araiedBlock, luminance);
                break;
            case 1:
            case 2:
                araiedBlock = quantize8Block(araiedBlock, chromminance);
            default:
                break;
        }

        //ZigZag block
        LinkedList<Double> ACDC = walkZigZag8Block(araiedBlock);

        result.dc = ACDC.pollFirst();
        result.acTuples = processLauflaenge(ACDC);

        return result;
    }

    private LinkedList<Boolean> processLauflaenge(LinkedList<Double> ACDC) {

        //Lauflaengenkodierung

        //Hier holen wir uns die Tupel mit der Anzahl Nullen vor einem Wert und dem Wert
        LinkedList<ZeroesTuple> zeroesTupleList = determineTupleList(ACDC);

        //Hier holen wir uns die Bit-Repräsentation des rechten Werts der Tupel-Liste sowie die Kategorie
        zeroesTupleList = determineBinaryRepresentation(zeroesTupleList);

        //Huffmann-Kodierung der Tupel aus Nullhäufigkeit und Kategorie
        LinkedList<Boolean> result = encodeHuffmann(zeroesTupleList);

        return result;
    }


    private LinkedList<Boolean> encodeHuffmann(LinkedList<ZeroesTuple> zeroesTupleList) {

        LinkedList<Integer> symbols = new LinkedList<Integer>();
        int[] symbolsArray = new int[zeroesTupleList.size()];

        int i = 0;
        int zeroPlusCategory = 0;

        for(ZeroesTuple current : zeroesTupleList) {

            zeroPlusCategory = Integer.parseInt(String.valueOf(current.numberOfPrecedingZeroes) + String.valueOf(current.category));
            symbols.add(zeroPlusCategory);

            symbolsArray[i++] = zeroPlusCategory;
        }

        HuffmanManager huffEncoder = new HuffmanManager();
        HuffmannTreeNode encodedTree = huffEncoder.encode(symbolsArray);

        HashMap<Integer, List<Boolean>> codeBook = encodedTree.getValueToCodeAsBoolsDictionary();

        List<Boolean> temp = null;
        LinkedList<Boolean> result = new LinkedList<>();

        ZeroesTuple tuple = null;
        int key = 0;
        for(int k = 0; k < symbols.size(); k++) {
            key = symbols.get(i);
            temp = codeBook.get(key);
            tuple = zeroesTupleList.get(i);

            result.addAll(temp);
            result.addAll(tuple.bitRepVal);
        }

        return result;
    }


    /**
     * Liefert die Bit-Repräsentation des rechten Werts der Tupel-Liste sowie die Kategorie
     *
     * @param zeroesTupleList
     * @return
     */
    private LinkedList<ZeroesTuple> determineBinaryRepresentation(LinkedList<ZeroesTuple> zeroesTupleList) {

        Integer valAsInt = 0;
        String finalResultString = "";
        String posValString = "";
        String negValString = "";
        for (ZeroesTuple current : zeroesTupleList) {

            valAsInt = current.val.intValue();

            //Negative Werte positiv machen, und die Länge der Binärrepresentation ermitteln
            if (valAsInt < 0) {

                Integer originalNegative = valAsInt;

                valAsInt *= -1;

                posValString = Integer.toBinaryString(valAsInt);
                negValString = Integer.toBinaryString(originalNegative);

                int length = negValString.length() - posValString.length();

                negValString.substring(length, negValString.length() - 1);

                finalResultString = negValString;
            }
            //Positive Werte brauchen keine Sonderbehandlung und haben die korrekte Länge
            else {
                posValString = Integer.toBinaryString(valAsInt);
                finalResultString = posValString;
            }

            List<Boolean> boolList = null;

            for (int i = 0; i < finalResultString.length(); i++) {
                boolList = new LinkedList<Boolean>();
                boolList.add(finalResultString.charAt(i) == '1' ? true : false);
            }

            current.bitRepVal = boolList;
            current.category = finalResultString.length();
        }

        return zeroesTupleList;
    }


    private LinkedList<ZeroesTuple> determineTupleList(LinkedList<Double> ACDC) {

        LinkedList<ZeroesTuple> zeroesTupleList = new LinkedList<ZeroesTuple>();
        ZeroesTuple zeroesTuple = null;

        //Von hinten durchgehen und Nuller durch EOB (double maxval) ersetzen
        if (ACDC.get(ACDC.size() - 1) == 0.0) {

            for (int i = ACDC.size() - 1; i > -1; i--) {

                if (ACDC.get(i) != 0.0) {
                    ACDC.set(i + 1, Double.MAX_VALUE);
                    break;
                }
            }
        }

        int countZeroes = 0;

        for (int i = 0; i < ACDC.size(); i++) {

            if (ACDC.get(i) == Double.MAX_VALUE) {
                zeroesTuple = new ZeroesTuple(0, 0.0);
                zeroesTupleList.add(zeroesTuple);

                break;
            } else if (ACDC.get(i) == 0.0) {
                countZeroes++;

                if (countZeroes > 15) {
                    zeroesTuple = new ZeroesTuple(15, 0.0);
                    zeroesTupleList.add(zeroesTuple);

                    countZeroes = 0;
                }
            } else {

                zeroesTuple = new ZeroesTuple(countZeroes, ACDC.get(i));
                zeroesTupleList.add(zeroesTuple);

                countZeroes = 0;
            }
        }

        return zeroesTupleList;
    }

    /**
     * Unterabtastung
     *
     * @param canal               Farbkanal (z.B. Matrix mit R, G oder B-Werten)
     * @param strideWidthPowerOf2 Kompressionsfaktor, muss Zweierprotenz sein
     * @return
     */
    public GMatrix subsample(GMatrix canal, int strideWidthPowerOf2) {

        //Pruefen, dass Zweierpotenz
        if (getDecimalsOfLogarithmDualis(canal.getNumCol()) == 0.0) {
            throw new ArithmeticException("Only Zweierpotenzen allowed!");
        }

        GMatrix compressedCanal = new GMatrix(canal.getNumRow(), canal.getNumCol() / strideWidthPowerOf2);

        //Fuer jede Zeile...
        for (int i = 0; i < canal.getNumRow(); i++) {

            //Extra-Zaehler notwendig, weil compressedCanal kleiner ist als Ursprungs-Kanal
            int columnOfCompressedCanal = 0;

            //...nimm immer die erste Spalte eines Segments mit gewuenschter Stride-Breite
            for (int j = 0; j < canal.getNumCol(); j += strideWidthPowerOf2) {

                compressedCanal.setElement(i, columnOfCompressedCanal, canal.getElement(i, j));
                columnOfCompressedCanal++;
            }
        }

        return compressedCanal;
    }

    /**
     * Mittelwertberechnung 4:2:0, immer pro Quadrat
     *
     * @param channel             Farbkanal (z.B. Matrix mit R, G oder B-Werten)
     * @param strideWidthPowerOf2 Kompressionsfaktor, muss Zweierprotenz sein
     * @return
     */
    public GMatrix locallyAverage(GMatrix channel, int strideWidthPowerOf2) {

        //Pruefen, dass Aufloesung um Zweierpotenz reduziert wird
        if (getDecimalsOfLogarithmDualis(channel.getNumCol()) == 0.0) {
            throw new ArithmeticException("Only Zweierpotenzen allowed!");
        }

        //Variable fuer den komprimierten Kanal anlegen
        GMatrix compressedCanal = new GMatrix(channel.getNumRow() / strideWidthPowerOf2, channel.getNumCol() / strideWidthPowerOf2);

        float colorVal = 0.0f;

        //Zeile und Spalte des Farbwerts fuer die Mittelwertberechnung fuer den compressedCanal
        int countRow = 0;
        int countColumn = 0;

        //Fuer jedes Set an Zeilen mit der gewuenschten Stride-Hoehe...
        for (int i = 0; i < channel.getNumRow() - 1; i += strideWidthPowerOf2) {
            countColumn = 0;

            //...nimm das Set an Spalten mit der gewuenschten Stride-Breite...
            for (int j = 0; j < channel.getNumCol(); j += strideWidthPowerOf2) {

                colorVal = 0.0f;

                //...und addiere sukzessive die Farbwerte des so ermittelten quadratischen Segments
                for (int k = i; k < i + strideWidthPowerOf2; k++) {

                    for (int l = j; l < j + strideWidthPowerOf2; l++) {

                        colorVal += channel.getElement(k, l);
                    }
                }
                //...und teile sie durch die Anzahl der betrachteten Felder => Mittelwert!
                colorVal /= strideWidthPowerOf2 * strideWidthPowerOf2;

                //Noch eintragen in den komprimierten Rueckgabe-Kanal
                compressedCanal.setElement(countRow, countColumn, colorVal);

                //Noetig, weil CompressedCanal ja kleiner ist als Ursprungskanal
                countColumn++;
            }
            //Noetig, weil CompressedCanal ja kleiner ist als Ursprungskanal
            countRow++;
        }
        return compressedCanal;
    }

    /**
     * Create Random RGB Pic
     *
     * @param sizeX
     * @param sizeY
     * @return
     */
    public Picture createRandomRGBPicture(int sizeX, int sizeY) {

        Random r = new Random();

        Picture pic = new Picture(sizeY, sizeX);

        for (int i = 0; i < sizeY; i++) {
            for (int j = 0; j < sizeX; j++) {
                pic.getChannel1().setElement(i, j, r.nextInt(255));
                pic.getChannel2().setElement(i, j, r.nextInt(255));
                pic.getChannel3().setElement(i, j, r.nextInt(255));
            }
        }
        return pic;
    }

    /**
     * Gibt die Nachkommastelle eines Logarithmus zu Basis 2
     *
     * @param x
     * @return
     */
    private double getDecimalsOfLogarithmDualis(int x) {
        double logg = Math.log(x) / Math.log(2.0);
        int comp = (int) logg;
        return logg - comp;
    }


    /**
     * @param picture Picture
     * @return void
     */
    public Picture convertToYCbCrPicture(Picture picture) {

        GMatrix canal1 = picture.getChannel1();
        GMatrix canal2 = picture.getChannel2();
        GMatrix canal3 = picture.getChannel3();

        for (int i = 0; i <= canal1.getNumCol(); i++) {
            for (int j = 0; i <= canal1.getNumCol(); i++) {

                GVector colorData = new GVector(new Vector3f((float) canal1.getElement(i, j), (float) canal2.getElement(i, j), (float) canal3.getElement(i, j)));

                canal1.setElement(i, j, Math.round(colorData.dot(topToYCbCr)) - 128.0f);
                canal2.setElement(i, j, Math.round(colorData.dot(middleToYCbCr)) - 128.0f);
                canal3.setElement(i, j, Math.round(colorData.dot(bottomToYCbCr)) - 128.0f);
            }
        }

        picture.setChannel1(canal1);
        picture.setChannel2(canal2);
        picture.setChannel3(canal3);

        return picture;
    }

    /**
     * @param picture Picture
     * @return void
     */
    private Picture calcRGBPicture(Picture picture) {

        GMatrix canal1 = picture.getChannel1();
        GMatrix canal2 = picture.getChannel2();
        GMatrix canal3 = picture.getChannel3();

        for (int i = 0; i <= canal1.getNumCol(); i++) {
            for (int j = 0; i <= canal1.getNumCol(); i++) {

                GVector colorData = new GVector(new Vector3f((float) canal1.getElement(i, j) + 128.0f, (float) (canal2.getElement(i, j) + 128.0f), (float) (canal3.getElement(i, j) + 128.0f)));

                canal1.setElement(i, j, Math.round(colorData.dot(topToRGB)));
                canal2.setElement(i, j, Math.round(colorData.dot(middleToRGB)));
                canal3.setElement(i, j, Math.round(colorData.dot(bottomToRGB)));
            }
        }

        picture.setChannel1(canal1);
        picture.setChannel2(canal2);
        picture.setChannel3(canal3);

        return picture;
    }

    /**
     * Writes a valid PPM-File out of a Picture and a given filename without extension.
     *
     * @param picture  the given Picture as Class-Picture
     * @param filename the given filename without extension
     */
    public void writePPMRGB(Picture picture, String filename) {


        filename += ".ppm";
        //int sizeX = picture.getAllPixelsInPictureArray()[0].length;
        //int sizeY = picture.getAllPixelsInPictureArray().length;
        int sizeX = picture.getChannel1().getNumCol();
        int sizeY = picture.getChannel1().getNumRow();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

            bw.write("P3");
            bw.newLine();
            bw.write("#  " + filename);
            bw.newLine();
            bw.write(sizeX + " " + sizeY);
            bw.newLine();
            bw.write("255");
            bw.newLine();
            for (int i = 0; i < sizeY; i++) {
                for (int j = 0; j < sizeX; j++) {
                    //bw.write(" " + (int) picture.getAllPixelsInPictureArray()[i][j].getItem1() + " ");
                    //bw.write(" " + (int) picture.getAllPixelsInPictureArray()[i][j].getItem2() + " ");
                    //bw.write(" " + (int) picture.getAllPixelsInPictureArray()[i][j].getItem3() + " ");
                    bw.write(" " + (int) picture.getChannel1().getElement(i, j) + " ");
                    bw.write(" " + (int) picture.getChannel2().getElement(i, j) + " ");
                    bw.write(" " + (int) picture.getChannel3().getElement(i, j) + " ");
                    bw.write("    ");
                }
                bw.newLine();
            }

            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Methode zum Umwandeln von RGB nach YCbCr
     *
     * @param picture
     * @param filename
     */
    public void writePPMYCbCr(Picture picture, String filename) {


        filename += ".ppm";
        int sizeX = picture.getAllPixelsInPictureArray()[0].length;
        int sizeY = picture.getAllPixelsInPictureArray().length;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

            bw.write("P3");
            bw.newLine();
            bw.write("#  " + filename);
            bw.newLine();
            bw.write(sizeX + " " + sizeY);
            bw.newLine();
            bw.write("255");
            bw.newLine();
            for (int i = 0; i < sizeY; i++) {
                for (int j = 0; j < sizeX; j++) {

                    bw.write(" " + (int) (float) picture.getAllPixelsInPictureArray()[i][j].getItem1() + " ");
                    bw.write(" " + (int) (float) picture.getAllPixelsInPictureArray()[i][j].getItem2() + " ");
                    bw.write(" " + (int) (float) picture.getAllPixelsInPictureArray()[i][j].getItem3() + " ");
                    bw.write("    ");

                }
                bw.newLine();
            }

            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Creates a Picture out of a given PPM-File
     *
     * @param filepath Path of the PPM-File
     * @return a Picture of Class-Picture
     * @throws NotP3Exception              thrown if there are a wrong format
     * @throws WrongColorMaxValueException thrown if there a wrong color-value
     */
    public Picture createPictureOutOfPPM(String filepath) throws NotP3Exception, WrongColorMaxValueException, InvalidPPMFileException {
        int sizeX, sizeY;
        Picture picture;
        ArrayList<Double> canal1, canal2, canal3, errorcanal;

        try {
            List<String> ppmOriginal = Files.readAllLines(Paths.get(filepath));
            List<String> ppmEdited;
            ppmEdited = killCommentsInPPMHeader(ppmOriginal);


            String[] size = ppmEdited.get(0).split(" ");
            sizeX = Integer.parseInt(size[0]);
            sizeY = Integer.parseInt(size[1]);

            ppmEdited.remove(0);
            ppmEdited.remove(0);


            String ppmString = "";
            for (String string : ppmEdited) {
                ppmString += " " + string;
            }

            String[] ppmValues = ppmString.trim().split(" +");

            if (ppmValues.length % 3 != 0)
                throw new InvalidPPMFileException("The number of your values is not divideable by 3");

            else {
                canal1 = new ArrayList<>();
                canal2 = new ArrayList<>();
                canal3 = new ArrayList<>();
                errorcanal = new ArrayList<>();
                for (int i = 0; i < ppmValues.length; i++) {
                    switch (i % 3) {
                        case 0:
                            canal1.add(Double.parseDouble(ppmValues[i]));
                            break;
                        case 1:
                            canal2.add(Double.parseDouble(ppmValues[i]));
                            break;
                        case 2:
                            canal3.add(Double.parseDouble(ppmValues[i]));
                            break;
                        default:
                            errorcanal.add(Double.parseDouble(ppmValues[i]));
                            break;
                    }
                }
                if (errorcanal.size() >= 1)
                    throw new InvalidPPMFileException("Something went wrong with the channel-adding");
                else {
                    picture = new Picture(sizeX, sizeY);
                    picture.setChannel1(new GMatrix(sizeY, sizeX, toDoubleArrayPrimitive(canal1)));
                    picture.setChannel2(new GMatrix(sizeY, sizeX, toDoubleArrayPrimitive(canal2)));
                    picture.setChannel3(new GMatrix(sizeY, sizeX, toDoubleArrayPrimitive(canal3)));
                }

                if (picture.getChannel1().getNumCol() % 8 != 0 || picture.getChannel1().getNumRow() % 8 != 0) {
                    picture = createPictureWithStride(picture, 8);
                }
                return picture;
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();

        }
        return null;
    }

    private double[] toDoubleArrayPrimitive(ArrayList<Double> doubleArrayList) {
        double[] back = new double[doubleArrayList.size()];

        for (int i = 0; i < doubleArrayList.size(); i++) {
            back[i] = doubleArrayList.get(i).doubleValue();
        }

        return back;
    }

    private List<String> killCommentsInPPMHeader(List<String> stringList) {
        List<String> back = new ArrayList<>();
        for (String s : stringList) {
            if (!(s.contains("#") || s.matches("[pP][1-6]") || s.equals("")))
                back.add(s);

        }
        return back;
    }


    /**
     * creates a picture with fixed stride and correctly places the given original image inside of it.
     *
     * @param originalIMG
     * @param stride
     * @return strided image with correctly colored out-of-bounds-pixels
     */
    public Picture createPictureWithStride(Picture originalIMG, int stride) {
        int rows = originalIMG.getChannel1().getNumRow(), columns = originalIMG.getChannel1().getNumCol();

        if (rows % stride != 0 || columns % stride != 0) {
            int numColumns = columns % stride != 0 ? (columns / stride) + 1 : columns / stride;


            int numRows = rows % stride != 0 ? (rows / stride) + 1 : rows / stride;

            Picture strideIMG = new Picture();
            strideIMG.setChannel1(new GMatrix(numRows * stride, numColumns * stride));

            strideIMG.setChannel1(fillChannelWithLastValue(originalIMG.getChannel1(), (GMatrix) strideIMG.getChannel1().clone()));
            strideIMG.setChannel2(fillChannelWithLastValue(originalIMG.getChannel2(), (GMatrix) strideIMG.getChannel1().clone()));
            strideIMG.setChannel3(fillChannelWithLastValue(originalIMG.getChannel3(), (GMatrix) strideIMG.getChannel1().clone()));


            return strideIMG;
        } else
            return originalIMG;
    }

    private GMatrix fillChannelWithLastValue(GMatrix originalIMGChannel, GMatrix strideIMGChannel) {
        for (int i = 0; i < strideIMGChannel.getNumCol(); i++) {
            for (int j = 0; j < strideIMGChannel.getNumRow(); j++) {
                if (i >= originalIMGChannel.getNumCol() && j <= originalIMGChannel.getNumRow() - 1)
                    strideIMGChannel.setElement(j, i, originalIMGChannel.getElement(j, originalIMGChannel.getNumCol() - 1));
                else if (j >= originalIMGChannel.getNumRow())
                    strideIMGChannel.setElement(j, i, strideIMGChannel.getElement(originalIMGChannel.getNumRow() - 1, i));
                else
                    strideIMGChannel.setElement(j, i, originalIMGChannel.getElement(j, i));

            }
        }
        return strideIMGChannel;
    }
    //endregion
}
