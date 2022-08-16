package library;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * 
 * @author kweers
 *
 */
public class OutputTxt {
  private String m_Header = "";
  private String m_Footer = "";
  private BufferedWriter m_writer;
  private String m_commentStart = "#";
  private String m_commentEnd = "";

  public OutputTxt(String a_OutputFile) throws IOException {
    Path outpath = Paths.get(a_OutputFile);
    Charset charset = Charset.forName("UTF-8");
    m_writer = Files.newBufferedWriter(outpath, charset);
  }

  public void SetFooter(String a_Footer) {
    m_Footer = m_commentStart + a_Footer + m_commentEnd;
  }

  public void SetComment(String a_CommentStart, String a_CommentEnd) {
    m_commentStart = a_CommentStart;
    m_commentEnd = a_CommentEnd;
  }

  public void SetHeader(String a_Header) {
    m_Header = m_commentStart + a_Header + m_commentEnd;
  }

  public void Close() throws IOException {
    LocalDate today = LocalDate.now();
    LocalTime time = LocalTime.now();
    m_writer.write(m_Footer + "\r\n");
    m_writer
        .write(m_commentStart + " Gegenereerd op " + today.toString() + " " + time.toString() + m_commentEnd + "\r\n");

    m_writer.close();
  }

  public void Schrijf(ArrayList<String> a_Regels) {
    if (!m_Header.isEmpty()) {
      try {
        m_writer.write(m_Header + "\r\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    a_Regels.forEach(v_Regel -> {
      try {
        m_writer.write(v_Regel + "\r\n");
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
  }
}
