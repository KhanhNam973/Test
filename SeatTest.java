package cinema.ticket.booking;

import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.CinemaHall;
import cinema.ticket.booking.model.CinemaSeat;
import cinema.ticket.booking.model.enumModel.ESeat;
import cinema.ticket.booking.model.enumModel.ESeatStatus;
import cinema.ticket.booking.repository.CinemaSeatRepository;
import cinema.ticket.booking.request.SeatEditRequest;
import cinema.ticket.booking.response.ErrorResponse;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.response.SeatsResponse;
import cinema.ticket.booking.service.impl.CinemaHallImpl;
import cinema.ticket.booking.service.impl.CinemaSeatServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

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
    private CinemaHall nullHall;

    @BeforeEach
    void setUp() {
        hall = new CinemaHall();
        hall.setName("HI");
        hall.setTotalRow(5);
        hall.setTotalCol(5);
        cinemaHallService.newHall(hall);
    }
//Test Create List Seat
    @Test
    void SEAT_001_testCreateListSeats() {
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
    void SEAT_002_testSeatExists() {
        assertTrue(cinemaSeatService.isExist(hall.getId(), 2, 3));
    }
//Test see not existed seat
    @Test
    void SEAT_003_testSeatNotExists() {
        assertFalse(cinemaSeatService.isExist(hall.getId(), 10, 10));
    }
//Test see not existed seat (negative test)
@Test
void SEAT_004_testSeatNotExists_Negative() {
    assertFalse(cinemaSeatService.isExist(hall.getId(), -1, 3));
}
//Test see seat in non-existing hall
    @Test
    void SEAT_005_testSeatExists_NotHall() {
        assertFalse(cinemaSeatService.isExist("9999", 2, 3));
    }
//Test get all seats from hall
    @Test
    void SEAT_006_testGetAllSeatsFromHall() {
        List<SeatsResponse> seats = cinemaSeatService.getAllSeatsFromHall(hall.getId());
        assertEquals(25, seats.size());
    }
//Test get all seats from hall with wrong id
    @Test
    void SEAT_007_testGetAllSeatsFromHallWithWrongId() {
        List<SeatsResponse> response = cinemaSeatService.getAllSeatsFromHall("9999");
        assertTrue(response.isEmpty());
    }
//Test edit seat
    @Test
    void SEAT_008_testEditSeat_Right() throws Exception {
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
        cinemaSeatService.Edit(hall.getId(), request);

        Optional<CinemaSeat> updatedSeat=hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex(hall.getId(), 2, 3);
    
        assertEquals(ESeat.PREMIUM.toString(), updatedSeat.get().getSeatType());
        assertEquals(ESeatStatus.UNAVAILABLE, updatedSeat.get().getStatus());
        
    }
    //Test edit non-existing seat
    @Test
    void SEAT_009_testEditSeat_NoExist() throws Exception {
        int a=hallSeatRepo.findAll().size();
        SeatEditRequest request = new SeatEditRequest();

        Field rowField = SeatEditRequest.class.getDeclaredField("row");
        rowField.setAccessible(true);
        rowField.setInt(request, 10); 
        
        Field colField = SeatEditRequest.class.getDeclaredField("col");
        colField.setAccessible(true);
        colField.setInt(request, 10);
    
        Field typeField = SeatEditRequest.class.getDeclaredField("type");
        typeField.setAccessible(true);
        typeField.set(request, "PREMIUM");  
        
        Field statusField = SeatEditRequest.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(request, "UNAVAILABLE");
        MyNotFoundException exception = assertThrows(
            MyNotFoundException.class,
            () -> cinemaSeatService.Edit(hall.getId(), request)
        );
        int b=hallSeatRepo.findAll().size();
        assertEquals(a, b);
        assertEquals(exception.getMessage(),"Seat not found");
    }

    @Test
    void SEAT_010_testEditSeat_NoExistHall() throws Exception {
        int a=hallSeatRepo.findAll().size();
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
        MyNotFoundException exception = assertThrows(
            MyNotFoundException.class,
            () -> cinemaSeatService.Edit("99999", request)
        );
        int b=hallSeatRepo.findAll().size();
        assertEquals(a, b);
        assertEquals(exception.getMessage(),"Seat not found");
    }

//Test edit seat with invalid type
    @Test
    void SEAT_011_testEditSeat_InvalidType() throws Exception{
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
        Optional<CinemaSeat> a=hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex(hall.getId(), 2, 3);
        assertEquals(a.get().getSeatType(), ESeat.REGULAR.toString());
        assertNotNull(response);
    }
    //Type.equals(null) is not a valid check, it should be type==null in main code

//Test edit seat with invalid status
    @Test
    void SEAT_012_testEditSeat_InvalidStatus() throws Exception{
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

        Optional<CinemaSeat> a=hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex(hall.getId(), 2, 3);
        assertEquals(a.get().getStatus(), ESeatStatus.AVAILABLE.toString());
        assertNotNull(response);
    }
    // status.equals(null) is not a valid check, it should be status==null

    //Test remove all seats from hall
    @Test
    void SEAT_013_testRemoveAllSeatsFromHall() {
        cinemaSeatService.RemoveAllSeatsFromHall(hall.getId());
        List<CinemaSeat> seats = hallSeatRepo.findByCinemaHallId(hall.getId());
        assertEquals(0, seats.size());
    }
//Test remove all seats from non-existing hall
    @Test
    void SEAT_014_testRemoveAllSeats_NoHall() {
        int a=hallSeatRepo.findAll().size();
        Exception exception = assertThrows(
            MyNotFoundException.class,
            () -> cinemaSeatService.RemoveAllSeatsFromHall("9999"));
        int b=hallSeatRepo.findAll().size();
        assertEquals(a, b);
        assertNotNull( exception);
    }
//Test remove all seats from null hall
    @Test
    void SEAT_015_testRemoveAllSeats_NullHall() {
        int a=hallSeatRepo.findAll().size();
        Exception exception = assertThrows(
            NullPointerException.class,
            () -> cinemaSeatService.RemoveAllSeatsFromHall(nullHall.getId()));
        int b=hallSeatRepo.findAll().size();
        assertEquals(a, b);
        assertNotNull(exception);
    }
//Test edit seat with invalid type
@Test
void SEAT_016_testEditSeat_NullType() throws Exception{
    SeatEditRequest request = new SeatEditRequest();
    Field rowField = SeatEditRequest.class.getDeclaredField("row");
    rowField.setAccessible(true);
    rowField.setInt(request, 2); 
    
    Field colField = SeatEditRequest.class.getDeclaredField("col");
    colField.setAccessible(true);
    colField.setInt(request, 3);

    Field typeField = SeatEditRequest.class.getDeclaredField("type");
    typeField.setAccessible(true);
    typeField.set(request, null);  
    
    Field statusField = SeatEditRequest.class.getDeclaredField("status");
    statusField.setAccessible(true);
    statusField.set(request, "UNAVAILABLE");
    
    ErrorResponse response = (ErrorResponse) cinemaSeatService.Edit(hall.getId(), request);
    Optional<CinemaSeat> a=hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex(hall.getId(), 2, 3);
    assertEquals(a.get().getSeatType(), ESeat.REGULAR.toString());
    assertNotNull(response);
}
//Type.equals(null) is not a valid check, it should be type==null in main code

//Test edit seat with invalid status
@Test
void SEAT_017_testEditSeat_NullStatus() throws Exception{
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
    statusField.set(request, null);

    MyApiResponse response = cinemaSeatService.Edit(hall.getId(), request);

    Optional<CinemaSeat> a=hallSeatRepo.findByCinemaHallIdAndRowIndexAndColIndex(hall.getId(), 2, 3);
    assertEquals(a.get().getStatus(), ESeatStatus.AVAILABLE.toString());
    assertNotNull(response);
}
@Test
void SEAT_018_testSeatNotExists_Zero() {
    assertFalse(cinemaSeatService.isExist(hall.getId(), 0, 3));
}
    
}
