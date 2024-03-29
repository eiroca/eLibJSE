/**
 *
 * Copyright (C) 1999-2021 Enrico Croce - AGPL >= 3.0
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 **/
package net.eiroca.library.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;
import net.eiroca.library.core.Helper;
import net.eiroca.library.core.LibStr;
import net.eiroca.library.system.IConfig;
import net.eiroca.library.system.IContext;

public class DBConfig {

  //
  private static final String ENV_ORACLEHOME = "ORACLE_HOME";
  private static final String ENV_TNSADMIN = "TNS_ADMIN";
  //
  private static final String ORACLE_NET_CONNECT_TIMEOUT = "oracle.net.CONNECT_TIMEOUT";
  private static final String ORACLE_NET_TNS_ADMIN = "oracle.net.tns_admin";
  private static final String ORACLE_NET_USER = "user";
  private static final String ORACLE_NET_PASSWORD = "password";
  //
  private static final String ORACLE_CFG_SID = "sid";
  //
  private static final String CONFIG_TYPE = "DBType";
  private static final String CONFIG_PORT = "port";
  private static final String CONFIG_DATABASE = "Database";
  private static final String CONFIG_SERVER = "server";
  private static final String CONFIG_DBURL = "DBURL";
  private static final String CONFIG_USERNAME = "DBUsername";
  private static final String CONFIG_PASSWORD = "DBPassword";
  private static final String CONFIG_AD = "SQLWindowsAuth";
  private static final String CONFIG_SID = "OracleType";
  private static final String CONFIG_DB2HASSCHEMA = "HasDB2Schema";
  private static final String CONFIG_DB2SCHEMA = "DB2Schema";
  private static final String CONFIG_TIMEOUT = "timeoutConnect";
  //
  private static final int CONFIG_TIMEOUT_DEF = 5000;

  public final static String TYPE_SQLSERVER = "Microsoft SQL Server";
  public final static String TYPE_ORACLE = "Oracle";
  public final static String TYPE_DB2 = "IBM DB2";
  public static final String TYPE_IBMNETEZZA = "IBM Netezza";
  public static final String TYPE_MYSQL = "MySQL";
  public static final String TYPE_POSTGRES = "Postgres";
  public static final String TYPE_INGRES = "Ingres";

  protected String SQLType;
  //
  protected String server;
  protected int port;
  //
  protected String database;
  protected String username;
  protected String password;
  //
  protected boolean sid;
  //
  protected String URL;
  //
  protected boolean windows;
  protected String DB2schema;
  //
  protected int timeoutConnect;
  //
  protected boolean autoCommit = false;

  String connectionUrl;

  String sqlclass;
  boolean prepared = false;

  Exception lastError;
  IContext context;

  private static HashMap<String, DriverInfo> drivers = new HashMap<>();
  private static HashMap<String, String> urlTemplate = new HashMap<>();
  static {
    DBConfig.drivers.put(DBConfig.TYPE_SQLSERVER, new DriverInfo("com.microsoft.sqlserver.jdbc.SQLServerDriver", true));
    DBConfig.drivers.put(DBConfig.TYPE_ORACLE, new DriverInfo("oracle.jdbc.OracleDriver", true));
    DBConfig.drivers.put(DBConfig.TYPE_DB2, new DriverInfo("com.ibm.db2.jcc.DB2Driver", true));
    DBConfig.drivers.put(DBConfig.TYPE_INGRES, new DriverInfo("com.ingres.jdbc.IngresDriver", false));
    DBConfig.drivers.put(DBConfig.TYPE_POSTGRES, new DriverInfo("org.postgresql.Driver", true));
    DBConfig.drivers.put(DBConfig.TYPE_MYSQL, new DriverInfo("com.mysql.jdbc.Driver", true));
    DBConfig.drivers.put(DBConfig.TYPE_IBMNETEZZA, new DriverInfo("org.netezza.Driver", true));
    //
    DBConfig.urlTemplate.put(DBConfig.TYPE_SQLSERVER, null);
    DBConfig.urlTemplate.put(DBConfig.TYPE_ORACLE, null);
    DBConfig.urlTemplate.put(DBConfig.TYPE_DB2, null);
    DBConfig.urlTemplate.put(DBConfig.TYPE_INGRES, "jdbc:ingres://{0}:{1}/{2}");
    DBConfig.urlTemplate.put(DBConfig.TYPE_POSTGRES, "jdbc:postgresql://{0}:{1}/{2}");
    DBConfig.urlTemplate.put(DBConfig.TYPE_MYSQL, "jdbc:mysql://{0}:{1}/{2}");
    DBConfig.urlTemplate.put(DBConfig.TYPE_IBMNETEZZA, "jdbc:netezza://{0}:{1}/{2}");
    //
    DBConfig.setTnsAdmin();
  }

