package service;

import model.WordLocation;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WordService {
	private final static Connection connection = DbConnection.getInstance().getConnection();

	private final static String SQL_INSERT_WORD = "INSERT INTO word (value) VALUES (?)";
	private final static String SQL_SELECT_ALL_WORDS = "SELECT value,word_id from word";
	private final static String SQL_SELECT_BY_VALUE = "SELECT word_id from word WHERE value = ?";
	private final static String SQL_INSERT_WORD_LOCATION = "INSERT INTO word_in_book " +
			"(word,book_id,index,line,index_in_line,sentence,paragraph,is_quote_before,is_quote_after,punctuation_mark)" +
			" VALUES (?,?,?,?,?,?,?,?,?,?)";

	private final static String SQL_FIND_LOCATIONS_BY_WORD = "SELECT * from word_in_book where word = ? ORDER by book_id, index";
	private final static String SQL_FIND_LOCATIONS_BY_WORD_AND_BOOK = "SELECT * from word_in_book where word = ? AND book_id = ? ORDER by index";
	private final static String SQL_PREVIEW = "SELECT word, is_quote_before, is_quote_after, punctuation_mark from word_in_book where book_id = ? AND paragraph = ? ORDER by index";

	public static long insertWord(String word) {
		long id = findWordByValue(word);
		if (id > 0) return id;
		try {
			PreparedStatement statement = connection.prepareStatement(SQL_INSERT_WORD,
					Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, word);

//			System.out.println(statement.toString());

			int affectedRows = statement.executeUpdate();

			if (affectedRows != 0) {
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						id = generatedKeys.getLong(1);
						statement.close();
						return id;
					} else {
						throw new SQLException("Creating word failed, no ID obtained.");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public static long findWordByValue(String word) {
		PreparedStatement statement = null;
		long id = -1;
		try {
			statement = connection.prepareStatement(SQL_SELECT_BY_VALUE);
			statement.setString(1, word);
			ResultSet rs = statement.executeQuery();
			while (rs.next())
				id = rs.getLong("word_id");
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return id;
	}

	public static Map<String, Long> getAllWordsId() {
		Map<String, Long> words = new HashMap<>();
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(SQL_SELECT_ALL_WORDS);
			while (rs.next())
				words.put(rs.getString(1), rs.getLong(2));
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return words;
	}

	public static void addWordLocationList(List<WordLocation> wordLocationList, long bookId) {
		try {
			PreparedStatement statement = connection.prepareStatement(SQL_INSERT_WORD_LOCATION);
			for (WordLocation wordLocation : wordLocationList) {
				statement.setString(1, wordLocation.getWord());
				statement.setLong(2, bookId);
				statement.setInt(3, wordLocation.getIndex());
				statement.setInt(4, wordLocation.getLine());
				statement.setInt(5, wordLocation.getIndexInLine());
				statement.setInt(6, wordLocation.getSentence());
				statement.setInt(7, wordLocation.getParagraph());
				statement.setBoolean(8, wordLocation.isQuoteAfter());
				statement.setBoolean(9, wordLocation.isQuoteAfter());
				statement.setString(10, wordLocation.getPunctuationMark());
				statement.addBatch();
				statement.clearParameters();
			}
			int[] results = statement.executeBatch();
			System.out.println("Loaded " + results.length + " word locations");
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static List<WordLocation> findWordInBooks(String word, Long bookId) {
		List<WordLocation> wordLocations = new LinkedList<>();
		PreparedStatement statement = null;
		try {
			if (bookId == null)
				statement = connection.prepareStatement(SQL_FIND_LOCATIONS_BY_WORD);

			else {
				statement = connection.prepareStatement(SQL_FIND_LOCATIONS_BY_WORD_AND_BOOK);
				statement.setLong(2, bookId);
			}
			statement.setString(1, word);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				WordLocation location = new WordLocation(word, rs.getInt("index"), rs.getInt("line"),
						rs.getInt("index_in_line"), rs.getInt("sentence"), rs.getInt("paragraph"));
				location.setBookId(rs.getLong("book_id"));
				wordLocations.add(location);
			}
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return wordLocations;
	}

	public static String buildPreview(long bookId, int paragraph) {
		StringBuilder sb = new StringBuilder();
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(SQL_PREVIEW);
			statement.setLong(1, bookId);
			statement.setInt(2, paragraph);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				sb.append(rs.getBoolean("is_quote_before") ? "\"" : "").append(rs.getString("word"))
						.append(rs.getBoolean("is_quote_after") ? "\"" : "");
				String punctuationMark = rs.getString("punctuation_mark");
				if(punctuationMark!=null) sb.append(punctuationMark);
				sb.append(" ");
			}

			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
