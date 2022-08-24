package ofxLibrary;

import java.math.BigDecimal;

public class OfxFilter {
  private String account = "";
  private String trntype = "";
  private String dtposted = "";
  private String trnamt = "";
  private String fitid = "";
  private String name = "";
  private String accountto = "";
  private String memo = "";

  public boolean CheckTrans(OfxTransaction a_tran1, OfxTransaction a_tran2) {
    boolean l_bstat = false;
    l_bstat = (a_tran1.getAccount() == a_tran2.getAccountto()) && (a_tran1.getDtposted() == a_tran2.getDtposted());

    BigDecimal bd1 = new BigDecimal(a_tran1.getTrnamt());
    BigDecimal bd2 = new BigDecimal(a_tran2.getTrnamt());
    BigDecimal bdDiff = bd1.add(bd2);
    l_bstat = l_bstat && (bdDiff.compareTo(BigDecimal.ZERO) == 0);

    return l_bstat;
  }

}
