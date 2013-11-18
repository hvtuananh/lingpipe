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

package com.aliasi.demo.framework;

import com.aliasi.util.Files;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.beans.PropertyVetoException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import javax.swing.filechooser.FileFilter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>DemoGUI</code> class provides a GUI interface to
 * streaming demos.  Properties are specified with command-line
 * arguments, and text is provided via web form, file upload
 * or directly through a web service.
 *
 * <p>See the superclass documentation and the GUI demo instructions
 * for more information on using this class.
 *
 * @author  Bob Carpenter
 * @version 3.8
 * @since   LingPipe2.3
 */
public class DemoGUI extends AbstractDemoCommand {

    private File mStartFileChooserDirectory;
    private File mInputFile;
    private byte[] mBytesFromFile = new byte[0];

    private final Map<String,JComboBox> mPropertyToComboBox 
        = new HashMap<String,JComboBox>();


    private JFrame mMainFrame;
      private JPanel mContentPanel;
        private JLabel mTitleLabel; 
                private JLabel mAliasILogo;
        private JEditorPane mDescriptionPane;
        private JSplitPane mIOSplitPane;
          private JPanel mInputPanel;
            private JLabel mInputPanelTitle;
            private JButton mSelectFileButton;  
                    private JLabel mInstructionsLabel;
            private JTextArea mInputTextArea;
            private JButton mAnalyzeButton;
            private JPanel mParameterPanel;
          private JPanel mOutputPanel;
            private JLabel mOutputPanelTitle;
            private JEditorPane mOutputHTMLPane;

    /**
     * Construct a GUI demo from the specified command-line arguments.
     *
     * @param args Command-line arguments.
     */
    public DemoGUI(String[] args) {
        super(args,DEFAULT_PROPERTIES);
    }

