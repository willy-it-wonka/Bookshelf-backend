package com.mybooks.bookshelfSB.book.note;

import com.mybooks.bookshelfSB.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelfSB.book.note.payload.NoteDto;
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
    public NoteDto getNoteByBookId(@PathVariable Long bookId) {
        Note note = noteService.getNoteByBookId(bookId);
        return convertToDto(note);
    }

    @PostMapping("/notes")
    public NoteDto createNote(@RequestBody CreateNoteRequest request) {
        Note note = noteService.createNote(request);
        return convertToDto(note);
    }

    @PutMapping("/notes/{bookId}")
    public NoteDto updateNote(@PathVariable Long bookId, @RequestBody UpdateNoteRequest request) {
        Note note = noteService.updateNote(bookId, request);
        return convertToDto(note);
    }

    @DeleteMapping("/notes/{bookId}")
    public void deleteNoteByBookId(@PathVariable Long bookId) {
        noteService.deleteNoteByBookId(bookId);
    }

    private NoteDto convertToDto(Note note) {
        return new NoteDto(
                note.getId(),
                note.getContent(),
                note.getBook().getId());
    }

}
