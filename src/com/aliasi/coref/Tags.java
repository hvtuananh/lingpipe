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

// moved from com.aliasi.ne & hidden for 2.3

/**
 * Class of static objects and variables representing the tags used to
 * estimate spans in token sequence data.  Tags are estimated by the
 * estimators and used during decoding by the decoder.  A tagging used
 * to annotate spans with labels assigns two tags for each label, a
 * start tag and an interior tag.  For instance, with a label
 * <code>"PERSON"</code> there would be a start tag
 * <code>"ST_PERSON"</code> and an interior tag <code>"PERSON"</code>.
 * There is also a distinguished <i>out</i> tag, which is assigned to
 * tokens that are not part of a span.  From a tagging as a sequence
 * of tags, a span is defined as a start tag followed by a maximal
 * sequence of interior tags.  Consider the following example:
 *
 * <br/><br/>
 * <table cellpadding="5" border="1">
 * <tr>
 *   <td><b>Token</b></td>
 *   <td>I</td>
 *   <td>showed</td>
 *   <td>John</td>
 *   <td>Smith</td>
 *   <td>Chicago</td>
 *   <td>.</td>
 * </tr>
 * <tr>
 *   <td><b>Tag</b></td>
 *   <td>OUT</td>
 *   <td>OUT</td>
 *   <td>ST_PERSON</td>
 *   <td>PERSON</td>
 *   <td>ST_LOCATION</td>
 *   <td>OUT</td>
 *  </tr>
 * </table>
 * <br/>
 *
 * indicating that the two-token span <code>"John","Smith"</code> is a
 * person, and the one-token span <code>"Chicago"</code> is a
 * location.  All other tokens are not part of spans.
 *
 * @author  Bob Carpenter
 * @version 3.9
 * @since   LingPipe1.0
 */
final class Tags {

    /**
     * The distinguished out tag.
     */
    public static final String OUT_TAG = "OUT";

    /**
     * The distinguished tag for filling in contexts for initial
     * sequences of tokens.
     */
    public static final String START_TAG = OUT_TAG;

    /**
     * The distinguished token for filling in contexts for initial
     * sequences of tokens.
     */
    public static final String START_TOKEN = ".";

    /**
     * The start-tag prefix.
     */
    private static final String START_PREFIX = "ST_";

    /**
     * The lengthof the start prefix, in characters.
     */
    private static final int START_PREFIX_LENGTH
        = START_PREFIX.length();

    // no instances
    private Tags() {
        /* do nothing */
    }

    /**
     * Return the base tag form of the specified tag.
     *
     * @param tag Tag to reduce to base form.
     * @return Base form of specified tag.
     */
    public static String baseTag(String tag) {
        return tag.startsWith(START_PREFIX)
            ? stripPrefix(tag)
            : tag;
    }

    /**
     * Returns <code>true</code> if both tags have the same base form.
     *
     * @param tag1 First tag to test.
     * @param tag2 Second tag to test.
     * @return <code>true</code> if both tags have the same base
     * form.
     */
    public static boolean equalBaseTags(String tag1, String tag2) {
        return baseTag(tag1).equals(baseTag(tag2));
    }

    /**
     * Returns <code>true</code> if the specified tag is a start tag.
     *
     * @param tag Tag to test.
     * @return <code>true</code> if the specified tag is a start tag.
     */
    public static boolean isStartTag(String tag) {
        return tag.startsWith(START_PREFIX);
    }

    /**
     * Returns <code>true</code> if the specified tag is an inner tag.
     *
     * @param tag Tag to test.
     * @return <code>true</code> if the specified tag is an inner tag.
     */
    public static boolean isInnerTag(String tag) {
        return !tag.equals(OUT_TAG) && !isStartTag(tag);
    }

    /**
     * Returns <code>true</code> if the specified tag is the out tag.
     *
     * @param tag Tag to test.
     * @return <code>true</code> if the specified tag is the out tag.
     */
    public static boolean isOutTag(String tag) {
        return tag.equals(OUT_TAG);
    }

    /**
     * Returns <code>true</code> if breaking a tag sequence between
     * the specified tags would be in the middle of a tag sequence.
     *
     * @param tag1 First tag in the sequence.
     * @param tag2 Second tag in the sequence.
     * @return <code>true</code> if breaking a tag sequence between
     * the specified tags would be in the middle of a tag sequence.
     */
    public static boolean isMidTag(String tag1, String tag2) {
        return Tags.isInnerTag(tag2);
    }

    /**
     * Returns <code>true</code> if the specified pair of tags cannot
     * occur in a legal tag sequence.  An illegal sequence is formed
     * if the second tag is an inner tag, and the first tag doesn't
     * have a matching base tag.
     *
     * @param tag1 First tag in sequence.
     * @param tag2 Second tag in sequence to test.
     * @return <code>true</code> if <code>tag1</code> cannot follow
     * <code>tag2</code>.
     */
    public static boolean illegalSequence(String tag1, String tag2) {
        return ( Tags.isInnerTag(tag2)
                 && !Tags.equalBaseTags(tag1,tag2) );
    }

    /**
     * Convert the specified tag to the equivalent start tag.  Assumes
     * that the tag does not already begin with the start prefix.  The
     * out tag is not modified.
     *
     * @param tag Tag to convert.
     * @return Start tag version of tag.
     */
    public static String toStartTag(String tag) {
        if (isOutTag(tag) || isStartTag(tag)) return tag;
        return START_PREFIX + tag;
    }


    /**
     * Strips the start prefix from a string.  Assumes that the tag
     * begins with the start prefix.
     *
     * @param tag Tag to strip.
     * @return The interior version of the tag.
     */
    private static String stripPrefix(String tag) {
        return tag.substring(START_PREFIX_LENGTH);
    }

    /**
     * Person tag assigned by the decoder.
     */
    public static String PERSON_TAG = "PERSON";

    /**
     * Location tag assigned by the decoder.
     */
    public static String LOCATION_TAG = "LOCATION";

    /**
     * Organization tag assigned by the decoder.
     */
    public static String ORGANIZATION_TAG = "ORGANIZATION";

    /**
     * Tag for other categories, defined by user.
     */
    public static String OTHER_TAG = "OTHER";

    /**
     * Tag assigned to male pronouns through dictionary lookup.
     */
    public static String MALE_PRONOUN_TAG = "MALE_PRONOUN";

    /**
     * Tag assigned to female pronouns through dictionary lookup.
     */
    public static String FEMALE_PRONOUN_TAG = "FEMALE_PRONOUN";

    /**
     * Tag assigned to neuter pronouns through dictionary lookup.
     */
    public static String NEUTER_PRONOUN_TAG = "NEUTER_PRONOUN";

    /**
     * Tag assigned to user-defined entities with XDC property.
     * (Only used for cross-document coreference.)
     */
    public static String DATABASE_MATCH_TAG_XDC = "USER_ENTITY_XDC1";

    /**
     * Tag assigned to user-defined entities without XDC property.
     * (Only used for cross-document coreference.)
     */
    public static String DATABASE_MATCH_TAG_NO_XDC = "USER_ENTITY_XDC0";

    /**
     * An array consisting of the complete set of tags.
     */
    public static final String[] TAG_SET = new String[] {
        PERSON_TAG,
        LOCATION_TAG,
        ORGANIZATION_TAG,
        OTHER_TAG,
        MALE_PRONOUN_TAG,
        FEMALE_PRONOUN_TAG,
        DATABASE_MATCH_TAG_XDC,
        DATABASE_MATCH_TAG_NO_XDC
    };

}
