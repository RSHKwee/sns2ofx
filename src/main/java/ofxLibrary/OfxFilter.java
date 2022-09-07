package ofxLibrary;

// import java.util.logging.Logger;

public class OfxFilter {
  // private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
  private String m_FilterAccounts = "";
  private String m_FilterMemo = "";

  public OfxFilter(String a_FilterMemo) {
    m_FilterMemo = a_FilterMemo;
  }

  public OfxFilter(String a_FilterMemo, String a_FilterAccounts) {
    m_FilterMemo = a_FilterMemo;
    m_FilterAccounts = a_FilterAccounts;
  }

  public boolean filterAccountTo(OfxTransaction a_transaction) {
    boolean bstat = false;
    if (!m_FilterAccounts.isBlank()) {
      bstat = m_FilterAccounts.toUpperCase().contains(a_transaction.getAccountto().toUpperCase());
    }
    return bstat;
  }

  public boolean filterMemo(OfxTransaction a_transaction) {
    boolean bstat = false;
    if (!m_FilterMemo.isBlank()) {
      bstat = a_transaction.getMemo().toUpperCase().contains(m_FilterMemo.toUpperCase())
          || a_transaction.getName().toUpperCase().contains(m_FilterMemo.toUpperCase());
    }
    return bstat;
  }

  public boolean filter(OfxTransaction a_transaction) {
    boolean bstat = false;
    bstat = filterAccountTo(a_transaction) || filterMemo(a_transaction);
    return bstat;
  }

}
