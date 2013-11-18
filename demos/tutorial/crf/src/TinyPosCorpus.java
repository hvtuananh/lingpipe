import com.aliasi.corpus.Corpus;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.tag.Tagging;

import java.util.Arrays;

public class TinyPosCorpus
    extends Corpus<ObjectHandler<Tagging<String>>> {

    public void visitTrain(ObjectHandler<Tagging<String>> handler) {
        for (String[][] wordsTags : WORDS_TAGSS) {
            String[] words = wordsTags[0];
            String[] tags = wordsTags[1];
            Tagging<String> tagging
                = new Tagging<String>(Arrays.asList(words),
                                      Arrays.asList(tags));
            handler.handle(tagging);
        }
    }

    public void visitTest(ObjectHandler<Tagging<String>> handler) {
        /* no op */
    }

    // legal starts: PN, DET
    // legal trans: DET-N, IV-EOS, N-EOS, N-IV, N-TV, PN-EOS, PN-IV, PN-TV, TV-DET, TV-PN,
    // legal ends: EOS
    static final String[][][] WORDS_TAGSS = new String[][][] {
        { { "John", "ran", "." },                 { "PN", "IV", "EOS" } },
        { { "Mary", "ran", "." },                 { "PN", "IV", "EOS" } },
        { { "John", "jumped", "!" },              { "PN", "IV", "EOS" } },
        { { "The", "dog", "jumped", "!" },        { "DET", "N", "IV", "EOS" } },
        { { "The", "dog", "sat", "." },           { "DET", "N", "IV", "EOS" } },
        { { "Mary", "sat", "!" },                 { "PN", "IV", "EOS" } },
        { { "Mary", "likes", "John", "." },       { "PN", "TV", "PN", "EOS" } },
        { { "The", "dog", "likes", "Mary", "." }, { "DET", "N", "TV", "PN", "EOS" } },
        { { "John", "likes", "the", "dog", "." }, { "PN", "TV", "DET", "N", "EOS" } },
        { { "The", "dog", "ran", "." },           { "DET", "N", "IV", "EOS", } },
        { { "The", "dog", "ran", "." },           { "DET", "N", "IV", "EOS", } }
    };

}

