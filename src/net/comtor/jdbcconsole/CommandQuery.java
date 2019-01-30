package net.comtor.jdbcconsole;

import java.io.FileOutputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**
 * Holder of Execution routines.
 */
public class CommandQuery {

    /**
     * Performs a sql Query and returns the result
     */
    public static String commandQuery(Connection conn, String query,
            boolean showHeaders, String separator, boolean showMetaData) {
        final String NEWLINE = System.getProperty("line.separator");
        StringWriter resp = new StringWriter();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            boolean executed = stmt.execute(query);
            if (executed) {
                ResultSet rs = stmt.getResultSet();
                ResultSetMetaData rsmd = rs.getMetaData();
                if (showHeaders) {
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        resp.append(rsmd.getColumnName(i) + separator);
                    }
                    resp.append(NEWLINE);
                }
                if (showMetaData) {
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        String scale = "";
                        if (rsmd.getScale(i) != 0) {
                            scale = "," + rsmd.getScale(i);
                        }
                        resp.append(rsmd.getColumnTypeName(i) + "("
                                + rsmd.getPrecision(i) + "" + scale + ")"
                                + separator);
                    }
                    resp.append(NEWLINE);
                }
                while (rs.next()) {
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        resp.append(rs.getString(i) + separator);
                    }
                    resp.append(NEWLINE);
                }
            } else {
                stmt.getUpdateCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }

        }
        return resp.toString();
    }

    public static void commandQueryExcel(Connection conn, String query,
            boolean showHeaders, boolean showMetaData, String filename) {
        Statement stmt = null;
        try {

            SXSSFWorkbook book = new SXSSFWorkbook();
            SXSSFSheet sheet = (SXSSFSheet) book.createSheet();
            stmt = conn.createStatement();
            long rowPos = 0;
            boolean resp = stmt.execute(query);
            if (resp) {
                ResultSet rs = stmt.getResultSet();
                ResultSetMetaData rsmd = rs.getMetaData();
                if (showHeaders) {
                    Row row = sheet.createRow((int) rowPos);
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        Cell cell = row.createCell(i - 1);
                        cell.setCellValue(rsmd.getColumnName(i));
                    }
                    rowPos++;

                }
                if (showMetaData) {
                    Row row = sheet.createRow((int) rowPos);
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        Cell cell = row.createCell(i - 1);
                        int scale = rsmd.getScale(i);
                        cell.setCellValue(rsmd.getColumnTypeName(i) + "("
                                + rsmd.getPrecision(i) + " " + scale + ")");

                    }
                    rowPos++;

                }
                while (rs.next()) {
                    Row row = sheet.createRow((int) rowPos);

                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        Cell cell = row.createCell(i - 1);
                        if (rsmd.getColumnType(i) == java.sql.Types.CHAR
                                || rsmd.getColumnType(i) == java.sql.Types.VARCHAR
                                || rsmd.getColumnType(i) == java.sql.Types.NCHAR
                                || rsmd.getColumnType(i) == java.sql.Types.LONGVARCHAR
                                || rsmd.getColumnType(i) == java.sql.Types.LONGNVARCHAR) {

                            cell.setCellValue(rs.getString(i));
                        } else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE
                                || rsmd.getColumnType(i) == java.sql.Types.FLOAT) {

                            cell.setCellValue(rs.getDouble(i));
                        } else if (rsmd.getColumnType(i) == java.sql.Types.NUMERIC
                                || rsmd.getColumnType(i) == java.sql.Types.DECIMAL
                                || rsmd.getColumnType(i) == java.sql.Types.INTEGER
                                || rsmd.getColumnType(i) == java.sql.Types.SMALLINT
                                || rsmd.getColumnType(i) == java.sql.Types.BIGINT
                                || rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
                            cell.setCellValue(rs.getLong(i));
                        } else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {

                            java.util.Date d = new java.util.Date();
                            d.setTime(rs.getDate(i).getTime());
                            cell.setCellValue(d);
                        } else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
                            java.util.Date d = new java.util.Date();
                            d.setTime(rs.getTimestamp(i).getTime());
                            cell.setCellValue(d);
                        } else {
                            cell.setCellValue("" + rs.getString(i));
                        }



                    }

                    rowPos++;
                }

                book.write(new FileOutputStream(filename));
            } else {
                stmt.getUpdateCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException ex) {
            }

        }

    }
}
