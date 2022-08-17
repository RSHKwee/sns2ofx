package ofxLibrary;

/**
 * Meta information storage for an account and its transactions.
 * <br>
 * Per account the following information is stored:
 * <li>Account name.
 * <li>File prefix for OFX transactions.
 * <li>Period start- and end date.
 * <li>Period end date balance.
 * <br>
 * 
 * @author rshkw
 */
import java.util.logging.Level;
import java.util.logging.Logger;

public class OfxMetaInfo {
  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());
  private String account = "";
  private String prefix = "";
  private int minDate = 999999999;
  private int maxDate = -1;
  private String balanceAfterTransaction = "";

  /**
   * Default constructor
   */
  public OfxMetaInfo() {
  }

  /**
   * Copy constructor
   * 
   * @param a_MetaInfo OFX Meta information
   */
  public OfxMetaInfo(OfxMetaInfo a_MetaInfo) {
    account = a_MetaInfo.getAccount();
    prefix = a_MetaInfo.getPrefix();
    minDate = a_MetaInfo.getIntMinDate();
    maxDate = a_MetaInfo.getIntMaxDate();
    balanceAfterTransaction = a_MetaInfo.getBalanceAfterTransaction();
  }

  /**
   * Get account
   * 
   * @return account
   */
  public String getAccount() {
    return account;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getMinDate() {
    return Integer.toString(minDate);
  }

  public int getIntMinDate() {
    return minDate;
  }

  public String getMaxDate() {
    return Integer.toString(maxDate);
  }

  public int getIntMaxDate() {
    return maxDate;
  }

  public String getBalanceDate() {
    if (balanceAfterTransaction.isBlank()) {
      return "190001010001"; // 1-1-1900 00:01
    } else {
      return Integer.toString(maxDate);
    }
  }

  public String getBalanceAfterTransaction() {
    if (balanceAfterTransaction.isBlank()) {
      return "0";
    } else {
      return balanceAfterTransaction;
    }
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setMinDate(String minDate) {
    int i_minDate = 999999999;
    try {
      i_minDate = Integer.parseInt(minDate);
    } catch (Exception e) {
    }
    if (i_minDate < this.minDate) {
      this.minDate = i_minDate;
    }
  }

  public boolean setMaxDate(String maxDate) {
    boolean bstat = false;
    int i_maxDate = -1;
    try {
      i_maxDate = Integer.parseInt(maxDate);
    } catch (Exception e) {
    }
    if (i_maxDate > this.maxDate) {
      this.maxDate = i_maxDate;
      bstat = true;
    }
    return bstat;
  }

  public void setBalanceAfterTransaction(String balanceAfterTransaction) {
    this.balanceAfterTransaction = balanceAfterTransaction;
  }

  public void printLog() {
    LOGGER.log(Level.INFO, "");
    LOGGER.info("Account           : " + getAccount());
    if (!getPrefix().isEmpty()) {
      LOGGER.info("File prefix       : " + getPrefix());
    }
    LOGGER.info("Period : " + getMinDate() + " til " + getMaxDate());
    LOGGER.info("Balance on end period: €" + getBalanceAfterTransaction());
  }

  @Override
  public String toString() {
    String l_beanStr;
    l_beanStr = String.join(";", getAccount(), getPrefix(), getMinDate(), getMaxDate(), getBalanceAfterTransaction());
    return l_beanStr;
  }
}
