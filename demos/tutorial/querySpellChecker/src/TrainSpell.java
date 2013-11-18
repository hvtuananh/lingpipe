import com.aliasi.spell.TrainSpellChecker;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.lm.NGramProcessLM;

import com.aliasi.util.AbstractExternalizable;
import com.aliasi.util.Files;
import com.aliasi.util.ObjectToCounterMap;


import java.io.File;

import java.util.LinkedList;



public class TrainSpell {

    public static void main(String[] args) throws Exception {
        // configure
        File inputPath = new File(args[0]);
        File outputTokensFile = new File(args[1]);
        File outputModelFile = new File(args[2]);
        int nGram = Integer.valueOf(args[3]);

        // test config
        if (!inputPath.exists()) {
            System.out.println("Could not find training data.");
            System.out.println("Have you unpacked the demo data?");
            System.out.println("If not, run: ");
            System.out.println("    > cd $LINGPIPE");
            System.out.println("    > ant jars");
            return;
        }


        // construct
        NGramProcessLM lm = new NGramProcessLM(nGram);
        FixedWeightEditDistance fixedEdit = new FixedWeightEditDistance(); // dummy
        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;
        TrainSpellChecker trainer
            = new TrainSpellChecker(lm,fixedEdit,tokenizerFactory);

        // train
        LinkedList<File> fileList = new LinkedList<File>();
        fileList.add(inputPath);
        while (fileList.size() > 0) {
            File file = fileList.removeFirst();
            if (file.isDirectory()) {
                System.out.println("Visiting directory=" + file);
                for (File subFile : file.listFiles())
                    fileList.addLast(subFile);
            } else {
                System.out.println("Training on file=" + file);
                String text = Files.readFromFile(file,"ISO-8859-1");
                trainer.handle(text);
            }
        }

        // compile
        System.out.println("\nCompiling spell checker to file=" + outputModelFile);
        AbstractExternalizable.compileTo(trainer,outputModelFile);
        System.out.println("\nSerializing token counts to file=" + outputTokensFile);
        AbstractExternalizable.serializeTo(trainer.tokenCounter(),outputTokensFile);

        System.out.println("\nFINISHED NORMALLY.");
    }

}

