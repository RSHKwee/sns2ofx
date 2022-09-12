package ofxLibrary;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import sns2ofx.convertor.OfxTransactions;

public class OfxDocument {
  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

  private Map<String, ArrayList<String>> m_OfxAcounts = new LinkedHashMap<String, ArrayList<String>>();
  private Map<String, OfxMetaInfo> m_metainfo = new HashMap<String, OfxMetaInfo>();

  private String m_OutputDir = "";
  private boolean m_separateOFX = true;
  private File m_File;
  private String m_FilterName = "";
  private String m_BankCode = "";

  public OfxDocument(String a_BankCode, File a_File, String a_outputdir, boolean a_separateOfx) {
    m_BankCode = a_BankCode;
    m_File = a_File;
    m_OutputDir = a_outputdir;
    m_separateOFX = a_separateOfx;
  }

  public OfxDocument(String a_BankCode, File a_File, boolean a_separateOfx) {
    m_BankCode = a_BankCode;
    m_File = a_File;
    m_OutputDir = a_File.getParent();
    m_separateOFX = a_separateOfx;
  }

  public OfxDocument(String a_BankCode, File a_File, String a_outputdir) {
    m_BankCode = a_BankCode;
    m_File = a_File;
    m_OutputDir = a_outputdir;
    m_separateOFX = true;
  }

  public OfxDocument(String a_BankCode, File a_File) {
    m_BankCode = a_BankCode;
    m_File = a_File;
    m_OutputDir = a_File.getParent();
    m_separateOFX = true;
  }

  public void load() {
    OfxTransactions l_OfxTrans = new OfxTransactions(m_File, m_BankCode);
    l_OfxTrans.load();
    l_OfxTrans.OfxXmlTransactionsForAccounts();
    m_OfxAcounts = l_OfxTrans.m_OfxAcounts;
    m_metainfo = l_OfxTrans.m_metainfo;
  }

  public void load(String a_FilterName) {
    OfxTransactions l_OfxTrans = new OfxTransactions(m_File, m_BankCode);
    l_OfxTrans.load();
    l_OfxTrans.OfxXmlTransactionsForAccounts(a_FilterName);
    m_OfxAcounts = l_OfxTrans.m_OfxAcounts;
    m_metainfo = l_OfxTrans.m_metainfo;
    m_FilterName = a_FilterName;
  }

  public void load(boolean a_AllInOne, String a_FilterName) {
    OfxTransactions l_OfxTrans = new OfxTransactions(m_File, m_BankCode);
    l_OfxTrans.load();
    l_OfxTrans.OfxXmlTransactionsForAccounts(a_AllInOne, a_FilterName);
    m_OfxAcounts = l_OfxTrans.m_OfxAcounts;
    m_metainfo = l_OfxTrans.m_metainfo;
    m_FilterName = a_FilterName;
  }

  private ArrayList<String> OfxXmlHeader() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
    LocalDateTime now = LocalDateTime.now();
    String datestr = dtf.format(now);

    ArrayList<String> l_regels = new ArrayList<String>();
    l_regels.add("<OFX>");
    l_regels.add("  <SIGNONMSGSRSV1>");
    l_regels.add("    <SONRS>                           <!-- Begin signon -->");
    l_regels.add("      <STATUS>                        <!-- Begin status aggregate -->");
    l_regels.add("         <CODE>0</CODE>               <!-- OK -->");
    l_regels.add("         <SEVERITY>INFO</SEVERITY>");
    l_regels.add("      </STATUS>");
    l_regels.add("      <DTSERVER>" + datestr + "</DTSERVER>  <!-- Oct. 29, 1999, 10:10:03 am -->");
    l_regels.add("      <LANGUAGE>ENG</LANGUAGE>            <!-- Language used in response -->");
    l_regels.add("      <DTPROFUP>" + datestr + "</DTPROFUP>  <!-- Last update to profile-->");
    l_regels.add("      <DTACCTUP>" + datestr + "</DTACCTUP>  <!-- Last account update -->");
    l_regels.add("      <FI>                            <!-- ID of receiving institution -->");
    l_regels.add("         <ORG>NCH</ORG>               <!-- Name of ID owner -->");
    l_regels.add("         <FID>1001</FID>              <!-- Actual ID -->");
    l_regels.add("      </FI>");
    l_regels.add("    </SONRS>                          <!-- End of signon -->");
    l_regels.add("  </SIGNONMSGSRSV1>");
    l_regels.add("  <BANKMSGSRSV1>");
    l_regels.add("   <STMTTRNRS>                        <!-- Begin response -->");
    l_regels.add("      <TRNUID>1001</TRNUID>           <!-- Client ID sent in request -->");
    l_regels.add("      <STATUS>                     <!-- Start status aggregate -->");
    l_regels.add("         <CODE>0</CODE>            <!-- OK -->");
    l_regels.add("         <SEVERITY>INFO</SEVERITY>");
    l_regels.add("      </STATUS>");
    return l_regels;
  }

  private ArrayList<String> OfxXmlFooter() {
    ArrayList<String> l_regels = new ArrayList<String>();
    l_regels.add("   </STMTTRNRS>                        <!-- End of accounts -->");
    l_regels.add("  </BANKMSGSRSV1>");
    l_regels.add("</OFX>");
    return l_regels;
  }

  ArrayList<String> m_regels = new ArrayList<String>();
  String m_Filename = "";

  public void CreateOfxDocument() {
    CreateOfxDocument("");
  }

  public void CreateOfxDocument(String a_FileName) {
    m_Filename = a_FileName;
    if (m_Filename.isBlank()) {
      m_Filename = library.FileUtils.getFileNameWithoutExtension(m_File.getName()) + ".ofx";
    }

    Set<String> accounts = m_OfxAcounts.keySet();
    if (m_separateOFX) {
      accounts.forEach(account -> {
        ArrayList<String> l_regels = new ArrayList<String>();
        l_regels = OfxXmlHeader();
        l_regels.addAll(m_OfxAcounts.get(account));
        l_regels.addAll(OfxXmlFooter());

        // Construct filename
        OfxMetaInfo l_info = m_metainfo.get(account);
        String l_prefix = l_info.getPrefix();
        String l_filename = "";
        if (!l_prefix.isBlank()) {
          l_filename = m_OutputDir + "\\" + String.join("_", l_prefix, account);
          if (!m_FilterName.isBlank()) {
            l_filename = String.join("_", l_filename, m_FilterName);
          }
          l_filename = String.join("_", l_filename, m_Filename);
        } else {
          l_filename = m_OutputDir + "\\" + String.join("_", account, m_Filename);
        }
        l_info.printLog();

        LOGGER.log(Level.INFO, "Create OFX file " + l_filename);
        library.TxtBestand.DumpXmlBestand(l_filename, l_regels);
      });
    } else {
      m_regels.clear();
      m_regels = OfxXmlHeader();
      accounts.forEach(account -> {
        OfxMetaInfo l_info = m_metainfo.get(account);
        m_regels.addAll(m_OfxAcounts.get(account));
        l_info.printLog();
      });
      m_regels.addAll(OfxXmlFooter());
      LOGGER.log(Level.INFO, "Create OFX file " + m_Filename);
      library.TxtBestand.DumpXmlBestand(m_OutputDir + "\\" + m_Filename, m_regels);
    }
  }
}