  public DBConfig(final IContext context) {
    if (context != null) {
      setup(context);
    }
    this.context = context;
  }

  private static void setTnsAdmin() {
    if (System.getProperty(DBConfig.ORACLE_NET_TNS_ADMIN) == null) {
      String tnsAdmin = System.getenv(DBConfig.ENV_TNSADMIN);
      if (tnsAdmin == null) {
        final String oracleHome = System.getenv(DBConfig.ENV_ORACLEHOME);
        if (oracleHome != null) {
          tnsAdmin = oracleHome + File.separatorChar + "network" + File.separatorChar + "admin";
        }
      }
      if (tnsAdmin != null) {
        System.setProperty(DBConfig.ORACLE_NET_TNS_ADMIN, tnsAdmin);
      }
    }
  }

  public void setup(final IConfig config) {
    username = config.getConfigString(DBConfig.CONFIG_USERNAME, null);
    password = config.getConfigPassword(DBConfig.CONFIG_PASSWORD);
    server = config.getConfigString(DBConfig.CONFIG_SERVER, null);
    port = config.getConfigInt(DBConfig.CONFIG_PORT, 0);
    database = config.getConfigString(DBConfig.CONFIG_DATABASE, null);
    URL = config.getConfigString(DBConfig.CONFIG_DBURL, null);
    setSQLType(config.getConfigString(DBConfig.CONFIG_TYPE, null));
    windows = config.getConfigBoolean(DBConfig.CONFIG_AD, false);
    sid = DBConfig.ORACLE_CFG_SID.equalsIgnoreCase(config.getConfigString(DBConfig.CONFIG_SID, null));
    DB2schema = config.getConfigBoolean(DBConfig.CONFIG_DB2HASSCHEMA, false) ? config.getConfigString(DBConfig.CONFIG_DB2SCHEMA, null) : null;
    timeoutConnect = config.getConfigInt(DBConfig.CONFIG_TIMEOUT, DBConfig.CONFIG_TIMEOUT_DEF);
    if (timeoutConnect > 0) {
      DriverManager.setLoginTimeout(timeoutConnect / 1000);
    }
    prepared = false;
  }

  public String getSQLType() {
    return SQLType;
  }

  public void setSQLType(final String sqlType) {
    if (context != null) {
      context.debug("Set SQLType = ", sqlType);
    }
    SQLType = sqlType;
  }

