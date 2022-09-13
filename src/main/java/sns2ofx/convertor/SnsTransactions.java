package sns2ofx.convertor;

/**
 * Convert SNS transactions to OFX transactions.
 * 
 * @author René
 *
 */
import java.io.File;
import java.io.FileInputStream;
// import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import camt053parser.Camt053Parser;
import camt053parser.model.AccountStatement2;
import camt053parser.model.CashBalance3;
import camt053parser.model.CreditDebitCode;
import camt053parser.model.Document;
import camt053parser.model.EntryDetails1;
import camt053parser.model.ReportEntry2;

import library.DateToNumeric;
import snsLibrary.SnsTransaction;
import ofxLibrary.OfxPairTransaction;
import ofxLibrary.OfxMetaInfo;
import ofxLibrary.OfxTransaction;

public class SnsTransactions {
  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

  private Camt053Parser m_reader;
  private String m_File;
  private Set<String> m_UniqueId = new LinkedHashSet<>();

  private List<SnsTransaction> m_Transactions;
  private List<OfxTransaction> m_OfxTransactions = new LinkedList<OfxTransaction>();
  private Map<String, OfxMetaInfo> m_metainfo = new HashMap<String, OfxMetaInfo>();

  /**
   * Constructor.
   * 
   * @param a_file XML File with SNS transactions
   */
  public SnsTransactions(File a_file) {
    m_File = a_file.getAbsolutePath();
  }

