package com.mybooks.bookshelf.book.note;

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

public class InMemoryNoteRepository implements NoteRepository {

    private final Map<Long, Note> notes = new HashMap<>();
    private long mapKey = 1; // Also to simulate note.id

    @Override
    public Optional<Note> findByBookId(@NonNull Long bookId) {
        return notes.values().stream()
                .filter(note -> note.getBook().getId().equals(bookId))
                .findFirst();
    }

    @Override
    @NonNull
    public Optional<Note> findById(@NonNull Long id) {
        return Optional.ofNullable(notes.get(id));
    }

    @Override
    @NonNull
    public <S extends Note> S save(S note) {
        if (note.getId() == null) // It's always null.
            note.setId(mapKey++);
        notes.put(note.getId(), note);
        return note;
    }

    @Override
    public void deleteByBookId(@NonNull Long bookId) {
        notes.values().removeIf(note -> note.getBook().getId().equals(bookId));
    }

    public void clear() {
        notes.clear();
        mapKey = 1;
    }



    /*
     * Below methods are not used. == Not implemented.
     */

    @Override
    public void flush() {
    }

    @Override
    public <S extends Note> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends Note> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<Note> entities) {
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
    }

    @Override
    public void deleteAllInBatch() {
    }

    @Override
    public Note getOne(Long aLong) {
        return null;
    }

    @Override
    public Note getById(Long aLong) {
        return null;
    }

    @Override
    public Note getReferenceById(Long aLong) {
        return null;
    }

    @Override
    public <S extends Note> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends Note> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends Note> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public List<Note> findAll() {
        return null;
    }

    @Override
    public List<Note> findAllById(Iterable<Long> longs) {
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
    public void deleteById(Long aLong) {
    }

    @Override
    public void delete(Note entity) {
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
    }

    @Override
    public void deleteAll(Iterable<? extends Note> entities) {
    }

    @Override
    public void deleteAll() {
    }

    @Override
    public <S extends Note> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends Note> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends Note> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Note> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends Note, R> R findBy(Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public List<Note> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<Note> findAll(Pageable pageable) {
        return null;
    }

}
