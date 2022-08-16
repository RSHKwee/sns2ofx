package sns2ofx.convertor;

/**
 * Conversion methods for conversion ING- to OFX transaction.
 * 
 * @author René
 *
 */
import java.util.Map;
//import java.util.logging.Logger;

import snsLibrary.SnsTransaction;
import ofxLibrary.OfxTransaction;

public class Sns2OfxTransaction {
//  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

  /**
   * Conversion of an ING transaction to an OFX transaction.
   * 
   * @param a_trans ING transaction
   * @return OFX Transaction
   */
  static public OfxTransaction convertToOfx(SnsTransaction a_trans) {
    OfxTransaction l_ofxtrans = new OfxTransaction();
    l_ofxtrans.setAccount(a_trans.getRekening().replaceAll(" ", ""));
    l_ofxtrans.setTrntype(transType(a_trans.getCode(), a_trans.getAf_Bij()));
    l_ofxtrans.setDtposted(a_trans.getDatum());

    if (a_trans.getAf_Bij().equalsIgnoreCase("Bij")) {
      l_ofxtrans.setTrnamt(a_trans.getBedrag());
    } else {
      l_ofxtrans.setTrnamt("-" + a_trans.getBedrag());
    }

    l_ofxtrans.setName(xmlFriendlyName(a_trans.getOmschrijving()));
    l_ofxtrans.setAccountto(a_trans.getTegenrekening());
    l_ofxtrans.setMemo(xmlFriendlyName(a_trans.getMededelingen()));
    return l_ofxtrans;
  }

  /**
   * Determine the Transaction type.
   * 
   * @param a_code  ING transaction code
   * @param a_afbij Debit or Credit
   * @return OFX Transaction code
   */
  private static String transType(String a_code, String a_afbij) {
    String l_code = "OTHER";
    Map<String, String> codex = Map.of("GT", "PAYMENT", "BA", "POS", "GM", "ATM", "DV", "xx", "OV", "xx", "VZ", "xx",
        "IC", "DIRECTDEBIT", "ST", "DIRECTDEP");

    if (codex.containsKey(a_code)) {
      if (codex.get(a_code).equalsIgnoreCase("xx")) {
        if (a_afbij.equalsIgnoreCase("Bij")) {
          l_code = "CREDIT";
        } else {
          l_code = "DEBIT";
        }
      } else {
        l_code = codex.get(a_code);
      }
    } else {
      l_code = "OTHER";
    }
    return l_code;
  }

  /**
   * Convert & to a HTML / XML friendly format.
   * 
   * @param a_name String
   * @return HTML / XML Friendly string.
   */
  private static String xmlFriendlyName(String a_name) {
    String l_name = a_name;
    l_name = l_name.strip().replaceAll("  ", " ").replace("&", "&amp");
    return l_name;
  }
}