  /**
   * Determine type of SNS transactions (saving or normal). <br>
   * Process the transactions and convert them to OFX transactions.
   */
  public void load() {
    try {
      m_reader = new Camt053Parser();
      FileInputStream fileInputStream = new FileInputStream(new File(m_File));
      Document camt053Document = m_reader.parse(fileInputStream);

      // Get all statements (usually one per bank statement)
      List<AccountStatement2> accountStatement2List = camt053Document.getBkToCstmrStmt().getStmt();
      for (AccountStatement2 accountStatement2 : accountStatement2List) {
        // String l_BankStatSeqNr = accountStatement2.getElctrncSeqNb().toString();
        Level l_Level = Level.FINEST;

        String l_IBANNr = accountStatement2.getAcct().getId().getIBAN();
        List<CashBalance3> l_balances = accountStatement2.getBal();
        l_balances.forEach(ll_balance -> {
          String l_balValue = ll_balance.getAmt().getValue().toString();
          Date l_balDate = ll_balance.getDt().getDt().toGregorianCalendar().getTime();
          String ls_balDate = DateToNumeric.dateToNumeric(l_balDate);
          OfxMetaInfo l_meta = m_metainfo.get(l_IBANNr);
          if (null == l_meta) {
            l_meta = new OfxMetaInfo();
          }

          l_meta.setAccount(l_IBANNr);
          l_meta.setMinDate(ls_balDate);
          if (l_meta.setMaxDate(ls_balDate)) {
            if (CreditDebitCode.DBIT == ll_balance.getCdtDbtInd()) {
              l_meta.setBalanceAfterTransaction("-" + l_balValue);
            } else {
              l_meta.setBalanceAfterTransaction(l_balValue);
            }
          }
          m_metainfo.put(l_IBANNr, l_meta);
        });

        for (ReportEntry2 reportEntry2 : accountStatement2.getNtry()) {
          OfxTransaction l_ofxtrans = new OfxTransaction();
          l_ofxtrans.setAccount(l_IBANNr);
          if (CreditDebitCode.DBIT == reportEntry2.getCdtDbtInd()) {
            l_ofxtrans.setTrntype("CREDIT");

          }
          if (CreditDebitCode.CRDT == reportEntry2.getCdtDbtInd()) {
            l_ofxtrans.setTrntype("DEBIT");
          }

          Date l_tranDate = reportEntry2.getBookgDt().getDt().toGregorianCalendar().getTime();
          String ls_tranDate = DateToNumeric.dateToNumeric(l_tranDate);

          l_ofxtrans.setDtposted(ls_tranDate);

          LOGGER.log(l_Level, "Credit or debit: " + reportEntry2.getCdtDbtInd());
          LOGGER.log(l_Level, "Booking date: " + reportEntry2.getBookgDt().getDt().toGregorianCalendar().getTime()
              + " (" + ls_tranDate + ")");

          List<EntryDetails1> entryDetails1List = reportEntry2.getNtryDtls();

          // Get payment details of the entry
          for (EntryDetails1 entryDetails1 : entryDetails1List) {
            // This is NOT a batch, but individual payments
            try {
              if (entryDetails1.getBtch() == null) {

                if (CreditDebitCode.DBIT == reportEntry2.getCdtDbtInd()) {
                  // Outgoing (debit) payments, show recipient (creditors) information, money was
                  // transferred from the bank (debtor) to a client (creditor)
                  // Outgoing (debit) payments, show recipient (creditors) information, money was
                  // transferred from the bank (debtor) to a client (creditor)
                  if (entryDetails1.getTxDtls().get(0).getRltdPties() != null) {
                    LOGGER.log(l_Level,
                        "Creditor name: " + entryDetails1.getTxDtls().get(0).getRltdPties().getCdtr().getNm());
                    LOGGER.log(l_Level, "Creditor IBAN: "
                        + entryDetails1.getTxDtls().get(0).getRltdPties().getCdtrAcct().getId().getIBAN());

                    l_ofxtrans.setName(entryDetails1.getTxDtls().get(0).getRltdPties().getCdtr().getNm());
                    l_ofxtrans
                        .setAccountto(entryDetails1.getTxDtls().get(0).getRltdPties().getCdtrAcct().getId().getIBAN());
                  } else {
                    l_ofxtrans.setName("");
                    l_ofxtrans.setAccountto("");
                  }

                  LOGGER.log(l_Level, "Creditor remittance information (payment description): " + entryDetails1
                      .getTxDtls().get(0).getRmtInf().getUstrd().stream().collect(Collectors.joining(",")));
                  if (entryDetails1.getTxDtls().get(0).getAmtDtls() != null) {
                    LOGGER.log(l_Level, "Creditor amount: "
                        + entryDetails1.getTxDtls().get(0).getAmtDtls().getTxAmt().getAmt().getValue());
                  }

                  LOGGER.log(l_Level,
                      "Report amount: -" + reportEntry2.getAmt().getValue() + " " + reportEntry2.getAmt().getCcy());
                  l_ofxtrans.setTrnamt("-" + reportEntry2.getAmt().getValue().toString());
                  l_ofxtrans.setTrntype("CREDIT");

                  LOGGER.log(l_Level, "Creditor remittance information (payment description): " + entryDetails1
                      .getTxDtls().get(0).getRmtInf().getUstrd().stream().collect(Collectors.joining(",")));

                  String l_memo = entryDetails1.getTxDtls().get(0).getRmtInf().getUstrd().stream()
                      .collect(Collectors.joining(","));
                  l_memo = l_memo.replaceAll("( )+", " ");
                  l_ofxtrans.setMemo(l_memo);

                  if (l_ofxtrans.getName().isBlank()) {
                    l_ofxtrans.setName(l_memo);
                  }
                }
                if (CreditDebitCode.CRDT == reportEntry2.getCdtDbtInd()) {
                  // Incoming (credit) payments, show origin (debtor) information, money was
                  // transferred from a client (debtor) to the bank (creditor)
                  if (entryDetails1.getTxDtls().get(0).getRltdPties() != null) {
                    LOGGER.log(l_Level,
                        "Debtor name: " + entryDetails1.getTxDtls().get(0).getRltdPties().getDbtr().getNm());
                    LOGGER.log(l_Level, "Debtor IBAN: "
                        + entryDetails1.getTxDtls().get(0).getRltdPties().getDbtrAcct().getId().getIBAN());

                    l_ofxtrans.setName(entryDetails1.getTxDtls().get(0).getRltdPties().getDbtr().getNm());
                    l_ofxtrans
                        .setAccountto(entryDetails1.getTxDtls().get(0).getRltdPties().getDbtrAcct().getId().getIBAN());
                  } else {
                    l_ofxtrans.setName("");
                    l_ofxtrans.setAccountto("");
                  }
                  LOGGER.log(l_Level, "Debtor remittance information (payment description): " + entryDetails1
                      .getTxDtls().get(0).getRmtInf().getUstrd().stream().collect(Collectors.joining(",")));
                  LOGGER.log(l_Level,
                      "Report amount: " + reportEntry2.getAmt().getValue() + " " + reportEntry2.getAmt().getCcy());
                  if (entryDetails1.getTxDtls().get(0).getAmtDtls() != null) {
                    LOGGER.log(l_Level, "Debtor amount: "
                        + entryDetails1.getTxDtls().get(0).getAmtDtls().getTxAmt().getAmt().getValue());
                  }

                  l_ofxtrans.setTrnamt(reportEntry2.getAmt().getValue().toString());
                  l_ofxtrans.setTrntype("DEBIT");

                  String l_memo = entryDetails1.getTxDtls().get(0).getRmtInf().getUstrd().stream()
                      .collect(Collectors.joining(","));
                  l_memo = l_memo.replaceAll("( )+", " ");
                  l_ofxtrans.setMemo(l_memo);

                  if (l_ofxtrans.getName().isBlank()) {
                    l_ofxtrans.setName(l_memo);
                  }
                }
                l_ofxtrans.setFitid(createUniqueId(l_ofxtrans));
                m_OfxTransactions.add(l_ofxtrans);
              }
            } catch (Exception e) {
              LOGGER.log(Level.INFO, e.getMessage());
            }
          }
        }
      }
      OfxPairTransaction l_filter = new OfxPairTransaction(m_OfxTransactions);
      m_OfxTransactions = l_filter.pair();

      LOGGER.log(Level.INFO, "Transactions read: " + Integer.toString(m_OfxTransactions.size()));
    } catch (Exception e) {
      LOGGER.log(Level.INFO, e.getMessage());
    }
  }

