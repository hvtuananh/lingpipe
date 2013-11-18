import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;

import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.AbstractExternalizable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AnnotateMedlineDb {
    static final String GET_CITATION_IDS_SQL = 
        "SELECT citation_id FROM citation";

    static final String GET_CITATION_TEXT_SQL = 
        "SELECT title, abstract FROM citation where citation_id = ?";

    static final String INSERT_SENTENCE_SQL = 
        "INSERT INTO sentence (sentence_id, citation_id, offset, length, type) "
        +" VALUES (NULL,?,?,?,?)";

    static final String INSERT_MENTION_SQL = 
        "INSERT INTO mention (mention_id, sentence_id, offset, length, type, text) "
        +" VALUES (NULL,?,?,?,?,?)";


    private Connection mCon;
    private final Properties mDbProperties;

    private final SentenceChunker sentenceChunker;
    private final SentenceModel sentenceModel;
    private final TokenizerFactory tokenizerFactory;

    private final Chunker neChunker;
    private final File genomicsModelfile;

    public static void main(String[] args) throws IOException, 
                                                  SecurityException,
                                                  ClassNotFoundException, 
                                                  InstantiationException, 
                                                  IllegalAccessException, 
                                                  SQLException {

        AnnotateMedlineDb amd = new AnnotateMedlineDb(args[0]);
        amd.openDb();
        Integer[] ids = amd.getCitationIds();
        for (int i = 0; i < ids.length; i++) {
            amd.annotateCitation(ids[i].intValue());
        }
        amd.closeDb();
    }

    AnnotateMedlineDb(String filename)  throws IOException, 
                                               ClassNotFoundException,
                                               SecurityException {
        // load database properties from file
        mDbProperties = new Properties();
        InputStream in = new FileInputStream(filename);
        mDbProperties.load(in); 

        // instantiate chunkers
        tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        sentenceModel  = new IndoEuropeanSentenceModel();
        sentenceChunker = new SentenceChunker(tokenizerFactory,sentenceModel);

        genomicsModelfile  = new File("../../models/ner-en-bio-genia.TokenShapeChunker");
        neChunker = (Chunker) 
            AbstractExternalizable
            .readObject(genomicsModelfile);
    }


    /* open database connection */
    protected void openDb() throws ClassNotFoundException, 
                                   InstantiationException, 
                                   IllegalAccessException,
                                   SQLException {
        // compose database url: jdbc:mysql://<hostname>:<port>/<database>
        String dbUrl = "jdbc:mysql://" 
            + mDbProperties.getProperty("hostname") 
            + ":" 
            + mDbProperties.getProperty("port")
            + "/medline";
        String username = "root";
        String password = "admin";

        // instantiate mysql driver manager, get database connection
        Class.forName("com.mysql.jdbc.Driver").newInstance();
        mCon = DriverManager.getConnection(dbUrl,username,password);
    }

    /* close database connection */
    protected void closeDb() {
        try { 
            mCon.close(); 
        } catch (SQLException se) {
        } 
    }

    protected Integer[] getCitationIds() throws SQLException {
        List<Integer> idList = new ArrayList<Integer>();
        Statement stmt = mCon.createStatement();
        ResultSet rs = stmt.executeQuery(GET_CITATION_IDS_SQL);
        while (rs.next()) {
            idList.add(Integer.valueOf(rs.getInt("citation_id")));
        }
        rs.close();
        stmt.close();
        Integer[] ids = new Integer[idList.size()];
        idList.toArray(ids);
        return ids;
    }

    protected void annotateCitation(int citationId) {
        System.out.println("Annotating citation_id=" + citationId);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String title = null;
        String abstr = null;
        try {
            pstmt = mCon.prepareStatement(GET_CITATION_TEXT_SQL);
            pstmt.setInt(1,citationId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                title = rs.getString("title");
                abstr = rs.getString("abstract");
            }
            rs.close();
            pstmt.close();
            annotateSentences(citationId,"Title",title);
            if (abstr == null) return;
            annotateSentences(citationId,"Abstr",abstr);
        } catch (SQLException se) {
            System.err.println("EXCEPTION HANDLING CITATION=" + se);
        } finally {
            try { rs.close(); } catch (SQLException se) {} 
            try { pstmt.close(); } catch (SQLException se) {} 
        }
    }

    protected void annotateSentences(int citationId, String type, String text) 
        throws SQLException {

        Chunking chunking 
            = sentenceChunker.chunk(text.toCharArray(),0,text.length());
        for (Chunk sentence : chunking.chunkSet()) {
            int start = sentence.start();
            int end = sentence.end();
            int sentenceId = storeSentence(citationId,start,end-start,type);
            annotateMentions(sentenceId,text.substring(start,end));
        }
    }

    protected int storeSentence(int citationId, int offset, int length, String type) 
        throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int sentenceId = -1;
        try { 
            pstmt = mCon.prepareStatement(INSERT_SENTENCE_SQL);
            pstmt.setLong(1,citationId);
            pstmt.setInt(2,offset);
            pstmt.setInt(3,length);
            pstmt.setString(4,type);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.first()) 
                sentenceId = rs.getInt(1);
            return sentenceId;
        } catch (SQLException se) {
            System.err.println("EXCEPTION STORING SENTENCE FROM CITATION_ID="
                               +citationId+": "+ se);
            throw se;
        } finally {
            try { rs.close(); } catch (SQLException se) {} 
            try { pstmt.close(); } catch (SQLException se) {} 
        }
    }

    protected void annotateMentions(int sentenceId, String text) throws SQLException {
        Chunking chunking
            = neChunker.chunk(text.toCharArray(),0,text.length());
        for (Chunk mention : chunking.chunkSet()) {
            int start = mention.start();
            int end = mention.end();
            storeMention(sentenceId,start,mention.type(),text.substring(start,end));
        }
    }

    protected int storeMention(int sentenceId, int offset, String type, String text) 
        throws SQLException {

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int mentionId = -1;
        try { 
            pstmt = mCon.prepareStatement(INSERT_MENTION_SQL);
            pstmt.setLong(1,sentenceId);
            pstmt.setInt(2,offset);
            pstmt.setInt(3,text.length());
            pstmt.setString(4,type);
            pstmt.setString(5,text);
            pstmt.executeUpdate();
            rs = pstmt.getGeneratedKeys();
            if (rs.first()) 
                mentionId = rs.getInt(1);
            return mentionId;
        } catch (SQLException se) {
            System.err.println("EXCEPTION STORING MENTION FROM SENTENCE_ID="
                               +sentenceId+": "+ se);
            throw se;
        } finally {
            try { rs.close(); } catch (SQLException se) {} 
            try { pstmt.close(); } catch (SQLException se) {} 
        }
    }
}
 
