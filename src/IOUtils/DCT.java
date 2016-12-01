package IOUtils;

import javax.vecmath.GMatrix;

/**
 * Created by matth on 13.12.2015.
 */
public class DCT {

    private final int N = 8;
    private final int NUMROWS = N;
    private final int NUMCOLS = N;

    private GMatrix A = null;
    private GMatrix A_T = null;

    public DCT() {

        A = initMatrix();
        A_T = (GMatrix) A.clone();
        A_T.transpose();
    }

    ;

    /**
     * Direkte DCT
     *
     * @param inputChannel
     * @return
     */
    public GMatrix directDiscreteCosineTransform(GMatrix inputChannel) {

        double c_Coefficient = 0.0;
        GMatrix outputChannel = new GMatrix(NUMROWS, NUMCOLS);

        double sumComponents, currentComponent, cosX, cosY, C_i, C_j;

        for (int i = 0; i < NUMROWS; i++) {

            C_i = (i == 0) ? 1.0 / Math.sqrt(2.0) : 1.0;

            for (int j = 0; j < NUMCOLS; j++) {

                C_j = (j == 0) ? 1.0 / Math.sqrt(2.0) : 1.0;

                c_Coefficient = 2.0 / N * C_i * C_j;

                sumComponents = 0.0;
                currentComponent = 0.0;
                cosX = 0.0;
                cosY = 0.0;

                for (int x = 0; x < NUMROWS; x++) {
                    for (int y = 0; y < NUMCOLS; y++) {

                        currentComponent = inputChannel.getElement(x, y);

                        cosX = Math.cos(((2 * x + 1) * i * Math.PI) / (2 * N));
                        cosY = Math.cos(((2 * y + 1) * j * Math.PI) / (2 * N));

                        sumComponents = sumComponents + currentComponent * cosX * cosY;
                    }
                }
                outputChannel.setElement(i, j, c_Coefficient * sumComponents);
            }
        }
        return outputChannel;
    }

    /**
     * Initialisierung der Koeffizientenmatrix
     *
     * @return
     */
    private GMatrix initMatrix() {

        GMatrix outputMatrix = new GMatrix(NUMROWS, NUMCOLS);

        double component = 0.0;
        double C_0 = 0.0;
        double argCos = 0.0;

        for (int k = 0; k < NUMROWS; k++) {

            C_0 = (k == 0) ? 1.0 / Math.sqrt(2.0) : 1.0;

            for (int n = 0; n < NUMCOLS; n++) {

                component = C_0 * Math.sqrt(2.0 / N) * Math.cos((2 * n + 1) * (k * Math.PI / (2.0 * N)));

                outputMatrix.setElement(k, n, component);
            }
        }
        return outputMatrix;
    }


    /**
     * separate DCT
     *
     * @param inputChannel
     * @return
     */
    public GMatrix separateDiscreteCosineTransform(GMatrix inputChannel) {

        inputChannel.mul(inputChannel,A_T);
        inputChannel.mul(A,inputChannel);

        return inputChannel;
    }


    /**
     * 2D-Arai-Kosinustransformation
     *
     * @param input8Block
     * @return
     */
    public GMatrix processArai(GMatrix input8Block) {

        GMatrix output8Block = arai_1D(input8Block);
        output8Block.transpose();

        output8Block = arai_1D(output8Block);
        output8Block.transpose();

        return output8Block;
    }

