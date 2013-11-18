import com.aliasi.spell.CompiledSpellChecker;

import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.Tokenizer;

public class StatisticalTokenizerFactory extends RegExTokenizerFactory {

    static final long serialVersionUID = -756374L;

    private final CompiledSpellChecker mSpellChecker;

    public StatisticalTokenizerFactory(CompiledSpellChecker spellChecker) {
        super("\\s+"); // break on spaces
        mSpellChecker = spellChecker;
    }

    public Tokenizer tokenizer(char[] cs, int start, int length) {
        String input = new String(cs,start,length);
        String output = mSpellChecker.didYouMean(input);
        char[] csOut = output.toCharArray();
        return super.tokenizer(csOut,0,csOut.length);
    }
}

