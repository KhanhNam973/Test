package cinema.ticket.booking;

import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.Genre;
import cinema.ticket.booking.model.Movie;
import cinema.ticket.booking.repository.GenreReposity;
import cinema.ticket.booking.repository.MovieRepo;
import cinema.ticket.booking.response.MovieInfoResponse;
import cinema.ticket.booking.service.impl.MovieServiceImpl;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

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
    void MOVIE_001_testGetMovies() {
        List<MovieInfoResponse> movies = movieService.getMovies(0, 10);
        assertFalse(movies.isEmpty());
        assertEquals(10, movies.size());
    }

//Save Movie right
    @Test
    void MOVIE_002_testSaveMovie() {
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
//Save Duplicate Movie
@Test
void MOVIE_003_testSaveMovie_Duplicate() {
    Movie movie = new Movie();
    movie.settitle("Avengers");
    movie.setDescription("A mind-bending thriller.");
    movie.setDurationInMins(148);
    movie.setLanguage("English");
    movie.setReleaseDate("2010-07-16");
    movie.setCountry("USA");
    movie.setGenres(Arrays.asList(action));

    MyBadRequestException savedMovie = assertThrows(MyBadRequestException.class, () -> movieService.saveMovie(movie));
    assertNotNull(savedMovie);
    List<MovieInfoResponse> movielist = movieService.getMatchingName("Avengers",0,32);
    assertEquals(movielist.size(), 1);
}   
//Save Movie with null name
@Test
void MOVIE_004_testSaveMovie_NullName() {
    Movie movie = new Movie();
    movie.settitle(null);
    movie.setDescription("A mind-bending thriller.");
    movie.setDurationInMins(148);
    movie.setLanguage("English");
    movie.setReleaseDate("2010-07-16");
    movie.setCountry("USA");
    movie.setGenres(Arrays.asList(action));

    MyBadRequestException savedMovie = assertThrows(MyBadRequestException.class, () -> movieService.saveMovie(movie));
    assertNotNull(savedMovie);
}   


//Get Movies By ID
    @Test
    void MOVIE_005_testGetMovieById() {
        MovieInfoResponse movie = movieService.getMovie(movie1.getId());
        assertNotNull(movie);
        assertEquals("Avengers", movie.getTitle());
    }
//Get Movies By Not Existed ID
    @Test
    void MOVIE_006_testGetMovieById_NonExisted() {
        MyNotFoundException ex = assertThrows(
            MyNotFoundException.class,
             () -> movieService.getMovie(999L));
        assertNotNull(ex);

    }
//Delete Movie with existed ID
    @Test
    void MOVIE_007_testDeleteMovie() {
        movieService.deleteMovie(movie1.getId());
        assertFalse(movieRepo.existsById(movie1.getId())); 
    }
//Delete Movie with not existed ID
    @Test
    void MOVIE_008_testDeleteMovie_NonExisted() {
        List<MovieInfoResponse> movies = movieService.getMovies(0, 999);
        MyNotFoundException ex = assertThrows(MyNotFoundException.class, () -> movieService.deleteMovie(999L));
        List<MovieInfoResponse> movies2 = movieService.getMovies(0, 999);
        assertNotNull(ex);
        assertEquals(movies.size(), movies2.size());
    }

//Test get exited movie by title
    @Test
    void MOVIE_009_testGetMatchingName() {
        List<MovieInfoResponse> movies = movieService.getMatchingName("Titanic",0,10);
        assertFalse(movies.isEmpty());
        assertEquals("Titanic", movies.get(0).getTitle());
    }
//Test get not exited movie by title
    @Test
    void MOVIE_010_testGetMatchingName_NoExist() {
        List<MovieInfoResponse> movies = movieService.getMatchingName("123 favotan",0,32);
        assertTrue(movies.isEmpty());
    }
// Test get movie by exited genre
    @Test
    void MOVIE_011_testGetMatchingGenre() {
        Object[] movies = movieService.getMatchingGenre("Drama",0,32);
        assertTrue(movies.length>0);
    }
// Test get movie by not exited genre
    @Test
    void MOVIE_012_testGetMatchingGenre_NotExist() {
        Object[] movies = movieService.getMatchingGenre("Phim_hom_nay",0,32);
        assertTrue(movies.length==0);
    }
//Save Movie List
@Test
void MOVIE_013_testSaveMovieList_NoDuplicate() {
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

    Movie movie4 = new Movie();
    movie4.settitle("Hom Nay Dep");
    movie4.setDescription("A Vietnamese action film.");
    movie4.setDurationInMins(136);
    movie4.setLanguage("English");
    movie4.setReleaseDate("1999-03-31");
    movie4.setCountry("USA");
    movie4.setGenres(Arrays.asList(action));
    movie4.setImage("No");
    movie4.setActors("People");

    List<Movie> movies = Arrays.asList(movie4,movie3);
    movieService.saveMovieList(movies);

    List<MovieInfoResponse> tim1= movieService.getMatchingName("The Matrix",0,32);
    List<MovieInfoResponse> tim2= movieService.getMatchingName("Hom Nay Dep",0,32);
    assertEquals(tim1.size(), 1);
    assertEquals(tim2.size(), 1);
    
    MovieInfoResponse movieFind1= tim1.get(0);
    MovieInfoResponse movieFind2= tim2.get(0);

    assertEquals(movieFind1.getTitle(), "The Matrix");  
    assertEquals(movieFind2.getTitle(), "Hom Nay Dep");
}
//Save Movie List with duplicate movie
@Test
void MOVIE_014_testSaveMovieList_Duplicate() {
    Movie movie3 = new Movie();
    movie3.settitle("Avengers");
    movie3.setDescription("A sci-fi action film.");
    movie3.setDurationInMins(136);
    movie3.setLanguage("English");
    movie3.setReleaseDate("1999-03-31");
    movie3.setCountry("USA");
    movie3.setGenres(Arrays.asList(action));
    movie3.setImage("No");
    movie3.setActors("People");

    Movie movie4 = new Movie();
    movie4.settitle("Avengers");
    movie4.setDescription("A hero film.");
    movie4.setDurationInMins(136);
    movie4.setLanguage("English");
    movie4.setReleaseDate("2020-03-31");
    movie4.setCountry("USA");
    movie4.setGenres(Arrays.asList(action));
    movie4.setImage("No");
    movie4.setActors("People");

    List<Movie> movies = Arrays.asList(movie4,movie3);
    movieService.saveMovieList(movies);
    List<MovieInfoResponse> tim1= movieService.getMatchingName("Avengers",0,32);
    assertEquals(tim1.size(), 1);
    
    MovieInfoResponse movieFind1= tim1.get(0);

    assertEquals(movieFind1.getTitle(), "Avengers");  
}

//Save Movie List with null title movie
@Test
void MOVIE_015_testSaveMovieList_NullTitle() {
    int a= movieRepo.findAll().size();
    Movie movie3 = new Movie();
    movie3.settitle(null);
    movie3.setDescription("A sci-fi action film.");
    movie3.setDurationInMins(136);
    movie3.setLanguage("English");
    movie3.setReleaseDate("1999-03-31");
    movie3.setCountry("USA");
    movie3.setGenres(Arrays.asList(action));
    movie3.setImage("No");
    movie3.setActors("People");

    Movie movie4 = new Movie();
    movie4.settitle("Avengers");
    movie4.setDescription("A hero film.");
    movie4.setDurationInMins(136);
    movie4.setLanguage("English");
    movie4.setReleaseDate("2020-03-31");
    movie4.setCountry("USA");
    movie4.setGenres(Arrays.asList(action));
    movie4.setImage("No");
    movie4.setActors("People");

    List<Movie> movies = Arrays.asList(movie4,movie3);
    movieService.saveMovieList(movies);
    int b= movieRepo.findAll().size();
    assertEquals(a,b);  
}
//Test get not exited movie by null name
@Test
void MOVIE_016_testGetMatchingName_NullName() {
    List<MovieInfoResponse> movies = movieService.getMatchingName(null,0,32);
    assertTrue(movies.isEmpty());
    MyBadRequestException e = assertThrows(MyBadRequestException.class, () -> movieService.getMatchingName(null,0,32)); 
    assertNotNull(e);
    assertEquals(e.getMessage(), "Movie name cannot be null or empty.");
}
//Get movie by null genre
@Test
void MOVIE_017_testGetMatchingGenre_Null() {
    Object[] movies = movieService.getMatchingGenre(null,0,32);
    assertTrue(movies.length==0);
    MyBadRequestException e = assertThrows(MyBadRequestException.class, () -> movieService.getMatchingGenre(null,0,32)); 
    assertNotNull(e.getMessage(),"Movie genre cannot be null or empty.");
}
//Get Movies By null ID
@Test
void MOVIE_018_testGetMovieById_Null() {
    InvalidDataAccessApiUsageException ex = assertThrows(
        InvalidDataAccessApiUsageException.class,
         () -> movieService.getMovie(null));
    assertNotNull(ex);

}

//Delete Movie with null ID
@Test
void MOVIE_019_testDeleteMovie_nullID() {
    List<MovieInfoResponse> movies = movieService.getMovies(0, 999);
    InvalidDataAccessApiUsageException ex = assertThrows
    (InvalidDataAccessApiUsageException.class, () -> movieService.deleteMovie(null));
    List<MovieInfoResponse> movies2 = movieService.getMovies(0, 999);
    assertNotNull(ex);
    assertEquals(movies.size(), movies2.size());
}
//NO UPDATE MOVIE FUNCTION

}
