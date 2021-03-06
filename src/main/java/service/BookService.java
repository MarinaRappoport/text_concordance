package service;

import model.Book;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Service for all SQL operation with table 'book'
 */
public class BookService {
	private final static Connection connection = DbConnection.getInstance().getConnection();

	private final static String SQL_INSERT_BOOK = "INSERT INTO book (title,author,translator,release_date," +
			"chars_count,words_count,sentence_count,line_count,paragraph_count,path) " +
			"VALUES (?,?,?,?,?,?,?,?,?,?)";

	private final static String SQL_FIND_BOOK_BY_DETAILS_PREFIX = "SELECT * FROM book WHERE ";

	private final static String SQL_FIND_BOOK_BY_ID = "SELECT * FROM book WHERE book_id = ?";

	private final static String SQL_FIND_ALL_BOOKS = "SELECT * FROM book ORDER by book_id";

	private final static String SQL_IF_BOOK_EXISTS = "SELECT COUNT(book_id) FROM book WHERE title = ? AND author = ?";

	/**
	 * Add new book to DB
	 *
	 * @param book book to instert
	 * @return book id
	 */
	public static int insertBook(Book book) {
		boolean isAlreadyExist = false;
		try {
			PreparedStatement stmt = connection.prepareStatement(SQL_IF_BOOK_EXISTS);
			stmt.setString(1, book.getTitle());
			stmt.setString(2, book.getAuthor());
			ResultSet rs = stmt.executeQuery();
			while (rs.next())
				isAlreadyExist = rs.getInt(1) > 0;
			rs.close();
			stmt.close();
			if (isAlreadyExist) return 0;

			PreparedStatement statement = connection.prepareStatement(SQL_INSERT_BOOK,
					Statement.RETURN_GENERATED_KEYS);
			fillTheParams(book, statement);

			System.out.println(statement.toString());

			int affectedRows = statement.executeUpdate();

			if (affectedRows != 0) {
				try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						book.setId(generatedKeys.getInt(1));
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

	/**
	 * Search book by details (also partly)
	 * @param title title of the book or part of it
	 * @param author author of the book or part of it
	 * @param translator translator of the book or part of it
	 * @param releaseFrom  release date minimum
	 * @param releaseTo release date maximum
	 * @return list or books (result of the search), could be empty
	 */
	public static List<Book> findBookByDetails(String title, String author, String translator, String releaseFrom, String releaseTo) {
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
		if (releaseTo != null)
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
		int id = rs.getInt("book_id");
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
		book.lineCount = rs.getInt("line_count");
		book.paragraphCount = rs.getInt("paragraph_count");
		book.setPath(rs.getString("path"));
		book.setId(id);
		return book;
	}

	/**
	 * Search book by id
	 */
	public static Book findBookById(int id) {
		Book book = null;
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(SQL_FIND_BOOK_BY_ID);
			statement.setInt(1, id);
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

	/**
	 * @return all books from DB
	 */
	public static ArrayList<Book> getAllBooks() {
		ArrayList<Book> books = new ArrayList<>();
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

	/**
	 * Insert several books to DB in batch
	 */
	public static void addBooks(List<Book> books) {
		try {
			PreparedStatement statement = connection.prepareStatement(SQL_INSERT_BOOK);
			for (Book book : books) {
				fillTheParams(book, statement);
				statement.addBatch();
				statement.clearParameters();
			}
			int[] results = statement.executeBatch();
			System.out.println("Loaded " + results.length + " books");
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void fillTheParams(Book book, PreparedStatement statement) throws SQLException {
		statement.setString(1, book.getTitle());
		statement.setString(2, book.getAuthor());
		statement.setString(3, book.getTranslator());
		if (book.getReleaseDate() == null) statement.setDate(4, null);
		else statement.setDate(4, new Date(book.getReleaseDate().getTime()));
		statement.setInt(5, book.characterCount);
		statement.setInt(6, book.wordCount);
		statement.setInt(7, book.sentenceCount);
		statement.setInt(8, book.lineCount);
		statement.setInt(9, book.paragraphCount);
		statement.setString(10, book.getPath());
	}
}
