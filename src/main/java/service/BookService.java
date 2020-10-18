package service;

import model.Book;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;

public class BookService {
	private final static Connection connection = DbConnection.getInstance().getConnection();

	private final static String SQL_INSERT_BOOK = "INSERT INTO book (title,author,translator,release_date," +
			"chars_count,words_count,sentence_count,paragraph_count) " +
			"VALUES (?,?,?,?,?,?,?,?)";

	private final static String SQL_FIND_BOOK_BY_DETAILS_PREFIX = "SELECT book_id, title,author,translator, " +
			"release_date FROM book WHERE ";

	private final static String SQL_FIND_BOOK_BY_ID = "SELECT * FROM book WHERE book_id = ?";

	private final static String SQL_FIND_ALL_BOOKS = "SELECT * FROM book";

	public static long insertBook(Book book) {
		try {
			PreparedStatement statement = connection.prepareStatement(SQL_INSERT_BOOK,
					Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, book.getTitle());
			statement.setString(2, book.getAuthor());
			statement.setString(3, book.getTranslator());
			if (book.getReleaseDate() == null) statement.setDate(4, null);
			else statement.setDate(4, new Date(book.getReleaseDate().getTime()));
			statement.setInt(5, book.characterCount);
			statement.setInt(6, book.wordCount);
			statement.setInt(7, book.sentenceCount);
			statement.setInt(8, book.paragraphCount);

			System.out.println(statement.toString());

			int affectedRows = statement.executeUpdate();

			if (affectedRows != 0) {
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						book.setId(generatedKeys.getLong(1));
						statement.close();
						return book.getId();
					} else {
						throw new SQLException("Creating book failed, no ID obtained.");
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return -1;
	}

	public static List<Book> findBookByDetails(String title, String author, String translator, Date releaseFrom, Date releaseTo) {
		List<Book> books = new LinkedList<>();
		StringBuilder sb = new StringBuilder(SQL_FIND_BOOK_BY_DETAILS_PREFIX);
		if (title != null)
			sb.append("title LIKE '%").append(title).append("%' AND ");
		if (author != null)
			sb.append("author LIKE '%").append(author).append("%' AND ");
		if (translator != null)
			sb.append("translator LIKE %").append(translator).append("%' AND ");
		if (releaseFrom != null)
			sb.append("release_date >= '").append(releaseFrom).append("' AND ");
		if (releaseFrom != null)
			sb.append("release_date <= '").append(releaseTo).append("' AND ");

		String sql = sb.toString();
		//remove last AND
		sql = sql.substring(0, sql.length() - 4);
		System.out.println(sql);
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next())
				books.add(parseBook(rs));
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return books;
	}

	private static Book parseBook(ResultSet rs) throws SQLException {
		long id = rs.getLong("book_id");
		String title = rs.getString("title");
		String author = rs.getString("author");
		String translator = rs.getString("translator");
		Date releaseDate = null;
		Timestamp ts = rs.getTimestamp("release_date");
		if (ts != null)
			releaseDate = new Date(ts.getTime());
		Book book = new Book(title, author, translator, releaseDate);
		book.characterCount = rs.getInt("chars_count");
		book.wordCount = rs.getInt("words_count");
		book.sentenceCount = rs.getInt("sentence_count");
		book.paragraphCount = rs.getInt("paragraph_count");
		book.setId(id);
		return book;
	}

	public static Book findBookById(long id) {
		Book book = null;
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(SQL_FIND_BOOK_BY_ID);
			statement.setLong(1, id);
			ResultSet rs = statement.executeQuery();
			while (rs.next())
				book = parseBook(rs);
			rs.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return book;
	}

	public static List<Book> getAllBooks() {
		List<Book> books = new LinkedList<>();
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(SQL_FIND_ALL_BOOKS);
			while (rs.next())
				books.add(parseBook(rs));
			rs.close();
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return books;
	}
}
