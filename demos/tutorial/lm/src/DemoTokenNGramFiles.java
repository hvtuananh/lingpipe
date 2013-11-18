import com.aliasi.lm.TokenizedLM;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import java.io.File;
import java.io.IOException;

import java.util.Arrays;

public class DemoTokenNGramFiles {

    public static void main(String[] args) throws IOException {
        TokenizerFactory tokenizerFactory
            = IndoEuropeanTokenizerFactory.INSTANCE;

        // train LM 1 on even-numbered inputs and write
        TokenizedLM lm1 = new TokenizedLM(tokenizerFactory,3);
        for (int i = 0; i < args.length; i += 2)
            lm1.handle(args[i]);
        File file1 = new File("temp1.lm.gz");
        TokenNGramFiles.writeNGrams(lm1,file1,0,3,1,"UTF-8");

        // train LM 2 on odd-numbered inputs and write
        TokenizedLM lm2 = new TokenizedLM(tokenizerFactory,3);
        for (int i = 1; i < args.length; i += 2)
            lm2.handle(args[i]);
        File file2 = new File("temp2.lm.gz");
        TokenNGramFiles.writeNGrams(lm2,file2,0,3,1,"UTF-8");

        // merge LMs to new file
        File file12 = new File("temp12.lm.gz");
        TokenNGramFiles.merge(Arrays.asList(file1,file2),
                              file12,
                              "UTF-8",
                              2,
                              false);

        // read merged LM and write
        TokenizedLM lmIo
            = new TokenizedLM(tokenizerFactory, 3,
                              lm1.unknownTokenLM(), lm1.whitespaceLM(), lm1.lambdaFactor(),
                              false);
        TokenNGramFiles.addNGrams(file12,"UTF-8",lmIo,0);
        File fileIo = new File("tempIo.lm.gz");
        TokenNGramFiles.writeNGrams(lmIo,fileIo,1,3,2,"UTF-8");

    }

}