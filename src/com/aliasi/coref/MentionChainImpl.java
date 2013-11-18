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

import com.aliasi.coref.matchers.EntityTypeMatch;
import com.aliasi.coref.matchers.ExactPhraseMatch;
import com.aliasi.coref.matchers.GenderKiller;
import com.aliasi.coref.matchers.HonorificConflictKiller;
import com.aliasi.coref.matchers.SequenceSubstringMatch;
import com.aliasi.coref.matchers.SynonymMatch;

/**
 * An implementation of mention chains.
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe1.0
 */
public class MentionChainImpl extends AbstractMentionChain {

    /**
     * Identifier for matching functions for this chain.
     */
    private int mMatcherDescriptor;

    /**
     * Construct a mention chain implementation containing the
     * single specified mention at the specified offset, assigning
     * the chain the specified identifier.
     *
     * @param mention Mention to add to mention chain.
     * @param offset Sentence offset of mention in document.
     * @param id Identifier for resulting mention chain.
     */
    public MentionChainImpl(Mention mention, int offset, int id) {
        super(mention,offset,id);
        mMatcherDescriptor = typeToMatcherDescriptor(mention.entityType());
    }

    /**
     * The matching functions defined for this mention chain.
     *
     * @return The matching functiosn for this mention chain.
     */
    @Override
    public Matcher[] matchers() {
        if (mMatcherDescriptor < 0) return MATCHERS[THING_MATCHERS];
        return MATCHERS[mMatcherDescriptor];
    }

    /**
     * The killing functions defined for this mention chain.
     *
     * @return The killing functions for this mention chain.
     */
    @Override
    public Killer[] killers() {
        if (mMatcherDescriptor < 0) return KILLERS[THING_MATCHERS];
        return KILLERS[mMatcherDescriptor];
    }

    /**
     * Adds the specified mention appearing at the specified
     * sentence offset to this mention chain.  Computes the
     * new set of matchers for this descriptor, as well as
     * setting the gender if the specified mention resolves it.
     *
     * @param mention Mention to add to this chain.
     */
    @Override
    protected void add(Mention mention) {
        int typeToMatchIndex = typeToMatcherDescriptor(mention.entityType());
        if (mMatcherDescriptor >= 0 && typeToMatchIndex >= 0) {
            mMatcherDescriptor
                = UNIFIER[mMatcherDescriptor][typeToMatchIndex];
        }
        if (gender() == null && mention.gender() != null)
            setGender(mention.gender());
        for (String honorific : mention.honorifics())
            addHonorific(honorific);
    }

    /**
     * The mapping from entity types to matching function
     * descriptors
     */
    private static int typeToMatcherDescriptor(String entityType) {
        // could replace with switch on first char
        if (entityType.equals(Tags.PERSON_TAG))
            return MALE_OR_FEMALE_MATCHERS;
        if (entityType.equals(Tags.LOCATION_TAG))
            return THING_MATCHERS;
        if (entityType.equals(Tags.ORGANIZATION_TAG))
            return THING_MATCHERS;
        if (entityType.equals(Tags.MALE_PRONOUN_TAG))
            return MALE_PRONOUN_MATCHERS;
        if (entityType.equals(Tags.FEMALE_PRONOUN_TAG))
            return FEMALE_PRONOUN_MATCHERS;
        return -1;
    }

    /**
     * Adds unifications for specified descriptor with itself,
     * resulting in itself.
     *
     * @param descriptor Descriptor to add to the collection.
     */
    private static final void descriptor(int descriptor) {
        subsume(descriptor,descriptor);
    }

    /**
     * Adds unification for a description that is subsumed by a
     * another descriptor, with the unification being equal
     * to the more specific descriptor.
     *
     * @param descriptorLess Less informative descriptor.
     * @param descriptorMore More informative descriptor.
     */
    private static void subsume(int descriptorLess, int descriptorMore) {
        unify(descriptorLess,descriptorMore,descriptorMore);
    }

    /**
     * Adds unification for specified descriptors yielding specified
     * result.
     *
     * @param descriptor1 First descriptor to unify.
     * @param descriptor2 Second descriptor to unify.
     * @return Result of unifying the two specified descriptors.
     */
    private static void unify(int descriptor1, int descriptor2, int result) {
        UNIFIER[descriptor1][descriptor2] = result;
        UNIFIER[descriptor2][descriptor1] = result;
    }

    /**
     * Identifier for matchers of either males or females.
     */
    private static final int MALE_OR_FEMALE_MATCHERS = 0; // e1f3m3s3u1w1

