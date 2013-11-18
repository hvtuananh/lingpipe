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

import java.util.HashSet;
import java.util.Set;


/**
 * An abstract implementation of a mention chain in terms of matching
 * and killing functions.  Concrete subclasses need onnly implement
 * {@link #matchers()} and {@link #killers()}.  This class manages the
 * fixed identifier, fixed entity type, variable maximum sentence
 * offset, and variable collection of mentions.  When mentions are
 * added through {@link #add(Mention,int)}, the maximum sentence
 * offset and set of mentions are maintained. Subclasses may override
 * add, but should always call it.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe1.0
 */
public abstract class AbstractMentionChain implements MentionChain {

    /**
     * The set of mentions backing this mention chain.
     */
    private final HashSet<Mention> mMentions = new HashSet<Mention>();

    /**
     * The entity type assigned to this mention chain.
     */
    String mEntityType;

    /**
     * The set of honorifics for this mention chain.
     * Subclasses may add honorifics.
     */
    final HashSet<String> mHonorifics = new HashSet<String>();

    /**
     * The maximum offset of any of the mentions in the mention chain.
     * This is the index of the last occurrence in a document of a
     * mention of the referent of this mention chain.
     */
    private int mMaxSentenceOffset;

    /**
     * The gender is initially set to the mention's gender, but
     * it may be changed by subclasses as a result of adding
     * new mentions.
     */
    String mGender = null;

    /**
     * Identifier assigned to this mention chain.
     */
    private final int mIdentifier;

    public AbstractMentionChain(Mention mention, int offset, int identifier) {
        mEntityType = mention.entityType();
        mMaxSentenceOffset = offset;
        mMentions.add(mention);
        mIdentifier = identifier;
        mGender = mention.gender();
        mHonorifics.addAll(mention.honorifics());
    }

    /**
     * Set this chain's gender to the specified value.
     *
     * @param gender Value of gender.
     */
    public void setGender(String gender) {
        mGender = gender;
    }

    /**
     * Add the specified honorific to the set of honorifics for
     * this chain.
     *
     * @param honorific Honorific to add.
     */
    public void addHonorific(String honorific) {
        mHonorifics.add(honorific);
    }

    /**
     * Returns the set of mentions underlying this mention chain.
     * These are collected with the constructor and as mentions
     * are added.
     *
     * @return Set of mentions underlying this mention chain.
     */
    public final Set<Mention> mentions() {
        return mMentions;
    }

    /**
     * Returns the set of honorifics for this mention chain.
     * These are collected with the constructor and as mentions
     * are added.
     *
     * @return Set of honorifics for this mention chain.
     */
    public final Set<String> honorifics() {
        return mHonorifics;
    }

    /**
     * Returns the gender of this mention chain.
     *
     * @return The gender of this mention chain.
     */
    public final String gender() {
        return mGender;
    }

    /**
     * Offset of the last sentence in which a mention belonging to
     * this mention chain appears.
     *
     * @return Offset of last sentence containing a mention in this
     * chain.
     */
    public final int maxSentenceOffset() {
        return mMaxSentenceOffset;
    }

    /**
     * Returns the entity type associated with this mention chain.
     *
     * @return Entity type associated with this mention chain.
     */
    public String entityType() {
        return mEntityType;
    }

    /**
     * Set the entity type of this mention chain to the specified value.
     *
     * @param entityType New type for this mention chain.
     */
    public void setEntityType(String entityType) {
        mEntityType = entityType;
    }

    /**
     * Adds the specified mention appearing at the specified
     * sentence offset to this mention chain, and calls the
     * method {@link #add(Mention)}, which may be overridden
     * by subclasses to carry out additional bookkeeping when
     * new mentions are added.
     *
     * @param mention Mention to add to this chain.
     * @param sentenceOffset Offset of mention added.
     */
    public final void add(Mention mention, int sentenceOffset) {
        mMentions.add(mention);
        if (sentenceOffset > mMaxSentenceOffset)
            mMaxSentenceOffset = sentenceOffset;
        add(mention);
    }

