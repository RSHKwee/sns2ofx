package ofxLibrary;

import java.util.logging.Logger;

public class OfxFilter {
  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
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
    bstat = m_FilterAccounts.contains(a_transaction.getAccountto());
    return bstat;
  }

  public boolean filterMemo(OfxTransaction a_transaction) {
    boolean bstat = false;
    bstat = a_transaction.getMemo().contains(m_FilterMemo) || a_transaction.getName().contains(m_FilterMemo);
    return bstat;
  }

  public boolean filter(OfxTransaction a_transaction) {
    boolean bstat = false;
    bstat = filterAccountTo(a_transaction) || filterMemo(a_transaction);
    return bstat;
  }

}
