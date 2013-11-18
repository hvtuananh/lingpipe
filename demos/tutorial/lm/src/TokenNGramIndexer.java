import com.aliasi.corpus.ObjectHandler;

import com.aliasi.lm.TokenizedLM;
import com.aliasi.lm.UniformBoundaryLM;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Strings;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TokenNGramIndexer
    implements ObjectHandler<CharSequence> {

    private final TokenizerFactory mTokenizerFactory;
    private final int mNGramOrder;
    private final int mMaxCharsBuffered;
    private final int mMaxFilesPerLevel;
    private final File mIndexDir;
    private final String mEncoding;


    private int mCharsBuffered;
    private TokenizedLM mLm;
    private List<Exception> mExceptions;

    public TokenNGramIndexer(int maxCharsBuffered,
                             int maxFilesPerLevel,
                             TokenizerFactory tokenizerFactory,
                             int nGramOrder,
                             File indexDir,
                             String encoding) {
        if (maxCharsBuffered < 1) {
            String msg = "Require at least one character buffered."
                + " Found maxCharsBuffered=" + maxCharsBuffered;
            throw new IllegalArgumentException(msg);
        }
        mMaxCharsBuffered = maxCharsBuffered;

        if (maxFilesPerLevel < 1) {
            String msg = "Require at least one file per level."
                + " Found maxFilesPerLevel=" + maxFilesPerLevel;
            throw new IllegalArgumentException(msg);
        }
        mMaxFilesPerLevel = maxFilesPerLevel;

        if (nGramOrder < 1) {
            String msg = "Require n-gram order at least 1."
                + " Found nGramOrder=" + nGramOrder;
            throw new IllegalArgumentException(msg);
        }
        mNGramOrder = nGramOrder;

        indexDir.delete();
        indexDir.mkdirs();
        if (!indexDir.isDirectory()) {
            String msg = "Index directory must be directory."
                + " Found path=" + indexDir;
            throw new IllegalArgumentException(msg);
        }
        mIndexDir = indexDir;

        mTokenizerFactory = tokenizerFactory;
        mEncoding = encoding;
        clearExceptions();
        newLm();
    }

    public void handle(CharSequence cs) {
        int len = cs.length();
        if (len > mMaxCharsBuffered) {
            String msg = "Sequence too long."
                + " maxCharsBuffered=" + mMaxCharsBuffered
                + " sequence length len=" + len;
            throw new IllegalArgumentException(msg);
        }
        if (len + mCharsBuffered > mMaxCharsBuffered) {
            write();
            merge();
            mCharsBuffered = 0;
        }
        mLm.handle(cs);
        mCharsBuffered += len;
    }

    public void close() {
        write();
        merge();
    }

    public List<Exception> clearExceptions() {
        List<Exception> exceptions = mExceptions;
        mExceptions = new ArrayList<Exception>();
        return exceptions;
    }


    void write() {
        List<File> levelOneFiles = levelFiles(1);
        String fileName = "1_" + (levelOneFiles.size() + 1);
        File file = new File(mIndexDir,fileName);
        levelOneFiles.add(file);
        try {
            TokenNGramFiles.writeNGrams(mLm,file,0,mNGramOrder,0,mEncoding);
        } catch (IOException e) {
            mExceptions.add(e);
        }
        newLm();
    }

    List<File> levelFiles(int level) {
        String prefix = level + "_";
        List<File> files = new ArrayList<File>();
        for (File file : mIndexDir.listFiles())
            if (file.getName().startsWith(prefix))
                files.add(file);
        return files;
    }

    public void optimize(int minCount) {
        File[] files = mIndexDir.listFiles();
        File mergedFile = nextFile(files);
        try {
            TokenNGramFiles.merge(Arrays.asList(files),mergedFile,
                                  mEncoding,minCount,true);
        } catch (IOException e) {
            mExceptions.add(e);
        }
    }

    File nextFile(File[] files) {
        String nextName = "";
        for (File file : files)
            if (file.getName().compareTo(nextName) > 0)
                nextName = file.getName();
        int idx = nextName.indexOf('_');
        int level = Integer.parseInt(nextName.substring(0,idx));
        return new File(mIndexDir,
                        (level+1) + "_" + 1);
    }



    void merge() {
        List<File> files = levelFiles(1);
        if (files.size() < mMaxFilesPerLevel) return;
        int level = 2;
        List<File> topLevelFiles = null;
        while (true) {
            topLevelFiles = levelFiles(level);
            if (topLevelFiles.size() < mMaxFilesPerLevel)
                break;
            files.addAll(topLevelFiles);
            ++level;
        }
        String fileName = level + "_" + (topLevelFiles.size()+1);
        File mergedFile = new File(mIndexDir,fileName);
        // System.out.println("Merge to " + mergedFile);
        // System.out.println("      from " + files);
        try {
            TokenNGramFiles.merge(files,mergedFile,mEncoding,0,true);
        } catch (IOException e) {
            mExceptions.add(e);
        }
    }

    void newLm() {
        mLm = new TokenizedLM(mTokenizerFactory,
                              mNGramOrder,
                              new UniformBoundaryLM(),
                              new UniformBoundaryLM(),
                              mNGramOrder,
                              false);
    }



}