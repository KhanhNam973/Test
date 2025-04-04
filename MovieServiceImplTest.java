package cinema.ticket.booking;

import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.Genre;
import cinema.ticket.booking.model.Movie;
import cinema.ticket.booking.repository.GenreReposity;
import cinema.ticket.booking.repository.MovieRepo;
import cinema.ticket.booking.response.MovieInfoResponse;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.service.impl.MovieServiceImpl;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
//OK
public class MovieServiceImplTest {

    @Autowired
    private MovieRepo movieRepo;

    @Autowired
    private GenreReposity genreRepo;

    @Autowired
    private MovieServiceImpl movieService;

    private Movie movie1;
    private Movie movie2;
    private Genre action;
    private Genre drama;

    @BeforeEach
    void setup() {
        action = new Genre();
        action.setGenre("Ok");

        drama = new Genre();
        drama.setGenre("Bruh");
        genreRepo.save(action);
        genreRepo.save(drama);

        movie1 = new Movie();
        movie1.settitle("Avengers");
        movie1.setDescription("A superhero movie.");
        movie1.setDurationInMins(120);
        movie1.setLanguage("English");
        movie1.setReleaseDate("2012-05-04");
        movie1.setCountry("USA");
        movie1.setGenres(Arrays.asList(action, drama));
        movie1.setImage("No");
        movie1.setActors("People");

        movie2 = new Movie();
        movie2.settitle("Titanic");
        movie2.setDescription("A romantic tragedy.");
        movie2.setDurationInMins(180);
        movie2.setLanguage("English");
        movie2.setReleaseDate("1997-12-19");
        movie2.setCountry("USA");
        movie2.setImage("No");
        movie2.setActors("People");
        movie2.setGenres(Arrays.asList(drama));

        movie1 = movieRepo.save(movie1); 
        movie2 = movieRepo.save(movie2);
    }
//Get Movies
    @Test
    void testGetMovies() {
        List<MovieInfoResponse> movies = movieService.getMovies(0, 10);
        assertFalse(movies.isEmpty());
        assertEquals(10, movies.size());
    }

//Save Movie
    @Test
    void testSaveMovie() {
        Movie movie = new Movie();
        movie.settitle("Inception");
        movie.setDescription("A mind-bending thriller.");
        movie.setDurationInMins(148);
        movie.setLanguage("English");
        movie.setReleaseDate("2010-07-16");
        movie.setCountry("USA");
        movie.setGenres(Arrays.asList(action));

        Movie savedMovie = movieService.saveMovie(movie);
        assertNotNull(savedMovie.getId());
        assertEquals("Inception", savedMovie.getTitle());
    }
//Get Movies By ID
    @Test
    void testGetMovieById() {
        MovieInfoResponse movie = movieService.getMovie(movie1.getId());
        assertNotNull(movie);
        assertEquals("Avengers", movie.getTitle());
    }
//Get Movies By Not Existed ID
    @Test
    void testGetMovieById_NonExisted() {
        MyNotFoundException ex = assertThrows(MyNotFoundException.class, () -> movieService.getMovie(999L));
        assertEquals("Movie not found", ex.getMessage());

    }
//Delete Movie with existed ID
    @Test
    void testDeleteMovie() {
        movieService.deleteMovie(movie1.getId());
        assertFalse(movieRepo.existsById(movie1.getId())); 
    }
//Delete Movie with not existed ID
    @Test
    void testDeleteMovie_NonExisted() {
        MyNotFoundException ex = assertThrows(MyNotFoundException.class, () -> movieService.deleteMovie(999L));
        assertEquals("Movie not found", ex.getMessage());
    }
//Save Movie List
   @Test
    void testSaveMovieList() {
        Movie movie3 = new Movie();
        movie3.settitle("The Matrix");
        movie3.setDescription("A sci-fi action film.");
        movie3.setDurationInMins(136);
        movie3.setLanguage("English");
        movie3.setReleaseDate("1999-03-31");
        movie3.setCountry("USA");
        movie3.setGenres(Arrays.asList(action));
        movie3.setImage("No");
        movie3.setActors("People");

        List<Movie> movies = Arrays.asList(movie1,movie3);
        MyApiResponse savedMovies = movieService.saveMovieList(movies);

        assertEquals("Success", savedMovies.getMessage());
    }
//Test get exited movie by title
    @Test
    void testGetMatchingName() {
        List<MovieInfoResponse> movies = movieService.getMatchingName("Titanic",0,32);
        assertFalse(movies.isEmpty());
        assertEquals("Titanic", movies.get(0).getTitle());
    }
//Test get not exited movie by title
    @Test
    void testGetMatchingName_NoExist() {
        List<MovieInfoResponse> movies = movieService.getMatchingName("123favotan",1,30);
        assertTrue(movies.isEmpty());
    }
// Test get movie by exited genre
    @Test
    void testGetMatchingGenre() {
        Object[] movies = movieService.getMatchingGenre("Drama",1,10);
        assertTrue(movies.length>0);
    }
// Test get movie by not exited genre
    @Test
    void testGetMatchingGenre_NotExist() {
        Object[] movies = movieService.getMatchingGenre("Phim_hom_nay",1,10);
        assertTrue(movies.length==0);
    }

}
