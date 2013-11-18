import com.aliasi.io.FileLineReader;

import com.aliasi.spell.AutoCompleter;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;

import com.aliasi.util.Files;
import com.aliasi.util.ScoredObject;

import javax.swing.SwingUtilities;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import java.util.concurrent.LinkedBlockingQueue;

/*
 * ComboBoxDemo.java uses these additional files:
 *   images/Bird.gif
 *   images/Cat.gif
 *   images/Dog.gif
 *   images/Rabbit.gif
 *   images/Pig.gif
 */
public class AutoCompleteGui implements Runnable {

    private final ActionListener mPopupListener;
    private final JFrame mFrame;
    private final JPanel mContentPane;
    private final JTextField mTextField;
    private final JButton mSubmitButton;
    private JPopupMenu mPopupMenu;


    private final AutoCompleter mCompleter;

    private final AutoCompleteRunner mCompleterRunner;
    private final Thread mAutoCompleteRunnerThread;

    static AutoCompleter autoCompleter(File words, String encoding) throws IOException {
        String[] lines = FileLineReader.readLineArray(words,encoding);
        Map<String,Double> phraseCounter = new HashMap<String,Double>();
        for (String line : lines) {
            int idx = line.lastIndexOf(' ');
            if (idx < 0) continue;
            String phrase = line.substring(0,idx);
            double count = Double.valueOf(line.substring(idx+1));
            phraseCounter.put(phrase,count);
        }
        WeightedEditDistance editDistance
            = new FixedWeightEditDistance(0.0, -10.0, -10.0, -10.0, Double.NEGATIVE_INFINITY);
        int maxResultsPerPrefix = 3;
        int maxSearchQueueSize = 10000;
        double minScore = -50.0;

        return new AutoCompleter(phraseCounter,
                                 editDistance,
                                 maxResultsPerPrefix,
                                 maxSearchQueueSize,
                                 minScore);
    }


    public AutoCompleteGui(File words, String encoding) throws IOException {
        this(autoCompleter(words,encoding),"Enter a U.S. State");
    }

    public AutoCompleteGui(AutoCompleter completer, String query) {
        mCompleter = completer;

        mPopupListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String text = e.getActionCommand(); // just text of menu item
                    mTextField.setText(text);
                    mTextField.grabFocus();
                }
            };

        mTextField = new JTextField();
        mTextField.setFocusCycleRoot(true);
        
        mTextField.addKeyListener(new KeyAdapter() {
                public void keyReleased(KeyEvent e) {
                    mCompleterRunner.complete(); // always running in background
                }
            });
        
        mContentPane = new JPanel(new GridBagLayout());
        mContentPane.setOpaque(true);

        mContentPane.add(new JLabel(query),
                         new GridBagConstraints(0,0,
                                                2,1,
                                                1.0,1.0,
                                                GridBagConstraints.CENTER,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5,5,5,5),
                                                5,5));

        mContentPane.add(mTextField,
                         new GridBagConstraints(0,1,
                                                1,1,
                                                1.0,1.0,
                                                GridBagConstraints.CENTER,
                                                GridBagConstraints.HORIZONTAL,
                                                new Insets(5,5,5,5),
                                                5,5));

        mSubmitButton = new JButton("OK");
        mSubmitButton.setActionCommand("<select>");
        mSubmitButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!"<select>".equals(e.getActionCommand())) return;
                    System.out.println("RESULT TEXT=|" + mTextField.getText() + "|");
                    mAutoCompleteRunnerThread.interrupt();
                    System.exit(0);
                }
            });

        mContentPane.add(mSubmitButton,
                         new GridBagConstraints(1,1,
                                                1,1,
                                                0.0,0.0,
                                                GridBagConstraints.CENTER,
                                                GridBagConstraints.NONE,
                                                new Insets(5,5,5,5),
                                                5,5));

        mFrame = new JFrame("LingPipe Auto Completer Demo");
        mFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mFrame.setContentPane(mContentPane);
        mFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    mAutoCompleteRunnerThread.interrupt();
                }
            });

        mCompleterRunner = new AutoCompleteRunner();
        mAutoCompleteRunnerThread = new Thread(mCompleterRunner);
        mAutoCompleteRunnerThread.start();
    }


    public void run() {
        mFrame.pack();
        mFrame.setSize(300,100);
        mFrame.setVisible(true);
    }


    class AutoCompleteRunner implements Runnable {
        LinkedBlockingQueue<String> mQueue 
            = new LinkedBlockingQueue<String>(100);
        public synchronized void complete() {
            try {
                mQueue.clear(); // remove anything we haven't gotten to yet
                mQueue.put(mTextField.getText()); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        public void run() {
            try {
                run2();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        public void run2() throws InterruptedException {
            String lastTextSuccessfullyCompleted = null; 
            while (!Thread.currentThread().isInterrupted()) { 
                String text = mQueue.take();
                if (text.equals(lastTextSuccessfullyCompleted))
                    continue; // no change since last update
                SortedSet<ScoredObject<String>> completions =
                    mCompleter.complete(text);
                if (!text.equals(mTextField.getText()))
                    continue;  // changed out from under us
                mPopupMenu = new JPopupMenu("title");
                for (ScoredObject<String> completion : completions) {
                    String phrase = completion.getObject();
                    JMenuItem menuItem = new JMenuItem(phrase);
                    menuItem.addActionListener(mPopupListener);
                    mPopupMenu.add(menuItem);
                    mPopupMenu.show(mTextField,
                                    0,mTextField.getHeight());
                    mPopupMenu.pack();
                }
                mTextField.grabFocus();
                lastTextSuccessfullyCompleted = text;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File wordCountFile = new File(args[0]);
        String encoding = "UTF-8";
        SwingUtilities.invokeLater(new AutoCompleteGui(wordCountFile,encoding));
    }


}

