package ofxLibrary;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OfxFilter {
  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
  private List<OfxTransaction> m_OfxTransactions = new LinkedList<OfxTransaction>();

  public OfxFilter(List<OfxTransaction> a_OfxTransactions) {
    m_OfxTransactions = a_OfxTransactions;
  }

  /**
   * Check if transactions are "partners"
   * 
   * Account from is Account to, Transaction dates are the same, Sum of amounts is
   * zero.
   * 
   * @param a_tran1
   * @param a_tran2
   * @return
   */
  private boolean CheckTrans(OfxTransaction a_tran1, OfxTransaction a_tran2) {
    /*
     * private String account = ""; private String trntype = ""; private String
     * dtposted = ""; private String trnamt = ""; private String fitid = ""; private
     * String name = ""; private String accountto = ""; private String memo = "";
     */
    boolean l_bstat = false;
    if ((null == a_tran2.getOfxTranPair()) && (null == a_tran1.getOfxTranPair())) {
      l_bstat = (a_tran1.getAccount().equalsIgnoreCase(a_tran2.getAccountto()));
      if (l_bstat) {
        LOGGER.log(Level.FINEST, "Account.");
      }
      l_bstat = l_bstat && (a_tran1.getDtposted().equalsIgnoreCase(a_tran2.getDtposted()));

      BigDecimal bd1 = new BigDecimal(a_tran1.getTrnamt());
      BigDecimal bd2 = new BigDecimal(a_tran2.getTrnamt());
      BigDecimal bdDiff = bd1.add(bd2);
      l_bstat = l_bstat && (bdDiff.compareTo(BigDecimal.ZERO) == 0);
    }
    return l_bstat;
  }

  public List<OfxTransaction> Filter() {
    for (int i = 0; i < m_OfxTransactions.size(); i++) {
      OfxTransaction l_OfxTransaction1 = m_OfxTransactions.get(i);
      boolean bstat = false;
      int j = i;
      while (!bstat && j < m_OfxTransactions.size()) {
        OfxTransaction l_OfxTransaction2 = m_OfxTransactions.get(j);
        bstat = CheckTrans(l_OfxTransaction1, l_OfxTransaction2);
        if (bstat) {
          String fitid1 = l_OfxTransaction1.getFitid();
          l_OfxTransaction2.setFitid(fitid1);

          l_OfxTransaction2.setOfxTranPair(l_OfxTransaction1);
          m_OfxTransactions.set(j, l_OfxTransaction2);

          l_OfxTransaction1.setOfxTranPair(l_OfxTransaction2);
          m_OfxTransactions.set(i, l_OfxTransaction1);
          LOGGER.log(Level.INFO, "Fit id adjusted.");
        }
        j++;
      }
    }
    return m_OfxTransactions;
  }

}
