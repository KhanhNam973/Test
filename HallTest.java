package cinema.ticket.booking;
import cinema.ticket.booking.model.CinemaHall;
import cinema.ticket.booking.repository.CinemaHallRepository;
import cinema.ticket.booking.request.CinemaHallRequest;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.response.ErrorResponse;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.service.impl.CinemaHallImpl;

import java.lang.reflect.Field;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional 
//OK
class HallTest {

    @Autowired
    private CinemaHallImpl cinemaHallService;
    @Autowired
    private CinemaHallRepository hallR;
//Test add new hall with valid data
@Test
void HALL_001_testNewHall_Success() {
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
//Test add new hall with duplcated name
@Test
void HALL_002_testNewHall_DuplicateName() {
    CinemaHall hall = new CinemaHall();
    hall.setName("IMAX");
    hall.setTotalRow(10);
    hall.setTotalCol(12);
    cinemaHallService.newHall(hall);
    int a=hallR.findAll().size();
    CinemaHall hall1 = new CinemaHall();
    hall1.setName("IMAX");
    hall1.setTotalRow(10);
    hall1.setTotalCol(12);
    MyApiResponse response = cinemaHallService.newHall(hall1);
    int b=hallR.findAll().size();
    assertEquals(a,b);
    assertEquals("This hall is existed", response.getMessage());
}
//Test add new hall with null name
@Test
void HALL_003_testNewHall_NullName() {
    int a=hallR.findAll().size();
    CinemaHall hall = new CinemaHall();
    hall.setName(null);
    hall.setTotalRow(10);
    hall.setTotalCol(12);
    NullPointerException f=assertThrows(NullPointerException.class, () -> cinemaHallService.newHall(hall));
    int b=hallR.findAll().size();
    assertEquals(a,b);
    assertNotNull(f.getMessage());
}

//Test new hall with invalid row
    @Test
    void HALL_004_testHall_InvalidRow() {
        int a=hallR.findAll().size();
        CinemaHall hall = new CinemaHall();
        hall.setName("Standard");
        hall.setTotalRow(0);
        hall.setTotalCol(12);

        MyApiResponse response = cinemaHallService.newHall(hall);
        int b=hallR.findAll().size();
        assertEquals(a,b);
        assertEquals("Row/Column number must be greater than 5", ((ErrorResponse) response).getMessage());
    }
//Test new hall with invalid column number
@Test
void HALL_005_testInvalid_HallCol() {
    int a=hallR.findAll().size();
    CinemaHall hall1 = new CinemaHall();
    hall1.setName("VIP");
    hall1.setTotalRow(12);
    hall1.setTotalCol(-1);
    cinemaHallService.newHall(hall1);
    MyApiResponse response = cinemaHallService.newHall(hall1);
    int b=hallR.findAll().size();
    assertEquals(a,b);
    assertEquals("Row/Column number must be greater than 5", ((ErrorResponse) response).getMessage());
}
//Test new hall with invalid column and row number
@Test
void HALL_006_testInvalid_HallRowCol() {
    int a=hallR.findAll().size();
    CinemaHall hall1 = new CinemaHall();
    hall1.setName("VIP");
    hall1.setTotalRow(2);
    hall1.setTotalCol(2);
    cinemaHallService.newHall(hall1);
    MyApiResponse response = cinemaHallService.newHall(hall1);
    int b=hallR.findAll().size();
    assertEquals(a,b);
    assertEquals("Row/Column number must be greater than 5", ((ErrorResponse) response).getMessage());
}
//Test new hall with invalid name
    @Test
    void HALL_007_testHall_IllegalCharacters() {
        CinemaHall hall = new CinemaHall();
        hall.setName("\"InvalidName@#\""); // Invalid characters are not defined clearly in the code, but this is an example.
        hall.setTotalRow(10);
        hall.setTotalCol(12);
        int a=hallR.findAll().size();
        MyApiResponse response = cinemaHallService.newHall(hall);
        int b=hallR.findAll().size();
        assertEquals(a,b);
        assertEquals("Illeagal charaters in name", response.getMessage());
    }

//Test edit hall
    @Test
    void HALL_008_testEditHall_Success() throws Exception {
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
    void HALL_009_testEditHall_NotFound() throws Exception {
        int a=hallR.findAll().size();
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
        int b=hallR.findAll().size();
        assertEquals(a,b);
        assertEquals("Hall is not found", f.getMessage());
    }
    //Test edit hall invalid column
    @Test
    void HALL_010_testEditHall_invalidColumnOrCoulumn() throws Exception {
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
        rowField.setInt(updateRequest, 10);

        Field colField = CinemaHallRequest.class.getDeclaredField("totalCol");
        colField.setAccessible(true);
        colField.setInt(updateRequest, 0);

        MyApiResponse response = cinemaHallService.editHall(hall.getId(), updateRequest);
        CinemaHall a= hallR.findById(hall.getId()).orElse(null);
        assertNotEquals(a.getTotalCol(),0);
        assertEquals("Row/Column number must be greater than 5", (response).getMessage());
    //Save row or column number less than 5=> wrong
    }

    //Test edit hall invalid row and column
    @Test
    void HALL_011_testEditHall_invalidRowAndColumn() throws Exception {
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
        CinemaHall a= hallR.findById(hall.getId()).orElse(null);
        assertNotEquals(a.getTotalCol(),2);
        assertNotEquals(a.getTotalRow(),2);
        assertEquals("Row/Column number must be greater than 5", (response).getMessage());
    //Save row or column number less than 5=> wrong
    }

    @Test
    void HALL_012_testEditHall_invalidName() throws Exception {
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
        CinemaHall a= hallR.findById(hall.getId()).orElse(null);
        assertNotEquals(a.getName(),"@@@@@");
        assertEquals("Illeagal charaters in name", (response).getMessage());
    //name is not defined invalid when
    }

    @Test
    void HALL_013_testEditHall_NullName() throws Exception {
        CinemaHall hall = new CinemaHall();
        hall.setName("Standard");
        hall.setTotalRow(10);
        hall.setTotalCol(10);
        cinemaHallService.newHall(hall);
        CinemaHallRequest updateRequest = new CinemaHallRequest();
        Field nameField = CinemaHallRequest.class.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(updateRequest, null);

        Field rowField = CinemaHallRequest.class.getDeclaredField("totalRow");
        rowField.setAccessible(true);
        rowField.setInt(updateRequest, 12);

        Field colField = CinemaHallRequest.class.getDeclaredField("totalCol");
        colField.setAccessible(true);
        colField.setInt(updateRequest, 15);

        assertThrows(NullPointerException.class, ()-> cinemaHallService.editHall(hall.getId(), updateRequest));
        CinemaHall a= hallR.findById(hall.getId()).orElse(null);
        assertNotEquals(a.getName(),null);
        
    //name is not defined invalid when
    }

//Test remove hall
    @Test
    void HALL_014_testRemoveSuccess() {
        CinemaHall hall = new CinemaHall();
        hall.setName("IMAX");
        hall.setTotalRow(10);
        hall.setTotalCol(12);

        cinemaHallService.newHall(hall);
        //assertNotNull(cinemaHallService.getHallById(hall.getId()));
        MyApiResponse response1 = cinemaHallService.removeHall(hall.getId());
        CinemaHall a= hallR.findById(hall.getId()).orElse(null);
        assertNull(a);
        assertEquals("Success", response1.getMessage());
    }

//Test remove hall not found
    @Test
    void HALL_015_testRemoveNotFound() {
        int a=hallR.findAll().size();
        MyNotFoundException f= assertThrows(MyNotFoundException.class,
         () -> cinemaHallService.removeHall("9999"));
        int b=hallR.findAll().size();
        assertEquals(a,b);
        assertEquals("Hall is not found", f.getMessage());
    }

    @Test
    void HALL_016_testRemoveNullID() {
        int a=hallR.findAll().size();
        InvalidDataAccessApiUsageException f= assertThrows(InvalidDataAccessApiUsageException.class,
         () -> cinemaHallService.removeHall(null));
        int b=hallR.findAll().size();
        assertEquals(a,b);
        assertNotNull(f);
    }

//Test hall existence by name
    @Test
    void HALL_017_testIsExistByName() {
        CinemaHall hall = new CinemaHall();
        hall.setName("4DX");
        hall.setTotalRow(10);
        hall.setTotalCol(10);
        cinemaHallService.newHall(hall);

        assertTrue(cinemaHallService.isExistByName("4DX"));
    }

//Test hall non-existence by name
    @Test
    void HALL_018_testNotExistByName() {
        assertFalse(cinemaHallService.isExistByName("Non-Existent Hall"));
    }
    @Test
    void HALL_019_testNUllByName() {
        assertFalse(cinemaHallService.isExistByName(null));
    }

//Test get all halls
    @Test
    void HALL_020_testGetAllHalls() {
        List<CinemaHall> halls = cinemaHallService.getAllHalls();
        assertFalse(halls.isEmpty());
    }

//Test get hall by ID
    @Test
    void HALL_021_testGetHallById() {
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
    void HALL_022_testGetHallById_NotFound() {
        assertThrows(MyNotFoundException.class, () -> cinemaHallService.getHallById("9999L"));
    }
    @Test
    void HALL_023_testGetHallById_Null() {
        assertThrows(InvalidDataAccessApiUsageException.class, () -> cinemaHallService.getHallById(null));
    }
}