    /**
     * Adds the specified mention appearing at the specified sentence
     * offset to this mention chain.  Called by {@link
     * #add(Mention,int)} when a mention is added.  
     * 
     * <p>The implementation
     * provided here does nothing, but subclasses may override it to
     * carry out additional bookkeeping when new mentions are added.
     *
     * @param mention Mention to add to this chain.
     */
    protected void add(Mention mention) {  
        // do nothing on purpose
    }

    /**
     * Returns the unique identifier for this mention chain.  Uniqueness
     * is guaranteed within a document, not across documents.
     *
     * @return Unique identifier for this mention chain in a document.
     */
    public final int identifier() {
        return mIdentifier;
    }

    /**
     * Returns <code>true</code> if there is a killing function that
     * defeats the match of the mention against the mention chain.  A
     * <code>true</code> return means that the mention will not
     * be allowed to match against the antecedent no matter what other
     * matching functions return.
     *
     * @param mention Mention to test against antecedent.
     * @return <code>true</code> if there is a killing function that
     * defeats the match of the mention against the mention chain.
     */
    public final boolean killed(Mention mention) {
        Killer[] killers = killers();
        for (int i = 0; i < killers.length; ++i)
            if (killers[i].kill(mention,this))
                return true;
        return false;
    }

    /**
     * Returns the best match score of the mention versus the mention
     * chain according to the matching functions determined by the
     * antecedent.  A score of {@link Matcher#NO_MATCH_SCORE}
     * indicates that all matching functions failed.  Otherwise, the
     * score will fall in the range <code>0</code> to {@link
     * Matcher#MAX_SCORE}, with a lower score being better.
     *
     * @param mention Mention to score against antecedent.
     * @return Best matching score between mention and antecedent.
     */
    public final int matchScore(Mention mention) {
        Matcher[] matchers = matchers();
        int bestScore = Matcher.MAX_SCORE+1;
        for (int i = 0; i < matchers.length; ++i) {
            int score = matchers[i].match(mention,this);
            if (score != Matcher.NO_MATCH_SCORE && score < bestScore) {
                bestScore = score;
            }
        }
        return bestScore > Matcher.MAX_SCORE
            ? Matcher.NO_MATCH_SCORE
            : bestScore;
    }

    /**
     * Returns <code>true</code> if the specified object is a mention
     * chain that is equal to this mention chain.
     *
     * @param that Object to test for equality with this mention chain.
     * @return <code>true</code> if the mention chains are equal.
     */
    @Override
    public boolean equals(Object that) {
        return that instanceof MentionChain
            && equals((MentionChain) that);
    }

    /**
     * Returns <code>true</code> if the specified mention chain is
     * equal to this mention chain.
     *
     * @param that Mention chain to test for equality with this
     * mention chain.
     * @return <code>true</code> if the mention chains are equal.
     */
    public boolean equals(MentionChain that) {
        return identifier() == that.identifier();
    }

    /**
     * Returns the hash code for this mention chain.
     *
     * @return Hash code for this mention chain.
     */
    @Override
    public int hashCode() {
        return identifier();
    }

    /**
     * Returns a string representation of this mention chain.
     *
     * @return A string representation of this mention chain.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID=");
        sb.append(identifier());
        sb.append("; mentions=");
        sb.append(mentions());
        sb.append("; gender=");
        sb.append(gender());
        sb.append("; honorifics=");
        sb.append(honorifics());
        sb.append("; maxSentenceOffset=");
        sb.append(maxSentenceOffset());
        return sb.toString();
    }


    /**
     * The matching functions defined for this mention chain.
     *
     * @return The matching functiosn for this mention chain.
     */
    public abstract Matcher[] matchers();

    /**
     * The killing functions defined for this mention chain.
     *
     * @return The killing functions for this mention chain.
     */
    public abstract Killer[] killers();


}