    /**
     * Creates and launches the GUI.
     */
    public void run() {
        mTitleLabel = new JLabel("<html><font size='8'>" 
                                 + mDemo.title()
                                 + "</font></html>");
        mTitleLabel.setForeground(ALIAS_I_YELLOW);
        
        mDescriptionPane = new JEditorPane();
        mDescriptionPane.setEditable(false);
        mDescriptionPane.setContentType(Constants.TEXT_HTML);
        mDescriptionPane.setText(mDemo.description());
        mDescriptionPane.setBackground(ALIAS_I_DARK_BLUE);
        mDescriptionPane.setBackground(Color.LIGHT_GRAY);
        
        URL imageURL =  null;
        try {
            imageURL 
                = this
                .getClass()
                .getResource(Constants.LOGO_PATH);
        } catch (SecurityException e) {
        }
        if (imageURL != null) {
            ImageIcon icon = new ImageIcon(imageURL);
            mAliasILogo = new JLabel(icon);
        } else {
            mAliasILogo = new JLabel("<html><font size='8'>"
                                     + "Alias-i"
                                     + "</font></html>");
            mAliasILogo.setForeground(ALIAS_I_YELLOW);
        }


        mAnalyzeButton = new JButton("Analyze");
        mAnalyzeButton.setForeground(Color.WHITE);
        mAnalyzeButton.setBackground(ALIAS_I_LIGHT_BLUE);
        mAnalyzeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    processText();
                }
            });

        mInputTextArea = new JTextArea();
        mInputTextArea.setEditable(true);
        // if it gets edited, don't revert under charset change
        mInputTextArea.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent evt) {
                }
                public void removeUpdate(DocumentEvent evt) {
                }
                public void changedUpdate(DocumentEvent evt) {
                }
            });
        new DropTarget(mInputTextArea,
                       DnDConstants.ACTION_COPY,
                       new FileDragListener());






        mInputPanelTitle = new JLabel("<html><font size='6'>Input</font></html>");
        mInputPanelTitle.setForeground(Color.WHITE);

        mSelectFileButton = new JButton("Select File");
        mSelectFileButton.setForeground(Color.WHITE);
        mSelectFileButton.setBackground(ALIAS_I_LIGHT_BLUE);
        mSelectFileButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectFile();
                }
            });

        mInstructionsLabel = new JLabel("Or drag & drop,  or enter text)");
        mInstructionsLabel.setForeground(Color.WHITE);

        mParameterPanel = new JPanel();
        mParameterPanel.setLayout(new GridBagLayout());
        mParameterPanel.setBackground(ALIAS_I_DARK_BLUE);
        
        mInputPanel = new JPanel();
        mInputPanel.setLayout(new GridBagLayout());
        mInputPanel.setBackground(ALIAS_I_DARK_BLUE);
        mInputPanel.add(mInputPanelTitle,
                        new GridBagConstraints(0,0,2,1,0.0,0.0,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5,5,5,5),0,0));
        mInputPanel.add(mSelectFileButton,
                        new GridBagConstraints(0,1,1,1,0.0,0.0,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5,5,5,5),0,0));
        mInputPanel.add(mInstructionsLabel,
                        new GridBagConstraints(1,1,1,1,0.0,0.0,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5,5,5,5),0,0));
        mInputPanel.setPreferredSize(new Dimension(400,300));
        mInputPanel.add(new JScrollPane(mInputTextArea),
                        new GridBagConstraints(0,2,2,1,1.0,1.0,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.BOTH,
                                               new Insets(5,5,5,5),0,0));
        mInputPanel.add(mParameterPanel,
                        new GridBagConstraints(0,3, 2,1, 0.0,0.0,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(5,5,5,5), 0,0));
        
        Map<String,String[]> propertyDeclarations = mDemo.propertyDeclarations();
        Iterator<Map.Entry<String,String[]>> it = propertyDeclarations.entrySet().iterator();
        for (int i = 0; it.hasNext(); ++i) {
            Map.Entry<String,String[]> entry = it.next();
            String key = entry.getKey().toString();
            JLabel label = new JLabel(key);
            label.setForeground(Color.WHITE);

            String[] vals = entry.getValue();
            JComboBox valSelector = new JComboBox(vals);
            valSelector.setEditable(true);
            mPropertyToComboBox.put(key,valSelector);

            mParameterPanel.add(label,
                                new GridBagConstraints(0,i, 1,1, 1.0,0.0,
                                                       GridBagConstraints.EAST,
                                                       GridBagConstraints.NONE,
                                                       new Insets(5,5,5,5),
                                                       0,0));
            mParameterPanel.add(valSelector,
                                new GridBagConstraints(1,i, 1,1, 1.0,0.0,
                                                       GridBagConstraints.WEST,
                                                       GridBagConstraints
                                                       .HORIZONTAL,
                                                       new Insets(5,5,5,5),
                                                       0,0));
        }

        mOutputHTMLPane = new JEditorPane();
        // mOutputHTMLPane.setContentType("text/html"); // not until styling
        mOutputHTMLPane.setEditable(false);

        mOutputPanelTitle = new JLabel("<html><font size='6'>"
                                       + "Output"
                                       + "</font></html>");
        mOutputPanelTitle.setForeground(Color.WHITE);

        mOutputPanel = new JPanel();
        mOutputPanel.setLayout(new GridBagLayout());
        mOutputPanel.setBackground(ALIAS_I_DARK_BLUE);
        mOutputPanel.add(mOutputPanelTitle,
                         new GridBagConstraints(0,0,2,1,0.0,0.0,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5,5,5,5),0,0));
        mOutputPanel.add(mAnalyzeButton,
                         new GridBagConstraints(0,1,1,1,0.0,0.0,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5,5,5,5),0,0));
        JLabel dummyLabel = new JLabel("(or drag & drop; or enter text)");
        dummyLabel.setForeground(ALIAS_I_DARK_BLUE);
        mOutputPanel.add(dummyLabel,
                         new GridBagConstraints(1,1,1,1,0.0,0.0,
                                                GridBagConstraints.WEST,
                                                GridBagConstraints.NONE,
                                                new Insets(5,5,5,5),0,0));

        mOutputHTMLPane.setPreferredSize(new Dimension(400,300));
        mOutputPanel.add(new JScrollPane(mOutputHTMLPane),
                         new GridBagConstraints(0,2,2,1,1.0,1.0,
                                                GridBagConstraints.CENTER,
                                                GridBagConstraints.BOTH,
                                                new Insets(5,5,5,5),0,0));

        mIOSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                      mInputPanel,mOutputPanel);
        mIOSplitPane.setResizeWeight(0.50);
        mIOSplitPane.setDividerLocation(0.50);

        mContentPanel = new JPanel();
        mContentPanel.setBackground(ALIAS_I_DARK_BLUE);
        mContentPanel.setLayout(new GridBagLayout());
        mContentPanel.add(mTitleLabel,
                          new GridBagConstraints(0,0,1,1,1.0,0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints.HORIZONTAL,
                                                 new Insets(5,5,5,5),0,0));
        mContentPanel.add(mAliasILogo,
                          new GridBagConstraints(1,0,1,1,0.0,0.0,
                                                 GridBagConstraints.EAST,
                                                 GridBagConstraints.NONE,
                                                 new Insets(5,5,5,5),0,0));

        mContentPanel.add(mDescriptionPane,
                          new GridBagConstraints(0,1, 2,1, 1.0,0.0,
                                                 GridBagConstraints.WEST,
                                                 GridBagConstraints
                                                 .HORIZONTAL,
                                                 new Insets(5,5,5,5), 0,0));
        mContentPanel.add(mIOSplitPane,
                          new GridBagConstraints(0,2, 2,1,1.0,1.0,
                                                 GridBagConstraints.CENTER,
                                                 GridBagConstraints.BOTH,
                                                 new Insets(5,5,5,5),0,0));


        mMainFrame = new JFrame(mDemo.title());
        mMainFrame.setContentPane(mContentPanel);
        mMainFrame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
        mMainFrame.setSize(768,1024);
        mMainFrame.setResizable(true);

        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    mMainFrame.setVisible(true);
                    mMainFrame.pack();
                }
            });
    }

    private void processText() {
        String text = mInputTextArea.getText();
        byte[] bytes = null;
        String inputCharset = inputCharset();
        try {
            bytes = text.getBytes(inputCharset);
            // System.out.println("bytes to string=" + new String(bytes,inputCharset));
        } catch (UnsupportedEncodingException e) {
            String msg ="Unsupported Input Encoding=" + inputCharset + '\n'
                + "Please select a supported encoding and try again.";
            errorDialog(msg,"Unsupported input encoding");
            return;
        }
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Properties properties = new Properties();
        Iterator<String> it = mPropertyToComboBox.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String val = getProperty(key);
            if (val == null) continue;
            properties.setProperty(key,val);
        }

        // System.out.println("Properties=" + properties);

        try {
            mDemo.process(in,out,properties);
        } catch (IOException e) {
            String msg = "IOException processing demo=" + e;
            errorDialog(msg,"IOException in Demo");
            return;
        }

        String outputCharset = outputCharset();
        try {
            String result = new String(out.toByteArray(),outputCharset);
            // System.out.println("result=" + result);
            mOutputHTMLPane.setText(result);
        } catch (UnsupportedEncodingException e) {
            String msg = "Unsupported Output Encoding=" + outputCharset + '\n'
                + "Please select a supported output encoding.";
            errorDialog(msg,"Unrecognized output charset");
            return;
        }
    }

    private void errorDialog(String msg, String title) {
        System.out.println("ERROR: " + msg);
        JOptionPane.showMessageDialog(mMainFrame,msg,title,
                                      JOptionPane.ERROR_MESSAGE);
    }

    private String outputCharset() {
        return getProperty(Constants.OUTPUT_CHAR_ENCODING_PARAM);
    }

    private String inputCharset() {
        return getProperty(Constants.INPUT_CHAR_ENCODING_PARAM);
    }

    private String getProperty(String key) {
        JComboBox comboBox 
            = mPropertyToComboBox.get(key);
        if (comboBox == null) return null;
        Object selectedItem = comboBox.getSelectedItem();
        if (selectedItem == null) return null;
        return selectedItem.toString();
    }
    
    private void selectFile() {
        JFileChooser chooser = new JFileChooser(mStartFileChooserDirectory);
        int returnVal = chooser.showOpenDialog(mMainFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.exists()) return;
            mStartFileChooserDirectory = file.getParentFile();
            loadFile(file);
        }
    }

    private void loadFile(File file) {
        try {
            mInputFile = file;
            mBytesFromFile = Files.readBytesFromFile(file);
        } catch (IOException e) {
            System.out.println("IOException=" + e);
            mInputTextArea.setText("Could not read file=" + file + '\n'
                                   + "Try changing the character set.\n"
                                   + "\nException=" + e);

            return;
        }
        loadBytesWithCurrentCharset();
    }

    private void loadBytesWithCurrentCharset() {
        String inputCharset = inputCharset();
        try {
            if (mBytesFromFile == null) {
                System.out.println("No file loaded to switch bytes with."
                                   + " Current text may have been edited.");
                return;
            }
            String text = new String(mBytesFromFile,inputCharset);
            if (mBytesFromFile.length > 0 && text.length() == 0) {
                return;
            }
            mInputTextArea.setText(text);
        } catch (UnsupportedEncodingException e) {
            String msg = "Unsupported input character set=" 
                + inputCharset + "\n"
                + "File not loaded.\n"
                + "Please select a supported input character set.";
            errorDialog(msg,"Unrecognized input character set");
        }
    }

    private class FileDragListener implements DropTargetListener {
        public void dragEnter(DropTargetDragEvent dtde) {
            if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                return;
        }
        public void dragOver(DropTargetDragEvent dtde) {
        }
        public void dragExit(DropTargetEvent dte) {
        }
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }
        public void drop(DropTargetDropEvent dtde) {
            if (!dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
                return;
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            Transferable t = dtde.getTransferable();
            List<File> fileList = null;
            try {
                @SuppressWarnings("unchecked")
                List<File> list
                    = (List<File>) t.getTransferData(DataFlavor
                                                     .javaFileListFlavor);
                fileList = list;
            } catch (UnsupportedFlavorException e) {
                return;
            } catch (IOException e) {
                return;
            }
            if (fileList.size() < 1) return;
            if (fileList.size() > 1) {
                System.out.println("Only reading first file"
                                   + "from list of length=" 
                                   + fileList.size());
            }
            for (File file : fileList)
                loadFile(file);
            dtde.dropComplete(true);
        }
    }

    /**
     * Provides the means to launch the GUI demo from the command line.
     */
    public static void main(String[] args) {
        new DemoGUI(args).run();
    }



    private static final Color ALIAS_I_DARK_BLUE
        = new Color(0x272651);
    private static final Color ALIAS_I_YELLOW
        = new Color(0xF7EB00);
    private static final Color ALIAS_I_LIGHT_BLUE
        = new Color(0x306AB1);

    private static final Properties DEFAULT_PROPERTIES
        = new Properties();
    

}
