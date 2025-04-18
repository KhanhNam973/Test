package cinema.ticket.booking;

import java.util.ArrayList;
import java.util.Arrays;
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
import cinema.ticket.booking.response.ShowSeatResponse;
import cinema.ticket.booking.service.CinemaHallService;
import cinema.ticket.booking.service.impl.CinemaShowServiceImpl;
import jakarta.transaction.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        CinemaShow test=showRepository.findById(savedMovieID.getMessage()).orElse(null);
        assertEquals(testHall.getId(), test.getCinemaHall().getId());
        assertEquals(movie1.getId(), test.getMovie().getId());
        assertEquals("2025-05-03T12:00", test.getStartTime().toString());
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
//Add show success
@Test
    void SHOW_025_testAdd_ListShow_Success() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="02/06/2025 18:00";
        startTime.set(showRequest, startTime2);

        ShowRequest showRequest1 = new ShowRequest();

        Field cinemaID1 = showRequest1.getClass().getDeclaredField("cinemaID");
        cinemaID1.setAccessible(true);
        cinemaID1.set(showRequest1,testHall.getId());

        Field movieID1 = showRequest1.getClass().getDeclaredField("movieID");
        movieID1.setAccessible(true);
        movieID1.set(showRequest1, movie1.getId());

        Field startTime1 = showRequest.getClass().getDeclaredField("start_time");
        startTime1.setAccessible(true);
        String startTime3="01/06/2025 18:00";
        startTime1.set(showRequest1, startTime3);

        List<ShowRequest> a = Arrays.asList(showRequest,showRequest1);
        List<MyApiResponse>b=cinemaShowService.addListShows(a);
        assertEquals(b.size(), 2);

        CinemaShow s1 = showRepository.findById(b.get(0).getMessage()).orElseThrow();
        assertEquals(s1.getMovie().getId(), movie1.getId());
        assertEquals(s1.getCinemaHall().getId(), testHall.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime expectedDateTime = LocalDateTime.parse("02/06/2025 18:00", formatter);
        assertEquals(s1.getStartTime(), expectedDateTime);
        
        CinemaShow s2 = showRepository.findById(b.get(1).getMessage()).orElseThrow();
        assertEquals(s2.getMovie().getId(), movie1.getId());
        assertEquals(s2.getCinemaHall().getId(), testHall.getId());
        LocalDateTime expectedDateTime2 = LocalDateTime.parse("01/06/2025 18:00", formatter);
        assertEquals(s2.getStartTime(), expectedDateTime2);
    }
//Add a show with null time
    @Test
    void SHOW_026_testAdd_ListShow_StartTimeNull() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="02/06/2025 18:00";
        startTime.set(showRequest, startTime2);

        ShowRequest showRequest1 = new ShowRequest();

        Field cinemaID1 = showRequest1.getClass().getDeclaredField("cinemaID");
        cinemaID1.setAccessible(true);
        cinemaID1.set(showRequest1,testHall.getId());

        Field movieID1 = showRequest1.getClass().getDeclaredField("movieID");
        movieID1.setAccessible(true);
        movieID1.set(showRequest1, movie1.getId());

        List<ShowRequest> a = Arrays.asList(showRequest,showRequest1);

        int trc=showRepository.findAll().size();

        assertThrows(MyBadRequestException.class,
        ()->cinemaShowService.addListShows(a));

        int sau=showRepository.findAll().size();
        assertEquals(trc, sau-1);
    }
//Add list with a show with null cinema
@Test
    void SHOW_027_testAdd_ListShow_nullCinema() throws Exception {
        int trc=showRepository.findAll().size();
        ShowRequest showRequest = new ShowRequest();
        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,null);

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="02/06/2025 18:00";
        startTime.set(showRequest, startTime2);

        ShowRequest showRequest1 = new ShowRequest();
        Field cinemaID1 = showRequest1.getClass().getDeclaredField("cinemaID");
        cinemaID1.setAccessible(true);
        cinemaID1.set(showRequest1,testHall.getId());

        Field movieID1 = showRequest1.getClass().getDeclaredField("movieID");
        movieID1.setAccessible(true);
        movieID1.set(showRequest1, movie1.getId());

        Field startTime1 = showRequest.getClass().getDeclaredField("start_time");
        startTime1.setAccessible(true);
        String startTime3="01/06/2025 18:00";
        startTime1.set(showRequest1, startTime3);

        List<ShowRequest> ok = Arrays.asList(showRequest,showRequest1);

        assertThrows(InvalidDataAccessApiUsageException.class,
        ()->cinemaShowService.addListShows(ok));

        int sau=showRepository.findAll().size();
        assertEquals(trc, sau-1);
    }
