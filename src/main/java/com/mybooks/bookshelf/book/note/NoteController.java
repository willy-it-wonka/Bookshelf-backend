package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.NoteResponse;
import com.mybooks.bookshelf.book.note.payload.UpdateNoteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes")
@Tag(name = "NoteController")
public class NoteController {

    private static final String NOTE_BY_BOOK_ID_SUMMARY = "Get note by book ID";
    private static final String NOTE_CREATION_SUMMARY = "Create a new note";
    private static final String NOTE_UPDATE_SUMMARY = "Update an existing note by book ID";
    private static final String NOTE_DELETION_SUMMARY = "Delete a note by book ID";

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/{bookId}")
    @Operation(summary = NOTE_BY_BOOK_ID_SUMMARY)
    public NoteResponse getNoteByBookId(@PathVariable Long bookId) {
        Note note = noteService.getNoteByBookId(bookId);
        return NoteMapper.mapToNoteResponse(note);
    }

    @PostMapping
    @Operation(summary = NOTE_CREATION_SUMMARY)
    public NoteResponse createNote(@Valid @RequestBody CreateNoteRequest request) {
        Note note = noteService.createNote(request);
        return NoteMapper.mapToNoteResponse(note);
    }

    @PutMapping("/{bookId}")
    @Operation(summary = NOTE_UPDATE_SUMMARY)
    public NoteResponse updateNote(@PathVariable Long bookId, @Valid @RequestBody UpdateNoteRequest request) {
        Note note = noteService.updateNote(bookId, request);
        return NoteMapper.mapToNoteResponse(note);
    }

    @DeleteMapping("/{bookId}")
    @Operation(summary = NOTE_DELETION_SUMMARY)
    public void deleteNoteByBookId(@PathVariable Long bookId) {
        noteService.deleteNoteByBookId(bookId);
    }

}
