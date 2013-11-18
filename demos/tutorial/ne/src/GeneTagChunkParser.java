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

package com.aliasi.corpus.parsers;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.ChunkingImpl;

import com.aliasi.corpus.ObjectHandler;
import com.aliasi.corpus.StringParser;

import com.aliasi.util.Files;
import com.aliasi.util.ObjectToSet;

import java.io.File;
import java.io.IOException;

/**
 * The <code>GeneTagChunkParser</code> class is designed to parse the
 * offset-annotated first-best GeneTag named entity corpus into a
 * chunk-based representation.  GeneTag was created at the United
 * States national Center for Biotechnology Information (NCBI) and is
 * a part of their MedTag distribution, which also includes a
 * part-of-speech corpus (see {@link MedPostPosParser}).
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
 * <P>The GeneTag corpus is distributed in both a tagged version and
 * in an offset representation.  The offset representation supplies
 * raw sentences in <code>medtag/genetag/genetag.sent</code>:
 *
 * <blockquote><table border='1' cellpadding='5'><tr><td><pre>
 * P00010943A0733
 * Flurazepam thus appears to be an effective hypnotic drug with the optimum dose for use in general practice being 15 mg at night.
 * P00013683A0210
 * When extracorporeal CO2 removal approximated CO2 production (VCO2), alveolar ventilation almost ceased.
 * ...
 * P00001606T0076"
 * Comparison with alkaline phosphatases and 5-nucleotidase"
 * ...
 * </pre></td></tr></table></blockquote>
 *
 * Every other line is an identifier containing the character 'P',
 * the PubMed identifier for the MEDLINE citation, either 'A' or
 * 'T' depending on whether the sentence is from the abstract or title,
 * and then a character offset into the abstract.

 * <P>First-best gold-standard taggings are in
 * <code>medtag/genetag/Gold.format</code>:
 *
 * <blockquote><table border='1' cellpadding='5'><tr><td><pre>
 * P00001606T0076|14 33|alkaline phosphatases
 * P00001606T0076|37 50|5-nucleotidase
 * P00015731A0090|36 52|carbonic anhydrase
 * ...
 * </pre></td></tr></table></blockquote>
 *
 * These are arranged one per line, beginning with the sentence
 * identifier, then the character offsets.

 *  <P><b>Warning:</b> The offsets only count non-whitespace
 *  characters.  Thus the term <code>&quot;alkaline
 *  phosphates&quot;</code> does not show up between characters 14 and
 *  33 as one might expect.  Consider the following numbering:

 * <blockquote><pre>
 * Comparison with alkaline phosphatases and 5-nucleotidase
 * 01234567890123456789012345678901234567890123456789012345
 * 0         1         2         3         4         5
 * </pre></blockquote>
 *
 * The characters between 14 and 33 inclusive are <code>&quot;h
 * alkaline phosph&quot;</code>, not the phrase <code>&quot;alkaline
 * phosphates&quot;</code> we are looking for.  Instead, the terms are
 * indexed <i>not counting whitespace</i>.  Thus the appropriate numbering
 * is actually:
 *
 * <blockquote><pre>
 * Comparison with alkaline phosphatases and 5-nucleotidase
 * 0123456789 0123 45678901 234567890123 456 7890123456789012345
 * 0          1          2          3           4         5
 * </pre></blockquote>
 *
 * and it's evident that the desired phrase is now runs from characters
 * numbered 14 to 33 inclusive.
 *
 * </pre></blockquote>
 * <P>Because there are two files, this parser cannot be implemented
 * as neatly as the other ones.  Instead, the gold format file must
 * be provided in the constructor so that when parsing happens,
 * it has the chunks.
 *
 * <P>The authors of the corpus do not indicate its character set, but
 * creating a histogram over the bytes shows that the data set
 * contains only 87 distinct ASCII characters.  In general, MEDLINE
 * titles and abstracts may contain non-ASCII Latin characters (see
 * the description in <a
 * href="http://www.nlm.nih.gov/databases/dtd/medline_characters.html">MEDLINE
 * characters overview</a> and the full character set in <a
 * href="http://www.nlm.nih.gov/databases/dtd/medline_character_database.html">MEDLINE
 * character database</a>).
 *
 * @author Bob Carpenter
 * @version 3.9.1
 * @since   LingPipe2.1
 * @deprecated This class will move to the demos in 4.0.
 */
@Deprecated
public class GeneTagChunkParser 
    extends StringParser<ObjectHandler<Chunking>> {

    ObjectToSet<String,Chunk> mIdToChunkSet
        = new ObjectToSet<String,Chunk>();

    /**
     * Construct a GeneTag chunk parser with the specified gold standard
     * file and no specified handler.
     *
     * @param goldFormatFile The gold standard format file.
     * @throws IOException If there is an I/O error reading the gold
     * standard file.
     */
    public GeneTagChunkParser(File goldFormatFile) throws IOException {
        this(goldFormatFile,null);
    }

    /**
     * Construct a GeneTag chunk parser with the specified gold standard
     * file and the specified chunk handler.
     *
     * @param goldFormatFile The gold standard format file.
     * @param handler Chunk handler for this parser.
     * @throws IOException If there is an I/O error reading the gold
     * standard file.
     */
    public GeneTagChunkParser(File goldFormatFile, 
                              ObjectHandler<Chunking> handler)
        throws IOException {

        super(handler);
        readChunks(goldFormatFile);
    }

    /**
     * Returns the chunk handler for this parser.
     *
     * @return The chunk handler.
     * @deprecated Use generic {@link #getHandler()} instead.
     */
    @Deprecated
    public ObjectHandler<Chunking> getChunkHandler() {
        return getHandler();
    }

    @Override
    public void parseString(char[] cs, int start, int end) {
        String s = new String(cs,start,end-start);
        String[] lines = s.split("\n");
        for (int i = 0; i < lines.length; ) {
            String id = lines[i++];
            String text = lines[i++];
            if (text.length() == 0) continue;
            int[] mapping = new int[text.length()];
            int target = 0;
            for (int k = 0; k < mapping.length; ++k)
                if (text.charAt(k) != ' ')
                    mapping[target++] = k;
            ChunkingImpl chunking = new ChunkingImpl(text);
            for (Chunk nextChunk : mIdToChunkSet.getSet(id)) {
                int chunkStart = mapping[nextChunk.start()];
                int chunkEnd = mapping[nextChunk.end()];
                Chunk remappedChunk
                    = ChunkFactory.createChunk(chunkStart,chunkEnd+1,
                                               GENE_CHUNK_TYPE);
                chunking.add(remappedChunk);
            }
            getChunkHandler().handle(chunking);
        }
    }

    /**
     * The type assigned to the chunks extracted by this parser,
     * namely <code>&quot;GENE&quot;</code>.
     */
    public static final String GENE_CHUNK_TYPE = "GENE";

    final void readChunks(File formatFile) throws IOException {
        String s = Files.readFromFile(formatFile,"ASCII");
        String[] lines = s.split("\n");
        for (int i = 0; i < lines.length; ++i)
            readChunk(lines[i]);
    }

    final void readChunk(String line) {
        int i = line.indexOf('|');
        if (i < 0) return;
        int j = line.indexOf('|',i+1);
        String sentenceId = line.substring(0,i);
        String numSection = line.substring(i+1,j);
        String[] nums = numSection.split(" ");
        int start = Integer.valueOf(nums[0]);
        int end = Integer.valueOf(nums[1]);
        Chunk chunk
            = ChunkFactory.createChunk(start,end);
        mIdToChunkSet.addMember(sentenceId,chunk);
    }


}
