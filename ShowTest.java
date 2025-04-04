package cinema.ticket.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import cinema.ticket.booking.repository.*;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.*;
import cinema.ticket.booking.request.ShowRequest;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.response.ShowInfoResponse;
import cinema.ticket.booking.service.CinemaHallService;
import cinema.ticket.booking.service.impl.CinemaShowServiceImpl;
import jakarta.transaction.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ShowTest {

    @Autowired
    private CinemaShowServiceImpl cinemaShowService;

    @Autowired
    private MovieRepo movieRepository;

    @Autowired
    private CinemaShowRepository showRepository;
    private CinemaHall testHall;
    private Movie movie1;
    private CinemaShow testShow;
    private CinemaHallService cinemaHallService;

    @BeforeEach
    public void setUp() {
        testHall = cinemaHallService.getHallById("E");

        movie1 =new Movie();
        movie1.settitle("Avengers");
        movie1.setDescription("A superhero movie.");
        movie1.setDurationInMins(120);
        movie1.setLanguage("English");
        movie1.setReleaseDate("2012-05-04");
        movie1.setCountry("USA");
        movie1.setImage("No");
        movie1.setActors("People");
        movieRepository.save(movie1);

        LocalDateTime startTime = LocalDateTime.of(2025, 5, 1, 18, 0);
        String startTime1=startTime.toString();
        testShow = new CinemaShow(testHall, movie1, startTime1);
        showRepository.save(testShow);
    }
// Test add show with valid data
    @Test
    void testAddShow() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, 1L);

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, 1L);

        Field startTime = showRequest.getClass().getDeclaredField("startTime");
        startTime.setAccessible(true);
        String startTime2="2025-05-03T18:00:00";
        startTime.set(showRequest, startTime2);

        MyApiResponse response = cinemaShowService.addShow(showRequest);
        assertNotNull(response);
        assertTrue(response.getMessage().length() > 0);
    }
//Test add show with invalid data
@Test
    void testAddShow_WrongData() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, 2L);

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, 2L);

        MyApiResponse response = cinemaShowService.addShow(showRequest);
        assertNotNull(response);
        assertTrue(response.getMessage().length() > 0);
    }
//Test get show info by ID
    @Test
    void testGetShowInfo() {
        ShowInfoResponse showInfoResponse = cinemaShowService.getShowInfo(testShow.getId());

        assertNotNull(showInfoResponse);
        assertEquals(testShow.getId(), showInfoResponse.getShowID());
        assertTrue(showInfoResponse.getTotalAvailableSeats() >= 0);
        assertTrue(showInfoResponse.getTotalReversedSeats() >= 0);
    }
//Test get show info by invalid ID
    void testGetShowInfo_InvalidID() {
        Exception exception = assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.getShowInfo("9999"));

        assertEquals("Show is not found", exception.getMessage());
}
//Get all shows
    @Test
    void testGetAllShows() {
        List<ShowInfoResponse> allShows = cinemaShowService.getAllShows();
        assertNotNull(allShows);
        assertTrue(allShows.size() > 0);
    }
//Update show 
    @Test
    void testUpdateShow() throws Exception {
        ShowRequest updateRequest = new ShowRequest();

        Field cinemaID = updateRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(updateRequest, 1L);

        Field movieID = updateRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(updateRequest, 1L);

        Field startTime = updateRequest.getClass().getDeclaredField("startTime");
        startTime.setAccessible(true);
        String startTime2="2025-05-02T18:00:00";
        startTime.set(updateRequest, startTime2);
        
        MyApiResponse response = cinemaShowService.updateShow(testShow.getId(), updateRequest);

        assertNotNull(response);
        assertEquals("Done", response.getMessage());

        CinemaShow updatedShow = showRepository.findById(testShow.getId()).orElseThrow();
        assertEquals(LocalDateTime.of(2025, 5, 2, 18, 0), updatedShow.getStartTime());
    }
//Update show with null time
    @Test
    void testUpdateShow_NullTime() throws Exception {
        ShowRequest updateRequest = new ShowRequest();

        Field cinemaID = updateRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(updateRequest, 1L);

        Field movieID = updateRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(updateRequest, 1L);
        
        MyApiResponse response = cinemaShowService.updateShow(testShow.getId(), updateRequest);
        assertNotNull(response);
        assertEquals("Done", response.getMessage());

        CinemaShow updatedShow = showRepository.findById(testShow.getId()).orElseThrow();
        assertEquals(testShow.getStartTime(), updatedShow.getStartTime());
    }
//Delete show by ID
    @Test
    void testDeleteShow() {
        MyApiResponse response = cinemaShowService.deleteShow(testShow.getId());
        assertEquals("Done", response.getMessage());
        assertFalse(showRepository.existsById(testShow.getId()));
    }
//Delete show by invalid ID
    @Test
    void testDeleteShow_InvalidID() {
        Exception exception = assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.deleteShow("9999"));

        assertEquals("Show is not found", exception.getMessage());
    }
//Delete show by hall ID and movie ID
    @Test
    void testDeleteShowByHallIDMovieID() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, 1L);

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, 1L);

        Field startTime = showRequest.getClass().getDeclaredField("startTime");
        startTime.setAccessible(true);
        String startTime2="2025-05-02T18:00:00";
        startTime.set(showRequest, startTime2);

        MyApiResponse response = cinemaShowService.deleteShowByHallIDMovieID(showRequest);
        assertNotNull(response);
        assertEquals("Deleted", response.getMessage());
    }
//Delete show by hall ID and movie ID with invalid starttime
    @Test
    void testDeleteShowByHallIDMovieID_InvalidStartTime() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, 1L);

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, 1L);

        Exception e=assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.deleteShowByHallIDMovieID(showRequest));

        assertEquals("Invaild date format, it must be dd/MM/yyyy HH:mm", e.getMessage());
    }
}

