package cinema.ticket.booking;
import cinema.ticket.booking.model.Genre;
import cinema.ticket.booking.repository.GenreReposity;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.service.impl.GenreServiceImpl;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
//OK
@SpringBootTest
@Transactional 
class GenreServiceImplTest {

    @Autowired
    private GenreServiceImpl genreService;

    @Autowired
    private GenreReposity genreReposity;
//Save Genre
    @Test
    void testSaveGenre() {
        Genre genre = new Genre();
        genre.setGenre("Anime");
        Genre savedGenre = genreService.saveGenre(genre);

        assertNotNull(savedGenre.getId());
        assertEquals("Anime", savedGenre.getGenre());
    }
    
//Save a genre that already exists
    @Test
    void testSave_DuplicateGenre() {
        Genre duplicateGenre = new Genre();
        duplicateGenre.setGenre("Comedy");
        Exception e=assertThrows(MyBadRequestException.class, () -> genreService.saveGenre(duplicateGenre));
        assertEquals("This genre is existed", e.getMessage());
    }

//Get all genres
    @Test
    void testGetGenres() {
        Genre genre = new Genre();
        genre.setGenre("Hello");
        genreService.saveGenre(genre);
        List<Genre> genres = genreService.getGenres();
        assertFalse(genres.isEmpty());
    }

//Get a genre by ID
    @Test
    void testGetGenre() {
        Genre foundGenre = genreService.getGenre(1L);
        assertEquals("Hi", foundGenre.getGenre());
    }

//Get a genre by ID that does not exist
void testGetGenre_NoExist() {
    Genre foundGenre = genreService.getGenre(9999L);
    assertNull(foundGenre.getGenre());
}

//Update a genre
    @Test
    void testUpdateGenre() {
        Genre genre = new Genre();
        genre.setGenre("Light novel");
        Genre savedGenre = genreService.saveGenre(genre);
        savedGenre.setGenre("Hello World");
        Genre updatedG = genreService.updateGenre(savedGenre);
        assertEquals("Hello World", updatedG.getGenre());
        assertNotEquals("Light novel", updatedG.getGenre());
    }
    //Update a genre but in the final code use save method

//Update a genre that does not exist
    @Test
    void testUpdateGenre_NonExist() {
        Genre genre = new Genre();
        genre.setId(9999L);
        genre.setGenre("Non-Existent");
        MyNotFoundException f= assertThrows(MyNotFoundException.class, () -> genreService.updateGenre(genre));
        assertEquals(f.getMessage(),"Genre ID not found");
    }
//Update a genre with a name that already exists
    @Test
    void testUpdateGenre_ExistingName() {
        Genre genre = genreService.getGenre(1L);
        genre.setGenre("Action");
        MyBadRequestException f=assertThrows(MyBadRequestException.class, () -> genreService.updateGenre(genre));
        assertEquals(f.getMessage(), "Can not update this name because another genre has this one");
        //Wrong message in code
    }

//Delete a genre
    @Test
    void testDeleteGenre() {
        Genre genre = new Genre();
        genre.setGenre("Dramatic");
        Genre savedGenre = genreService.saveGenre(genre);

        MyApiResponse response = genreService.deleteGenre(savedGenre.getId());
        assertEquals("Delete genre ID " + savedGenre.getId(), response.getMessage());

        assertThrows(MyNotFoundException.class, () -> genreService.getGenre(savedGenre.getId()));
    }

//Delete a genre that does not exist
    @Test
    void testDeleteGenre_NoExist() {
        MyNotFoundException e=assertThrows(MyNotFoundException.class, () -> genreService.deleteGenre(9999L));
        assertEquals(e.getMessage(), "Genre ID 9999 not found");
    }

//Save a list of genres
    @Test
    void testSaveListGenres() {
        Genre genre1 = new Genre();
        genre1.setGenre("Criminal");

        Genre genre2 = new Genre();
        genre2.setGenre("Romantic");

        genreService.saveGenre(genre1); // Save one genre manually

        List<Genre> genres = Arrays.asList(genre1, genre2);
        MyApiResponse response = genreService.saveListGenres(genres);

        assertEquals("Success", response.getMessage());
        assertNotNull(genreReposity.findByGenre("Romantic"));
    }

//Save a list of genres with one that already exists
    @Test
    void testSaveListGenres_HasExisted() {
        Genre genre1 = new Genre();
        genre1.setGenre("Action");

        Genre genre2 = new Genre();
        genre2.setGenre("Romantic");

        List<Genre> genres = Arrays.asList(genre1, genre2);
        MyApiResponse response = genreService.saveListGenres(genres);

        assertEquals("Success", response.getMessage());
        assertNotNull(genreReposity.findByGenre("Romantic"));
    }
}
