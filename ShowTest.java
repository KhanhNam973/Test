package cinema.ticket.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import cinema.ticket.booking.repository.*;
import cinema.ticket.booking.exception.MyBadRequestException;
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
    @Autowired
    private CinemaHallService cinemaHallService;
    String id;

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

        String startTime1="01/05/2025 18:00";
        testShow = new CinemaShow(testHall, movie1, startTime1);
        showRepository.save(testShow);
        id=testShow.getId();
    }

    
// Test add show with valid date
    @Test
    void SHOW_001_testAddShow_ValidDate() throws Exception {
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

        MyApiResponse savedMovieID = cinemaShowService.addShow(showRequest);
        assertNotNull(savedMovieID.getMessage());
        //MyApiResponse is ID of saved movie
    }

    //Test add show with duplicate date
    @Test
    void SHOW_002_testAddShow_DuplicateDate() throws Exception {
        int a=showRepository.findAll().size();
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
        MyBadRequestException e= assertThrows(MyBadRequestException.class, 
        () ->   cinemaShowService.addShow(showRequest));
        int b=showRepository.findAll().size();
        assertEquals(a, b);
        assertEquals(e.getMessage(),"This show existed");
    }

//Test add show with invalid date
@Test
    void SHOW_003_testAddShow_NullDate() throws Exception {
        int a=showRepository.findAll().size();
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest,movie1.getId());

        MyBadRequestException exception=assertThrows(MyBadRequestException.class, 
        () -> cinemaShowService.addShow(showRequest));
        int b=showRepository.findAll().size();
        assertEquals(a, b);
        assertEquals(exception.getMessage(),"Invalid date format, it must be dd/MM/yyyy HH:mm");
    }
    //Test add show with null cinema
    @Test
    void SHOW_004_testAddShow_NullCinema() throws Exception {
        int a=showRepository.findAll().size();
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, null);

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest,movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="03/05/2025 18:00";
        startTime.set(showRequest, startTime2);

        InvalidDataAccessApiUsageException exception=assertThrows
        (InvalidDataAccessApiUsageException.class, 
        () -> cinemaShowService.addShow(showRequest));
        int b=showRepository.findAll().size();
        assertNotNull(exception);
        assertEquals(a, b);
    }
    //Test add show with null movie ID
    @Test
    void SHOW_005_testAddShow_NullMovie() throws Exception {
        int a=showRepository.findAll().size();
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest,null);

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="01/05/2025 18:00";
        startTime.set(showRequest, startTime2);

        InvalidDataAccessApiUsageException exception=
        assertThrows(InvalidDataAccessApiUsageException.class, 
        () -> cinemaShowService.addShow(showRequest));
        int b=showRepository.findAll().size();
        assertNotNull(exception);
        assertEquals(a, b);
    }

//Test get show info by ID
    @Test
    void SHOW_006_testGetShowInfo() {
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
    void SHOW_007_testGetShowInfo_InvalidID() {
        MyNotFoundException exception = assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.getShowInfo("9999"));
        assertNotNull(exception);
        assertEquals("Show is not found", exception.getMessage());
    
}
//Get all shows
    @Test
    void SHOW_008_testGetAllShows() {
        List<ShowInfoResponse> allShows = cinemaShowService.getAllShows();
        assertNotNull(allShows);
        assertEquals(allShows.size(),32);
    }
//Update show 
    @Test
    void SHOW_009_testUpdateShow() throws Exception {
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

        CinemaShow updatedShow = showRepository.findById(testShow.getId()).orElseThrow();
        assertEquals(LocalDateTime.of(2025, 5, 4, 12, 0), updatedShow.getStartTime());
        //if (starttime != null && starttime.equals(show.getStartTime()))
    }
