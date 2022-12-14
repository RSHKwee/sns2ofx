package sns2ofx;

import camt053parser.Camt053Parser;
//import Camt053Parser;
import camt053parser.model.*;

import java.io.File;
import java.io.FileInputStream;

import java.util.List;
import java.util.stream.Collectors;

public class importCamt053 {
  public static void main(String[] args) {
    Camt053Parser camt053Parser = new Camt053Parser();
    // final String CAMT053FILENAME = "camt053file.xml";
    final String CAMT053FILENAME = "D:\\Users\\René\\OneDrive\\Documenten\\Administraties\\csv\\transactie-historie_NL02ZWLB8828137371_20220721153921.xml";
    try {
      FileInputStream fileInputStream = new FileInputStream(new File(CAMT053FILENAME));
      Document camt053Document = camt053Parser.parse(fileInputStream);
      List<AccountStatement2> accountStatement2List = camt053Document.getBkToCstmrStmt().getStmt();

      // Get all statements (usually one per bank statement)
      for (AccountStatement2 accountStatement2 : accountStatement2List) {
        System.out.println("Bank statement sequence number: " + accountStatement2.getElctrncSeqNb());
        System.out.println("Bank statement account IBAN: " + accountStatement2.getAcct().getId().getIBAN());

        System.out
            .println("Balance on " + accountStatement2.getBal().get(0).getDt().getDt().toGregorianCalendar().getTime()
                + " : " + accountStatement2.getBal().get(0).getAmt().getValue() + " "
                + accountStatement2.getBal().get(0).getAmt().getCcy());
        // Get a list of all payment entries
        System.out
            .println("Balance on " + accountStatement2.getBal().get(1).getDt().getDt().toGregorianCalendar().getTime()
                + " : " + accountStatement2.getBal().get(1).getAmt().getValue() + " "
                + accountStatement2.getBal().get(1).getAmt().getCcy());
        // Get a list of all payment entries
        for (int i = 0; i < 80; i++)
          System.out.print("-");
        System.out.println();

        for (ReportEntry2 reportEntry2 : accountStatement2.getNtry()) {
          System.out.println("Credit or debit: " + reportEntry2.getCdtDbtInd());
          System.out.println("Booking date: " + reportEntry2.getBookgDt().getDt().toGregorianCalendar().getTime());

          List<EntryDetails1> entryDetails1List = reportEntry2.getNtryDtls();

          // Get payment details of the entry
          for (EntryDetails1 entryDetails1 : entryDetails1List) {
            // This is NOT a batch, but individual payments
            try {
              if (entryDetails1.getBtch() == null) {
                if (CreditDebitCode.DBIT == reportEntry2.getCdtDbtInd()) {
                  // Outgoing (debit) payments, show recipient (creditors) information, money was
                  // transferred from the bank (debtor) to a client (creditor)
                  if (entryDetails1.getTxDtls().get(0).getRltdPties() != null) {
                    System.out
                        .println("Creditor name: " + entryDetails1.getTxDtls().get(0).getRltdPties().getCdtr().getNm());
                    System.out.println("Creditor IBAN: "
                        + entryDetails1.getTxDtls().get(0).getRltdPties().getCdtrAcct().getId().getIBAN());
                  }
                  System.out.println("Creditor remittance information (payment description): " + entryDetails1
                      .getTxDtls().get(0).getRmtInf().getUstrd().stream().collect(Collectors.joining(",")));
                  System.out.println(
                      "Report amount: " + reportEntry2.getAmt().getValue() + " " + reportEntry2.getAmt().getCcy());
                  if (entryDetails1.getTxDtls().get(0).getAmtDtls() != null) {
                    System.out.println("Creditor amount: "
                        + entryDetails1.getTxDtls().get(0).getAmtDtls().getTxAmt().getAmt().getValue());
                  }
                }
                if (CreditDebitCode.CRDT == reportEntry2.getCdtDbtInd()) {
                  // Incoming (credit) payments, show origin (debtor) information, money was
                  // transferred from a client (debtor) to the bank (creditor)
                  if (entryDetails1.getTxDtls().get(0).getRltdPties() != null) {
                    System.out
                        .println("Debtor name: " + entryDetails1.getTxDtls().get(0).getRltdPties().getDbtr().getNm());
                    System.out.println("Debtor IBAN: "
                        + entryDetails1.getTxDtls().get(0).getRltdPties().getDbtrAcct().getId().getIBAN());
                  }
                  System.out.println("Debtor remittance information (payment description): " + entryDetails1.getTxDtls()
                      .get(0).getRmtInf().getUstrd().stream().collect(Collectors.joining(",")));
                  System.out.println(
                      "Report amount: " + reportEntry2.getAmt().getValue() + " " + reportEntry2.getAmt().getCcy());
                  if (entryDetails1.getTxDtls().get(0).getAmtDtls() != null) {
                    System.out.println("Debtor amount: "
                        + entryDetails1.getTxDtls().get(0).getAmtDtls().getTxAmt().getAmt().getValue());
                  }
                }
              } else {
                // This is an entry about an outgoing batch payment
                if (CreditDebitCode.DBIT == reportEntry2.getCdtDbtInd()) {
                  System.out.println("Batch creditor total amount: " + entryDetails1.getBtch().getTtlAmt().getValue());
                  for (EntryTransaction2 entryTransaction2 : entryDetails1.getTxDtls()) {
                    // Outgoing (debit) payments, show recipient (creditor) information, money was
                    // transferred from the bank (debtor) to a client (creditor)
                    if (entryDetails1.getTxDtls().get(0).getRltdPties() != null) {
                      System.out.println("Batch creditor name: " + entryTransaction2.getRltdPties().getCdtr().getNm());
                      System.out.println(
                          "Batch creditor IBAN: " + entryTransaction2.getRltdPties().getCdtrAcct().getId().getIBAN());
                    }
                    if (entryDetails1.getTxDtls().get(0).getAmtDtls() != null) {
                      System.out.println(
                          "Batch creditor amount: " + entryTransaction2.getAmtDtls().getTxAmt().getAmt().getValue());
                    }
                    System.out
                        .println("Batch creditor remittance information: " + entryTransaction2.getRmtInf().getUstrd());
                  }
                }
              }
            } catch (Exception e) {
              System.out.print(e.getMessage());
            }
            for (int i = 0; i < 80; i++)
              System.out.print("-");
            System.out.println();
          }
        }
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
