package ofxLibrary;

// import java.util.logging.Logger;

public class OfxFilter {
  // private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
  private String m_FilterAccounts;
  private String m_FilterMemo = "";

  public OfxFilter() {
    m_FilterAccounts = "NL30INGB0702367362;NL52INGB0003157113;NL65INGB0001845439";
  }

  public OfxFilter(String a_FilterMemo) {
    m_FilterMemo = a_FilterMemo;
  }

  public OfxFilter(String a_FilterMemo, String a_FilterAccounts) {
    m_FilterMemo = a_FilterMemo;
    m_FilterAccounts = a_FilterAccounts;
  }

  public boolean filterAccountTo(OfxTransaction a_transaction) {
    boolean bstat = false;
    if (!m_FilterAccounts.isBlank() && !a_transaction.getAccountto().isBlank()) {
      bstat = m_FilterAccounts.toUpperCase().contains(a_transaction.getAccountto().toUpperCase());
    }
    return bstat;
  }

  public boolean filterMemo(OfxTransaction a_transaction) {
    boolean bstat = false;
    if (!m_FilterMemo.isBlank()) {
      if (!a_transaction.getMemo().isBlank()) {
        bstat = a_transaction.getMemo().toUpperCase().contains(m_FilterMemo.toUpperCase());
      }
      if (!a_transaction.getName().isBlank()) {
        bstat = bstat || a_transaction.getName().toUpperCase().contains(m_FilterMemo.toUpperCase());

      }
    }
    return bstat;
  }

  public boolean filter(OfxTransaction a_transaction) {
    boolean bstat = false;
    bstat = filterAccountTo(a_transaction) || filterMemo(a_transaction);
    return bstat;
  }

}
