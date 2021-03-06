package gui;

import model.Book;
import service.FilesManager;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

/**
GUI class of word search options
including searching a specific word - shows all locations of this word
and view the paragraph that contains the word in each location.
 */
public class ShowWords extends JFrame {
	private JPanel searchDetailsPanel, searchButtonPanel, north, center, searchWords, chooseBook;
    private JButton search;
	private JTextField wordTextField;
	private JLabel enterWord, chooseBookLabel;
    private JComboBox<String> booksList;
	private TextPreviewComponent context;
	private LocationsTableComponent locationsTable;
	private ArrayList<Integer> bookIdList;
    private ArrayList<Book> books;
    private String word;

    private final Border BORDER = BorderFactory.createLineBorder(GuiConstants.DEFAULT, 2);

    public ShowWords(){
        setTitle("Show Words");

	    this.books = FilesManager.getInstance().getBooks();
        word = "";
	    bookIdList = new ArrayList<>();

        north = new JPanel();
	    searchDetailsPanel = new JPanel();
	    searchButtonPanel = new JPanel();
        center = new JPanel();
        center.setLayout(new GridLayout(2,1,0,0));
        center.setBorder(BORDER);
        searchWords = new JPanel();
        searchWords.setBorder(BORDER);
        chooseBook = new JPanel();
        chooseBook.setBorder(BORDER);

        search = new JButton("Search");
        search.setFont(GuiConstants.MY_FONT);

        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
	            context.clearText();
	            word = wordTextField.getText();
	            bookIdList = locationsTable.searchWord(books, booksList.getSelectedIndex(), new String[]{word});
            }
        });

	    enterWord = new JLabel("Enter a word : ");
        enterWord.setFont(GuiConstants.MY_FONT);
	    wordTextField = new JTextField("");
	    wordTextField.setColumns(15);
	    wordTextField.setFont(GuiConstants.MY_FONT);
        chooseBookLabel = new JLabel("Choose a book");
        chooseBookLabel.setFont(GuiConstants.MY_FONT);

	    locationsTable = new LocationsTableComponent();
        JScrollPane tableSP=new JScrollPane(locationsTable);
        tableSP.setVisible(true);

        locationsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                int row = locationsTable.getSelectedRow();
	            context.createPreview(new String[]{word}, bookIdList.get(row), (int) locationsTable.getValueAt(row, 4));
            }
        });

        if (books.size() == 0){
            booksList = new JComboBox<>(new String[]{"No books to show"});
        }else {
            String[] booksArray = new String[books.size()+1];
            booksArray[0] = "All";
            int i = 1;
            for (Book curr : books) {
                booksArray[i] = curr.getTitle();
                i++;
            }
            booksList = new JComboBox<>(booksArray);
        }

	    context = new TextPreviewComponent(false);

	    searchButtonPanel.add(search);
        searchWords.add(enterWord);
	    searchWords.add(wordTextField);
        chooseBook.add(chooseBookLabel);
        chooseBook.add(booksList);
	    searchDetailsPanel.add(searchWords);
	    searchDetailsPanel.add(chooseBook);

	    north.add(searchDetailsPanel);
	    north.add(searchButtonPanel);

        center.add(tableSP);
	    center.add(context);

        add(north, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
    }
}

