package service;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton class that stores connection to DB
 */
public class DbConnection {
	// JDBC driver name and database URL
	private static final String JDBC_DRIVER = "org.postgresql.Driver";
	private static final String DB_URL = "jdbc:postgresql://localhost:5432/";
	private static final String DB_NAME = "concordance";

	//  Database credentials
	private static final String USER = "postgres";
	private static final String PASS = "admin";

	private static DbConnection instance;
	private Connection connection;

	private DbConnection() {
		try {
			Class.forName(JDBC_DRIVER);
			System.out.println("Connecting to database...");
			this.connection = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
			System.out.println("Connected to DB");
		} catch (ClassNotFoundException | SQLException ex) {
			System.out.println("Database Connection Creation Failed : " + ex.getMessage());
		}
	}


	/**
	 * Init DB schema from sql script
	 */
	public static void initSchema() {
		try {
			Class.forName(JDBC_DRIVER);
			System.out.println("Connecting to database...");
			Connection connection = DriverManager.getConnection(DB_URL, USER, PASS);

			Statement stm = connection.createStatement();
			try {
				stm.executeUpdate("CREATE DATABASE " + DB_NAME);
			} catch (Exception e) {
			}
			stm.close();
			connection.close();

			connection = DriverManager.getConnection(DB_URL + DB_NAME, USER, PASS);
			//Initialize the script runner
			ScriptRunner sr = new ScriptRunner(connection);
			//Creating a reader object

			Reader reader = null;
			try {
				reader = new BufferedReader(new FileReader
						(new File(DbConnection.class.getClassLoader().getResource("schema.sql").toURI())));
				//Running the script
				sr.runScript(reader);
			} catch (Exception e) {
				System.out.println("Sql script not found! : " + e.getMessage());
			}
			connection.close();
		} catch (Exception ex) {
			System.out.println("Database Initialization Failed : " + ex.getMessage());
		}
	}

	/**
	 * @return connection to DB
	 */
	public Connection getConnection() {
		return connection;
	}

	public static DbConnection getInstance() {
		if (instance == null) {
			instance = new DbConnection();
		} else {
			try {
				if (instance.getConnection().isClosed()) {
					instance = new DbConnection();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return instance;
	}

	/**
	 * Close connection to DB
	 */
	public void closeConnection() {
		if (instance != null) {
			try {
				instance.connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			System.out.println("Connection safely closed!");
		}
	}


	/**
	 * Removes all data from DB and reset serials
	 */
	public void clearDB() {
		try {
			Statement stm = connection.createStatement();
			stm.executeUpdate("TRUNCATE word_in_book, word_in_group, word_in_phrase, word, phrase, groups, book RESTART IDENTITY CASCADE");
			stm.close();
		} catch (SQLException e) {
			System.out.print("Failed to clear the DB data");
		}
	}
}
