package sns2ofx.convertor;

/**
 * Convert SNS transactions to OFX transactions.
 * 
 * @author René
 *
 */
import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
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
import camt053parser.model.EntryTransaction2;
import camt053parser.model.ReportEntry2;

import snsLibrary.SnsTransaction;

import ofxLibrary.OfxMetaInfo;
import ofxLibrary.OfxTransaction;

public class SnsTransactions {
  private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

  private Camt053Parser m_reader;
  private String m_File;
  private boolean m_saving = false;
  private Set<String> m_UniqueId = new LinkedHashSet<>();

  private List<SnsTransaction> m_Transactions;
  private List<OfxTransaction> m_OfxTransactions = new LinkedList<OfxTransaction>();
  private Map<String, OfxMetaInfo> m_metainfo = new HashMap<String, OfxMetaInfo>();

  /**
   * Constructor.
   * 
   * @param a_file CSV File with ING transactions
   */
  public SnsTransactions(File a_file) {
    m_File = a_file.getAbsolutePath();
  }

  /**
   * Determine type of ING transactions (saving or normal). <br>
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
        String l_IBANNr = accountStatement2.getAcct().getId().getIBAN();

        List<CashBalance3> l_balances = accountStatement2.getBal();
        l_balances.forEach(ll_balance -> {
          String l_balValue = ll_balance.getAmt().getValue().toString();
          Date l_balDate = ll_balance.getDt().getDt().toGregorianCalendar().getTime();

          OfxMetaInfo l_meta = new OfxMetaInfo();
          l_meta.setAccount(l_IBANNr);
          l_meta.setMinDate(l_balDate);
          if (l_meta.setMaxDate(l_balDate)) {
            l_meta.setBalanceAfterTransaction(l_balValue);
          }
          m_metainfo.put(l_IBANNr, l_meta);
        });

        for (ReportEntry2 reportEntry2 : accountStatement2.getNtry()) {
          System.out.println("Credit or debit: " + reportEntry2.getCdtDbtInd());
          System.out.println("Booking date: " + reportEntry2.getBookgDt().getDt().toGregorianCalendar().getTime());

          List<EntryDetails1> entryDetails1List = reportEntry2.getNtryDtls();

          // Get payment details of the entry
          for (EntryDetails1 entryDetails1 : entryDetails1List) {
            // This is NOT a batch, but individual payments
            try {
              if (entryDetails1.getBtch() == null) {
                OfxTransaction l_ofxtrans = new OfxTransaction();

                if (CreditDebitCode.DBIT == reportEntry2.getCdtDbtInd()) {
                  // Outgoing (debit) payments, show recipient (creditors) information, money was
                  // transferred from the bank (debtor) to a client (creditor)
                  l_ofxtrans.setTrnamt(
                      entryDetails1.getTxDtls().get(0).getAmtDtls().getTxAmt().getAmt().getValue().toString());

                  System.out
                      .println("Creditor name: " + entryDetails1.getTxDtls().get(0).getRltdPties().getCdtr().getNm());
                  System.out.println("Creditor IBAN: "
                      + entryDetails1.getTxDtls().get(0).getRltdPties().getCdtrAcct().getId().getIBAN());
                  System.out.println("Creditor remittance information (payment description): " + entryDetails1
                      .getTxDtls().get(0).getRmtInf().getUstrd().stream().collect(Collectors.joining(",")));
                  System.out.println(
                      "Report amount: " + reportEntry2.getAmt().getValue() + " " + reportEntry2.getAmt().getCcy());
                  System.out.println("Creditor amount: "
                      + entryDetails1.getTxDtls().get(0).getAmtDtls().getTxAmt().getAmt().getValue());
                }
                if (CreditDebitCode.CRDT == reportEntry2.getCdtDbtInd()) {
                  // Incoming (credit) payments, show origin (debtor) information, money was
                  // transferred from a client (debtor) to the bank (creditor)
                  System.out
                      .println("Debtor name: " + entryDetails1.getTxDtls().get(0).getRltdPties().getDbtr().getNm());
                  System.out.println("Debtor IBAN: "
                      + entryDetails1.getTxDtls().get(0).getRltdPties().getDbtrAcct().getId().getIBAN());
                  System.out.println("Debtor remittance information (payment description): " + entryDetails1.getTxDtls()
                      .get(0).getRmtInf().getUstrd().stream().collect(Collectors.joining(",")));
                  System.out.println(
                      "Report amount: " + reportEntry2.getAmt().getValue() + " " + reportEntry2.getAmt().getCcy());
                  System.out.println(
                      "Debtor amount: " + entryDetails1.getTxDtls().get(0).getAmtDtls().getTxAmt().getAmt().getValue());
                }

              } else {
                // This is an entry about an outgoing batch payment
                if (CreditDebitCode.DBIT == reportEntry2.getCdtDbtInd()) {
                  System.out.println("Batch creditor total amount: " + entryDetails1.getBtch().getTtlAmt().getValue());
                  for (EntryTransaction2 entryTransaction2 : entryDetails1.getTxDtls()) {
                    // Outgoing (debit) payments, show recipient (creditor) information, money was
                    // transferred from the bank (debtor) to a client (creditor)
                    System.out.println("Batch creditor name: " + entryTransaction2.getRltdPties().getCdtr().getNm());
                    System.out.println(
                        "Batch creditor IBAN: " + entryTransaction2.getRltdPties().getCdtrAcct().getId().getIBAN());
                    System.out.println(
                        "Batch creditor amount: " + entryTransaction2.getAmtDtls().getTxAmt().getAmt().getValue());
                    System.out
                        .println("Batch creditor remittance information: " + entryTransaction2.getRmtInf().getUstrd());
                  }
                }
              }
            } catch (Exception e) {

            }
            for (int i = 0; i < 80; i++)
              System.out.print("-");
            System.out.println();
          }
        }
      }

    } catch (Exception e) {
      LOGGER.log(Level.INFO, e.getMessage());
    }

    if (m_saving) {
      // HeaderColumnNameMappingStrategy<IngSavingTransaction> beanStrategy = new
      // HeaderColumnNameMappingStrategy<IngSavingTransaction>();
      // beanStrategy.setType(IngSavingTransaction.class);
      // m_SavingTransactions = new CsvToBeanBuilder<IngSavingTransaction>(new
      // FileReader(m_File))
      // .withSeparator(m_separator).withMappingStrategy(beanStrategy).build().parse();
      m_Transactions = null;

//        m_SavingTransactions.forEach(l_trans -> {
      OfxTransaction l_ofxtrans;
//          l_ofxtrans = Ing2OfxTransaction.convertSavingToOfx(l_trans);
//          l_ofxtrans.setFitid(createUniqueId(l_ofxtrans));
//          if (m_metainfo.containsKey(l_ofxtrans.getAccount())) {
//          updateOfxMetaInfo(l_ofxtrans, l_trans.getSaldo_na_mutatie());
    } else {
//            createOfxMetaInfo(l_ofxtrans, l_trans.getSaldo_na_mutatie());
    }
//          m_OfxTransactions.add(l_ofxtrans);
//        });
//      } else {
//        HeaderColumnNameMappingStrategy<IngTransaction> beanStrategy = new HeaderColumnNameMappingStrategy<IngTransaction>();
//        beanStrategy.setType(IngTransaction.class);
//        m_Transactions = new CsvToBeanBuilder<IngTransaction>(new FileReader(m_File)).withSeparator(m_separator)
//            .withMappingStrategy(beanStrategy).build().parse();
//        m_SavingTransactions = null;

    m_Transactions.forEach(l_trans -> {
      OfxTransaction l_ofxtrans;
//          l_ofxtrans = Ing2OfxTransaction.convertToOfx(l_trans);
//          l_ofxtrans.setFitid(createUniqueId(l_ofxtrans));
//          if (m_metainfo.containsKey(l_ofxtrans.getAccount())) {
//            updateOfxMetaInfo(l_ofxtrans, l_trans.getSaldo_na_mutatie());
//          } else {
//            createOfxMetaInfo(l_ofxtrans, l_trans.getSaldo_na_mutatie());
//          }
      // m_OfxTransactions.add(l_ofxtrans);
    });
  }

  /**
   * Returns true when the processed transactions are saving transactions.
   * 
   * @return True for Saving transactions
   */
  public boolean isSavingCsvFile() {
    return m_saving;
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
   * Update meta information of OFX Transactions.
   * 
   * @param a_OfxTransaction OFX Transaction
   * @param a_SaldoNaMutatie Balance after transaction
   */
  private void updateOfxMetaInfo(OfxTransaction a_OfxTransaction, String a_SaldoNaMutatie) {
    OfxMetaInfo l_meta = m_metainfo.get(a_OfxTransaction.getAccount());
    try {
      String sDtPosted = a_OfxTransaction.getDtposted();
      l_meta.setMaxDate(sDtPosted);
      if (l_meta.getMaxDate().equalsIgnoreCase(sDtPosted)) {
        if (l_meta.getBalanceAfterTransaction().isBlank()) {
          l_meta.setBalanceAfterTransaction(a_SaldoNaMutatie);
        }
      }
      l_meta.setMaxDate(sDtPosted);
      l_meta.setMinDate(sDtPosted);
      if (m_saving && (l_meta.getPrefix().isBlank())) {
        if (!a_OfxTransaction.getAccountto().isBlank()) {
          l_meta.setPrefix(a_OfxTransaction.getAccountto());
        }
      }
      l_meta.setMaxDate(sDtPosted);
      l_meta.setMinDate(sDtPosted);

      m_metainfo.put(a_OfxTransaction.getAccount(), l_meta);
    } catch (Exception e) {
    }
  }

  /**
   * Store meta information of OFX Transactions.
   * 
   * @param a_OfxTransaction OFX Transaction
   * @param a_SaldoNaMutatie Balance after transaction
   */
  private void createOfxMetaInfo(OfxTransaction a_OfxTransaction, String a_SaldoNaMutatie) {
    OfxMetaInfo l_meta = new OfxMetaInfo();
    l_meta.setAccount(a_OfxTransaction.getAccount());
    String sDtPosted = a_OfxTransaction.getDtposted();
    l_meta.setMaxDate(sDtPosted);
    // if (l_meta.getMaxDate().equalsIgnoreCase(sDtPosted)) {
    // if (l_meta.getBalanceAfterTransaction().isBlank()) {
    l_meta.setBalanceAfterTransaction(a_SaldoNaMutatie);
    // }
    // }
    l_meta.setMaxDate(sDtPosted);
    l_meta.setMinDate(sDtPosted);

    if (l_meta.getPrefix().isBlank()) {
      if (!a_OfxTransaction.getAccountto().isBlank()) {
        l_meta.setPrefix(a_OfxTransaction.getAccountto());
      }
    }
    m_metainfo.put(a_OfxTransaction.getAccount(), l_meta);
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
