import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;

import com.aliasi.matrix.SvdMatrix;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.symbol.MapSymbolTable;

import com.aliasi.util.Files;
import com.aliasi.util.ObjectToDoubleMap;
import com.aliasi.util.Strings;

import java.io.File;
import java.io.PrintWriter;
import java.io.OutputStreamWriter;

import java.util.Arrays;
import java.util.List;


public class PartialTokenBigramSvd {


    public static void main(String[] args) throws Exception {
        System.out.println("TokenBigramSVD");

        File textFile = new File(args[0]);
        MapSymbolTable symbolTable = new MapSymbolTable();
        TokenizerFactory tokenizerFactory 
            = IndoEuropeanTokenizerFactory.INSTANCE;
        String charset = "ASCII";
        System.out.println("  Extracting Bigrams");
        System.out.println("    File=" + textFile.getCanonicalPath());
        System.out.println("    tokenizerFactory.getClass()=" 
                           + tokenizerFactory.getClass());
        System.out.println("    input charset=" + charset);
        double[][] values 
            = TokenBigramSvd
            .extractBigrams(textFile,symbolTable,tokenizerFactory,charset);

        int[][] columnIds = columnIds(values);
        double[][] partialValues = partialValues(values);

        // 2, 1.0, 0.002, 100, 0.0, 0.0, 10, 10000 : 9900 rmse=3.9478464572
  
        int maxFactors = 3;
        double featureInit = 1.0;
        double initialLearningRate = 0.002;
        int annealingRate = 100;
        double regularization = 0.0;
        double minImprovement = 0.0000;
        int minEpochs = 10;
        int maxEpochs = 10000;
        PrintWriter verbosePrintWriter
            = new PrintWriter(new OutputStreamWriter(System.out,charset));
        Reporter reporter
            = Reporters.writer(verbosePrintWriter).setLevel(LogLevel.DEBUG);

        System.out.println("  Computing SVD");
        System.out.println("    maxFactors=" + maxFactors);
        System.out.println("    featureInit=" + featureInit);
        System.out.println("    initialLearningRate=" + initialLearningRate);
        System.out.println("    annealingRate=" + annealingRate);
        System.out.println("    regularization" + regularization);
        System.out.println("    minImprovement=" + minImprovement);
        System.out.println("    minEpochs=" + minEpochs);
        System.out.println("    maxEpochs=" + maxEpochs);
        System.out.println("    output charset=" + charset);
        SvdMatrix matrix
            = SvdMatrix.partialSvd(columnIds,
                                   partialValues,
                                   maxFactors,
                                   featureInit,
                                   initialLearningRate,
                                   annealingRate,
                                   regularization,
                                   reporter,
                                   minImprovement,
                                   minEpochs,
                                   maxEpochs);

        TokenBigramSvd.reportSvd(values,matrix,symbolTable);
    }

    static int[][] columnIds(double[][] values) {
        int[][] columnIds = new int[values.length][];
        for (int i = 0; i < values.length; ++i)
            columnIds[i] = columnIdsRow(values[i]);
        return columnIds;
    }

    static int[] columnIdsRow(double[] values) {
        int count = 0;
        for (int i = 0; i < values.length; ++i)
            if (values[i] != 0)
                ++count;
        int[] columnIdsRow = new int[count];
        
        count = 0;
        for (int i = 0; i < values.length; ++i)
            if (values[i] != 0)
                columnIdsRow[count++] = i;
        return columnIdsRow;
    }



    static double[][] partialValues(double[][] values) {
        double[][] partialValues = new double[values.length][];
        for (int i = 0; i < values.length; ++i)
            partialValues[i] = partialValuesRow(values[i]);
        return partialValues;
    }

    static double[] partialValuesRow(double[] values) {
        int count = 0;
        for (int i = 0; i < values.length; ++i)
            if (values[i] != 0)
                ++count;
        double[] partialValuesRow = new double[count];

        count = 0;
        for (int i = 0; i < values.length; ++i)
            if (values[i] != 0)
                partialValuesRow[count++] = values[i];
        return partialValuesRow;
    }



}


