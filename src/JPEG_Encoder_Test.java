import ColorModels.Pixel;
import Exceptions.InvalidPPMFileException;
import Exceptions.NotP3Exception;
import Exceptions.WrongColorMaxValueException;
import HuffmannTree.HuffmannTreeNode;
import IOUtils.Bitstream;
import IOUtils.DCT;

import javax.vecmath.GMatrix;
import java.util.function.BiFunction;

/**
 * Created by timo on 12.10.15.
 */
public class JPEG_Encoder_Test {

    //private static final String path = "C:\\Users\\matth\\Desktop\\TEST.jpeg";
    //private static final String path = "/Users/maddin/Desktop/JPEG_Encoder_Test.jpeg";
    private static final String path = "/Users/timo/Desktop/JPEG_Encoder_Test.jpeg";
    private static final String PPMpath = "/Users/timo/Desktop/JPEG_Encoder_Test";



    private static final int BLOCK_WIDTH = 8;
    private static final int SUBSAMPLE_MODE_LOCALLY_AVERAGE = 2;

    /**
     * MAIN
     *
     * @param args
     */
    public static void main(String[] args) {

        PictureManager pictureManager = PictureManager.getInstance();

//        Picture pictureToEncode = null;
//
//        try {
//            pictureToEncode = pictureManager.createPictureOutOfPPM(path);
//        } catch (NotP3Exception e) {
//            e.printStackTrace();
//        } catch (WrongColorMaxValueException e) {
//            e.printStackTrace();
//        } catch (InvalidPPMFileException e) {
//            e.printStackTrace();
//        }
//
//        pictureToEncode = pictureManager.convertToYCbCrPicture(pictureToEncode);
//
//        pictureToEncode = pictureManager.subsamplePictureChannelsCbCr(pictureToEncode,SUBSAMPLE_MODE_LOCALLY_AVERAGE,BLOCK_WIDTH);
//
//        pictureToEncode = pictureManager.encodeBlocks(pictureToEncode);



        createsRandomRGBTestPPM(PPMpath,8,8);
        try {
            Picture p = PictureManager.getInstance().createPictureOutOfPPM(PPMpath+".ppm");
            System.out.println(p.getChannel1().toString());
            System.out.println();
            System.out.println(p.getChannel2().toString());
            System.out.println();
            System.out.println(p.getChannel3().toString());

        } catch (NotP3Exception e) {
            e.printStackTrace();
        } catch (WrongColorMaxValueException e) {
            e.printStackTrace();
        } catch (InvalidPPMFileException e){
            e.printStackTrace();
        }


































            // DCT dct = new DCT();

    /*
        GMatrix testMatrix = new GMatrix(8, 8);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {

                testMatrix.setElement(i, j, (i + j * 8) % 256);
            }
        }
        System.out.println(testMatrix.toString());

        System.out.println("\n\nDISCRETE:\n");
        GMatrix resultDiscrete = dct.directDiscreteCosineTransform(testMatrix);
        System.out.println(resultDiscrete.toString());

        System.out.println("\n\nSEPARATED:\n");
        GMatrix resultSeparated = dct.separateDiscreteCosineTransform(testMatrix);
        System.out.println(resultSeparated.toString());

        testMatrix = new GMatrix(8, 8);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                testMatrix.setElement(i, j, (i + j * 8) % 256);
            }
        }

        System.out.println("\n\nARAI:\n");
        GMatrix resultArai = dct.processBlock(testMatrix);
        System.out.println(resultArai.toString());



        System.out.println("\n\nINVDISC:\n");
        GMatrix invDisc = dct.invertedDiscreteCosineTransform(resultDiscrete);
        System.out.println(invDisc.toString());

        System.out.println("\n\nINVSEP:\n");
        GMatrix invSep = dct.invertedDiscreteCosineTransform(resultSeparated);
        System.out.println(invSep.toString());

        System.out.println("\n\nINVARAI:\n");
        GMatrix invArai = dct.invertedDiscreteCosineTransform(resultArai);
        System.out.println(invArai.toString());
*/

        /**
         * Huffmann-Code JPEG_Encoder_Test
         */

        //writeTestJPEG();

//
//        //Testmatrix anlegen
//        System.out.println("===TEST START========================\n\n");
//        int n = 256;
//
//        GMatrix testMatrix256 = getFreshTestMatrix(n);
//        stopWatchTestForDCTVariant(DCTMethod.DISCRETE, testMatrix256);
//
//        testMatrix256 = getFreshTestMatrix(n);
//        stopWatchTestForDCTVariant(DCTMethod.SEPARATE,testMatrix256);
//
//        testMatrix256 = getFreshTestMatrix(n);
//        stopWatchTestForDCTVariant(DCTMethod.ARAI,testMatrix256);
    }



    /**
     * Kenner für die angewandte Methode für die Kosinus-Transformation
     */
    private enum DCTMethod {DISCRETE,SEPARATE,ARAI}

