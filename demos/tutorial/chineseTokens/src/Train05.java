import com.aliasi.classify.PrecisionRecallEvaluation;

import com.aliasi.lm.NGramProcessLM;

import com.aliasi.spell.CompiledSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Compilable;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;
import com.aliasi.util.Streams;
import com.aliasi.util.Strings;
import com.aliasi.util.Tuple;

import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Train05 {

    public static void main(String[] args) throws Exception {
        File zipFile = new File(args[0],"icwb2-data.zip");
        String corpusName = args[1];
        int maxNGram = Integer.valueOf(args[2]);
        double lambdaFactor = Double.valueOf(args[3]);
        int numChars = Integer.valueOf(args[4]);
        File modelDir = new File(args[5]);

        NGramProcessLM lm
            = new NGramProcessLM(maxNGram,numChars,lambdaFactor);
        WeightedEditDistance distance
            = CompiledSpellChecker.TOKENIZING;
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,distance,null);

        String trainingEntryName = corpusName + "_training.utf8";
        String testEntryName = corpusName + "_test_gold.utf8";
        String testEntryName2 = corpusName + "_testing_gold.utf8";

        FileInputStream fileIn = new FileInputStream(zipFile);
        ZipInputStream zipIn = new ZipInputStream(fileIn);
        ZipEntry entry = null;
        while ((entry = zipIn.getNextEntry()) != null) {
            String name = entry.getName();
            if ((!name.endsWith(trainingEntryName))
                && (!name.endsWith(testEntryName))
                && (!name.endsWith(testEntryName2))) continue;
            System.out.println("Reading Data from entry=" + name);
            InputStreamReader reader
                = new InputStreamReader(zipIn,Strings.UTF8);
            BufferedReader bufReader = new BufferedReader(reader);
            String refLine;
            while ((refLine = bufReader.readLine()) != null) {
                String trimmedLine = refLine.trim() + " ";
                String normalizedLine = trimmedLine.replaceAll("\\s+"," ");
                trainer.handle(normalizedLine);
            }
        }
        Streams.closeQuietly(zipIn);

        File modelFile
            = new File(modelDir,
                       "words-zh-" + corpusName + ".CompiledSpellChecker");
        System.out.println("Compiling Spell Checker to file=" + modelFile);
        FileOutputStream fileOut = null;
        BufferedOutputStream bufOut = null;
        ObjectOutputStream objOut = null;
        try {
            fileOut = new FileOutputStream(modelFile);
            bufOut = new BufferedOutputStream(fileOut);
            objOut = new ObjectOutputStream(bufOut);
            trainer.compileTo(objOut);
        } finally {
            Streams.closeQuietly(objOut);
            Streams.closeQuietly(bufOut);
            Streams.closeQuietly(fileOut);
        }

    }


}
