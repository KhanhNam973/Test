package cinema.ticket.booking;
import cinema.ticket.booking.model.CinemaHall;

import cinema.ticket.booking.request.CinemaHallRequest;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.response.ErrorResponse;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.service.impl.CinemaHallImpl;

import java.lang.reflect.Field;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional 
//OK
class HallTest {

    @Autowired
    private CinemaHallImpl cinemaHallService;
//Test add new hall with valid data
@Test
void testNewHall_Success() {
    CinemaHall hall = new CinemaHall();
    hall.setName("IMAX");
    hall.setTotalRow(10);
    hall.setTotalCol(12);
    MyApiResponse response = cinemaHallService.newHall(hall);
    assertEquals("Success", response.getMessage());
    CinemaHall savedHall = cinemaHallService.getHallById(hall.getId());
    assertNotNull(savedHall);
    assertEquals("IMAX", savedHall.getName());
}
//Test new hall with invalid row number
    @Test
    void testHall_InvalidRow() {
        CinemaHall hall = new CinemaHall();
        hall.setId("H2");
        hall.setName("Standard");
        hall.setTotalRow(0); // Invalid row number
        hall.setTotalCol(12);

        MyApiResponse response = cinemaHallService.newHall(hall);
        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("Row/Column number must be greater than 5", ((ErrorResponse) response).getMessage());
    }
//Test new hall with invalid name
    @Test
    void testHall_IllegalCharacters() {
        CinemaHall hall = new CinemaHall();
        hall.setName("\"InvalidName@#\""); // Invalid characters are not defined clearly in the code, but this is an example.
        hall.setTotalRow(10);
        hall.setTotalCol(12);

        MyApiResponse response = cinemaHallService.newHall(hall);
        assertEquals("Illeagal charaters in name", response.getMessage());
    }
//Test new hall with duplicate name
    @Test
    void testDuplicateHallName() {
        CinemaHall hall1 = new CinemaHall();
        hall1.setId("H4");
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

//Test new hall with invalid column number
    @Test
    void testInvalid_HallCol() {
        CinemaHall hall1 = new CinemaHall();
        hall1.setId("H5");
        hall1.setName("VIP");
        hall1.setTotalRow(12);
        hall1.setTotalCol(0);
        cinemaHallService.newHall(hall1);
        MyApiResponse response = cinemaHallService.newHall(hall1);
        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("Row/Column number must be greater than 5", ((ErrorResponse) response).getMessage());
    }
//Test new hall with invalid column and column number
    @Test
    void testInvalid_HallRowCol() {
        CinemaHall hall1 = new CinemaHall();
        hall1.setId("H5");
        hall1.setName("VIP");
        hall1.setTotalRow(2);
        hall1.setTotalCol(2);
        cinemaHallService.newHall(hall1);
        MyApiResponse response = cinemaHallService.newHall(hall1);
        assertInstanceOf(ErrorResponse.class, response);
        assertEquals("Row/Column number must be greater than 5", ((ErrorResponse) response).getMessage());
    }

//Test edit hall
    @Test
    void testEditHall_Success() throws Exception {
        CinemaHall hall = new CinemaHall();
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

//Test edit hall not found
    @Test
    void testEditHall_NotFound() throws Exception {
        CinemaHallRequest updateRequest = new CinemaHallRequest();
        Field nameField = CinemaHallRequest.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(updateRequest, "Standard Deluxe");
        Field rowField = CinemaHallRequest.class.getDeclaredField("totalRow");
        rowField.setAccessible(true);
        rowField.setInt(updateRequest, 10);
        Field colField = CinemaHallRequest.class.getDeclaredField("totalCol");
        colField.setAccessible(true);
        colField.setInt(updateRequest, 10);
        MyNotFoundException f=assertThrows(MyNotFoundException.class, () -> cinemaHallService.editHall("9999", updateRequest));
        assertEquals("Hall is not found", f.getMessage());
    }
    //Test edit hall invalid row
    @Test
    void testEditHall_invalidColumn() throws Exception {
        CinemaHall hall = new CinemaHall();
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
        colField.setInt(updateRequest, 0);

        MyApiResponse response = cinemaHallService.editHall(hall.getId(), updateRequest);
        assertEquals("Row/Column number must be greater than 5", (response).getMessage());
    //Save row or column number less than 5=> wrong
    }

    //Test edit hall invalid row
    @Test
    void testEditHall_invalidRowAndColumn() throws Exception {
        CinemaHall hall = new CinemaHall();
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
        rowField.setInt(updateRequest, 2);

        Field colField = CinemaHallRequest.class.getDeclaredField("totalCol");
        colField.setAccessible(true);
        colField.setInt(updateRequest, 2);

        MyApiResponse response = cinemaHallService.editHall(hall.getId(), updateRequest);
        assertEquals("Row/Column number must be greater than 5", (response).getMessage());
    //Save row or column number less than 5=> wrong
    }

    @Test
    void testEditHall_invalidName() throws Exception {
        CinemaHall hall = new CinemaHall();
        hall.setName("Standard");
        hall.setTotalRow(10);
        hall.setTotalCol(10);
        cinemaHallService.newHall(hall);
        CinemaHallRequest updateRequest = new CinemaHallRequest();

        Field nameField = CinemaHallRequest.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(updateRequest, "@@@@@");

        Field rowField = CinemaHallRequest.class.getDeclaredField("totalRow");
        rowField.setAccessible(true);
        rowField.setInt(updateRequest, 12);

        Field colField = CinemaHallRequest.class.getDeclaredField("totalCol");
        colField.setAccessible(true);
        colField.setInt(updateRequest, 15);

        MyApiResponse response = cinemaHallService.editHall(hall.getId(), updateRequest);
        assertEquals("Illeagal charaters in name", (response).getMessage());
    //name is not defined invalid when
    }

//Test remove hall
    @Test
    void testRemoveSuccess() {
        CinemaHall hall = new CinemaHall();
        hall.setName("IMAX");
        hall.setTotalRow(10);
        hall.setTotalCol(12);

        cinemaHallService.newHall(hall);
        MyApiResponse response1 = cinemaHallService.removeHall(hall.getId());
        assertEquals("Success", response1.getMessage());
    }

//Test remove hall not found
    @Test
    void testRemoveNotFound() {
        MyNotFoundException f= assertThrows(MyNotFoundException.class, () -> cinemaHallService.removeHall("9999"));
        assertEquals("Hall is not found", f.getMessage());
    }

//Test hall existence by name
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

//Test hall non-existence by name
    @Test
    void testNotExistByName() {
        assertFalse(cinemaHallService.isExistByName("Non-Existent Hall"));
    }

//Test get all halls
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

//Test get hall by ID
    @Test
    void testGetHallById() {
        CinemaHall hall = new CinemaHall();
        hall.setName("Dolby Atmos");
        hall.setTotalRow(10);
        hall.setTotalCol(10);
        cinemaHallService.newHall(hall);

        CinemaHall retrievedHall = cinemaHallService.getHallById(hall.getId());
        assertNotNull(retrievedHall);
        assertEquals("Dolby Atmos", retrievedHall.getName());
    }
//Test get hall by ID that does not exist
    @Test
    void testGetHallById_NotFound() {
        assertThrows(MyNotFoundException.class, () -> cinemaHallService.getHallById("9999L"));
    }
}

