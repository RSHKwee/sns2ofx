package sns2ofx.convertor;

/**
 * OFX Transactions handling.
 * 
 * @author René
 *
 */
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import ofxLibrary.OfxFilter;
import ofxLibrary.OfxMetaInfo;
import ofxLibrary.OfxTransaction;

public class OfxTransactions {
  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
  private String m_BankCode = "";
  private OfxFilter m_OfxFilter = new OfxFilter();

  private List<OfxTransaction> m_OfxTransactions = new LinkedList<OfxTransaction>();

  public Map<String, OfxMetaInfo> m_metainfo = new HashMap<String, OfxMetaInfo>();
  public Map<String, ArrayList<String>> m_OfxAcounts = new LinkedHashMap<String, ArrayList<String>>();

  private File m_file;

  /**
   * Constructor
   * 
   * @param a_file CSV file with ING transactions.
   */
  public OfxTransactions(File a_file, String a_BankCode) {
    m_file = a_file;
    m_BankCode = a_BankCode;
  }

  public void setOfxFilter(OfxFilter a_OfxFilter) {
    m_OfxFilter = a_OfxFilter;
  }

  /**
   * Load SNS Transactions and initialize OFX Transactions and meta information.
   */
  public void load() {
    SnsTransactions l_transactions = new SnsTransactions(m_file);
    l_transactions.load();
    m_OfxTransactions = l_transactions.getOfxTransactions();
    m_metainfo = l_transactions.getOfxMetaInfo();
  }

  /**
   * Get the OFX meta information of all processed accounts.
   * 
   * @return OFX meta information of all accounts.
   */
  public Map<String, OfxMetaInfo> getOfxMetaInfo() {
    return m_metainfo;
  }

  /**
   * Return Account information of the processed transactions.
   * 
   * @return Account information
   */
  public Map<String, ArrayList<String>> getAccountTransactions() {
    return m_OfxAcounts;
  }

  /**
   * OFX XML header for OFX transactions of an account and certain period.
   * 
   * @param account Account
   * @param mindate Start date of period
   * @param maxdate End date of period
   * @return List of lines with the XML content for a header
   */
  private ArrayList<String> OfxXmlTransactionsHeader(String account, String mindate, String maxdate) {
    ArrayList<String> l_regels = new ArrayList<String>();
    l_regels.add("      <STMTRS>                            <!-- Begin statement response -->");
    l_regels.add("         <CURDEF>EUR</CURDEF>");
    l_regels.add("         <BANKACCTFROM>                   <!-- Identify the account -->");
    l_regels.add("            <BANKID>" + m_BankCode + "</BANKID>     <!-- Routing transit or other FI ID -->");
    l_regels.add("            <ACCTID>" + account + "</ACCTID>  <!-- Account number -->");
    l_regels.add("            <ACCTTYPE>CHECKING</ACCTTYPE> <!-- Account type -->");
    l_regels.add("         </BANKACCTFROM>                  <!-- End of account ID -->");
    l_regels.add("         <BANKTRANLIST>                   <!-- Begin list of statement trans. -->");
    l_regels.add("            <DTSTART>" + mindate + "</DTSTART>");
    l_regels.add("            <DTEND>" + maxdate + "</DTEND>");
    return l_regels;
  }

  /**
   * OFX XML footer for OFX transactions of an account and certain period.
   * 
   * @param saldonatran Balance at end of period
   * @param maxdate     End date period
   * @return List of lines with the XML content for a footer
   */
  private ArrayList<String> OfxXmlTransactionsFooter(String saldonatran, String maxdate) {
    ArrayList<String> l_regels = new ArrayList<String>();
    l_regels.add("         </BANKTRANLIST>                   <!-- End list of statement trans. -->");
    l_regels.add("         <LEDGERBAL>                       <!-- Ledger balance aggregate -->");
    l_regels.add("            <BALAMT>" + saldonatran + "</BALAMT>");
    l_regels.add(
        "            <DTASOF>" + maxdate + "2359</DTASOF>  <!-- Bal date: Last date in transactions, 11:59 pm -->");
    l_regels.add("         </LEDGERBAL>                      <!-- End ledger balance -->");
    l_regels.add("      </STMTRS>");
    return l_regels;
  }

  /**
   * 
   */
  public void OfxXmlTransactionsForAccounts() {
    OfxXmlTransactionsForAccounts(false, "");
  }

  /**
   * 
   * @param a_FilterName
   */
  public void OfxXmlTransactionsForAccounts(String a_FilterName) {
    OfxXmlTransactionsForAccounts(false, a_FilterName);
  }

  /**
   * 
   * @param a_AllInOne
   */
  public void OfxXmlTransactionsForAccounts(boolean a_AllInOne) {
    OfxXmlTransactionsForAccounts(a_AllInOne, "");
  }

  /**
   * 
   * @param a_AllInOne
   * @param a_FilterName
   */
  int m_NumberOfTransactions = 0;

  public void OfxXmlTransactionsForAccounts(boolean a_AllInOne, String a_FilterName) {
    Set<String> accounts = m_metainfo.keySet();
    accounts.forEach(account -> {
      OfxMetaInfo l_metainfo = m_metainfo.get(account);
      ArrayList<String> l_regelshead = new ArrayList<String>();
      l_regelshead = OfxXmlTransactionsHeader(account, l_metainfo.getMinDate(), l_metainfo.getMaxDate());
      m_OfxAcounts.put(account, l_regelshead);
      m_NumberOfTransactions = 0;

      LOGGER.log(Level.INFO, "");
      LOGGER.log(Level.INFO, "Process account:           " + account);
      m_OfxTransactions.forEach(transaction -> {
        if (a_AllInOne || (transaction.getAccount().equalsIgnoreCase(account))) {
          ArrayList<String> l_regelstrans = new ArrayList<String>();
          if (!m_OfxFilter.filter(transaction)) {
            l_regelstrans = transaction.OfxXmlTransaction();
            ArrayList<String> prevregels = m_OfxAcounts.get(account);
            prevregels.addAll(l_regelstrans);
            m_OfxAcounts.put(account, prevregels);
            m_NumberOfTransactions++;
          }
        }
      });

      ArrayList<String> l_regelsfoot = new ArrayList<String>();
      l_regelsfoot = OfxXmlTransactionsFooter(l_metainfo.getBalanceAfterTransaction(), l_metainfo.getBalanceDate());

      ArrayList<String> prevregels = m_OfxAcounts.get(account);
      prevregels.addAll(l_regelsfoot);
      m_OfxAcounts.put(account, prevregels);
      LOGGER.log(Level.INFO, "Transactions processed: " + Integer.toString(m_NumberOfTransactions));
    });
    LOGGER.log(Level.FINE, "");
  }
}