  /**
   * Returns true when the processed transactions are saving transactions.
   * 
   * @return True for Saving transactions
   */
  public boolean isSavingCsvFile() {
    return false;
  }

  /**
   * Return a list of normal transactions or null when savings transactions are
   * processed.
   * 
   * @return List of normal transactions
   */
  public List<SnsTransaction> getSnsTransactions() {
    return m_Transactions;
  }

  /**
   * Return a list of OFX transactions.
   * 
   * @return List of saving transactions
   */
  public List<OfxTransaction> getOfxTransactions() {
    return m_OfxTransactions;
  }

  /**
   * Returns meta information of the OFX transactions.
   * 
   * @return OFX Meta information
   */
  public Map<String, OfxMetaInfo> getOfxMetaInfo() {
    return m_metainfo;
  }

  /**
   * Create a unique fitid for an OFX Transaction.
   * 
   * @param l_ofxtrans OFX Transaction
   * @return A unique fitid
   */
  private String createUniqueId(OfxTransaction l_ofxtrans) {
    String uniqueid = "";
    /*
     * @formatter:off
    String memo = l_ofxtrans.getMemo();
    String time = "";
     * time = "" matches = re.search("\s([0-9]{2}:[0-9]{2})\s", memo) if matches:
     * time = matches.group(1).replace(":", "")

    Pattern patt = Pattern.compile("([0-9]{2}:[0-9]{2})");
    Matcher matcher = patt.matcher(memo);
    if (matcher.find()) {
      time = matcher.group(1).replace(":", ""); // you can get it from desired index as well
    }
     * @formatter:on
    */
    String fitid = l_ofxtrans.getDtposted() + l_ofxtrans.getTrnamt().replace(",", "").replace("-", "").replace(".", "");
    uniqueid = fitid;
    if (m_UniqueId.contains(fitid)) {
      // # Make unique by adding time and sequence nr.
      int idcount = 0;
      uniqueid = fitid;
      while (m_UniqueId.contains(uniqueid)) {
        idcount = idcount + 1;
        // uniqueid = fitid + time + Integer.toString(idcount);
        uniqueid = fitid + Integer.toString(idcount);
      }
      m_UniqueId.add(uniqueid);
    } else {
      m_UniqueId.add(fitid);
    }
    return uniqueid;
  }
}
