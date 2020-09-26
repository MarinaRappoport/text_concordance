import model.Book;
import model.WordLocation;
import org.junit.jupiter.api.Test;
import service.BookService;
import service.DbConnection;
import service.FileParser;
import service.WordService;

import java.util.Date;
import java.util.List;

public class DbTest {

	@Test
	public void wordLoadTest(){
		FileParser parser = new FileParser();
		List<WordLocation> wordLocationList = parser.parseFile(new Book("D:\\uni\\sql_seminar\\text_books\\pg5200.txt"));
		Date start = new Date();
		for (WordLocation wordLocation: wordLocationList) {
			long bookId = WordService.insertWord(wordLocation.getWord());
			wordLocation.setWordId(bookId);
			WordService.addWordPosition(wordLocation);
		}
		Date end = new Date();
		long seconds = (end.getTime() - start.getTime())/(1000);
		System.out.println(seconds + " secs");
	}

	@Test
	public void init() {
		//to init the schema
		DbConnection.initSchema();
	}

	@Test
	public void testWordService(){
		System.out.println(WordService.insertWord("am"));
		System.out.println(WordService.insertWord("but"));
		System.out.println(WordService.insertWord("buy"));
		System.out.println(WordService.insertWord("am"));

		System.out.println(WordService.findWordByValue("am"));
		System.out.println(WordService.findWordByValue("buy"));
		System.out.println(WordService.findWordByValue("no"));
	}

	public void testBookService(){
		Book book = new Book("Book2", "Author2", "", "");
		BookService.insertBook(book);

		List<Book> books = BookService.findBookByDetails("Book", null, null, null, null);
		System.out.println("Found " + books.size() + " books");
	}


}