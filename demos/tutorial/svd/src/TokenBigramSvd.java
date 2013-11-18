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

public class TokenBigramSvd {


    public static void main(String[] args) throws Exception {
        System.out.println("TokenBigramSVD");

        File textFile = new File(args[0]);
        MapSymbolTable symbolTable = new MapSymbolTable();
        TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        String charset = "ASCII";
        System.out.println("  Extracting Bigrams");
        System.out.println("    File=" + textFile.getCanonicalPath());
        System.out.println("    tokenizerFactory.getClass()=" + tokenizerFactory.getClass());
        System.out.println("    input charset=" + charset);
        double[][] values = extractBigrams(textFile,symbolTable,tokenizerFactory,charset);

        int maxFactors = 3;
        double featureInit = 0.1;
        double initialLearningRate = 0.001;
        int annealingRate = 100;
        double regularization = 0.00;
        double minImprovement = 0.0000;
        int minEpochs = 10;
        int maxEpochs = 200;
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
            = SvdMatrix.svd(values,
			    maxFactors,
			    featureInit,
			    initialLearningRate,
			    annealingRate,
			    regularization,
                            reporter,
			    minImprovement,
			    minEpochs,
			    maxEpochs);

        reportSvd(values,matrix,symbolTable);

    }




    public static void reportSvd(double[][] values,
                          SvdMatrix matrix,
                          MapSymbolTable symbolTable) {
        double[] singularValues = matrix.singularValues();
        double[][] leftSingularVectors = matrix.leftSingularVectors();
        double[][] rightSingularVectors = matrix.rightSingularVectors();
        for (int order = 0; order < singularValues.length; ++order) {
            System.out.println("\n\nORDER=" + order + " singular value=" + singularValues[order]);

            System.out.println("Extreme Left Values");
            extremeValues(leftSingularVectors,order,symbolTable);

            System.out.println("\nExtreme Right Values");
            extremeValues(rightSingularVectors,order,symbolTable);
        }



        ObjectToDoubleMap<String> topPairCounts = new ObjectToDoubleMap<String>();
        int numSymbols = symbolTable.numSymbols();
        for (int i = 0; i < numSymbols; ++i) {
            for (int j = 0; j < numSymbols; ++j) {
                if (values[i][j] != 0) {
                    topPairCounts.set(symbolTable.idToSymbol(i) + "," + symbolTable.idToSymbol(j),
                                      values[i][j]);
                }
            }
        }
        int numPairs = topPairCounts.size();
        System.out.println("#unique pairs=" + numPairs);
        List<String> pairsByCount = topPairCounts.keysOrderedByValueList();
        for (int i = 0; i < 25; ++i) {
            String pair = pairsByCount.get(i);
            System.out.println("     " + pair + " count=" + topPairCounts.getValue(pair));
        }



        System.out.println("\nRECONSTRUCTED TOP COUNTS");
        System.out.println("LeftToken,RightToken OriginalValue SvdValue");
        for (int i = 0; i < 25; ++i) {
            String pair = pairsByCount.get(i);
            String[] tokenPair = pair.split(",");
            String leftToken = tokenPair[0];
            String rightToken = tokenPair[1];
            int leftSymbol = symbolTable.symbolToID(leftToken);
            int rightSymbol = symbolTable.symbolToID(rightToken);
            double originalValue = topPairCounts.getValue(pair);
            double reconstructedValue = matrix.value(leftSymbol,rightSymbol);
            System.out.println(pair + "  " + originalValue + "  " + reconstructedValue);
        }



    }


    public static double[][] extractBigrams(File file, MapSymbolTable symbolTable,
                                     TokenizerFactory tokenizerFactory,
                                     String charset)
        throws Exception {

        char[] cs = Files.readCharsFromFile(file,charset);
        String[] tokens
            = tokenizerFactory.tokenizer(cs,0,cs.length).tokenize();
        System.out.println("    Number of tokens=" + tokens.length);

        int[] symbols = new int[tokens.length];
        for (int i = 0; i < tokens.length; ++i) {
            symbols[i] = Strings.allLetters(tokens[i].toCharArray())
                ? symbolTable.getOrAddSymbol(tokens[i])
                : -1;
        }

        int numSymbols = symbolTable.numSymbols();
        System.out.println("    Number of distinct tokens=" + numSymbols);
        System.out.println("    #Matrix entries=" + numSymbols * numSymbols);

        double[][] values = new double[numSymbols][numSymbols];
        for (int i = 0; i < numSymbols; ++i)
            Arrays.fill(values[i],0.0);

        for (int i = 1; i < symbols.length; ++i) {
            int left = symbols[i-1];
            int right = symbols[i];
            if (left >= 0 && right >= 0)
                values[symbols[i-1]][symbols[i]] += 1.0;
        }

        return values;
    }



    public static void extremeValues(double[][] values,
                              int order,
                              MapSymbolTable symbolTable) {
        ObjectToDoubleMap<String> topVals = new ObjectToDoubleMap<String>();
        for (int i = 0; i < values.length; ++i) {
            String token = symbolTable.idToSymbol(i);
            topVals.set(token,values[i][order]);
        }
        List<String> tokensByValue = topVals.keysOrderedByValueList();
        int size = tokensByValue.size();
        for (int i = 0; i < 10 && i < size; ++i) {
            String token = tokensByValue.get(i);
            double value = topVals.getValue(token);
            System.out.printf("     %6d %-15s % 5.3f\n",i,token,value);
        }
        System.out.println("...");
        for (int i = 10; --i >= 0; ) {
            String token = tokensByValue.get(size-i-1);
            double value = topVals.getValue(token);
            System.out.printf("     %6d %-15s % 5.3f\n",size-i-1,token,value);
        }
    }

}