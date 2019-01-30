package net.comtor.jdbcconsole;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author juriel
 */
public class JDBCSqlConsole {

    private static ResourceBundle bundle = ResourceBundle.getBundle(JDBCSqlConsole.class.getCanonicalName());
    public final static String name = "JDBCSqlConsole";
    public final static String version = "2.0.1 - 2014/Jan/27";
    public final static int CMD_QUERY = 1;
    public final static int CMD_EXPORT_CSV = 2;
    public final static int CMD_EXPORT_EXCEL = 3;
    public final static int CMD_GENERATE_CREATE_TABLE = 4;
    public final static int CMD_GENERATE_CREATE_CLASS = 5;
    public static final String DRIVER_MYSQL = "com.mysql.jdbc.Driver";
    public static final String DRIVER_ORACLE = "oracle.jdbc.OracleDriver";
    public static final String DRIVER_POSTGRESQL = "org.postgresql.Driver";
    public static final String DRIVER_SQLSERVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    public static final String DRIVER_SYBASE = "com.sybase.jdbc2.jdbc.SybDriver";
    private static int CMD = -1;
    private static String host = null;
    private static int port = -1;
    private static String database = null;
    private static String driver = null;
    private static String url = null;
    private static String user = null;
    private static String password = null;
    private static String separator = "\t";
    private static String query = null;
    private static String filename = null;
    private static String sourceFilename = null;
    private static boolean showHeaders = true;
    private static boolean showMetaData = false;
    private static boolean showSummary = false;
    private static boolean interactive = false;
    private static boolean displaySourceQuery = false;
    /**
     * Supported commands in the {@link CommandConsole}
     */
    private static final String showmetadataCMD = "-show-metadata";
    private static final String exporttocsvCMD = "-export-to-csv";
    private static final String exporttoexcelCMD = "-export-to-excel";
    private static final String generatecreatetableCMD = "-generate-create-table";
    private static final String generatecreateclassCMD = "-generate-create-class";
    /**
     * Supported commands in the {@link CommandConsole}
     */
    public static final String[] commandSupportedCommands = {exporttocsvCMD, exporttoexcelCMD, generatecreateclassCMD,
        generatecreatetableCMD, showmetadataCMD};

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            usage();
            return;
        }
        parseArgs(args);

        if (!interactive) {
            if (query == null || query.trim().length() == 0) {
                System.out.println(bundle.getString("error.noValidQuery") + (query == null ? "" : query));
                return;
            }
        }

        if (url == null) {
            url = JDBCURLHelper.generateURL(driver, host, port, database);
        }

        try {
            Class.forName(driver);
        } catch (Exception e) {
            System.out.println("Unable to load driver " + driver);
            System.out.println("ERROR " + e.getMessage());
            return;
        }
        java.sql.Connection conn = null;
        try {
            conn = DriverManager.getConnection(url, user, password);
        } catch (SQLException ex) {
            System.out.println("Unable to create connection " + url);
            System.out.println("ERROR " + ex.getMessage());
            return;
        }

        if (interactive) {
            final CommandConsole commandConsole = new CommandConsole();
            commandConsole.setConn(conn);
            commandConsole.run();
        } else {
            if (CMD == -1) {
                CMD = CMD_QUERY;
            }
            executeCommand(conn, CMD, query);
        }
        try {
            conn.close();
        } catch (SQLException ex) {
        }
    }

    public static void parseArgs(String[] args) throws NumberFormatException {
        int size = args.length;
        String[] interactiveCommands = {"-i", "-interactive"};
        int paramCounter;
        for (paramCounter = 0; paramCounter < size; paramCounter++) {
            String str = args[paramCounter];

            if (str.equals("-url")) {
                paramCounter++;
                url = args[paramCounter];
            } else if (str.equals("-oracle")) {
                driver = DRIVER_ORACLE;
            } else if (str.equals("-mysql")) {
                driver = DRIVER_MYSQL;
            } else if (str.equals("-sqlserver")) {
                driver = DRIVER_SQLSERVER;
            } else if (str.equals("-postgresql")) {
                driver = DRIVER_POSTGRESQL;
            } else if (str.equals("-sybase")) {
                driver = DRIVER_SYBASE;
            } else if (str.equals("-host")) {
                paramCounter++;
                host = args[paramCounter];
            } else if (str.equals("-port")) {
                paramCounter++;
                port = Integer.parseInt(args[paramCounter]);
            } else if (str.equals("-database") || str.equals("-sid")) {
                paramCounter++;
                database = args[paramCounter];
            } else if (str.equals("-driver")) {
                paramCounter++;
                driver = args[paramCounter];
            } else if (str.equals("-user")) {
                paramCounter++;
                user = args[paramCounter];
            } else if (str.equals("-password")) {
                paramCounter++;
                password = args[paramCounter];
            } else if (str.equals("-P")) {
                Console in = System.console();
                System.out.println(bundle.getString("password.prompt"));
                password = new String(in.readPassword());
            } else if (str.equals("-separator")) {
                paramCounter++;
                separator = args[paramCounter];
            } else if (str.equals("-hide-headers")) {
                showHeaders = false;
            } else if (str.equals(showmetadataCMD)) {
                showMetaData = true;
            } else if (str.equals("-query")) {
                CMD = CMD_QUERY;
            } else if (str.equals(exporttocsvCMD)) {
                CMD = CMD_EXPORT_CSV;
            } else if (str.equals(exporttoexcelCMD)) {
                CMD = CMD_EXPORT_EXCEL;
            } else if (str.equals(generatecreatetableCMD)) {
                CMD = CMD_GENERATE_CREATE_TABLE;
            } else if (str.equals(generatecreateclassCMD)) {
                CMD = CMD_GENERATE_CREATE_CLASS;
            } else if (str.equals("-filename")) {
                paramCounter++;
                filename = args[paramCounter];
            } else if (str.equals("-source")) {
                paramCounter++;
                sourceFilename = args[paramCounter];
                try {
                    getQueryFromFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            } else if (str.equals("-ds")) {
                displaySourceQuery = true;
            } else if (Arrays.binarySearch(interactiveCommands, str.toLowerCase()) >= 0) {
                interactive = true;
            } else if (str.startsWith("--help")) {//General help
                if (size > paramCounter + 1) {
                    paramCounter++;
                    String ctxhelp = args[paramCounter];
                    //Specific help
                    if (ctxhelp.toLowerCase().equals("sqlserver")) {
                        System.out.println(bundle.getString("sqlserver.help"));
                        System.exit(0);
                    } else if (ctxhelp.toLowerCase().equals("oracle")) {
                        System.out.println(bundle.getString("oracle.help"));
                        System.exit(0);
                    } else if (ctxhelp.toLowerCase().equals("mysql")) {
                        System.out.println(bundle.getString("mysql.help"));
                        System.exit(0);
                    }
                }
                usage();
                System.exit(0);
            } else {
                query = str;
            }
        }
    }

    protected static void usage() {
        System.out.println(bundle.getString("usage.msg"));
    }

    /**
     * Executes a command given its command name
     */
    protected static void executeCommand(Connection conn, String commandName, String queryStr) {
        int commandId = -1;
        if (commandName.equals(exporttoexcelCMD)) {
            commandId = CMD_EXPORT_EXCEL;
        } else if (commandName.equals(generatecreatetableCMD)) {
            commandId = CMD_GENERATE_CREATE_TABLE;
        } else if (commandName.equals(generatecreateclassCMD)) {
            commandId = CMD_GENERATE_CREATE_CLASS;
        }
        executeCommand(conn, commandId, queryStr);
    }

    /**
     * Executes a command given its id
     */
    private static void executeCommand(Connection conn, int commandId, String queryStr) {
//System.out.println("Command ID:"+commandId+"/query:"+queryStr);
        if (commandId == CMD_QUERY) {
            System.out.println(CommandQuery.commandQuery(conn, queryStr, showHeaders, separator, showMetaData));
        } else if (commandId == CMD_GENERATE_CREATE_TABLE) {
            CommandCreateTable.commandCreateTable(conn, queryStr);
        } else if (commandId == CMD_GENERATE_CREATE_CLASS) {
            CommandCreateClass.commandCreateClass(conn, queryStr);
        } else if (commandId == CMD_EXPORT_EXCEL) {
            CommandQuery.commandQueryExcel(conn, queryStr, showHeaders, showMetaData, filename);
        } else if (commandId == CMD_EXPORT_CSV) {
            if (filename != null) {
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
                    writer.write(CommandQuery.commandQuery(conn, queryStr, showHeaders, ",", showMetaData));
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(JDBCSqlConsole.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * @return the filename
     */
    public static String getFilename() {
        return filename;
    }

    /**
     * @return the separator
     */
    public static String getSeparator() {
        return separator;
    }

    private static void getQueryFromFile() throws IOException {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            File f = new File(sourceFilename);
            fr = new FileReader(f);
            br = new BufferedReader(fr);

            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }

            query = sb.toString();
            if (displaySourceQuery) {
                System.out.println("----------");
                System.out.println("Query:\n\n" + query);
                System.out.println("----------");
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
            }
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e) {
                }
            }
        }
    }
}
