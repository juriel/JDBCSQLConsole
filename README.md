# JDBCSQLConsole
Command line program to connect to any database with JDBC driver.


Documentation

Main commands:

    -query Execute query (this is the default command)
    -export-to-cvs Generate text file
    -export-to-excel Generate Microsoft Excel File
    -generate-create-table Generate create table

JDBC Drivers:

    -driver JDBC driver i.e: com.mysql.jdbc.Driver
    -oracle to use Oracle driver
    -mysql to use mysql driver
    -sqlserver to use Microsoft SQLServer driver
    -postgresql to use Postgresql driver

URL:

    -url JDBC Url
    -host server host if not url
    -port server port (optional) if not url
    -database database if not url
    -sid SID if not url

Authentication:

    -user user to connnect to database
    -password password to connnect to database

Options:

    -separator column separator for output (text file o stdout
    -hide-headers hide headers on text output
    -show-metadata show additional row with metadata

Examples

java -jar JDBCSqlConsole.jar -mysql -host 127.0.0.1 -database myDB -user peter -password changeit "SELECT * FROM element_model"

java -jar JDBCSqlConsole.jar -mysql -generate-create-table -host 127.0.0.1 -database myDB -user peter -password changeit "SELECT * FROM users";

CREATE TABLE users ( login VARCHAR(50), password VARCHAR(255), name VARCHAR(150), email VARCHAR(100), login_mobile VARCHAR(50), password_mobile VARCHAR(255), phone VARCHAR(50), identification VARCHAR(50), mobile_phone VARCHAR(50) )



