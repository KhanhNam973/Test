package cinema.ticket.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cinema.ticket.booking.repository.*;
import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.*;
import cinema.ticket.booking.request.ShowRequest;
import cinema.ticket.booking.response.ErrorResponse;
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
    @Autowired
    private CinemaHallService cinemaHallService;

    @BeforeEach
    public void setUp() {
        testHall = cinemaHallService.getHallById("bd52169a-73795f48-9625ea04");

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

        //LocalDateTime startTime = LocalDateTime.of(2025, 5, 1, 18, 0);
        String startTime1="01/05/2025 18:00";
        testShow = new CinemaShow(testHall, movie1, startTime1);
        showRepository.save(testShow);
    }

    
// Test add show with valid date
    @Test
    void testAddShow() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="03/05/2025 12:00";
        startTime.set(showRequest, startTime2);

        MyApiResponse response = cinemaShowService.addShow(showRequest);
        assertNotNull(response);
        assertTrue(response.getMessage().length() > 0);
        assertEquals("Show is saved", response.getMessage());
    }
    //Test add show with duplicate date
    @Test
    void testAddShow_Duplicate() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="01/05/2025 18:00";
        startTime.set(showRequest, startTime2);

        MyApiResponse response = cinemaShowService.addShow(showRequest);
        assertNotNull(response);
        assertTrue(response.getMessage().length() > 0);
        assertEquals("Show has existed", response.getMessage());
        //duplicate date is still saved in database
    }

//Test add show with invalid date
@Test
    void testAddShow_WrongData() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest,movie1.getId());

        Exception exception = assertThrows(MyBadRequestException.class, 
        () -> cinemaShowService.addShow(showRequest));
        assertEquals(exception.getMessage(),"Invalid date format, it must be dd/MM/yyyy HH:mm");
        // message in code is "Invaild date format, it must be dd/MM/yyyy HH:mm"=> wrong spelling
    }

//Test get show info by ID
    @Test
    void testGetShowInfo() {
        ShowInfoResponse showInfoResponse = cinemaShowService.getShowInfo(testShow.getId());
        assertNotNull(showInfoResponse);
        assertEquals(testShow.getId(), showInfoResponse.getShowID());
        assertEquals(testShow.getStartTime().toString(), showInfoResponse.getStartTime());
        assertEquals(testShow.getMovie().getId().toString(), showInfoResponse.getMovieId());
        assertEquals(testShow.getCinemaHall().getId().toString(), showInfoResponse.getHallId());
        assertTrue(showInfoResponse.getTotalAvailableSeats() >= 0);
        assertTrue(showInfoResponse.getTotalReversedSeats() >= 0);
    }
//Test get show info by invalid ID
    @Test
    void testGetShowInfo_InvalidID() {
        Exception exception = assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.getShowInfo("9999"));

        assertEquals("Show is not found", exception.getMessage());
}
//Test get show info by invalid ID with null ID- ID can not be null
    // @Test
    // void testGetShowInfo_NullID() {
    //     Exception exception = assertThrows(MyNotFoundException.class, 
    //     () -> cinemaShowService.getShowInfo(null));
    //     assertEquals("Show is not found", exception.getMessage());
    // }
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
        cinemaID.set(updateRequest, testHall.getId());

        Field movieID = updateRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(updateRequest, movie1.getId());

        Field startTime = updateRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="04/05/2025 12:00";
        startTime.set(updateRequest, startTime2);
        
        MyApiResponse response = cinemaShowService.updateShow(testShow.getId(), updateRequest);

        assertNotNull(response);
        assertEquals("Done", response.getMessage());

        CinemaShow updatedShow = showRepository.findById(testShow.getId()).orElseThrow();
        assertEquals(LocalDateTime.of(2025, 5, 3, 12, 0), updatedShow.getStartTime());
        //if (starttime != null && starttime.equals(show.getStartTime()))
    }
//Update show with null time
    @Test
    void testUpdateShow_NullTime() throws Exception {
        ShowRequest updateRequest = new ShowRequest();

        Field cinemaID = updateRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(updateRequest, testHall.getId());

        Field movieID = updateRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(updateRequest, movie1.getId());
        
        MyApiResponse response = cinemaShowService.updateShow(testShow.getId(), updateRequest);
        assertNotNull(response);
        assertEquals("Done", response.getMessage());

        CinemaShow updatedShow = showRepository.findById(testShow.getId()).orElseThrow();
        assertEquals(testShow.getStartTime(), updatedShow.getStartTime());
    }

// Update show with invalid ID
    @Test
    void testUpdateShow_InvalidID() throws Exception {
        ShowRequest updateRequest = new ShowRequest();
        Field cinemaID = updateRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(updateRequest, testHall.getId());
        Field movieID = updateRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(updateRequest, movie1.getId());
        Exception exception = assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.updateShow("9999", updateRequest));
        assertEquals("Show is not found", exception.getMessage());
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
        MyApiResponse response = cinemaShowService.deleteShow("9999");
        assertNotNull(response);
        assertTrue(response instanceof ErrorResponse);
        assertEquals("Show is not found", response.getMessage());
        //code message is "Show is found"
    }
//Delete show by hall ID and movie ID with not null starttime
    @Test
    void testDeleteShowByHallIDMovieID() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="01/05/2025 18:00";
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
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Exception e=assertThrows(MyBadRequestException.class, 
        () -> cinemaShowService.deleteShowByHallIDMovieID(showRequest));

        assertEquals("Invaild date format, it must be dd/MM/yyyy HH:mm", e.getMessage());
    }
}

