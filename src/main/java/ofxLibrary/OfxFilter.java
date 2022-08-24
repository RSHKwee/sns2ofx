package ofxLibrary;

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

    return l_bstat;
  }

}
