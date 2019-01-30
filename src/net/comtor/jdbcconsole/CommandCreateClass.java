package net.comtor.jdbcconsole;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class CommandCreateClass {

    static void commandCreateClass(Connection conn, String query) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            boolean resp = stmt.execute(query);
            if (!resp) {
                return;

            }

            ResultSet rs = stmt.getResultSet();
            ResultSetMetaData rsmd = rs.getMetaData();

            System.out.println("public class " + rsmd.getTableName(1) + " { ");
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {

                String scale = "";
                if (rsmd.getScale(i) != 0) {
                    scale = "," + rsmd.getScale(i);
                }

                String type = rsmd.getColumnTypeName(i);
                if (type.toLowerCase().indexOf("varchar") >= 0) {
                    type = "String";
                }
                System.out.print("private " + type + "  " + rsmd.getColumnName(i) + " ;");
                System.out.println();

            }
            System.out.println("}");


        } catch (Exception e) {
        } finally {
            try {
                stmt.close();
            } catch (SQLException ex) {
            }

        }
    }
}
