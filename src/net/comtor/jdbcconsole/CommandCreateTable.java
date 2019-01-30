package net.comtor.jdbcconsole;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class CommandCreateTable {

    static void commandCreateTable(Connection conn, String tableName) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            String SELECTquery = tableName;
            if (!SELECTquery.contains(" ")) {
                SELECTquery = "SELECT TOP 1 * FROM " + tableName;
            }
            boolean resp = stmt.execute(SELECTquery);
            if (!resp) {
                return;

            }

            ResultSet rs = stmt.getResultSet();
            ResultSetMetaData rsmd = rs.getMetaData();

            System.out.println("CREATE TABLE " + tableName + " (");
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                System.out.print("    " + rsmd.getColumnName(i) + "    ");
                String scale = "";
                if (rsmd.getScale(i) != 0) {
                    scale = "," + rsmd.getScale(i);
                }
                System.out.print(rsmd.getColumnTypeName(i) + "(" + rsmd.getPrecision(i) + "" + scale + ")");
                if (i != rsmd.getColumnCount()) {
                    System.out.print(",");
                }
                System.out.println();

            }
            System.out.println(")");


        } catch (Exception e) {
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }

        }
    }
}