    /**
     * Identifier for matchers for things.
     */
    private static final int THING_MATCHERS = 1;  // e1s3u1w1

    /**
     * Identifier for matchers for just male pronouns.
     */
    private static final int MALE_PRONOUN_MATCHERS = 2; // m1

    /**
     * Identifier for matchers for just female pronouns.
     */
    private static final int FEMALE_PRONOUN_MATCHERS = 3; // f1

    /**
     * Identifier for matchers for male persons.
     */
    private static final int MALE_MATCHERS = 4; // e1m1s3u1w1

    /**
     * Identifier for matchers for female persons.
     */
    private static final int FEMALE_MATCHERS = 5; // e1f1s3u1w1

    /**
     * The table storing the results of unifying descriptors.
     */
    private static final int[][] UNIFIER = new int[6][6];
    static {
        descriptor(MALE_OR_FEMALE_MATCHERS);
        descriptor(THING_MATCHERS);
        descriptor(MALE_PRONOUN_MATCHERS);
        descriptor(FEMALE_PRONOUN_MATCHERS);
        descriptor(MALE_MATCHERS);
        descriptor(FEMALE_MATCHERS);
        subsume(MALE_PRONOUN_MATCHERS,MALE_MATCHERS);
        subsume(FEMALE_PRONOUN_MATCHERS,FEMALE_MATCHERS);
        subsume(MALE_OR_FEMALE_MATCHERS,MALE_MATCHERS);
        subsume(MALE_OR_FEMALE_MATCHERS,FEMALE_MATCHERS);
        unify(MALE_OR_FEMALE_MATCHERS,MALE_PRONOUN_MATCHERS,
              MALE_MATCHERS);
        unify(MALE_OR_FEMALE_MATCHERS,FEMALE_PRONOUN_MATCHERS,
              FEMALE_MATCHERS);
    }

    /**
     * The array mapping matcher descriptors into arrays of
     * matchers.
     */
    private static final Matcher[][] MATCHERS = new Matcher[6][];
    static {
        MATCHERS[MALE_OR_FEMALE_MATCHERS]
            = new Matcher[] {
                new ExactPhraseMatch(1),
                new EntityTypeMatch(3,Tags.MALE_PRONOUN_TAG),
                new EntityTypeMatch(3,Tags.FEMALE_PRONOUN_TAG),
                new SequenceSubstringMatch(3),
                new SynonymMatch(1)
            };
        MATCHERS[THING_MATCHERS]
            = new Matcher[] {
                new ExactPhraseMatch(1),
                new SequenceSubstringMatch(3),
                new SynonymMatch(1)
            };
        MATCHERS[MALE_PRONOUN_MATCHERS]
            = new Matcher[] {
                new EntityTypeMatch(1,Tags.MALE_PRONOUN_TAG),
            };
        MATCHERS[FEMALE_PRONOUN_MATCHERS]
            = new Matcher[] {
                new EntityTypeMatch(1,Tags.FEMALE_PRONOUN_TAG),
            };
        MATCHERS[MALE_MATCHERS]
            = new Matcher[] {
                new ExactPhraseMatch(1),
                new EntityTypeMatch(1,Tags.MALE_PRONOUN_TAG),
                new SequenceSubstringMatch(3),
                new SynonymMatch(1)
            };
        MATCHERS[FEMALE_MATCHERS]
            = new Matcher[] {
                new ExactPhraseMatch(1),
                new EntityTypeMatch(1,Tags.FEMALE_PRONOUN_TAG),
                new SequenceSubstringMatch(3),
                new SynonymMatch(1)
            };
    }

    /**
     * The array mapping matcher descriptors into arrays of
     * killers.
     */
    private static final Killer[][] KILLERS = new Killer[6][];
    static {
        KILLERS[MALE_OR_FEMALE_MATCHERS]
            = new Killer[] {
                new GenderKiller(),
                new HonorificConflictKiller()
            };
        KILLERS[THING_MATCHERS]
            = new Killer[] {
                new GenderKiller()
            };
        KILLERS[MALE_PRONOUN_MATCHERS]
            = new Killer[] {
                new GenderKiller()
            };
        KILLERS[FEMALE_PRONOUN_MATCHERS]
            = new Killer[] {
                new GenderKiller()
            };
        KILLERS[MALE_MATCHERS]
            = new Killer[] {
                new GenderKiller(),
                new HonorificConflictKiller()
            };
        KILLERS[FEMALE_MATCHERS]
            = new Killer[] {
                new GenderKiller(),
                new HonorificConflictKiller()
            };
    }

}
