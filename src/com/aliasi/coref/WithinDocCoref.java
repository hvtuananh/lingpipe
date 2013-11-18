/*
 * LingPipe v. 4.1.0
 * Copyright (C) 2003-2011 Alias-i
 *
 * This program is licensed under the Alias-i Royalty Free License
 * Version 1 WITHOUT ANY WARRANTY, without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the Alias-i
 * Royalty Free License Version 1 for more details.
 *
 * You should have received a copy of the Alias-i Royalty Free License
 * Version 1 along with this program; if not, visit
 * http://alias-i.com/lingpipe/licenses/lingpipe-license-1.txt or contact
 * Alias-i, Inc. at 181 North 11th Street, Suite 401, Brooklyn, NY 11211,
 * +1 (718) 290-9170.
 */

package com.aliasi.coref;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A <code>WithinDocCoref</code> object handles resolution of
 * coreference relations between mentions of entities.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe1.0
 */
public final class WithinDocCoref {

    private final List<MentionChain> mMentionChains
        = new ArrayList<MentionChain>();

    /**
     * The mention factory used to create mentions and mention
     * chains.
     */
    private final MentionFactory mMentionFactory;

    /**
     * Construct an instance of within-document coreference with
     * the specified mention factory.
     *
     * @param mentionFactory Factory for creating mentions and chains.
     */
    public WithinDocCoref(MentionFactory mentionFactory) {
        mMentionFactory = mentionFactory;
    }

    /**
     * Returns the set of mention chains, sorted in order of
     * identifier from first appearance to last.
     *
     * @return Array of mention chains resolved for this document.
     */
    public MentionChain[] mentionChains() {
        return mMentionChains.<MentionChain>toArray(EMPTY_MENTION_CHAIN_ARRAY);
    }

    /**
     * Resolves a specified mention at a specified offset, returning
     * the integer identifier of the mention, or <code>-1</code> if the
     * mention could not be resolved, which should only occur for
     * pronominal mentions.
     *
     * @param mention Mention to resolve.
     * @param offset Sentence offset of the specified mention.
     * @return Integer identifier for the mention, or <code>-1</code>
     * if it could not be resolved.
     */
    public int resolveMention(Mention mention, int offset) {

        // required for array
        @SuppressWarnings({"unchecked","rawtypes"})
        List<MentionChain>[] hypotheses
            = new List[Matcher.MAX_SCORE+1];
        for (int i = 0; i < hypotheses.length; ++i)
            hypotheses[i] = new ArrayList<MentionChain>();
        MentionChain[] antecedents = mentionChains();
        Arrays.sort(antecedents,SENTENCE_FINAL_COMPARATOR);
        for (int i = 0; i < antecedents.length; ++i) {
            MentionChain nextAntecedent = antecedents[i];
            if (finished(offset,nextAntecedent,hypotheses)) break;
            addPossibleAntecedent(mention,offset,nextAntecedent,hypotheses);
        }
        return selectAntecedent(hypotheses,mention,offset);
    }

    /**
     * Returns <code>true</code> if there is a hypothesis in the
     * specified list of hypotheses that has a better score than the
     * chain under consideration can possibly have versus the mention.
     * Currently, only distance is used, with coreference being
     * finished if a mention has a potential antecedent with a total
     * score less than the distance from the specified chain.
     *
     * @param mentionOffset Offset of mention to measure.
     * @param chain Mention chain to measure against mention.
     * @param hypotheses Current hypotheses for antecedent of mention.
     * @return <code>true</code> if there is a better hypothesis in
     * the list of hypotheses than the mention can score against the
     * chain.
     */
    private boolean finished(int mentionOffset,
                             MentionChain chain,
                             List<MentionChain>[] hypotheses) {

        int distance = distanceScore(mentionOffset,chain);
        for (int i = distance; i > 0; --i)
            if (hypotheses[i].size() > 0) return true;
        return false;
    }

