package cinema.ticket.booking;

import cinema.ticket.booking.model.CinemaHall;
import cinema.ticket.booking.model.CinemaSeat;
import cinema.ticket.booking.model.enumModel.ESeat;
import cinema.ticket.booking.model.enumModel.ESeatStatus;
import cinema.ticket.booking.repository.CinemaSeatRepository;
import cinema.ticket.booking.request.SeatEditRequest;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.response.SeatsResponse;
import cinema.ticket.booking.response.ErrorResponse;
import cinema.ticket.booking.service.impl.CinemaHallImpl;
import cinema.ticket.booking.service.impl.CinemaSeatServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SeatTest {

    @Autowired
    private CinemaSeatRepository hallSeatRepo;

    @Autowired
    private CinemaSeatServiceImpl cinemaSeatService;
    @Autowired
    private CinemaHallImpl cinemaHallService = new CinemaHallImpl();

    private CinemaHall hall;
    private CinemaHall hall1;
    private CinemaHall testHall;

    @BeforeEach
    void setUp() {
        hall = new CinemaHall();
        hall.setName("HI");
        hall.setTotalRow(5);
        hall.setTotalCol(5);
        cinemaHallService.newHall(hall);
        testHall= new CinemaHall();
        testHall.setId("12345");
    }
//Test Create List Seat
    @Test
    void testCreateListSeats() {
        hall1 = new CinemaHall();
        hall1.setName("Hello");
        hall1.setTotalRow(6);
        hall1.setTotalCol(6);
        cinemaHallService.newHall(hall1);
        List<CinemaSeat> seats = hallSeatRepo.findByCinemaHallId(hall1.getId());
        assertEquals(36, seats.size());  
    }
//Test see existed seat
    @Test
    void testSeatExists() {
        assertTrue(cinemaSeatService.isExist(hall.getId(), 2, 3));
    }
//Test see not existed seat
    @Test
    void testSeatNotExists() {
        assertFalse(cinemaSeatService.isExist(hall.getId(), 6, 6));
    }
//Test remove all seats from hall
    @Test
    void testRemoveAllSeatsFromHall() {
        cinemaSeatService.RemoveAllSeatsFromHall(hall.getId());
        List<CinemaSeat> seats = hallSeatRepo.findByCinemaHallId(hall.getId());
        assertEquals(0, seats.size());
    }
//Test get all seats from hall
    @Test
    void testGetAllSeatsFromHall() {
        List<SeatsResponse> seats = cinemaSeatService.getAllSeatsFromHall(hall.getId());
        assertEquals(25, seats.size());
    }
//Test get all seats from hall with wrong id
    @Test
    void testGetAllSeatsFromHallWithWrongId() {
        List<SeatsResponse> response = cinemaSeatService.getAllSeatsFromHall(testHall.getId());
        assertTrue(response.isEmpty());
    }
//Test edit seat
    @Test
    void testEditSeat_Right() throws Exception {
        SeatEditRequest request = new SeatEditRequest();

        Field rowField = SeatEditRequest.class.getDeclaredField("row");
        rowField.setAccessible(true);
        rowField.setInt(request, 2); 
        
        Field colField = SeatEditRequest.class.getDeclaredField("col");
        colField.setAccessible(true);
        colField.setInt(request, 3);
    
        Field typeField = SeatEditRequest.class.getDeclaredField("type");
        typeField.setAccessible(true);
        typeField.set(request, "PREMIUM");  
        
        Field statusField = SeatEditRequest.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(request, "UNAVAILABLE");
        MyApiResponse response = cinemaSeatService.Edit(hall.getId(), request);

        CinemaSeat updatedSeat = hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex(hall.getId(), 2, 3)
                    .orElseThrow(() -> new RuntimeException("Seat not found"));
    
        assertEquals("Success", response.getMessage());
        assertEquals(ESeat.PREMIUM.toString(), updatedSeat.getSeatType());
        assertEquals(ESeatStatus.UNAVAILABLE, updatedSeat.getStatus());
        
    }
//Test edit seat with invalid type
    @Test
    void testEditSeat_InvalidType() throws Exception{
        SeatEditRequest request = new SeatEditRequest();
        Field rowField = SeatEditRequest.class.getDeclaredField("row");
        rowField.setAccessible(true);
        rowField.setInt(request, 2); 
        
        Field colField = SeatEditRequest.class.getDeclaredField("col");
        colField.setAccessible(true);
        colField.setInt(request, 3);
    
        Field typeField = SeatEditRequest.class.getDeclaredField("type");
        typeField.setAccessible(true);
        typeField.set(request, "HELLO");  
        
        Field statusField = SeatEditRequest.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(request, "UNAVAILABLE");
        
        MyApiResponse response = cinemaSeatService.Edit(hall.getId(), request);
        //assertTrue(response instanceof ErrorResponse);
        assertEquals("Type is not found. It must be REGULAR or PREMIUM", response.getMessage());
    }
    //Type.equals(null) is not a valid check, it should be type==null

//Test edit seat with invalid status
    @Test
    void testEditSeat_InvalidStatus() throws Exception{
        SeatEditRequest request = new SeatEditRequest();
        Field rowField = SeatEditRequest.class.getDeclaredField("row");
        rowField.setAccessible(true);
        rowField.setInt(request, 2); 
        
        Field colField = SeatEditRequest.class.getDeclaredField("col");
        colField.setAccessible(true);
        colField.setInt(request,3);
    
        Field typeField = SeatEditRequest.class.getDeclaredField("type");
        typeField.setAccessible(true);
        typeField.set(request, "PREMIUM");  
        
        Field statusField = SeatEditRequest.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(request, "HELLO");

        MyApiResponse response = cinemaSeatService.Edit(hall.getId(), request);

        assertTrue(response instanceof ErrorResponse);
        assertEquals("Status is not found. It must be AVAILABLE or UNAVAILABLE", response.getMessage());
    }
    // status.equals(null) is not a valid check, it should be status==null
}
