package sns2ofx.gui;

/**
 * ing2ofx GUI
 */
import logger.MyLogger;
import logger.TextAreaHandler;

import net.miginfocom.swing.MigLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import java.io.File;
import java.io.IOException;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import sns2ofx.main.Main;

import javax.swing.JCheckBoxMenuItem;

import library.UserSetting;
import library.OutputToLoggerReader;

public class GUILayout extends JPanel implements ItemListener {
  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
  private static final long serialVersionUID = 1L;

  // Loglevels: OFF SEVERE WARNING INFO CONFIG FINE FINER FINEST ALL
  static final String[] c_levels = { "OFF", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST", "ALL" };
  static final String[] c_LogToDisk = { "Yes", "No" };

  // Variables
  private String m_LogDir = "c:\\";
  private boolean m_OutputFolderModified = false;
  private JTextArea output;

  // Preferences
  private UserSetting m_param = Main.m_param;

  private File m_GnuCashExecutable = new File("C:\\Program Files (x86)\\gnucash\\bin\\gnucash.exe");
  private File m_OutputFolder;
  private File[] m_CsvFiles;

  private boolean m_toDisk = false;
  private Level m_Level = Level.INFO;
  private boolean m_AcountSeparateOFX = true;
  private boolean m_Interest = true;

  /**
   * Define GUI layout
   * 
   */
  public GUILayout() {
    // GUI items
    JMenuBar menuBar = new JMenuBar();

    JCheckBoxMenuItem chckbxAcountSeparateOFX = new JCheckBoxMenuItem("Accounts in separate OFX files");
    JMenu mnGnuCashExe = new JMenu("GnuCash executable");
    JMenuItem mntmLoglevel = new JMenuItem("Loglevel");

    JCheckBox chckbxInterest = new JCheckBox("Only interest transaction");
    JTextField txtOutputFilename = new JTextField();
    JLabel lblOutputFolder = new JLabel("");
    JButton btnConvert = new JButton("Convert to OFX");

    // Initialize parameters
    m_GnuCashExecutable = new File(m_param.get_GnuCashExecutable());

    if (!m_param.get_OutputFolder().isBlank()) {
      m_OutputFolder = new File(m_param.get_OutputFolder());
      lblOutputFolder.setText(m_OutputFolder.getAbsolutePath());
    }
    if (!(m_param.get_CsvFiles() == null)) {
      m_CsvFiles = m_param.get_CsvFiles();
    }

    m_Level = m_param.get_Level();
    m_toDisk = m_param.is_toDisk();
    m_AcountSeparateOFX = m_param.is_AcountSeparateOFX();
    m_Interest = m_param.is_Interest();
    m_LogDir = m_param.get_LogDir();

    // Define Layout
    setLayout(new BorderLayout(0, 0));
    add(menuBar, BorderLayout.NORTH);

    // Define Setting menu in menubalk:
    JMenu mnSettings = new JMenu("Settings");
    menuBar.add(mnSettings);

    // Checkbox Separate OFX files
    chckbxAcountSeparateOFX.setHorizontalAlignment(SwingConstants.LEFT);
    chckbxAcountSeparateOFX.setSelected(m_AcountSeparateOFX);
    chckbxAcountSeparateOFX.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean selected = chckbxAcountSeparateOFX.isSelected();
        m_AcountSeparateOFX = selected;
        m_param.set_AcountSeparateOFX(selected);
        LOGGER.log(Level.CONFIG, "Accounts in separate OFX files :" + Boolean.toString(selected));
      }
    });
    mnSettings.add(chckbxAcountSeparateOFX);

    // Option Location GnuCash exe
    mnSettings.add(mnGnuCashExe);

    String l_GnuCashExecutable = "Install GnuCash?";
    if (m_GnuCashExecutable.exists()) {
      l_GnuCashExecutable = m_GnuCashExecutable.getAbsolutePath();
    }
    JMenuItem mntmGnuCashExe = new JMenuItem(l_GnuCashExecutable);
    mntmGnuCashExe.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser(m_GnuCashExecutable);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(m_GnuCashExecutable);
        FileFilter filter = new FileNameExtensionFilter("EXE File", "exe");
        fileChooser.setFileFilter(filter);
        int option = fileChooser.showOpenDialog(GUILayout.this);
        if (option == JFileChooser.APPROVE_OPTION) {
          File file = fileChooser.getSelectedFile();
          LOGGER.log(Level.INFO, "GnuCash executable: " + file.getAbsolutePath());
          m_GnuCashExecutable = file;
          m_param.set_GnuCashExecutable(m_GnuCashExecutable);
          mntmGnuCashExe.setText(m_GnuCashExecutable.getAbsolutePath());
        }
      }
    });
    mnGnuCashExe.add(mntmGnuCashExe);

    // Option log level
    mntmLoglevel.setHorizontalAlignment(SwingConstants.LEFT);
    mntmLoglevel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFrame frame = new JFrame("Loglevel");
        String level = "";
        level = (String) JOptionPane.showInputDialog(frame, "Loglevel?", "INFO", JOptionPane.QUESTION_MESSAGE, null,
            c_levels, m_Level.toString());
        if (level != null) {
          m_Level = Level.parse(level.toUpperCase());
          m_param.set_Level(m_Level);
          MyLogger.changeLogLevel(m_Level);
        }
      }
    });
    mnSettings.add(mntmLoglevel);

    // Add item Look and Feel
    JMenu menu = new JMenu("Look and Feel");
    menu.setHorizontalAlignment(SwingConstants.LEFT);
    mnSettings.add(menu);

    // Get all the available look and feel that we are going to use for
    // creating the JMenuItem and assign the action listener to handle
    // the selection of menu item to change the look and feel.
    UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
    for (UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeels) {
      JMenuItem item = new JMenuItem(lookAndFeelInfo.getName());
      item.addActionListener(event -> {
        try {
          // Set the look and feel for the frame and update the UI
          // to use a new selected look and feel.
          UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
          SwingUtilities.updateComponentTreeUI(this);
          m_param.set_LookAndFeel(lookAndFeelInfo.getClassName());
        } catch (Exception e) {
          e.printStackTrace();
        }
      });
      menu.add(item);
    }

    // Option Logging to Disk
    JCheckBoxMenuItem mntmLogToDisk = new JCheckBoxMenuItem("Create logfiles");
    mntmLogToDisk.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean selected = mntmLogToDisk.isSelected();
        if (selected) {
          JFileChooser fileChooser = new JFileChooser();
          fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
          fileChooser.setSelectedFile(new File(m_LogDir));
          int option = fileChooser.showOpenDialog(GUILayout.this);
          if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            LOGGER.log(Level.INFO, "Log folder: " + file.getAbsolutePath());
            m_LogDir = file.getAbsolutePath() + "\\";
            m_param.set_LogDir(m_LogDir);
            m_param.set_toDisk(true);
            m_toDisk = selected;
          }
        } else {
          m_param.set_toDisk(false);
          m_toDisk = selected;
        }
        try {
          MyLogger.setup(m_Level, m_LogDir, m_toDisk);
        } catch (IOException es) {
          LOGGER.log(Level.SEVERE, Class.class.getName() + ": " + es.toString());
          es.printStackTrace();
        }
      }
    });
    mnSettings.add(mntmLogToDisk);

    // Do the layout.
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    add(splitPane);

    JPanel topHalf = new JPanel();
    topHalf.setLayout(new BoxLayout(topHalf, BoxLayout.LINE_AXIS));

    topHalf.setMinimumSize(new Dimension(1, 1));
    topHalf.setPreferredSize(new Dimension(1, 1));
    splitPane.add(topHalf);

    JPanel bottomHalf = new JPanel();
    bottomHalf.setLayout(new BoxLayout(bottomHalf, BoxLayout.X_AXIS));

    // Build output area.
    try {
      MyLogger.setup(m_Level, m_LogDir, m_toDisk);
    } catch (IOException es) {
      LOGGER.log(Level.SEVERE, Class.class.getName() + ": " + es.toString());
      es.printStackTrace();
    }
    Logger rootLogger = Logger.getLogger("");
    for (Handler handler : rootLogger.getHandlers()) {
      if (handler instanceof TextAreaHandler) {
        TextAreaHandler textAreaHandler = (TextAreaHandler) handler;
        output = textAreaHandler.getTextArea();
      }
    }

    output.setEditable(false);
    output.setTabSize(4);
    JScrollPane outputPane = new JScrollPane(output, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    bottomHalf.add(outputPane);

    JPanel panel = new JPanel();
    outputPane.setColumnHeaderView(panel);
    panel.setLayout(new MigLayout("", "[129px][65px,grow][111px,grow][105px]", "[23px][][][][][][][][][][]"));
    outputPane.setColumnHeaderView(panel);
    outputPane.setSize(300, 500);

    panel.setLayout(
        new MigLayout("", "[46px,grow][][grow][205px,grow]", "[23px][23px][23px][23px][23px][23px][][][][]"));

    panel.setMinimumSize(new Dimension(350, 300));
    panel.setPreferredSize(new Dimension(350, 290));

    // Choose CSV File
    JLabel lblCSVFile = new JLabel("Select SNS XML file(s)");
    lblCSVFile.setEnabled(false);
    lblCSVFile.setHorizontalAlignment(SwingConstants.RIGHT);
    panel.add(lblCSVFile, "cell 1 0");

    JButton btnCSVFile = new JButton("XML File");
    btnCSVFile.setHorizontalAlignment(SwingConstants.RIGHT);
    btnCSVFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filter = new FileNameExtensionFilter("XML File", "xml");
        fileChooser.setFileFilter(filter);

        File[] l_files = null;
        m_param.set_CsvFiles(m_CsvFiles);
        // l_files[0] = m_CsvFile;
        fileChooser.setSelectedFiles(l_files);
        fileChooser.setMultiSelectionEnabled(true);

        fileChooser.setSelectedFile(m_CsvFiles[0]);
        int option = fileChooser.showOpenDialog(GUILayout.this);
        if (option == JFileChooser.APPROVE_OPTION) {
          // File file = fileChooser.getSelectedFile();
          File[] files = fileChooser.getSelectedFiles();
          m_CsvFiles = files;
          m_param.set_CsvFiles(m_CsvFiles);

          File file = null;
          String l_filename = "";

          for (int i = 0; i < files.length; i++) {
            file = files[i];
            LOGGER.log(Level.INFO, "XML File: " + file.getAbsolutePath());
            lblCSVFile.setText(file.getAbsolutePath());
            l_filename = l_filename + library.FileUtils.getFileNameWithoutExtension(file) + ".ofx" + "; ";
          }
          txtOutputFilename.setText(l_filename);
          txtOutputFilename.setEnabled(true);

          btnConvert.setEnabled(true);
          lblCSVFile.setEnabled(true);
          if (!m_OutputFolderModified) {
            lblOutputFolder.setText(file.getParent());
            m_OutputFolder = new File(file.getParent());
            m_param.set_OutputFolder(m_OutputFolder);
          }
        }
      }
    });
    panel.add(btnCSVFile, "cell 0 0");

    // Savings transactions
    chckbxInterest.setSelected(m_Interest);
    chckbxInterest.setHorizontalAlignment(SwingConstants.LEFT);
    chckbxInterest.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        boolean selected = chckbxInterest.isSelected();
        m_Interest = selected;
        m_param.set_Interest(m_Interest);
        LOGGER.log(Level.CONFIG, "Save only interest :" + Boolean.toString(selected));
      }
    });
    panel.add(chckbxInterest, "cell 1 1");

    // Define output folder
    // Output folder & filename
    JButton btnOutputFolder = new JButton("Output folder");
    btnOutputFolder.setHorizontalAlignment(SwingConstants.RIGHT);
    btnOutputFolder.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setSelectedFile(new File(lblOutputFolder.getText()));
        int option = fileChooser.showOpenDialog(GUILayout.this);
        if (option == JFileChooser.APPROVE_OPTION) {
          File file = fileChooser.getSelectedFile();
          LOGGER.log(Level.INFO, "Output folder: " + file.getAbsolutePath());
          lblOutputFolder.setText(file.getAbsolutePath());
          m_OutputFolder = new File(file.getAbsolutePath());
          m_param.set_OutputFolder(m_OutputFolder);
          m_OutputFolderModified = true;
        }
      }
    });
    panel.add(btnOutputFolder, "cell 0 3");

    lblOutputFolder.setHorizontalAlignment(SwingConstants.LEFT);
    panel.add(lblOutputFolder, "cell 1 3");

    txtOutputFilename.setHorizontalAlignment(SwingConstants.LEFT);
    txtOutputFilename.setText("Output filename");
    txtOutputFilename.setEnabled(false);
    txtOutputFilename.setColumns(100);
    panel.add(txtOutputFilename, "cell 1 4");

    // Convert button
    btnConvert.setEnabled(false);
    btnConvert.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        m_param.save();
        ActionPerformScript l_action = new ActionPerformScript(m_CsvFiles, lblOutputFolder.getText(),
            chckbxAcountSeparateOFX.isSelected(), chckbxInterest.isSelected());
        l_action.execute();
      }
    });
    panel.add(btnConvert, "cell 1 5");

    JLabel lblNewLabel = new JLabel("    ");
    panel.add(lblNewLabel, "cell 0 6");

    // Start GnuCash
    JButton btnGNUCash = new JButton("Start GnuCash");
    btnGNUCash.setHorizontalAlignment(SwingConstants.RIGHT);
    btnGNUCash.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        LOGGER.log(Level.INFO, "Start GNUCash: " + m_GnuCashExecutable.getAbsolutePath());
        OutputToLoggerReader l_reader = new OutputToLoggerReader();
        try {
          String l_logging = l_reader.getReadOut(m_GnuCashExecutable.getAbsolutePath());
          LOGGER.log(Level.INFO, l_logging);
        } catch (IOException | InterruptedException e1) {
          e1.printStackTrace();
        }
        LOGGER.log(Level.INFO, "Stop GNUCash");
      }
    });
    panel.add(btnGNUCash, "cell 1 7");

    bottomHalf.setMinimumSize(new Dimension(500, 100));
    bottomHalf.setPreferredSize(new Dimension(500, 400));
    splitPane.add(bottomHalf);
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    LOGGER.log(Level.INFO, "itemStateChanged");
  }
}
