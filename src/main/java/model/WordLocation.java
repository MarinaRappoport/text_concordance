package model;

public class WordLocation {
	private long wordId;
	private String word;
	private int bookId;
	private int index;
    private int line;
    private int indexInLine;
	private int sentence;
	private int paragraph;

	public WordLocation(String word, int bookId, int index, int line, int indexInLine, int sentence, int paragraph) {
		this.word = word;
		this.bookId = bookId;
		this.index = index;
		this.line = line;
		this.indexInLine = indexInLine;
		this.sentence = sentence;
		this.paragraph = paragraph;
	}

	public String getWord() {
		return word;
	}

	public void setWordId(long wordId) {
		this.wordId = wordId;
	}

	public long getWordId() {
		return wordId;
	}

	public int getBookId() {
		return bookId;
	}

	public int getIndex() {
		return index;
	}

	public int getSentence() {
		return sentence;
	}

	public int getLine() {
		return line;
	}

	public int getIndexInLine() {
		return indexInLine;
	}

	public int getParagraph() {
		return paragraph;
	}
}