    /**
     * Scores the specified mention against the specified mention
     * chain, adding the antecedent to the list of hypotheses at the
     * index corresponding to its score, not adding it if there is no
     * match.
     *
     * @param mention Mention to score.
     * @param chain Mention chain to score against mention.
     * @param hypotheses Current hypotheses for antecedent of mention.
     */
    private void addPossibleAntecedent(Mention mention,
                                       int offset,
                                       MentionChain antecedent,
                                       List<MentionChain>[] hypotheses) {
        if (antecedent.killed(mention)) return;
        int matchScore = antecedent.matchScore(mention);
        if (matchScore == Matcher.NO_MATCH_SCORE) return;
        int totalScore = matchScore + distanceScore(offset,antecedent);
        hypotheses[totalScore].add(antecedent);
    }

    /**
     * Given a complete list of hypotheses antecedents for the
     * specified mention, the mention will be resolved and either
     * added to the set of chains as a new mention chain or will be
     * merged into an antecedent provided in the hypotheses.  The
     * algorithm picks the first index (lowest index, which is best
     * scoring) in the hypotheses list that is non-empty, and if it is
     * a singleton, resolves the antecedent, and if it is not a
     * singleton, promotes it to a new mention chain. If there are no
     * antecedents in the list of hypotheses, then the mention is
     * also promoted to a new mention chain. The integer identifier
     * of the resulting mention chain is returned, or <code>-1</code>
     * if no mention chain is created.
     *
     * @param hypotheses List of sets of candidate antecedents.
     * @param mention Mention to be resolved.
     * @return Integer identifier for the mention chain against which
     * the mention is resolved, or <code>-1</code> if it could not be
     * resolved.
     */
    private int selectAntecedent(List<MentionChain>[] hypotheses,
                                 Mention mention, int offset) {
        for (int score = 0; score < hypotheses.length; ++score) {
            if (hypotheses[score].size() == 1) {
                MentionChain antecedent = hypotheses[score].get(0);
                antecedent.add(mention,offset);
                return antecedent.identifier();
            } else if (hypotheses[score].size() > 1) {
                // multiple antecedents, don't select any
                return promoteToNewChain(mention,offset);
            }
        }
        // no antecedent
        return promoteToNewChain(mention,offset);
    }

    /**
     * Promotes a mention to a new mention chain, adding it to the set
     * of chains, returning the identifier of the new chain, or
     * <code>-1</code> if none is created.  A return will be
     * <code>false</code> only if the mention is pronominal.
     *
     * @param mention Mention to promote to a mention chain.
     * @param offset Sentence offset for this token.
     * @return Integer identifier of new chain, or <code>-1</code>
     * if none is created.
     */
    private int promoteToNewChain(Mention mention, int offset) {
        if (mention.isPronominal()) return -1;
        MentionChain chain = mMentionFactory.promote(mention,offset);
        mMentionChains.add(chain);
        return chain.identifier();
    }

    /**
     * Returns the ``distance'' between a mention and an antecedent
     * mention chain, based on their sentence offsets.  The distance
     * returned is <code>0</code> if they are in the same sentence,
     * <code>1</code> if the antecedent is in the previous sentence or
     * the sentence before the previous sentence, and <code>2</code>
     * otherwise.
     *
     * @param mentionOffset Offset of mention.
     * @param antecedent Mention chain to measure.
     * @return Distance between mention and antecedent.
     */
    private static int distanceScore(int mentionOffset,
                                     MentionChain antecedent) {
        switch (mentionOffset - antecedent.maxSentenceOffset()) {
        case 0: return 0;
        case 1: return 1;
        case 2: return 1;
        default: return 2;
        }
    }

    /**
     *  A comparator for comparing two mentions chains. The greater
     *  one is the one with the largest sentence offset.  May return
     *  <code>0</code> for entities that are not equal according to
     *  but this is OK because they're just being sorted.  Also * not
     *  coordinated with the mention chain's equality or hash code.  *
     *  The sort is also not stable; changing mention chains by adding
     *  mentions may change the results in subsequent runs.
     */
    public static final Comparator<MentionChain> SENTENCE_FINAL_COMPARATOR
        = new Comparator<MentionChain>() {
                public int compare(MentionChain chain1,
                                   MentionChain chain2) {
                    if (chain1.maxSentenceOffset()
                        < chain2.maxSentenceOffset()) return 1;
                    if (chain1.maxSentenceOffset()
                        > chain2.maxSentenceOffset()) return -1;
                    return 0;
                }
            };


    static final MentionChain[] EMPTY_MENTION_CHAIN_ARRAY = new MentionChain[0];
}
