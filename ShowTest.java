package cinema.ticket.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import cinema.ticket.booking.repository.*;
import cinema.ticket.booking.model.*;
import cinema.ticket.booking.request.ShowRequest;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.response.ShowInfoResponse;
import cinema.ticket.booking.service.CinemaHallService;
import cinema.ticket.booking.service.impl.CinemaShowServiceImpl;


import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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

    @Test
    void testAddShow() throws Exception {
        ShowRequest showRequest = new ShowRequest();

        Field cinemaID = showRequest.getClass().getDeclaredField("cinemaID");
        cinemaID.setAccessible(true);
        cinemaID.set(showRequest, 1L);

        Field movieID = showRequest.getClass().getDeclaredField("movieID");
        movieID.setAccessible(true);
        movieID.set(showRequest, 1L);

        MyApiResponse response = cinemaShowService.addShow(showRequest);
        assertNotNull(response);
        assertTrue(response.getMessage().length() > 0);
    }

    @Test
    void testGetShowInfo() {
        ShowInfoResponse showInfoResponse = cinemaShowService.getShowInfo(testShow.getId());

        assertNotNull(showInfoResponse);
        assertEquals(testShow.getId(), showInfoResponse.getShowID());
        assertTrue(showInfoResponse.getTotalAvailableSeats() >= 0);
        assertTrue(showInfoResponse.getTotalReversedSeats() >= 0);
    }

    @Test
    void testGetAllShows() {
        List<ShowInfoResponse> allShows = cinemaShowService.getAllShows();
        assertNotNull(allShows);
        assertTrue(allShows.size() > 0);
    }

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

    @Test
    void testDeleteShow() {
        String showIdToDelete = testShow.getId();

        MyApiResponse response = cinemaShowService.deleteShow(showIdToDelete);

        assertNotNull(response);
        assertEquals("Done", response.getMessage());
        assertFalse(showRepository.existsById(showIdToDelete));
    }
}

