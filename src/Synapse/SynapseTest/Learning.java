
package Synapse.SynapseTest;

import Synapse.Synapse;

import java.util.Random;

/**
 *
 * @author mchen
 */
public class Learning implements Synapse<double[][], double[][]> {

    double[][] weight;

    public Learning(double[][] weight) {
        this.weight = weight;
    }

    @Override
    public double[][] output(double[][] i) {
        double[][] testValue = crossTransA(i, weight, true);
        nonLin(testValue, false);
        return testValue;
    }

    @Override
    public void train(double[][] in, double[][] expectedResult) {
        double[][] simulatedResult = output(in); // simulate

        int nRow = expectedResult.length;
        int nCol = expectedResult[0].length;
        double result[][] = simulatedResult.clone();
        for (int i = 0; i < result.length; i++) {
            result[i] = simulatedResult[i].clone();
        }
        double sigmoidslope[][] = simulatedResult.clone();
        for (int i = 0; i < sigmoidslope.length; i++) {
            sigmoidslope[i] = simulatedResult[i].clone();
        }
        nonLin(sigmoidslope, true);
        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                result[j][i] = expectedResult[i][j] - simulatedResult[j][i];
                result[j][i] *= sigmoidslope[j][i];
            }
        }
        
        double[][] w = crossTransA(in, result, false);

        for (int i = 0; i < weight.length; i++) {
            for (int j = 0; j < weight[0].length; j++) {
                weight[i][j] += w[i][j];
            }
        }
    }

    private static void nonLin(double[][] matrix, boolean deriv) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (deriv) {
                    matrix[i][j] = matrix[i][j]*(1-matrix[i][j]); // newton ftw
                } else {
                    matrix[i][j] = 1 / (1 + Math.exp(-matrix[i][j]));
                }
            }
        }
    }

    private static double[][] crossTransA(double[][] A, double[][] B, boolean transA) { // A transpose * B
        int nRow = A.length;
        int kLimit = A[0].length;
        if (transA) {
            int buffer = kLimit;
            kLimit = nRow;
            nRow = buffer;
        }
        int nCol = B[0].length;
        double[][] product = new double[nRow][nCol];
        
        for (int i = 0; i < nRow; i++) {
            for (int j = 0; j < nCol; j++) {
                double sum = 0;
                for (int k = 0; k < kLimit; k++) {
                    double e;
                    if (transA) {
                        e = A[k][i];
                    } else {
                        e = A[i][k];
                    }
                    sum += e*B[k][j];
                }
                product[i][j] = sum;
            }
        }
        
        return product;
    }
    
    public static void main(String[] args) {
        double inputs[][] = {
            {0, 0, 1, 1},
            {0, 1, 0, 1},
            {1, 1, 1, 1}
        };
        
        double expectedOutput[][] = {
            {0, 0, 1, 1}
        };

        int nRow = 3, nCol = 1;
        Random r = new Random(1);
        double[][] w = new double[nRow][nCol];
        for (double row[] : w) {
            for (int i = 0; i < nCol; i++) {
                row[i] = r.nextDouble();
            }
        }
        int iterations = 10000;
        Learning syn = new Learning(w);
        for (int i = 0; i < iterations; i++) {
            syn.train(inputs, expectedOutput);
        }
        double[][] afterTraining = syn.output(inputs);
        for (double row[] : afterTraining) {
            for (double d : row) {
                System.out.println(d+",");
            }
        }
    }
}
