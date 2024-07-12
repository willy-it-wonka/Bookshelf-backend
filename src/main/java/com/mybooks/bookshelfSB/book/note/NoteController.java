package com.mybooks.bookshelfSB.book.note;

import com.mybooks.bookshelfSB.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelfSB.book.note.payload.NoteResponse;
import com.mybooks.bookshelfSB.book.note.payload.UpdateNoteRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/notes/{bookId}")
    public NoteResponse getNoteByBookId(@PathVariable Long bookId) {
        Note note = noteService.getNoteByBookId(bookId);
        return convertToDto(note);
    }

    @PostMapping("/notes")
    public NoteResponse createNote(@RequestBody CreateNoteRequest request) {
        Note note = noteService.createNote(request);
        return convertToDto(note);
    }

    @PutMapping("/notes/{bookId}")
    public NoteResponse updateNote(@PathVariable Long bookId, @RequestBody UpdateNoteRequest request) {
        Note note = noteService.updateNote(bookId, request);
        return convertToDto(note);
    }

    @DeleteMapping("/notes/{bookId}")
    public void deleteNoteByBookId(@PathVariable Long bookId) {
        noteService.deleteNoteByBookId(bookId);
    }

    private NoteResponse convertToDto(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getContent(),
                note.getBook().getId());
    }

}
