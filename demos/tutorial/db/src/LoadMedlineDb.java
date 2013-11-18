import com.aliasi.lingmed.medline.parser.Article;
import com.aliasi.lingmed.medline.parser.Abstract;
import com.aliasi.lingmed.medline.parser.MedlineCitation;
import com.aliasi.lingmed.medline.parser.MedlineHandler;
import com.aliasi.lingmed.medline.parser.MedlineParser;

import com.aliasi.util.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import java.util.Properties;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @deprecated Moved to sandbox lingmed project.
 */
@SuppressWarnings("deprecation") @Deprecated
public class LoadMedlineDb {

    

    static MedlineParser PARSER 
        = new MedlineParser(false);

    public static void main(String[] args) 
        throws IOException, SAXException, ClassNotFoundException, 
               InstantiationException, IllegalAccessException, SQLException {
        MedlineDbLoader dbLoader = new MedlineDbLoader(args[0]);
        dbLoader.openDb();
        for (int i = 1; i < args.length; ++i) {
            System.out.println("Indexing file=" + args[i]);
            if (args[i].endsWith(".xml")) 
                loadXML(dbLoader, new File(args[i]));
        }
        dbLoader.closeDb();
    }

    static void loadXML(MedlineDbLoader dbLoader, File file) 
        throws IOException, SAXException {
        String url = file.toURI().toURL().toString();
        InputSource inSource = new InputSource(url);
        PARSER.setHandler(dbLoader);
        PARSER.parse(inSource);
    }

    /**
     * @deprecated see class doc
     */
    @SuppressWarnings("deprecation") @Deprecated
    static class MedlineDbLoader implements MedlineHandler {
        static final String INSERT_CITATION_SQL = 
            "INSERT INTO citation (pubmed_id, title, abstract) VALUES (?,?,?)";

        private final Properties mDbProperties;
        private Connection mCon;

        MedlineDbLoader(String filename) throws IOException { 
            mCon = null;
            mDbProperties = new Properties();
            InputStream in = new FileInputStream(filename);
            mDbProperties.load(in);     
        }

        /**
         * @deprecated see class doc
         */
        @SuppressWarnings("deprecation") @Deprecated
        public void handle(MedlineCitation citation) {
            System.out.println("Handling PMID=" + citation.pmid());
            PreparedStatement pstmt = null;
            try {
                pstmt = mCon.prepareStatement(INSERT_CITATION_SQL);
                pstmt.setString(1,citation.pmid());
                pstmt.setString(2,citation.article().articleTitleText());
                Abstract abstr = citation.article().abstrct();
                if (abstr != null)
                    pstmt.setString(3,abstr.text());
                else 
                    pstmt.setNull(3,Types.VARCHAR);
                pstmt.executeUpdate();
            }
            catch (SQLException se) {
                System.err.println("EXCEPTION HANDLING CITATION="+citation.pmid()+" "+ se);
            }
            finally {
                try { pstmt.close(); } catch (SQLException se) {} 
            }
        }

        // trivial implementation of method delete, (see interface MedlineHandler)
        public void delete(String s) { };

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
            String username = mDbProperties.getProperty("username");
            String password = mDbProperties.getProperty("password");

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

    }
}
