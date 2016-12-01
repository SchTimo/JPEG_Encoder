package IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;


/**
 * Created by martin on 01/11/15.
 * <p>
 * Doku für JPEG-Spezifikation:
 * http://vip.sugovica.hu/Sardi/kepnezo/JPEG%20File%20Layout%20and%20Format.htm
 */

public class Bitstream {

    //private final static String path = "/Users/maddin/Desktop/JPEG_Encoder_Test";

    private String path;

    private FileOutputStream bitstream = null;
    private byte currentBit = 7;
    private int byteToAdd = 0x0000;


    /*public static void main (String[] args)
    {
        Bitstream s = new Bitstream("C:\\Users\\matth\\Desktop\\Test1.jpg");

        for(int i = 0; i < 9; i++) {
            s.addBit(true);
        }

        s.flush();
    }*/

    public Bitstream(String path) {
        this.path = path;
    }

    /**
     * Ermöglicht das schreiben einzelner Segemente der JPG-Datei
     *
     * @param width
     * @param height
     * @param subSampling
     * @param codeToValueDic
     */
    public void writeJPGFile(short width, short height, byte subSampling, HashMap<List<Boolean>, Integer> codeToValueDic) {

        ArrayList<Byte> result = new ArrayList();

        //Start of Image
        byte SOI1 = (byte) 0xff;
        byte SOI2 = (byte) 0xd8;

        result.add(SOI1);
        result.add(SOI2);

        //Hinzufügen der Segmente APP0 und SOF0
        result.addAll(Arrays.asList(writeAPP0()));
        result.addAll(Arrays.asList(writeSOF0(width, height, subSampling)));
        result.addAll(Arrays.asList(writeDHT(codeToValueDic)));

        //End of Image
        byte EOI1 = (byte) 0xff;
        byte EOI2 = (byte) 0xd9;

        result.add(EOI1);
        result.add(EOI2);

        writeStreamData(toBytePrimitiveArray(result), result.size() * 8, 0, path);
    }