    /**
     * 1D-Kosinustransformation
     *
     * @param input8Block
     * @return
     */
    public GMatrix arai_1D(GMatrix input8Block) {

        double a_1 = getC((short) 4);
        double a_2 = getC((short) 2) - getC((short) 6);
        double a_3 = a_1;
        double a_4 = getC((short) 2) + getC((short) 6);
        double a_5 = getC((short) 6);

        GMatrix outputChannel = new GMatrix(NUMROWS, NUMCOLS);

        for (int i = 0; i < NUMROWS; i++) {

            double[] step1 = new double[8];
            double[] step2 = new double[8];
            double[] step3 = new double[8];
            double[] step4 = new double[8];
            double[] step5 = new double[8];
            double[] step6 = new double[8];

            step1[0] = input8Block.getElement(i, 0) + input8Block.getElement(i, 7);
            step1[1] = input8Block.getElement(i, 1) + input8Block.getElement(i, 6);
            step1[2] = input8Block.getElement(i, 2) + input8Block.getElement(i, 5);
            step1[3] = input8Block.getElement(i, 3) + input8Block.getElement(i, 4);
            step1[4] = input8Block.getElement(i, 3) - input8Block.getElement(i, 4);
            step1[5] = input8Block.getElement(i, 2) - input8Block.getElement(i, 5);
            step1[6] = input8Block.getElement(i, 1) - input8Block.getElement(i, 6);
            step1[7] = input8Block.getElement(i, 0) - input8Block.getElement(i, 7);

            step2[0] = step1[0] + step1[3];
            step2[1] = step1[1] + step1[2];
            step2[2] = step1[1] - step1[2];
            step2[3] = step1[0] - step1[3];
            step2[4] = -step1[4] - step1[5];
            step2[5] = step1[5] + step1[6];
            step2[6] = step1[6] + step1[7];
            step2[7] = step1[7];

            step3[0] = step2[0] + step2[1];
            step3[1] = step2[0] - step2[1];
            step3[2] = step2[2] + step2[3];
            step3[3] = step2[3];
            step3[4] = step2[4];
            step3[5] = step2[5];
            step3[6] = step2[6];
            step3[7] = step2[7];

            step4[0] = step3[0];
            step4[1] = step3[1];
            step4[2] = step3[2] * a_1;
            step4[3] = step3[3];
            step4[4] = -(step3[4] * a_2) - (step3[4] + step3[6]) * a_5;
            step4[5] = step3[5] * a_3;
            step4[6] = step3[6] * a_4 - (step3[4] + step3[6]) * a_5;
            step4[7] = step3[7];

            step5[0] = step4[0];
            step5[1] = step4[1];
            step5[2] = step4[2] + step4[3];
            step5[3] = -step4[2] + step4[3];
            step5[4] = step4[4];
            step5[5] = step4[5] + step4[7];
            step5[6] = step4[6];
            step5[7] = -step4[5] + step4[7];

            step6[0] = step5[0];
            step6[1] = step5[1];
            step6[2] = step5[2];
            step6[3] = step5[3];
            step6[4] = step5[4] + step5[7];
            step6[5] = step5[5] + step5[6];
            step6[6] = step5[5] - step5[6];
            step6[7] = -step5[4] + step5[7];

            step6[0] *= getS((short) 0);
            step6[1] *= getS((short) 4);
            step6[2] *= getS((short) 2);
            step6[3] *= getS((short) 6);
            step6[4] *= getS((short) 5);
            step6[5] *= getS((short) 1);
            step6[6] *= getS((short) 7);
            step6[7] *= getS((short) 3);


            outputChannel.setElement(i, 0, step6[0]);
            outputChannel.setElement(i, 4, step6[1]);
            outputChannel.setElement(i, 2, step6[2]);
            outputChannel.setElement(i, 6, step6[3]);
            outputChannel.setElement(i, 5, step6[4]);
            outputChannel.setElement(i, 1, step6[5]);
            outputChannel.setElement(i, 7, step6[6]);
            outputChannel.setElement(i, 3, step6[7]);
        }

        return outputChannel;
    }

    /**
     * Hilfsmethode für C-Koeffizienten Arai
     *
     * @param c
     * @return
     */
    private double getC(short c) {

        return Math.cos(c * Math.PI / 16.0);
    }

    /**
     * Hilfsmethode für S-Koeffizienten Arai
     *
     * @param k
     * @return
     */
    private double getS(short k) {

        return k == 0 ? 1.0 / (2.0 * Math.sqrt(2.0)) : 1.0 / (4.0 * getC(k));
    }

    /**
     * Invertierte DCT
     *
     * @param inputChannel
     * @return
     */
    public GMatrix invertedDiscreteCosineTransform(GMatrix inputChannel) {

        double currentYComponent = 0.0;
        double sumComponents = 0.0;
        double c_Component = 0.0;
        GMatrix outputChannel = new GMatrix(NUMROWS, NUMCOLS);

        double C_i, C_j;

        double cosX = 0.0;
        double cosY = 0.0;

        for (int x = 0; x < NUMROWS; x++) {
            for (int y = 0; y < NUMCOLS; y++) {

                sumComponents = 0.0;

                for (int i = 0; i < N; i++) {

                    C_i = (i == 0) ? 1.0 / Math.sqrt(2.0) : 1.0;

                    for (int j = 0; j < N; j++) {

                        currentYComponent = inputChannel.getElement(i, j);

                        C_j = (j == 0) ? 1.0 / Math.sqrt(2.0) : 1.0;

                        c_Component = (2.0 / N) * C_i * C_j;

                        cosX = Math.cos(((2.0 * x + 1.0) * i * Math.PI) / (2.0 * N));
                        cosY = Math.cos(((2.0 * y + 1.0) * j * Math.PI) / (2.0 * N));

                        sumComponents = sumComponents + c_Component * currentYComponent * cosX * cosY;
                    }
                }

                outputChannel.setElement(x, y, sumComponents);
            }
        }
        return outputChannel;
    }
}
