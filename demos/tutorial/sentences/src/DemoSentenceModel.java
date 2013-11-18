import com.aliasi.sentences.HeuristicSentenceModel;

import java.util.HashSet;
import java.util.Set;

/**
 * This <code>DemoSentenceModel</code> extends the LingPipe
 * <code>HeuristicSentenceModel</code> and is intended to be used as
 * testbed for developing application or domain-specific sentence
 * models.
 * 
 * It provides minimally populated sets for
 * <code>POSSIBLE_STOPS</code>, <code>IMPOSSIBLE_PENULTIMATES</code>,
 * and <code>IMPOSSIBLE_SENTENCE_STARTS</code>.
 * 
 * The LingPipe API Tutorial on using Sentence Models provides
 * examples of how to build out this class.
 */
public class DemoSentenceModel extends HeuristicSentenceModel {

    /**
     * Construct a demo sentence model.
     */
    public DemoSentenceModel() {
        super(POSSIBLE_STOPS,
              IMPOSSIBLE_PENULTIMATES,
              IMPOSSIBLE_SENTENCE_STARTS,
	      false,  // force final stop
	      false); // balance parens
    }

    private static final Set<String> POSSIBLE_STOPS = new HashSet<String>();
    static {
        POSSIBLE_STOPS.add(".");
    }

    private static final Set<String> IMPOSSIBLE_PENULTIMATES
        = new HashSet<String>();
    static {
        // Common Abbreviations
	//        IMPOSSIBLE_PENULTIMATES.add("Bros");
        // Personal Honorifics
        IMPOSSIBLE_PENULTIMATES.add("Mme");
	//        IMPOSSIBLE_PENULTIMATES.add("Mr");
        // Professional Honorifics
        IMPOSSIBLE_PENULTIMATES.add("Dr");    
        // Name Suffixes
	//        IMPOSSIBLE_PENULTIMATES.add("Jr");
        // Corporate Designators
	//        IMPOSSIBLE_PENULTIMATES.add("Co");
    }

    private static final Set<String> IMPOSSIBLE_SENTENCE_STARTS
        = new HashSet<String>();
    static {
        IMPOSSIBLE_SENTENCE_STARTS.add(")");
        IMPOSSIBLE_SENTENCE_STARTS.add("]");
        IMPOSSIBLE_SENTENCE_STARTS.add("}");
        IMPOSSIBLE_SENTENCE_STARTS.add(">");
	//        IMPOSSIBLE_SENTENCE_STARTS.add("<");
        IMPOSSIBLE_SENTENCE_STARTS.add(".");
        IMPOSSIBLE_SENTENCE_STARTS.add("!");
        IMPOSSIBLE_SENTENCE_STARTS.add("?");
        IMPOSSIBLE_SENTENCE_STARTS.add(":");
        IMPOSSIBLE_SENTENCE_STARTS.add(";");
        IMPOSSIBLE_SENTENCE_STARTS.add("-");
        IMPOSSIBLE_SENTENCE_STARTS.add("--");
        IMPOSSIBLE_SENTENCE_STARTS.add("---");
        IMPOSSIBLE_SENTENCE_STARTS.add("%");
    }
}
