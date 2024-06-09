package com.mybooks.bookshelfSB.book.note;

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
    public Note createNote(@RequestBody Note note) {
        return noteService.createNote(note);
    }

    @PutMapping("/notes/{bookId}")
    public Note updateNote(@PathVariable Long bookId, @RequestBody Note note) {
        return noteService.updateNote(bookId, note);
    }

    @DeleteMapping("/notes/{bookId}")
    public void deleteNoteByBookId(@PathVariable Long bookId) {
        noteService.deleteNoteByBookId(bookId);
    }

}
