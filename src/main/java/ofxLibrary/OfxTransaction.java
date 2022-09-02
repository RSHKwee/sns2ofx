package ofxLibrary;

import java.util.ArrayList;
//import java.util.logging.Logger;

import com.opencsv.bean.CsvToBean;

public class OfxTransaction extends CsvToBean<Object> {
//  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

  private String account = "";
  private String trntype = "";
  private String dtposted = "";
  private String trnamt = "";
  private String fitid = "";
  private String name = "";
  private String accountto = "";
  private String memo = "";
  private int OfxTranPair = -1;

  public String getAccount() {
    return account;
  }

  public String getTrntype() {
    return trntype;
  }

  public String getDtposted() {
    return dtposted;
  }

  public String getTrnamt() {
    return trnamt;
  }

  public String getFitid() {
    return fitid;
  }

  public String getName() {
    return name;
  }

  public String getAccountto() {
    return accountto;
  }

  public String getMemo() {
    return memo;
  }

  public int getOfxTranPair() {
    return OfxTranPair;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public void setTrntype(String trntype) {
    this.trntype = trntype;
  }

  public void setDtposted(String dtposted) {
    this.dtposted = dtposted;
  }

  public void setTrnamt(String trnamt) {
    this.trnamt = trnamt;
  }

  public void setFitid(String fitid) {
    this.fitid = fitid;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setAccountto(String accountto) {
    this.accountto = accountto;
  }

  public void setMemo(String memo) {
    this.memo = memo;
  }

  public void setOfxTranPair(int OfxTranPair) {
    this.OfxTranPair = OfxTranPair;
  }

  public ArrayList<String> OfxXmlTransaction() {
    ArrayList<String> l_regels = new ArrayList<String>();
    l_regels.add("               <STMTTRN>");
    l_regels.add("                  <TRNTYPE>" + trntype + "</TRNTYPE>");
    l_regels.add("                  <DTPOSTED>" + dtposted + "</DTPOSTED>");
    l_regels.add("                  <TRNAMT>" + trnamt + "</TRNAMT>");
    l_regels.add("                  <FITID>" + fitid + "</FITID>");
    l_regels.add("                  <NAME>" + name + "</NAME>");
    l_regels.add("                  <BANKACCTTO>");
    l_regels.add("                     <BANKID></BANKID>");
    l_regels.add("                     <ACCTID>" + accountto + "</ACCTID>");
    l_regels.add("                     <ACCTTYPE>CHECKING</ACCTTYPE>");
    l_regels.add("                  </BANKACCTTO>");
    l_regels.add("                  <MEMO>" + memo + "</MEMO>");
    l_regels.add("               </STMTTRN>");
    return l_regels;
  }

}