//Update show with null time
    @Test
    void SHOW_010_testUpdateShow_NullTime() throws Exception {
        ShowRequest updateRequest = new ShowRequest();

        Field cinemaID = updateRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(updateRequest, testHall.getId());

        Field movieID = updateRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(updateRequest, movie1.getId());
        
        cinemaShowService.updateShow(testShow.getId(), updateRequest);
        CinemaShow updatedShow = showRepository.findById(testShow.getId()).orElseThrow();
        assertEquals(testShow.getStartTime(), updatedShow.getStartTime());
    }
//Update show to null cinema
    @Test
    void SHOW_011_testUpdateShow_NullCinema() throws Exception {
        ShowRequest updateRequest = new ShowRequest();

        Field cinemaID = updateRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(updateRequest, null);

        InvalidDataAccessApiUsageException e = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> cinemaShowService.updateShow(testShow.getId(), updateRequest)
        );
        CinemaShow updatedShow = showRepository.findById(testShow.getId()).orElseThrow();
        assertNotNull(updatedShow.getCinemaHall().getId());
        assertNotNull(e);
    }
//Update show to null movie
    @Test
    void SHOW_012_testUpdateShow_NullMovie() throws Exception {
        ShowRequest updateRequest = new ShowRequest();

        Field movieID = updateRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(updateRequest, null);
        
        InvalidDataAccessApiUsageException e = assertThrows(
            InvalidDataAccessApiUsageException.class,
            () -> cinemaShowService.updateShow(testShow.getId(), updateRequest)
        );
        CinemaShow updatedShow = showRepository.findById(testShow.getId()).orElseThrow();
        assertNotNull(updatedShow.getMovie().getId());
        assertNotNull(e);
    }

// Update show with invalid ID
    @Test
    void SHOW_013_testUpdateShow_InvalidShowID() throws Exception {
        int a=showRepository.findAll().size();
        ShowRequest updateRequest = new ShowRequest();
        Field cinemaID = updateRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(updateRequest, testHall.getId());
        Field movieID = updateRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(updateRequest, movie1.getId());
        MyNotFoundException exception = assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.updateShow("9999", updateRequest));
        int b=showRepository.findAll().size();
        assertEquals(a, b);
        assertNotNull(exception);
        assertEquals("Show is not found", exception.getMessage());
    }
// Update show with invalid hall ID
    @Test
    void SHOW_014_testUpdateShow_InvalidCinemaID() throws Exception {
        int a=showRepository.findAll().size();
        ShowRequest updateRequest = new ShowRequest();
        Field cinemaID = updateRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(updateRequest, "999999");
        MyNotFoundException exception = assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.updateShow(testShow.getId(), updateRequest));
        int b=showRepository.findAll().size();
        assertNotNull(exception);
        assertEquals(a, b);
        assertEquals("Hall is not found", exception.getMessage());
    }
// Update show with invalid ID
@Test
void SHOW_015_testUpdateShow_InvalidMovieID() throws Exception {
    int a=showRepository.findAll().size();
    ShowRequest updateRequest = new ShowRequest();
    Field movieID = updateRequest.getClass().getDeclaredField("movieID");
    movieID.setAccessible(true);
    movieID.set(updateRequest, 9999L);
    MyNotFoundException exception = assertThrows(MyNotFoundException.class, 
    () -> cinemaShowService.updateShow(testShow.getId(), updateRequest));
    int b=showRepository.findAll().size();
    assertEquals(a, b);
    assertEquals("Movie is not found", exception.getMessage());
    assertNotNull(exception);
}
//Delete show by ID
    @Test
    void SHOW_016_testDeleteShow() {
        MyApiResponse response = cinemaShowService.deleteShow(testShow.getId());
        assertNotNull(response);
        assertFalse(showRepository.existsById(testShow.getId()));
    }