    /**
     * Methode zum Hinzufügen eines einzelnen Bits an einen Filestream
     * Ist kein Filestream geöffnet, so öffnet diese Methode diesen
     *
     * @param bit Das zu schreibende Bit - 1 oder 0
     */
    public void addBit(boolean bit) {

        if (this.bitstream == null) {
            try {
                this.bitstream = new FileOutputStream(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.byteToAdd |= (bit ? 1 : 0) << currentBit;

        currentBit--;

        if (currentBit < 0) {
            try {
                this.bitstream.write(byteToAdd);
            } catch (Exception e) {
                e.printStackTrace();
            }

            byteToAdd = 0x0000;
            currentBit = 7;
        }
    }


    /**
     * Füllt letztes Byte ab dem letzten Bit mit Nullern auf
     * und schließt den Stream
     */
    public void flush() {

        if (currentBit == 7) {
            try {
                this.bitstream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        while (currentBit > -1) {
            this.byteToAdd |= 0 << currentBit;
            currentBit--;
        }

        try {
            this.bitstream.write(byteToAdd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        currentBit = 7;
        this.byteToAdd = 0x0000;

        try {
            this.bitstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.bitstream = null;
        System.gc();
    }


    /**
     * Liest variable Menge an Bit, mit gewünschten Offset aus gegebenem Pfad aus
     *
     * @param bitAnzahl
     * @param offset
     * @param path
     * @return
     */

    public byte[] readStreamData(int bitAnzahl, int offset, String path) {
        int size = bitAnzahl / 8;
        int remainder = bitAnzahl % 8;
        byte lastByte;

        if (remainder > 0) {
            size++;
        }

        byte[] result = new byte[size];

        try (FileInputStream fis = new FileInputStream(path)) {
            fis.read(result, offset, size);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (remainder > 0) {
            // -128 ist binär 1000 0000 wird von links mit Einsern aufgefüllt.
            lastByte = (byte) (-128 >> (remainder - 1));
            result[size - 1] &= lastByte;
        }

        return result;
    }


    /**
     * Schreibt variable Menge an Bit, mit gewünschten Offset an gegebenem Pfad
     *
     * @param data
     * @param bitAnzahl
     * @param offset
     * @param path
     */

    public void writeStreamData(byte[] data, int bitAnzahl, int offset, String path) {
        int size = bitAnzahl / 8;
        int remainder = bitAnzahl % 8;
        byte lastByte;

        if (remainder > 0) {
            // 127 ist binär 0111 1111 wird von links mit Nullern aufgefüllt. (Vorbereitung Huffman)
            lastByte = (byte) (127 >> (remainder - 1));
            data[size - 1] |= lastByte;
        }

        try (FileOutputStream fis = new FileOutputStream(path)) {
            fis.write(data, offset, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Implementierung für Segment APP0
     *
     * @return
     */

    private Byte[] writeAPP0() {
        byte length = 0x10;

        Byte[] segmentAPP0 = new Byte[length + 2];

        //byte APP0-1
        segmentAPP0[0] = (byte) 0xff;

        //byte APP0-2
        segmentAPP0[1] = (byte) 0xe0;

        //Java Big Endian
        //byte highByte
        segmentAPP0[2] = (byte) (0x00);

        //byte lowByte
        segmentAPP0[3] = (byte) length;

        //byte J
        segmentAPP0[4] = (byte) 0x4a;

        //byte F
        segmentAPP0[5] = (byte) 0x46;

        //byte I
        segmentAPP0[6] = (byte) 0x49;

        //byte F
        segmentAPP0[7] = (byte) 0x46;

        //byte Nullterminal
        segmentAPP0[8] = (byte) 0x00;

        //byte MAJOR_REVISION
        segmentAPP0[9] = 0x01;

        //byte MINOR_REVISION
        segmentAPP0[10] = 0x01;

        //byte SIZE_PIXEL
        segmentAPP0[11] = 0x00;

        //byte X_DENSITY_HIGH
        segmentAPP0[12] = 0x00;

        //byte X_DENSITY_LOW
        segmentAPP0[13] = 0x48;

        //byte Y_DENSITY_HIGH
        segmentAPP0[14] = 0x00;

        //byte Y_DENSITY_LOW
        segmentAPP0[15] = 0x48;

        //byte SIZE_PREVIEW_X
        segmentAPP0[16] = 0x00;

        //byte SIZE_PREVIEW_Y
        segmentAPP0[17] = 0x00;

        //byte PREVIEW
        //segmentAPP0[18] = (byte)0x00;

        return segmentAPP0;
    }


    /**
     * Implementierung für Segment SOF0
     *
     * @param width
     * @param height
     * @param subSampling
     * @return
     */

    private Byte[] writeSOF0(short width, short height, byte subSampling) {

        Byte[] segmentSOF0 = new Byte[19];

        //byte SOF0-1
        segmentSOF0[0] = (byte) 0xff;

        //byte SOF0-2
        segmentSOF0[1] = (byte) 0xc0;

        //Java Big Endian
        //byte highByte
        segmentSOF0[2] = (byte) 0x00;

        //byte lowByte
        segmentSOF0[3] = (byte) 0x11;

        //byte ACCURACY -- 12 und 16-Bit/Sample möglich, aber meist nicht unterstützt
        segmentSOF0[4] = (byte) 0x08;

        //byte SIZE_Y_HIGH
        segmentSOF0[5] = (byte) 0x00;

        //byte SIZE_Y_LOW
        segmentSOF0[6] = (byte) 0x10;

        //byte SIZE_X_HIGH
        segmentSOF0[7] = (byte) 0x00;

        //byte SIZE_X_LOW
        segmentSOF0[8] = (byte) 0x10;

        //byte COMPONENTS
        segmentSOF0[9] = (byte) 0x03;


        //--------COMPONENT Y ----------------------------------
        //byte COMPONENT_ID_Y
        segmentSOF0[10] = (byte) 0x01;

        //byte COMPONENT_SUBSAMPLING_Y
        segmentSOF0[11] = 0x22;

        //byte COMPONENT_TABLE_Y
        segmentSOF0[12] = (byte) 0x00;


        //--------COMPONENT Cb ----------------------------------
        //byte COMPONENT_ID_Cb
        segmentSOF0[13] = (byte) 0x02;

        //byte COMPONENT_SUBSAMPLING_Cb
        segmentSOF0[14] = subSampling;

        //byte COMPONENT_TABLE_Cb
        segmentSOF0[15] = (byte) 0x00;


        //--------COMPONENT Cr ----------------------------------
        //byte COMPONENT_ID_Cr
        segmentSOF0[16] = (byte) 0x03;

        //byte COMPONENT_SUBSAMPLING_Cr
        segmentSOF0[17] = subSampling;

        //byte COMPONENT_TABLE_Cr
        segmentSOF0[18] = (byte) 0x00;

        return segmentSOF0;
    }


    private Byte[] writeDHT(HashMap<List<Boolean>, Integer> codeToValueDic) {

        List<Byte> segmentDHT = new ArrayList<Byte>();

        //
        //Kenner
        //
        segmentDHT.add((byte) 0xff);
        segmentDHT.add((byte) 0xc4);

        //
        //Platzhalter Länge
        //
        short length = (short) (2 + 1 + 16 + codeToValueDic.size());

        byte lengthHigh = (byte) ((length >> 8) & 0xff);
        byte lengthLow = (byte) (length & 0xff);

        segmentDHT.add(lengthHigh);
        segmentDHT.add(lengthLow);

        //
        //Information Byte
        //
        segmentDHT.add((byte) 0x00);

        //
        //Anzahl von Symbolen pro Baumtiefe (= Codelänge)
        //
        KeyValuePair[] anzahlen = new KeyValuePair[16];

        for (Map.Entry<List<Boolean>, Integer> current : codeToValueDic.entrySet()) {
            if (anzahlen[current.getKey().size() - 1] == null) {
                anzahlen[current.getKey().size() - 1] = new KeyValuePair();
            }
            anzahlen[current.getKey().size() - 1].anzahl++;
            anzahlen[current.getKey().size() - 1].value.add(current.getValue());
        }


        //
        //16 Byte für die Anzahl der Symbole pro Ebene im Baum
        //
        for (KeyValuePair current : anzahlen) {
            if (current != null) {
                segmentDHT.add(current.anzahl);
            } else {
                segmentDHT.add((byte) 0);
            }
        }

        //
        //Tabelle mit n Symbolen geordnet nach Codelänge (nicht nach Code selbst!)
        //
        for (KeyValuePair current : anzahlen) {
            if (current != null) {
                for (int i = 0; i < current.value.size(); i++) {
                    segmentDHT.add(current.value.get(i).byteValue());
                }
            }
        }

        return (Byte[]) segmentDHT.toArray(new Byte[0]);
    }

    /**
     * Konvertierung einer Byte ArrayList zu Array des primitiven Datentyp byte
     *
     * @param list
     * @return
     */

    private byte[] toBytePrimitiveArray(ArrayList<Byte> list) {
        byte[] result = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i).byteValue();
        }
        return result;
    }
}