//Add list with a show with null cinema after
    @Test
    void SHOW_028_testAdd_ListShow_nullCinema_After() throws Exception {
        int trc=showRepository.findAll().size();
        ShowRequest showRequest = new ShowRequest();
        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="02/06/2025 18:00";
        startTime.set(showRequest, startTime2);

        ShowRequest showRequest1 = new ShowRequest();
        Field cinemaID1 = showRequest1.getClass().getDeclaredField("cinemaID");
        cinemaID1.setAccessible(true);
        cinemaID1.set(showRequest1,null);

        Field movieID1 = showRequest1.getClass().getDeclaredField("movieID");
        movieID1.setAccessible(true);
        movieID1.set(showRequest1, movie1.getId());

        Field startTime1 = showRequest.getClass().getDeclaredField("start_time");
        startTime1.setAccessible(true);
        String startTime3="01/06/2025 18:00";
        startTime1.set(showRequest1, startTime3);

        List<ShowRequest> ok = Arrays.asList(showRequest,showRequest1);

        assertThrows(InvalidDataAccessApiUsageException.class,
        ()->cinemaShowService.addListShows(ok));

        int sau=showRepository.findAll().size();
        assertEquals(trc, sau-1);
    }
//Add list with a show with null movie in first
    @Test
    void SHOW_028_testAdd_ListShow_nullMovie() throws Exception {
        int trc=showRepository.findAll().size();
        ShowRequest showRequest = new ShowRequest();
        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, null);

        Field startTime = showRequest.getClass().getDeclaredField("start_time");
        startTime.setAccessible(true);
        String startTime2="02/06/2025 18:00";
        startTime.set(showRequest, startTime2);

        ShowRequest showRequest1 = new ShowRequest();
        Field cinemaID1 = showRequest1.getClass().getDeclaredField("cinemaID");
        cinemaID1.setAccessible(true);
        cinemaID1.set(showRequest1,testHall.getId());

        Field movieID1 = showRequest1.getClass().getDeclaredField("movieID");
        movieID1.setAccessible(true);
        movieID1.set(showRequest1, movie1.getId());

        Field startTime1 = showRequest.getClass().getDeclaredField("start_time");
        startTime1.setAccessible(true);
        String startTime3="01/06/2025 18:00";
        startTime1.set(showRequest1, startTime3);

        List<ShowRequest> ok = Arrays.asList(showRequest,showRequest1);

        assertThrows(InvalidDataAccessApiUsageException.class,
        ()->cinemaShowService.addListShows(ok));

        int sau=showRepository.findAll().size();
        assertEquals(trc, sau-1);
    }

    @Test
    void SHOW_029_test_convertToListShowInfo() throws Exception {
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("convertToListShowInfo", List.class);
        method.setAccessible(true);
        List<CinemaShow>a=Arrays.asList(testShow);
        List<ShowInfoResponse>b=(List<ShowInfoResponse>) method.invoke(cinemaShowService, a);
        ShowInfoResponse test=b.get(0);
        assertEquals(test.getMovieId(),movie1.getId().toString());
        assertEquals(test.getStartTime(), "2025-05-01T18:00");
    }

    @Test
    void SHOW_030_test_convertToListShowInfo_NullList() throws Exception {
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("convertToListShowInfo", List.class);
        method.setAccessible(true);
        List<CinemaShow>a=new ArrayList<CinemaShow>();
        List<ShowInfoResponse>b=(List<ShowInfoResponse>) method.invoke(cinemaShowService, a);
        assertEquals(b.size(), 0);
    }

    @Test
    void SHOW_031_testAddOneShow_ValidDate() throws Exception {
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
        String startTime2="03/05/2025 12:00";
        startTime.set(showRequest, startTime2);

        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("addOneShow", ShowRequest.class);
        method.setAccessible(true);

        String result=(String) method.invoke(cinemaShowService, showRequest);
        int b=showRepository.findAll().size();
        assertEquals(a+1, b);
        CinemaShow test=showRepository.findById(result).orElse(null);
        assertEquals(testHall.getId(), test.getCinemaHall().getId());
        assertEquals(movie1.getId(), test.getMovie().getId());
        assertEquals("2025-05-03T12:00", test.getStartTime().toString());
    }

    @Test
    void SHOW_032_testAddOneShow_NullDate() throws Exception {
        int a=showRepository.findAll().size();
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest,testHall.getId());

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, movie1.getId());

        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("addOneShow", ShowRequest.class);
        method.setAccessible(true);

        assertThrows(InvocationTargetException.class, ()->method.invoke(cinemaShowService, showRequest)) ;
        int b=showRepository.findAll().size();
        assertEquals(a, b);
    }

    @Test
    void SHOW_032_testUpdate_NewMovie() throws Exception {
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("updateNewMovie", CinemaShow.class,Long.class);
        method.setAccessible(true);
        method.invoke(cinemaShowService, testShow,2L);

        CinemaShow test=showRepository.findById(testShow.getId()).orElse(null);
        assertEquals(2L, test.getMovie().getId());
    }

    @Test
    void SHOW_033_testUpdate_NewMovie_NotFoundMovie() throws Exception {
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("updateNewMovie", CinemaShow.class,Long.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, ()->method.invoke(cinemaShowService, testShow,99999L));
        assertEquals(movie1.getId(), testShow.getMovie().getId());
    }

    @Test
    void SHOW_034_testUpdate_Update_Starttime() throws Exception {
        String dateTimeString = "01/06/2025 18:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("updateNewStartTime", CinemaShow.class,LocalDateTime.class);
        method.setAccessible(true);
        method.invoke(cinemaShowService, testShow,localDateTime);
        CinemaShow test=showRepository.findById(testShow.getId()).orElse(null);
        assertEquals(localDateTime, test.getStartTime());
    }

    @Test
    void SHOW_035_testUpdate_Update_NullStarttime() throws Exception {
        LocalDate localDate = null;
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("updateNewStartTime", CinemaShow.class,LocalDateTime.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, ()->method.invoke(cinemaShowService, testShow,localDate));
    }
    
    @Test
    void SHOW_036_testUpdate_Update_SameStarttime() throws Exception {
        String dateTimeString = "01/05/2025 18:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeString, formatter);
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("updateNewStartTime", CinemaShow.class,LocalDateTime.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, ()->method.invoke(cinemaShowService, testShow,localDateTime));
    }
    //d42f2662-d807a285-e929a245

    @Test
    void SHOW_037_testUpdate_Update_HalID() throws Exception {
        String hall_id = "d42f2662-d807a285-e929a245";
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("updateNewHall", CinemaShow.class,String.class);
        method.setAccessible(true);
        CinemaShow result =(CinemaShow) method.invoke(cinemaShowService, testShow,hall_id);
        String a=result.getId();
        CinemaShow test=showRepository.findById(a).orElse(null);

        assertEquals(test.getStartTime(),testShow.getStartTime());
        assertEquals(test.getEndTime(),testShow.getEndTime());
        assertEquals(test.getCinemaHall().getId(), hall_id);
        assertEquals(test.getMovie().getId(), testShow.getMovie().getId());
    }
    @Test
    void SHOW_038_testUpdate_Update_NotFoundHalID() throws Exception {
        String hall_id = "Hello";
        Method method=CinemaShowServiceImpl.class.getDeclaredMethod("updateNewHall", CinemaShow.class,String.class);
        method.setAccessible(true);
        assertThrows(InvocationTargetException.class, ()->method.invoke(cinemaShowService, testShow,hall_id)) ;
    }
    @Test
    void SHOW_039_test_getAllShowByMovieID() throws Exception {
        List<ShowInfoResponse> a=cinemaShowService.getAllShowByMovieID(movie1.getId().toString());
        assertEquals(a.size(),1 );
        ShowInfoResponse m=a.get(0);
        assertEquals(m.getShowID(),testShow.getId());
        assertEquals(m.getMovieName(),movie1.getTitle());
    }

    @Test
    void SHOW_040_test_getAllShowByMovieID_NotFound() throws Exception {
       assertThrows(MyNotFoundException.class, ()->cinemaShowService.getAllShowByMovieID("a"));
    }

    @Test
    void SHOW_041_test_getAllShowSeats() throws Exception {
        List<ShowSeatResponse> a=cinemaShowService.getAllShowSeats("9c93fcf6-4ee7aba9-9824ff4d");
        assertEquals(a.size(), 40);
    }

    @Test
    void SHOW_042_test_getAllShowSeats_NotFoundShow() throws Exception {
        assertThrows(MyBadRequestException.class,()-> cinemaShowService.getAllShowSeats("HAHA"));
    }
}
