import java.io.*;
import java.util.regex.*;

public class Munge {


    public static void main(String[] args) throws Exception {
	File inDir = new File(args[0]);
	File outDir = new File(args[1]);
	String[] languageDirNames = inDir.list();
	for (int i = 0; i < languageDirNames.length; ++i) {
	     if (languageDirNames[i].startsWith(".")) {
		continue;
	    }
	    Pattern pattern = Pattern.compile("[a-zA-Z]+");
	    Matcher matcher = pattern.matcher(languageDirNames[i]);
	    matcher.find();
	    String language = matcher.group();
	    File langDir = new File(inDir,languageDirNames[i]);
	    String charset = extractCharset(langDir);
	    transcode(language,langDir,charset,outDir);
	}
    }

    static void transcode(String language, File langDir, String charset, 
			  File outDir) 
	throws IOException {

	File inFile = new File(langDir,"sentences.txt");
	FileInputStream fileIn = new FileInputStream(inFile);
	InputStreamReader isReader = new InputStreamReader(fileIn,charset);
	BufferedReader bufReader = new BufferedReader(isReader);
	
	File langOutDir = new File(outDir,language);
	langOutDir.mkdir();
	File outFile = new File(langOutDir,language + ".txt");
	FileOutputStream fileOut = new FileOutputStream(outFile);
	OutputStreamWriter osWriter = new OutputStreamWriter(fileOut,"UTF-8");
	BufferedWriter bufWriter = new BufferedWriter(osWriter);


	System.out.println("\n" + language);
	System.out.println("reading from=" + inFile + " charset=" + charset);
	System.out.println("writing to=" + outFile + " charset=utf-8");
	
	long totalLength = 0L;
	String line;
	while ((line = bufReader.readLine()) != null) {
	    if (line.length() == 0) continue;
	    int index = line.indexOf("\t");
	    String newline = line.substring(index+1);
	    // System.out.println("line=" + line);
	    // System.out.println("New line=" + newline);
	    totalLength += newline.length();
	    bufWriter.write(newline);
	    bufWriter.write(" ");
	}
	System.out.println("total length=" + totalLength);
	
	bufWriter.close();
	bufReader.close();
    }

    static String extractCharset(File dir) throws IOException {
	File metaFile = new File(dir,"meta.txt");
	FileInputStream fileIn = new FileInputStream(metaFile);
	InputStreamReader isReader = new InputStreamReader(fileIn);
	BufferedReader bufReader = new BufferedReader(isReader);
	while (true) {
	    String line = bufReader.readLine();
	    Pattern pattern = Pattern.compile("content encoding\\s+(\\S+)$");
	    Matcher matcher = pattern.matcher(line);
	    if (!matcher.find()) continue;
	    bufReader.close();
	    return matcher.group(1);
	}
    }

}
