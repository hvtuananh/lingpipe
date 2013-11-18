package com.aliasi.corpus.parsers;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunking;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;

import com.aliasi.tag.StringTagging;

import com.aliasi.util.Strings;

import java.util.Arrays;

/**
 * The <code>GeneTagParser</code> class provides a tag parser for the
 * GeneTag named-entity corpus.  GeneTag was created at the United States
 * national Center for Biotechnology Information (NCBI) and is a part
 * of their MedTag distribution, which also includes a part-of-speech
 * corpus (see {@link MedPostPosParser}).
 *
 * <P>NCBI distributes the GeneTag corpus freely for public use as a
 * &quot;United States Government Work&quot; (see included
 * <code>README</code> file for more information):
 *
 * <UL>
 * <LI> <a href="ftp://ftp.ncbi.nlm.nih.gov/pub/lsmith/MedTag/medtag.tar.gz"
 *      >MedTag Corpus FTP</a> (tar.gz 14.7MB)
 * </UL>
 *
 * The GeneTag corpus is in the single file
 * <code>/medtag/genetag/genetag.tag</code> relative to the directory
 * into which the distribution is unpacked.
 *
 * <P>An excerpt of two training sentences in the file is:
 *
 * <blockquote><table border='1'><tr><td><pre>
 * P00073344A0367
 * In_TAG 2_TAG subjects_TAG the_TAG phytomitogen_TAG reactivity_TAG of_TAG the_TAG lymphocytes_TAG was_TAG improved_TAG after_TAG treatment_TAG ._TAG
 * P00083846T0000
 * Albumin_GENE2 and_TAG cyclic_TAG AMP_TAG levels_TAG in_TAG peritoneal_TAG fluids_TAG in_TAG the_TAG child_TAG
 * P00088391A0181
 * On_TAG the_TAG other_TAG hand_TAG factor_GENE1 IX_GENE1 activity_TAG is_TAG decreased_TAG in_TAG coumarin_TAG treatment_TAG with_TAG factor_GENE2 IX_GENE2 antigen_TAG remaining_TAG normal_TAG ._TAG
 * </pre></td></tr></table></blockquote>
 *
 * GeneTag marks up individual sentences with a combination of
 * <code>GENE1</code>, <code>GENE2</code> and <code>TAG</code> tags.
 * A chunk is a contiguous sequence of <code>GENE1</code> or
 * <code>GENE2</code> tags.  The indices <code>1</code> and
 * <code>2</code> are not to differentiate types, but to allow two
 * genes in a row.  In fact, the corpus is annotateed such that the
 * gene references alternate even across sentences.
 *
 * <P>The output tagging is in the standard LingPipe BIO format.
 *
 * <P>The primary reference for GeneTag is:
 *
 * <UL>
 * <LI> Lorraine Tanabe, Natalie Xie, Lynne H. Thom, Wayne Matten and W.
 * John Wilbur. 2005. <a
 * href="http://www.biomedcentral.com/1471-2105/6/S1/S3">GENETAG: a
 * tagged corpus for gene/protein named entity recognition</a>.
 * <i>BMC Bioinformatics</i> 2005, <b>6</b>(Suppl 1):S3.
 * </UL>
 *
 * @author  Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe2.1
 */
public class GeneTagParser extends StringParser<ObjectHandler<Chunking>> {

    /**
     * Parse the specified input source and send extracted taggings to
     * the current handler.  This string should correspond to the
     * contents of an input file.
     *
     * @param cs Character array underlying string.
     * @param start First character of string.
     * @param end Index of one past the last character in the string.
     */
    @Override
    public void parseString(char[] cs, int start, int end) {
        String in = new String(cs,start,end-start);
        String[] sentences = in.split("\n");
        for (int i = 0; i < sentences.length; ++i) {
            if (Strings.allWhitespace(sentences[i])) continue;
            if (sentences[i].indexOf('_') < 0) continue;
            processSentence(sentences[i]);
        }
    }

    void processSentence(String sentence) {
        String[] tagTokenPairs = sentence.split(" ");
        String[] tokens = new String[tagTokenPairs.length];
        String[] tags = new String[tagTokenPairs.length];

        for (int i = 0; i < tagTokenPairs.length; ++i) {
            String pair = tagTokenPairs[i];
            int j = pair.lastIndexOf('_');
            tokens[i] = pair.substring(0,j).trim();
            tags[i] = pair.substring(j+1).trim();
        }
        String[] whitespaces = new String[tokens.length+1];
        if (whitespaces.length > 3)
            Arrays.fill(whitespaces,1,whitespaces.length-2," ");
        whitespaces[0] = "";
        whitespaces[whitespaces.length-1] = "";
        String[] normalTags = normalizeTags(tags);
        
        BioTagChunkCodec codec = new BioTagChunkCodec();
        int[] tokenStarts = new int[tokens.length];
        int[] tokenEnds = new int[tokens.length];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.length; ++i) {
            tokenStarts[i] = sb.length();
            sb.append(tokens[i]);
            tokenEnds[i] = sb.length();
            if (i+1 < tokens.length)
                sb.append(' '); // single space separator except at end
        }
        StringTagging tagging = new StringTagging(Arrays.asList(tokens),
                                                  Arrays.asList(normalTags),
                                                  sb.toString(),
                                                  tokenStarts,
                                                  tokenEnds);
        Chunking chunking = codec.toChunking(tagging);
        getHandler().handle(chunking);
    }

    String[] normalizeTags(String[] tags) {
        String[] result = new String[tags.length];
        for (int i = 0; i < tags.length; ) {
            if (tags[i].startsWith("GENE")) {
                String tag = tags[i];
                result[i] = "B_GENE";
                ++i;
                while (i < tags.length && tags[i].equals(tag))
                    result[i++] = "I_GENE";
            } else { // (tags[i].equals("TAG")) {
                result[i++] = "O";
            }
        }
        return result;
    }




}