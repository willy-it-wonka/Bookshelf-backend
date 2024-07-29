package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.user.User;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryBookRepository implements BookRepository {

    private final Map<Long, Book> books = new HashMap<>();
    private long mapKey = 1; // Also to simulate book.id

    @Override
    @NonNull
    public Optional<Book> findById(@NonNull Long id) {
        return Optional.ofNullable(books.get(id));
    }

    @Override
    @NonNull
    public <S extends Book> S save(S book) {
        if (book.getId() == null) // Always is null.
            book.setId(mapKey++);
        books.put(book.getId(), book);
        return book;
    }

    @Override
    public void deleteById(@NonNull Long id) {
        books.remove(id);
    }

    @Override
    public List<Book> findByBookOwner(User user) {
        return books.values().stream()
                .filter(book -> book.getBookOwner().equals(user))
                .toList();
    }

    @Override
    public List<Book> findByStatusAndBookOwner(BookStatus status, User user) {
        return books.values().stream()
                .filter(book -> book.getStatus() == status && book.getBookOwner().equals(user))
                .toList();
    }

    public void clear() {
        books.clear();
        mapKey = 1;
    }



    /*
     * Below methods are not used. == Not implemented.
     */

    @Override
    public <S extends Book> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public List<Book> findAll() {
        return null;
    }

    @Override
    public List<Book> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(Book entity) {
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
    }

    @Override
    public void deleteAll(Iterable<? extends Book> entities) {
    }

    @Override
    public void deleteAll() {
    }

    @Override
    public List<Book> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Book> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Book> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Book> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Book> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Book> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Book, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public void flush() {
    }

    @Override
    public <S extends Book> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Book> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Book> entities) {
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
    }

    @Override
    public void deleteAllInBatch() {
    }

    @Override
    public Book getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Book> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Book> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public Book getOne(Long aLong) {
        return null;
    }

    @Override
    public Book getById(Long aLong) {
        return null;
    }
}