package app.quranhub.data.remote.model;

import java.util.List;

public class BooksResponse {

    private List<BookContent> books;

    public BooksResponse(List<BookContent> books) {
        this.books = books;
    }

    public List<BookContent> getBooks() {
        return books;
    }

    public void setBooks(List<BookContent> books) {
        this.books = books;
    }

}
