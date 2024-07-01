package com.mybooks.bookshelfSB.book.note;

import com.mybooks.bookshelfSB.book.note.payload.CreateNoteRequest;
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
    public Note getNoteByBookId(@PathVariable Long bookId) {
        return noteService.getNoteByBookId(bookId);
    }

    @PostMapping("/notes")
    public Note createNote(@RequestBody CreateNoteRequest request) {
        return noteService.createNote(request);
    }

    @PutMapping("/notes/{bookId}")
    public Note updateNote(@PathVariable Long bookId, @RequestBody UpdateNoteRequest request) {
        return noteService.updateNote(bookId, request);
    }

    @DeleteMapping("/notes/{bookId}")
    public void deleteNoteByBookId(@PathVariable Long bookId) {
        noteService.deleteNoteByBookId(bookId);
    }

}