    /**
     * Testmethode zur wiederholten Messung über 15 Sekunden
     *
     * @param enumVerf
     * @param singleColorChannel
     */
    private static void stopWatchTestForDCTVariant(DCTMethod enumVerf, GMatrix singleColorChannel) {

        BiFunction<DCT,GMatrix,GMatrix> dctMethodLambda = null;

        switch(enumVerf) {

            case DISCRETE:
                dctMethodLambda = (DCT dct,GMatrix m) -> dct.directDiscreteCosineTransform(m);
                break;

            case SEPARATE:
                dctMethodLambda = (DCT dct,GMatrix m) -> dct.separateDiscreteCosineTransform(m);
                break;

            case ARAI:
                dctMethodLambda = (DCT dct,GMatrix m) -> dct.processArai(m);
                break;

            default:
                break;
        }


        long timePassed = 0,timeDelta = 0,start = 0,end = 0;
        int timesRun = 0;
        double sumAverageCallTimesPerImage = 0;
        final long MINTIME = 15000; //15 Sekunden

        while (timePassed < MINTIME) {

            start = System.currentTimeMillis();

            //Durchschnittliche Zeiten aufsummieren
            sumAverageCallTimesPerImage += applyDCTtoSingleColorChannel(singleColorChannel, dctMethodLambda);

            end = System.currentTimeMillis();


            timeDelta = end - start;

            timePassed += timeDelta;
            timesRun++;
        }

        System.out.println("Durchschnittliche Dauer für DCTMethod " + enumVerf + " in Mikrosekunden: " + String.format("%.2f",sumAverageCallTimesPerImage / timesRun * 1000));
        System.out.println("Anzahl an " + singleColorChannel.getNumRow() + "x" + singleColorChannel.getNumCol() + " Bildern im JPEG_Encoder_Test verarbeitet: " + timesRun);
        System.out.println("Anzahl Aufrufe von " + enumVerf.toString() + " (= Gesamtzahl verarbeiteter 8-er-Blöcke): " + timesRun * (singleColorChannel.getNumRow()) / 8 * (singleColorChannel.getNumCol()) / 8 + "\n");
    }

    /**
     * Testmethode einzelner Farbkanal eines Bildes
     *
     * @param colorChannelToEncode Farbkanal, für den die Koeffizienten zu bestimmen sind
     * @param dctMethodOn8x8Block DCT-Verfahren, das benutzt werden soll
     * @return
     */
    private static double applyDCTtoSingleColorChannel(GMatrix colorChannelToEncode, BiFunction<DCT, GMatrix, GMatrix> dctMethodOn8x8Block) {

        final int N = 8;

        DCT dct = new DCT();

        long start = 0,end = 0;
        double sumCallTime = 0;
        int numCalls = 0;

        GMatrix temp = new GMatrix(N,N);

        for (int row = 0; row < colorChannelToEncode.getNumRow(); row += N) {
            for (int col = 0; col < colorChannelToEncode.getNumCol(); col += N) {

                colorChannelToEncode.copySubMatrix(row,col,N,N,0,0,temp);


                start = System.currentTimeMillis();

                //Aufruf des gewählten Verfahrens
                temp = dctMethodOn8x8Block.apply(dct,temp);

                end = System.currentTimeMillis();


                temp.copySubMatrix(0, 0, N, N, row, col, colorChannelToEncode);


                sumCallTime += (end - start);
                numCalls++;
            }
        }

        return sumCallTime / numCalls;
    }

    /**
     * Stellt quadratische Testmatrix mit Spaltenbreite N bereit
     *
     * @param n
     * @return
     */
    private static GMatrix getFreshTestMatrix(int n) {
        GMatrix pic = new GMatrix(n,n);

        for(int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                pic.setElement(i,j,(i+j*8) % 256);
            }
        }
        return pic;
    }


    private static void writeTestJPEG() {

        /**
         * Testwerte
         */
        //int[] values = {1,2,2,3,3,3,4,4,4,4,5,5,5,5,5,6,6,6,6,6,6,7,7,7,7,7,7,7,8,8,8,8,8,8,8,8,9,9,9,9,9,9,9,9,9};

        int[] values = {1,1,1,1,2,2,2,2,3,3,3,3,3,3,4,4,4,4,4,4,5,5,5,5,5,5,5,6,6,6,6,6,6,6,6,6};


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
        int maxDepth = 4;

        HuffmannTreeNode cutTree = h.guaranteeMaxDepthOfLevel(maxDepth, balancedTree);

        System.out.println("\n\nPrinting Positions after Cut:");
        cutTree.printTree();


        /**
         * Schauen, dass kein Code nur aus Einsern besteht
         */
        h.removeCodeConsistingOfOnlyNumberOnes(cutTree);

        System.out.println("\n\nPrinting Positions after Removing OnesOnlyCode:");
        cutTree.printTree();




        Bitstream b = new Bitstream(JPEG_Encoder_Test.path);
        b.writeJPGFile((short)20,(short)20,(byte)0x22,HuffmannTreeNode.getCodeAsBoolsToValDictionary());
    }

    /**
     * PictureManager
     */
    private static PictureManager mgr;

    /**
     * Creates a Random RGB ppmFile with a given Size x and y and names it like the given name
     *
     * @param filename The name the file should have; could also be a path like '../Users/ProjectX/filename
     * @param sizeX    the given horizontal size
     * @param sizeY    the given vertical size
     */
    public static void createsRandomRGBTestPPM(String filename, int sizeX, int sizeY) {

        PictureManager.getInstance().writePPMRGB(PictureManager.getInstance().createRandomRGBPicture(sizeX, sizeY), filename);
    }
    /**
     * Creates a RGB ppmFile out of a given Structure of Values
     *
     * @param filename    The name the file should have; could also be a path like '../Users/ProjectX/filename
     * @param givenValues The given Structure of Values
     */
    public static void createsRGBTestPPM(String filename, Pixel[][] givenValues) {

        Picture pic = new Picture();
        pic.setAllPixelsInPictureArray(givenValues);

        PictureManager.getInstance().writePPMRGB(pic, filename);
    }
}
