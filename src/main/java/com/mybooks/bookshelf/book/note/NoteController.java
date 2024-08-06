package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.NoteResponse;
import com.mybooks.bookshelf.book.note.payload.UpdateNoteRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes")
@Tag(name = "NoteController")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping("/{bookId}")
    public NoteResponse getNoteByBookId(@PathVariable Long bookId) {
        Note note = noteService.getNoteByBookId(bookId);
        return NoteMapper.mapToNoteResponse(note);
    }

    @PostMapping
    public NoteResponse createNote(@RequestBody CreateNoteRequest request) {
        Note note = noteService.createNote(request);
        return NoteMapper.mapToNoteResponse(note);
    }

    @PutMapping("/{bookId}")
    public NoteResponse updateNote(@PathVariable Long bookId, @RequestBody UpdateNoteRequest request) {
        Note note = noteService.updateNote(bookId, request);
        return NoteMapper.mapToNoteResponse(note);
    }

    @DeleteMapping("/{bookId}")
    public void deleteNoteByBookId(@PathVariable Long bookId) {
        noteService.deleteNoteByBookId(bookId);
    }

}
