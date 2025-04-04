package cinema.ticket.booking;

import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.CinemaHall;
import cinema.ticket.booking.model.CinemaSeat;
import cinema.ticket.booking.model.enumModel.ESeat;
import cinema.ticket.booking.model.enumModel.ESeatStatus;
import cinema.ticket.booking.repository.CinemaSeatRepository;
import cinema.ticket.booking.request.SeatEditRequest;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.response.SeatsResponse;
import cinema.ticket.booking.response.ErrorResponse;
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
    private CinemaHall hall;
    private CinemaSeat seat;

    @BeforeEach
    void setUp() {
        hall = new CinemaHall();
        hall.setId("H1");
        hall.setName("Deluxe Hall");
        hall.setTotalRow(5);
        hall.setTotalCol(5);
        cinemaSeatService.CreateListSeats(hall);
        seat = new CinemaSeat(hall, 2, 3, ESeat.REGULAR);
        seat.setStatus(ESeatStatus.AVAILABLE);
        hallSeatRepo.save(seat);
    }
//Test Create List Seat
    @Test
    void testCreateListSeats() {
        List<CinemaSeat> seats = hallSeatRepo.findByCinemaHallId("H1");
        assertEquals(25, seats.size());  
    }
//Test see existed seat
    @Test
    void testSeatExists() {
        assertTrue(cinemaSeatService.isExist("H1", 2, 3));
    }
//Test see not existed seat
    @Test
    void testSeatNotExists() {
        assertFalse(cinemaSeatService.isExist("H1", 6, 6));
    }
//Test remove all seats from hall
    @Test
    void testRemoveAllSeatsFromHall() {
        cinemaSeatService.RemoveAllSeatsFromHall("D");
        List<CinemaSeat> seats = hallSeatRepo.findByCinemaHallId("D");
        assertEquals(0, seats.size());
    }
//Test get all seats from hall
    @Test
    void testGetAllSeatsFromHall() {
        List<SeatsResponse> seats = cinemaSeatService.getAllSeatsFromHall("H1");
        assertEquals(25, seats.size());
    }
//Test get all seats from hall with wrong id
    @Test
    void testGetAllSeatsFromHallWithWrongId() {
        assertThrows(MyNotFoundException.class, () -> cinemaSeatService.getAllSeatsFromHall("12345"));
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

        Boolean isExist = cinemaSeatService.isExist("H1", 2, 3);
        if(isExist == false){
            throw new MyNotFoundException("Seat not found");
        }
        else{
            MyApiResponse response = cinemaSeatService.Edit("H1", request);

            CinemaSeat updatedSeat = hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex("H1", 2, 3)
                    .orElseThrow(() -> new RuntimeException("Seat not found"));
    
            assertEquals("Success", response.getMessage());
            assertEquals(ESeat.PREMIUM, updatedSeat.getSeatType());
            assertEquals(ESeatStatus.UNAVAILABLE, updatedSeat.getStatus());
        }
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
        MyApiResponse response = cinemaSeatService.Edit("H1", request);

        assertTrue(response instanceof ErrorResponse);
        assertEquals("Type is not found. It must be REGULAR or PREMIUM", response.getMessage());
    }
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

        MyApiResponse response = cinemaSeatService.Edit("H1", request);

        assertTrue(response instanceof ErrorResponse);
        assertEquals("Status is not found. It must be AVAILABLE or UNAVAILABLE", response.getMessage());
    }
}
