package cinema.ticket.booking;
import cinema.ticket.booking.model.CinemaHall;

import cinema.ticket.booking.request.CinemaHallRequest;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.response.ErrorResponse;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.service.CinemaHallService;
import java.lang.reflect.Field;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // Ensures database rolls back after each test
class HallTest {

    @Autowired
    private CinemaHallService cinemaHallService;
    @Test
    void testNewHall_Success() {
        CinemaHall hall = new CinemaHall();
        hall.setId("H1");
        hall.setName("IMAX");
        hall.setTotalRow(10);
        hall.setTotalCol(12);

        MyApiResponse response = cinemaHallService.newHall(hall);
        assertEquals("Success", response.getMessage());

        CinemaHall savedHall = cinemaHallService.getHallById(hall.getId());
        assertNotNull(savedHall);
        assertEquals("IMAX", savedHall.getName());
    }

    @Test
    void testHall_IllegalCharacters() {
        CinemaHall hall = new CinemaHall();
        hall.setId("H2");
        hall.setName("IM@X!"); // Invalid characters
        hall.setTotalRow(10);
        hall.setTotalCol(12);

        MyApiResponse response = cinemaHallService.newHall(hall);
        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("Illeagal charaters in name", ((ErrorResponse) response).getMessage());
    }

    @Test
    void testDuplicateHallName() {
        CinemaHall hall1 = new CinemaHall();
        hall1.setId("H3");
        hall1.setName("VIP");
        hall1.setTotalRow(10);
        hall1.setTotalCol(10);
        cinemaHallService.newHall(hall1);

        CinemaHall hall2 = new CinemaHall();
        hall2.setId("H4");
        hall2.setName("VIP"); 
        hall2.setTotalRow(10);
        hall2.setTotalCol(10);

        MyApiResponse response = cinemaHallService.newHall(hall2);
        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("This hall is existed", ((ErrorResponse) response).getMessage());
    }

    @Test
    void testEditHall_Success() throws Exception {
        // Create and save a CinemaHall
        CinemaHall hall = new CinemaHall();
        hall.setId("H5");
        hall.setName("Standard");
        hall.setTotalRow(10);
        hall.setTotalCol(10);
        cinemaHallService.newHall(hall);
        CinemaHallRequest updateRequest = new CinemaHallRequest();

        Field nameField = CinemaHallRequest.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(updateRequest, "Standard Deluxe");
        Field rowField = CinemaHallRequest.class.getDeclaredField("totalRow");
        rowField.setAccessible(true);
        rowField.setInt(updateRequest, 12);

        
        Field colField = CinemaHallRequest.class.getDeclaredField("totalCol");
        colField.setAccessible(true);
        colField.setInt(updateRequest, 15);
        MyApiResponse response = cinemaHallService.editHall(hall.getId(), updateRequest);
        assertEquals("Success", response.getMessage());

       
        CinemaHall updatedHall = cinemaHallService.getHallById(hall.getId());
        assertEquals("Standard Deluxe", updatedHall.getName());
        assertEquals(12, updatedHall.getTotalRow());
        assertEquals(15, updatedHall.getTotalCol());
    }

    @Test
    void testEditHall_NotFound() throws Exception {
        CinemaHallRequest updateRequest = new CinemaHallRequest();
        Field nameField = CinemaHallRequest.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(updateRequest, "Non-Existent Hall");
        Field rowField = CinemaHallRequest.class.getDeclaredField("totalRow");
        rowField.setAccessible(true);
        rowField.setInt(updateRequest, 10);
        Field colField = CinemaHallRequest.class.getDeclaredField("totalCol");
        colField.setAccessible(true);
        colField.setInt(updateRequest, 10);
        assertThrows(MyNotFoundException.class, () -> cinemaHallService.editHall("9999", updateRequest));
    }

    @Test
    void testRemoveSuccess() {
        CinemaHall hall = new CinemaHall();
        hall.setId("H6");
        hall.setName("Gold Class");
        hall.setTotalRow(12);
        hall.setTotalCol(15);
        cinemaHallService.newHall(hall);

        MyApiResponse response = cinemaHallService.removeHall(hall.getId());
        assertEquals("Success", response.getMessage());

        assertThrows(MyNotFoundException.class, () -> cinemaHallService.getHallById(hall.getId()));
    }

    @Test
    void testRemoveNotFound() {
        assertThrows(MyNotFoundException.class, () -> cinemaHallService.removeHall("9999"));
    }

    @Test
    void testIsExistByName() {
        CinemaHall hall = new CinemaHall();
        hall.setId("H7");
        hall.setName("4DX");
        hall.setTotalRow(10);
        hall.setTotalCol(10);
        cinemaHallService.newHall(hall);

        assertTrue(cinemaHallService.isExistByName("4DX"));
    }

    @Test
    void testNotExistByName() {
        assertFalse(cinemaHallService.isExistByName("Non-Existent Hall"));
    }

    @Test
    void testGetAllHalls() {
        CinemaHall hall = new CinemaHall();
        hall.setId("H8");
        hall.setName("Platinum");
        hall.setTotalRow(12);
        hall.setTotalCol(14);
        cinemaHallService.newHall(hall);

        List<CinemaHall> halls = cinemaHallService.getAllHalls();
        assertFalse(halls.isEmpty());
    }

    @Test
    void testGetHallById() {
        CinemaHall hall = new CinemaHall();
        hall.setId("H9");
        hall.setName("Dolby Atmos");
        hall.setTotalRow(10);
        hall.setTotalCol(10);
        cinemaHallService.newHall(hall);

        CinemaHall retrievedHall = cinemaHallService.getHallById(hall.getId());
        assertNotNull(retrievedHall);
        assertEquals("Dolby Atmos", retrievedHall.getName());
    }

    @Test
    void testGetHallById_NotFound() {
        assertThrows(MyNotFoundException.class, () -> cinemaHallService.getHallById("9999"));
    }
}