  /**
   * setConnectionConfig Formats and sets the 'connectionUrl' and 'sqlclass' variables based on the
   * configuration data set for the plugin.
   *
   * Returns null if everything worked ok Status with error code if the data entered by the user was
   * determined to be invalid.
   */
  public boolean prepareConnection() {
    /*
     * Format the connection URL to use to connect to the database, and determine which sql driver
     * should be used.
     */
    lastError = null;
    connectionUrl = null;
    final DriverInfo info = DBConfig.drivers.get(SQLType);
    if (info == null) {
      lastError = new Exception("Unknown SQLType: " + SQLType);
      return false;
    }
    sqlclass = info.getClassName();
    if (info.isLegacy()) {
      try {
        Class.forName(sqlclass);
      }
      catch (final ClassNotFoundException e) {
        lastError = new Exception("Missing JDBC driver: " + sqlclass);
        return false;
      }
    }
    String template;
    if (LibStr.isNotEmptyOrNull(URL)) {
      template = URL;
    }
    else {
      template = DBConfig.urlTemplate.get(SQLType);
      if (SQLType.equals(DBConfig.TYPE_SQLSERVER)) {
        if (windows) {
          template = "jdbc:sqlserver://{0}:{1};databaseName={2};integratedSecurity=true;";
        }
        else {
          template = "jdbc:sqlserver://{0}:{1};databaseName={2};username={3};password={4};";
        }
      }
      else if (SQLType.equals(DBConfig.TYPE_ORACLE)) {
        if (sid) {
          template = "jdbc:oracle:thin:@{0}:{1}:{2}";
        }
        else {
          template = "jdbc:oracle:thin:@{0}:{1}/{2}";
        }
      }
      else if (SQLType.equals(DBConfig.TYPE_DB2)) {
        if (DB2schema != null) {
          template = "jdbc:db2://{0}:{1}/{2}:currentSchema={5}";
        }
        else {
          template = "jdbc:db2://{0}:{1}/{2}";
        }
      }
    }
    connectionUrl = MessageFormat.format(template, server, Integer.toString(port), database, username, password, DB2schema);
    prepared = (connectionUrl != null);
    return prepared;
  }

  public void releaseConnection(final Connection c) {
    if (c != null) {
      Helper.close(c);
    }
  }

  public Connection getConnection() {
    // Establish the connection.
    if (!prepared && !prepareConnection()) { return null; }
    lastError = null;
    Connection con = null;
    try {
      if (SQLType.equals(DBConfig.TYPE_SQLSERVER)) {
        con = DriverManager.getConnection(connectionUrl);
      }
      else if (SQLType.equals(DBConfig.TYPE_ORACLE)) {
        final Properties props = new Properties();
        props.setProperty(DBConfig.ORACLE_NET_USER, username);
        props.setProperty(DBConfig.ORACLE_NET_PASSWORD, password);
        props.setProperty(DBConfig.ORACLE_NET_CONNECT_TIMEOUT, String.valueOf(timeoutConnect));
        con = DriverManager.getConnection(connectionUrl, props);
      }
      else {
        con = DriverManager.getConnection(connectionUrl, username, password);
      }
      if (con != null) {
        con.setAutoCommit(autoCommit);
      }
    }
    catch (final Exception e) {
      lastError = e;
      if (con != null) {
        Helper.close(con);
        con = null;
      }
    }
    return con;
  }

  public Exception getLastError() {
    return lastError;
  }

  public String getServer() {
    return server;
  }

  public void setServer(final String server) {
    this.server = server;
  }

  public String getConnectionUrl() {
    return connectionUrl;
  }

  public String getDatabase() {
    return database;
  }

  public int getPort() {
    return port;
  }

  public void setPort(final int port) {
    this.port = port;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(final String password) {
    this.password = password;
  }

  public boolean isSid() {
    return sid;
  }

  public void setSid(final boolean sid) {
    this.sid = sid;
  }

  public void setDatabase(final String database) {
    this.database = database;
  }

  @Override
  public String toString() {
    return "DBConfig [SQLType=" + SQLType + ", server=" + server + ", port=" + port + ", database=" + database + ", username=" + username + ", password=" + (password != null ? "YES" : "NO") + ", sid=" + sid + ", URL=" + URL + ", windows=" + windows + ", DB2schema=" + DB2schema + ", connectionUrl="
        + connectionUrl + ", sqlclass=" + sqlclass + ", prepared=" + prepared + "]";
  }

}
