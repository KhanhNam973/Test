package cinema.ticket.booking;
import cinema.ticket.booking.model.Genre;
import cinema.ticket.booking.repository.GenreReposity;
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
    void GENRE_001_testSaveGenre() {
        Genre genre = new Genre();
        genre.setGenre("Anime");
        Genre savedGenre = genreService.saveGenre(genre);

        assertNotNull(savedGenre.getId());
        assertEquals("Anime", savedGenre.getGenre());
    }
    
//Save a genre that already exists
    @Test
    void GENRE_002_testSave_DuplicateGenre() {
        Genre duplicateGenre = new Genre();
        duplicateGenre.setGenre("Comedy");
        Exception e=assertThrows(MyBadRequestException.class, () -> genreService.saveGenre(duplicateGenre));
        List<Genre> a= genreReposity.findAllByGenre("Comedy");
        assertEquals(a.size(),1);
        assertNotNull(e);
    }

//Get all genres
    @Test
    void GENRE_003_testGetGenres() {
        Genre genre = new Genre();
        genre.setGenre("Hello");
        genreService.saveGenre(genre);
        List<Genre> genres = genreService.getGenres();
        assertFalse(genres.isEmpty());
    }

//Get a genre by ID
    @Test
    void GENRE_004_testGetGenre() {
        Genre foundGenre = genreService.getGenre(1L);
        assertEquals("Science Fiction", foundGenre.getGenre());
    }

//Get a genre by ID that does not exist
void GENRE_005_testGetGenre_NoExist() {
    Genre foundGenre = genreService.getGenre(9999L);
    assertNull(foundGenre.getGenre());
}

//Update a genre
    @Test
    void GENRE_006_testUpdateGenre() {
        Genre genre = new Genre();
        genre.setGenre("Light novel");
        Genre savedGenre = genreService.saveGenre(genre);
        savedGenre.setGenre("Hello World");
        Genre updatedG = genreService.updateGenre(savedGenre);
        assertEquals("Hello World", updatedG.getGenre());
        assertNotEquals("Light novel", updatedG.getGenre());
    }

//Update a genre that does not exist
    @Test
    void GENRE_007_testUpdateGenre_NonExist() {
        Genre genre = new Genre();
        genre.setId(9999L);
        genre.setGenre("Non-Existent");
        MyNotFoundException f= assertThrows(MyNotFoundException.class, () -> genreService.updateGenre(genre));
        assertNotNull(f);
    }
//Update a genre with a name that already exists
    @Test
    void GENRE_008_testUpdateGenre_ExistingName() {
        Genre genre = genreService.getGenre(1L);
        genre.setGenre("Action");
        MyBadRequestException f=assertThrows(MyBadRequestException.class, () -> genreService.updateGenre(genre));
        //genreService.updateGenre(genre);
        List<Genre> a= genreReposity.findAllByGenre("Action");
        assertEquals(1,a.size());
        assertNotNull(f);
    }

//Delete a genre
    @Test
    void GENRE_009_testDeleteGenre() {
        Genre genre = new Genre();
        genre.setGenre("Dramatic");
        Genre savedGenre = genreService.saveGenre(genre);

        genreService.deleteGenre(savedGenre.getId());
        Genre a=genreReposity.findByGenre("Dramatic");
        assertNull(a);
    }

//Delete a genre that does not exist
    @Test
    void GENRE_010_testDeleteGenre_NoExist() {
        List<Genre>a= genreReposity.findAll();
        MyNotFoundException e=assertThrows(MyNotFoundException.class, () -> genreService.deleteGenre(9999L));
        List<Genre>b= genreReposity.findAll();
        assertNotNull(e);
        assertEquals(a.size(), b.size());
    }

//Save a list of genres
    @Test
    void GENRE_011_testSaveListGenres() {
        Genre genre1 = new Genre();
        genre1.setGenre("Criminal");

        Genre genre2 = new Genre();
        genre2.setGenre("Romantic");

        List<Genre> genres = Arrays.asList(genre1, genre2);
        genreService.saveListGenres(genres);
        assertNotNull(genreReposity.findByGenre("Romantic"));
        assertNotNull(genreReposity.findByGenre("Criminal"));
    }

//Save a list of genres with one that already exists
    @Test
    void GENRE_012_testSaveListGenres_HasExisted() {
        Genre genre1 = new Genre();
        genre1.setGenre("Action");

        Genre genre2 = new Genre();
        genre2.setGenre("Romantic");

        List<Genre> genres = Arrays.asList(genre1, genre2);
        genreService.saveListGenres(genres);
        List<Genre>a=genreReposity.findAllByGenre("Action");
        assertEquals(1,a.size());;
        assertNotNull(genreReposity.findByGenre("Romantic"));
    }
//Save a null genre
    @Test
    void GENRE_013_testSave_NullNameGenre() {
        int a= genreReposity.findAll().size();
        Genre genre = new Genre();
        genre.setGenre(null);
        //MyBadRequestException e=
        assertThrows(
            MyBadRequestException.class,
        () -> genreService.saveGenre(genre));
        //genreService.saveGenre(genre);
        int b= genreReposity.findAll().size();
        assertEquals(a, b);
        //assertNotNull(e);
    }
//Update a genre to null name
    @Test
    void GENRE_014_testUpdate_NullNameGenre() {
        Genre genre = genreService.getGenre(1L);
        genre.setGenre(null);
        MyBadRequestException e=assertThrows(MyBadRequestException.class, () -> genreService.updateGenre(genre));
        assertNotNull(e);
        Genre genre1 = genreService.getGenre(1L);
        assertEquals(genre1.getGenre(), "Science Fiction");;
    } 
// Save a list with null genre
@Test
    void GENRE_015_testSaveListGenres_HasNullName() {
        int a= genreReposity.findAll().size();
        Genre genre1 = new Genre();
        genre1.setGenre(null);

        Genre genre2 = new Genre();
        genre2.setGenre("Romantic");
        
        List<Genre> genres = Arrays.asList(genre1, genre2);
        //MyBadRequestException e=
        assertThrows(
            MyBadRequestException.class, () 
            ->genreService.saveListGenres(genres));
        //genreService.saveListGenres(genres);
        int b= genreReposity.findAll().size();
        assertEquals(a+1,b);
        //assertNotNull(e);
    }

}