//Delete show by invalid ID
    @Test
    void SHOW_017_testDeleteShow_InvalidID() {
        List<ShowInfoResponse> a= cinemaShowService.getAllShows();
        MyApiResponse response = cinemaShowService.deleteShow("9999");
        List<ShowInfoResponse> b = cinemaShowService.getAllShows();
        assertNotNull(response);
        assertEquals(a.size(), b.size());
        //a.size() is the size of all shows before delete and b.size() is the size of all shows after delete
    }
//Delete show by hall ID and movie ID with not null starttime
    @Test
    void SHOW_018_testDeleteShowByHallIDMovieID() throws Exception {
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

        cinemaShowService.deleteShowByHallIDMovieID(showRequest);
        assertFalse(showRepository.existsById(testShow.getId()));
    }
//Delete show by hall ID and movie ID with invalid starttime
    @Test
    void SHOW_019_testDeleteShowByHallIDMovieID_InvalidStartTime() throws Exception {
       
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        List<ShowInfoResponse> a= cinemaShowService.getAllShows();
        Exception e=assertThrows(MyBadRequestException.class, 
        () -> cinemaShowService.deleteShowByHallIDMovieID(showRequest));
        List<ShowInfoResponse> b= cinemaShowService.getAllShows();
        assertEquals(a.size(), b.size());
        assertNotNull(e);
    }
    //Delete show with null hall ID
    @Test
    void SHOW_020_testDeleteShowByHallIDMovieID_nullHallID() throws Exception {
       
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,null);

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="01/05/2025 18:00";
        startTime.set(showRequest, startTime2);
        
        List<ShowInfoResponse> a= cinemaShowService.getAllShows();
        NullPointerException e=assertThrows(NullPointerException.class, 
        () -> cinemaShowService.deleteShowByHallIDMovieID(showRequest));
        List<ShowInfoResponse> b= cinemaShowService.getAllShows();
        assertEquals(a.size(), b.size());
        assertNotNull(e);
    }

    //Delete show with null movie ID
    @Test
    void SHOW_021_testDeleteShowByHallIDMovieID_nullMovieID() throws Exception {
       
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, null);

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="01/05/2025 18:00";
        startTime.set(showRequest, startTime2);
        
        List<ShowInfoResponse> a= cinemaShowService.getAllShows();
        NullPointerException e=assertThrows(NullPointerException.class, 
        () -> cinemaShowService.deleteShowByHallIDMovieID(showRequest));
        List<ShowInfoResponse> b= cinemaShowService.getAllShows();
        assertEquals(a.size(), b.size());
        assertNotNull(e);
    }
//delete non existing hall ID
    @Test
    void SHOW_022_testDeleteShowByHallIDMovieID_Non_Exising_HallID() throws Exception {
       
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,"99999");

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="01/05/2025 18:00";
        startTime.set(showRequest, startTime2);
        
        List<ShowInfoResponse> a= cinemaShowService.getAllShows();
        MyNotFoundException e=assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.deleteShowByHallIDMovieID(showRequest));
        List<ShowInfoResponse> b= cinemaShowService.getAllShows();
        assertEquals(a.size(), b.size());
        assertNotNull(e);
    }
    //delete non existing movie ID
    @Test
    void SHOW_023_testDeleteShowByHallIDMovieID_Non_Exising_MovieID() throws Exception {
       
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, 9999L);

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="01/05/2025 18:00";
        startTime.set(showRequest, startTime2);
        
        List<ShowInfoResponse> a= cinemaShowService.getAllShows();
        MyNotFoundException e=assertThrows(MyNotFoundException.class, 
        () -> cinemaShowService.deleteShowByHallIDMovieID(showRequest));
        List<ShowInfoResponse> b= cinemaShowService.getAllShows();
        assertEquals(a.size(), b.size());
        assertNotNull(e);
    }
    @Test
    void SHOW_024_testGetShowInfo_nullID() {
        InvalidDataAccessApiUsageException exception = assertThrows
        (InvalidDataAccessApiUsageException.class, 
        () -> cinemaShowService.getShowInfo(null));
        assertNotNull(exception);
}
}
