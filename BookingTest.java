package cinema.ticket.booking;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cinema.ticket.booking.exception.MyAccessDeniedException;
import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyConflictExecption;
import cinema.ticket.booking.exception.MyLockedException;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.Account;
import cinema.ticket.booking.model.Booking;
import cinema.ticket.booking.model.CinemaShow;
import cinema.ticket.booking.model.Payment;
import cinema.ticket.booking.model.ShowSeat;
import cinema.ticket.booking.model.SpamUser;
import cinema.ticket.booking.model.enumModel.BookingStatus;
import cinema.ticket.booking.model.enumModel.ESeatStatus;
import cinema.ticket.booking.model.enumModel.PaymentStatus;
import cinema.ticket.booking.model.enumModel.UserStatus;
import cinema.ticket.booking.repository.BookingRepository;
import cinema.ticket.booking.repository.PaymentRepository;
import cinema.ticket.booking.repository.ShowRepository;
import cinema.ticket.booking.repository.ShowSeatRepository;
import cinema.ticket.booking.repository.SpamUserRepository;
import cinema.ticket.booking.repository.UserRepository;
import cinema.ticket.booking.request.BookingRequest;
import cinema.ticket.booking.response.BookingResponse;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.service.impl.BookingServiceImpl;
import cinema.ticket.booking.service.impl.UserServiceImpl;
import jakarta.transaction.Transactional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Transactional
public class BookingServiceTest {

    @Autowired
    private BookingServiceImpl bookingServiceImpl;

    @Autowired
    private ShowSeatRepository showSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SpamUserRepository spamUserRepository;

    @InjectMocks
    private BookingServiceImpl bookingServiceImplMock;

    @Test
    void BOOK_001_seatsAreFull_test1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        Method method = BookingServiceImpl.class.getDeclaredMethod("seatsAreFull", CinemaShow.class);
        method.setAccessible(true);

        String cinema_show_id = "00165067-e870465b-45245005";
        CinemaShow show = new CinemaShow();
        show.setId(cinema_show_id);

        boolean check = (boolean) method.invoke(bookingServiceImpl, show);

        assertFalse(check);

    }

    @Test
    void BOOK_002_seatsAreFull_test2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        Method method = BookingServiceImpl.class.getDeclaredMethod("seatsAreFull", CinemaShow.class);
        method.setAccessible(true);

        String cinema_show_id = "00165067-e870465b-45245005";
        CinemaShow show = new CinemaShow();
        show.setId(cinema_show_id);

        List<ShowSeat> showSeats = showSeatRepository.findByShowId(cinema_show_id);
        for (ShowSeat seat : showSeats) {
            seat.setStatus(ESeatStatus.BOOKED);
            showSeatRepository.save(seat);
        }

        boolean check = (boolean) method.invoke(bookingServiceImpl, show);

        assertTrue(check);

    }

    @Test
    void BOOK_003_getSeatFromStatus_test1() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        Method method = BookingServiceImpl.class.getDeclaredMethod("getSeatFromStatus", String.class, CinemaShow.class, ESeatStatus.class);
        method.setAccessible(true);

        String cinema_show_id = "0c591085-01d67658-65dd19df";
        String seat_id = "EDPqnjYvdd";
        CinemaShow show = new CinemaShow();
        show.setId(cinema_show_id);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> method.invoke(bookingServiceImpl, seat_id, show, ESeatStatus.AVAILABLE)
        );

        assertNotNull(exception);

        String expectedMessage = "Do not found the seat";
        Throwable actualException = exception.getCause();

        assertTrue(actualException instanceof MyNotFoundException);
        assertEquals(expectedMessage, actualException.getMessage());

    }

    @Test
    void BOOK_004_getSeatFromStatus_test2() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        Method method = BookingServiceImpl.class.getDeclaredMethod("getSeatFromStatus", String.class, CinemaShow.class, ESeatStatus.class);
        method.setAccessible(true);

        String cinema_show_id = "00165067-e870465b-45245005";
        String seat_id = "EDPqnjYvdd";
        CinemaShow show = new CinemaShow();
        show.setId(cinema_show_id);

        ShowSeat seat = (ShowSeat) method.invoke(bookingServiceImpl, seat_id, show, ESeatStatus.AVAILABLE);

        assertNotNull(seat);
        assertEquals(seat_id, seat.getId());
        assertEquals(cinema_show_id, seat.getShow().getId());

    }

    @Test
    void BOOK_005_getSeatFromStatus_test3() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        Method method = BookingServiceImpl.class.getDeclaredMethod("getSeatFromStatus", String.class, CinemaShow.class, ESeatStatus.class);
        method.setAccessible(true);

        String cinema_show_id = "9ec7e849-190e222e-de378f6a";
        String seat_id = "1vjhQQUkZz";
        CinemaShow show = new CinemaShow();
        show.setId(cinema_show_id);

        InvocationTargetException exception = assertThrows(InvocationTargetException.class,
                () -> method.invoke(bookingServiceImpl, seat_id, show, ESeatStatus.AVAILABLE)
        );

        assertNotNull(exception);

        String expectedMessage = "The seat is booked";
        Throwable actualException = exception.getCause();

        assertTrue(actualException instanceof MyNotFoundException);
        assertEquals(expectedMessage, actualException.getMessage());

    }

    @Test
    void BOOK_006_setStatusForBookingAndSeats() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        Method method = BookingServiceImpl.class.getDeclaredMethod("setStatusForBookingAndSeats", Booking.class, BookingStatus.class, ESeatStatus.class);
        method.setAccessible(true);

        String booking_id = "76027d91-63cf320f-4f35d314";
        Booking booking = bookingRepository.findById(booking_id).get();

        method.invoke(bookingServiceImpl, booking, BookingStatus.PENDING, ESeatStatus.PENDING);

        Booking changedBooking = bookingRepository.findById(booking_id).get();
        assertEquals(BookingStatus.PENDING.name(), changedBooking.getStatus().name());
        for (ShowSeat seat : changedBooking.getSeats()) {
            assertEquals(ESeatStatus.PENDING.name(), seat.getStatus());
        }
    }

    @Test
    void BOOK_007_cancelBookingFromID() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        Method method = BookingServiceImpl.class.getDeclaredMethod("cancleBookingFromID", Booking.class);
        method.setAccessible(true);

        String booking_id = "76027d91-63cf320f-4f35d314";
        Booking booking = bookingRepository.findById(booking_id).get();

        method.invoke(bookingServiceImpl, booking);

        Booking changedBooking = bookingRepository.findById(booking_id).get();
        assertEquals(BookingStatus.CANCLED, changedBooking.getStatus());
        for (ShowSeat seat : changedBooking.getSeats()) {
            assertEquals(ESeatStatus.AVAILABLE.name(), seat.getStatus());
        }

    }

    @Test
    void BOOK_008_removeDuplicate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, NoSuchFieldException {

        Method method = BookingServiceImpl.class.getDeclaredMethod("removeDuplicate", List.class);
        method.setAccessible(true);

        List<String> list = new ArrayList<>();
        list.add("a");
        list.add("a");
        list.add("a");
        list.add("a");
        list.add("b");
        list.add("b");
        list.add("b");
        list.add("c");
        list.add("c");

        String[] removedDuplicate = (String[]) method.invoke(bookingServiceImpl, list);

        assertEquals(3, removedDuplicate.length);
    }

    @Test
    void BOOK_009_createBooking_test1() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP", "JW09iZw1Sj"] 
            }        
        """;

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        String username = "user_1";
        Exception exception = assertThrows(MyBadRequestException.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "You can not book more than 3 seats";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_010_createBooking_test2() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        String username = "";
        Exception exception = assertThrows(MyBadRequestException.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "Username must not empty";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_011_createBooking_test3() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        String username = "user_3";
        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "User is not found";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_012_createBooking_test4() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        String username = "user_1";
        String user_id = "fa19dcbd-241797c7-b83f36c5";
        Account user = userRepository.findById(user_id).get();
        user.setStatus(UserStatus.BLACKLISTED);
        userRepository.save(user);

        Exception exception = assertThrows(MyAccessDeniedException.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "You are not allowed to book ticket as you have been banned";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_013_createBooking_test5() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "a",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        String username = "user_1";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "Show is not found";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_014_createBooking_test6() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        String show_id = "1253d416-1461a365-45f8c445";
        CinemaShow show = showRepository.findById(show_id).get();

        String username = "user_1";
        String user_id = "fa19dcbd-241797c7-b83f36c5";
        Account user = userRepository.findById(user_id).get();

        String[] addData_seat_ids = {"zztfAVRs1c", "zQ4wHCFUeS", "ZFOXciSIXF", "Wl7TNW1LNs", "Weyeo8nmLy"};

        for (String seat_id : addData_seat_ids) {
            List<ShowSeat> addList = new ArrayList<>();
            addList.add(showSeatRepository.findById(seat_id).get());
            bookingRepository.save(new Booking(user, show, addList));
        }

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        Exception exception = assertThrows(MyLockedException.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "You have booked 5 tickets in this show, so you can pay no more tickets in this show";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_015_createBooking_test7() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        String show_id = "1253d416-1461a365-45f8c445";
        String username = "user_1";

        CinemaShow show = new CinemaShow();
        show.setId(show_id);

        List<ShowSeat> showSeats = showSeatRepository.findByShowId(show_id);
        for (ShowSeat seat : showSeats) {
            seat.setStatus(ESeatStatus.BOOKED);
            showSeatRepository.save(seat);
        }

        Exception exception = assertThrows(MyLockedException.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "Sorry, seats of this show are full. Please choose another show";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_016_createBooking_test8() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        String username = "user_1";
        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String show_id = "1253d416-1461a365-45f8c445";

        BookingResponse response = bookingServiceImpl.createBooking(username, request);
        assertNotNull(response);

        List<Booking> bookingList = bookingRepository.findAllByUserId(user_id);
        assertNotNull(bookingList);

        boolean foundBooking = false;
        for (Booking booking : bookingList) {
            if (booking.getUser().getId().equals(user_id) && booking.getShow().getId().equals(show_id)) {

                foundBooking = true;
                assertEquals(BookingStatus.PENDING.name(), booking.getStatus().name());
            }
        }

        assertTrue(foundBooking);
    }

    @Test
    void BOOK_017_createBooking_test9() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        String show_id = "1253d416-1461a365-45f8c445";
        CinemaShow show = showRepository.findById(show_id).get();

        String username = "user_1";
        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String user_2_id = "04767f9e-2fbe4a11-1684c93d";
        Account user = userRepository.findById(user_2_id).get();

        String[] addData_seat_ids = {"zztfAVRs1c", "zQ4wHCFUeS", "ZFOXciSIXF", "Wl7TNW1LNs", "Weyeo8nmLy"};

        for (String seat_id : addData_seat_ids) {
            List<ShowSeat> addList = new ArrayList<>();
            addList.add(showSeatRepository.findById(seat_id).get());
            bookingRepository.save(new Booking(user, show, addList));
        }

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        BookingResponse response = assertDoesNotThrow(() -> bookingServiceImpl.createBooking(username, request));
        assertNotNull(response);

        List<Booking> bookingList = bookingRepository.findAllByUserId(user_id);
        assertNotNull(bookingList);

        boolean foundBooking = false;
        for (Booking booking : bookingList) {
            if (booking.getUser().getId().equals(user_id) && booking.getShow().getId().equals(show_id)) {
                foundBooking = true;
                assertEquals(BookingStatus.PENDING.name(), booking.getStatus().name());
            }
        }

        assertTrue(foundBooking);

    }

    @Test
    void BOOK_018_createBooking_test10() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": ["9cXjBtl8nM", "a6uga7JgYS", "DwpPzw2gxP"] 
            }        
        """;

        String changing_seat_id = "9cXjBtl8nM";
        ShowSeat changing_seat = showSeatRepository.findById(changing_seat_id).get();
        changing_seat.setStatus(ESeatStatus.BOOKED);
        showSeatRepository.save(changing_seat);

        String username = "user_1";

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        Exception exception = assertThrows(MyConflictExecption.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "Seat ID 9cXjBtl8nM is booked, please choose other seats";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_019_createBooking_test11() throws JsonMappingException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        String json
                = """
            {
            "show_id": "1253d416-1461a365-45f8c445",
            "seat_ids": [] 
            }        
        """;

        String username = "user_1";

        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);

        Exception exception = assertThrows(Exception.class,
                () -> bookingServiceImpl.createBooking(username, request)
        );

        String expectedMessage = "Please choose at least one seat";
        assertEquals(expectedMessage, exception.getMessage());

    }

    @Test
    void BOOK_020_cancelBooking_test1() {

        String booking_id = "76027d91-63cf320f-4f35d314";
        String username = "";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.cancleBooking(username, booking_id)
        );

        String message = "Username must not empty";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_021_cancelBooking_test2() {

        String booking_id = "76027d91-63cf320f-4f35d314";
        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.cancleBooking(username, booking_id)
        );

        String message = "User is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_022_cancelBooking_test3() {

        String booking_id = "a";
        String username = "user_1";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.cancleBooking(username, booking_id)
        );

        String message = "Booking is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_023_cancelBooking_test4() {

        String booking_id = "76027d91-63cf320f-4f35d314";
        String username = "user_1";

        Exception exception = assertThrows(MyConflictExecption.class,
                () -> bookingServiceImpl.cancleBooking(username, booking_id)
        );

        String message = "This ticket is not belongs to user user_1";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_024_cancelBooking_test5() {

        String booking_id = "76027d91-63cf320f-4f35d314";
        String username = "avart";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> bookingServiceImpl.cancleBooking(username, booking_id)
        );

        String message = "This ticket can not be canceled";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_025_cancelBooking_test6() {

        String booking_id = "76027d91-63cf320f-4f35d314";
        String username = "avart";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        MyApiResponse response = bookingServiceImpl.cancleBooking(username, booking_id);
        assertNotNull(response);

        Booking changedBooking = bookingRepository.findById(booking_id).get();
        assertNotNull(changedBooking);
        assertEquals(BookingStatus.CANCLED, changedBooking.getStatus());

        List<ShowSeat> seats = changedBooking.getSeats();
        for (ShowSeat seat : seats) {
            assertEquals(ESeatStatus.AVAILABLE.name(), seat.getStatus());
        }

    }

    @Test
    void BOOK_026_listOfBooking_test1() {

        String username = "";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.listOfBooking(username)
        );

        String message = "Username must not empty";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_027_listOfBooking_test2() {

        String username = "user_3";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.listOfBooking(username)
        );

        String message = "User is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_028_listOfBooking_test3() {

        String username = "avart";

        List<BookingResponse> bookingList = bookingServiceImpl.listOfBooking(username);

        assertNotNull(bookingList);
        assertEquals(1, bookingList.size());

    }

    @Test
    void BOOK_029_getBookingFromID_test1() {

        String username = "";
        String booking_id = "76027d91-63cf320f-4f35d314";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.getBookingFromID(username, booking_id)
        );

        String message = "Username must not empty";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_030_getBookingFromID_test2() {

        String username = "user_3";
        String booking_id = "76027d91-63cf320f-4f35d314";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.getBookingFromID(username, booking_id)
        );

        String message = "User is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_031_getBookingFromID_test3() {

        String username = "avart";
        String booking_id = "a";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.getBookingFromID(username, booking_id)
        );

        String message = "Booking is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_032_getBookingFromID_test4() {

        String username = "avart";
        String booking_id = "76027d91-63cf320f-4f35d314";

        BookingResponse response = bookingServiceImpl.getBookingFromID(username, booking_id);
        assertNotNull(response);

        Account user = userRepository.getByUsername(username).get();

        assertEquals(booking_id, response.getId());
        assertEquals(user.getFullname(), response.getFullname());

    }

    @Test
    void BOOK_033_setBookingStatus_test1() {

        String username = "";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String status = "BOOKED";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.setBookingStatus(username, booking_id, status)
        );

        String message = "Username must not empty";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_034_setBookingStatus_test2() {

        String username = "user_3";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String status = "BOOKED";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.setBookingStatus(username, booking_id, status)
        );

        String message = "User is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_035_setBookingStatus_test3() {

        String username = "avart";
        String booking_id = "a";
        String status = "BOOKED";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.setBookingStatus(username, booking_id, status)
        );

        String message = "Booking is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_036_setBookingStatus_test4() {

        String username = "avart";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String status = "BOOKED";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> bookingServiceImpl.setBookingStatus(username, booking_id, status)
        );

        String message = "This ticket already have this status";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_037_setBookingStatus_test5() {

        String username = "avart";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String status = "BRUH";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> bookingServiceImpl.setBookingStatus(username, booking_id, status)
        );

        String message = "Not found status BRUH";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_038_setBookingStatus_test6() {

        String username = "avart";
        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String status = "PENDING";

        MyApiResponse response = bookingServiceImpl.setBookingStatus(username, booking_id, status);
        assertNotNull(response);

        List<Booking> expectedBooking = bookingRepository.findAllByUserId(user_id);
        boolean foundBooking = false;
        for (Booking booking : expectedBooking) {
            if (booking.getUser().getId().equals(user_id) && booking.getStatus().name().equals(BookingStatus.PENDING.name())) {
                for (ShowSeat seat : booking.getSeats()) {
                    assertEquals(ESeatStatus.AVAILABLE.name(), seat.getStatus());
                }
                foundBooking = true;
            }
        }
        assertTrue(foundBooking);
    }

    @Test
    void BOOK_039_setBookingStatus_test7() {

        String username = "avart";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String status = "CANCELED";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> bookingServiceImpl.setBookingStatus(username, booking_id, status)
        );

        String message = "This ticket can not be canceled";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_040_setBookingStatus_test8() {

        String username = "avart";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String status = "CANCELED";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        MyApiResponse response = assertDoesNotThrow(() -> bookingServiceImpl.setBookingStatus(username, booking_id, status));
        assertNotNull(response);

        Booking changedBooking = bookingRepository.findById(booking_id).get();
        assertNotNull(changedBooking);
        assertEquals(status, changedBooking.getStatus().name());

        List<ShowSeat> seats = changedBooking.getSeats();
        for (ShowSeat seat : seats) {
            assertEquals(ESeatStatus.AVAILABLE.name(), seat.getStatus());
        }

    }

    @Test
    void BOOK_041_setBookingStatus_test9() {

        String username = "avart";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String status = "BOOKED";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        MyApiResponse response = assertDoesNotThrow(() -> bookingServiceImpl.setBookingStatus(username, booking_id, status));
        assertNotNull(response);

        Booking changedBooking = bookingRepository.findById(booking_id).get();
        assertNotNull(changedBooking);
        assertEquals(status, changedBooking.getStatus().name());

        List<ShowSeat> seats = changedBooking.getSeats();
        for (ShowSeat seat : seats) {
            assertEquals(ESeatStatus.BOOKED.name(), seat.getStatus());
        }

    }

    @Test
    void BOOK_042_autoCancelBooking_test1() {

        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String show_id = "66bf2624-fb3bb5fa-210bed10";
        String seat_id = "23uAd50gKw";

        Account user = userRepository.findById(user_id).get();
        CinemaShow show = showRepository.findById(show_id).get();
        List<ShowSeat> seats = new ArrayList<>();
        seats.add(showSeatRepository.findById(seat_id).get());

        Booking booking = new Booking(user, show, seats);
        bookingRepository.save(booking);

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> bookingServiceImpl.autoCancleBooking()
        );

        String message = "No payment for booking found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void BOOK_043_autoCancelBooking_test2() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String show_id = "66bf2624-fb3bb5fa-210bed10";
        String seat_id = "23uAd50gKw";

        Account user = userRepository.findById(user_id).get();
        CinemaShow show = showRepository.findById(show_id).get();
        List<ShowSeat> seats = new ArrayList<>();
        seats.add(showSeatRepository.findById(seat_id).get());

        Booking booking = new Booking(user, show, seats);
        bookingRepository.save(booking);

        Booking updatedBooking = bookingRepository.findById(booking.getId()).get();
        Field field = Booking.class.getDeclaredField("create_at");
        field.setAccessible(true);

        LocalDateTime fakeTime = LocalDateTime.now().minusMinutes(16);
        Date fakeDate = Date.from(fakeTime.atZone(ZoneId.systemDefault()).toInstant());
        field.set(updatedBooking, fakeDate);
        bookingRepository.save(updatedBooking);

        Payment payment = new Payment(updatedBooking, 20000);
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        bookingServiceImpl.autoCancleBooking();

        Booking changedBooking = bookingRepository.findById(booking.getId()).get();
        assertNotNull(changedBooking);
        assertEquals(BookingStatus.CANCLED, changedBooking.getStatus());
        for (ShowSeat seat : changedBooking.getSeats()) {
            assertEquals(ESeatStatus.AVAILABLE.name(), seat.getStatus());
        }

    }

    @Test
    void BOOK_044_autoCancelBooking_test3() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String show_id = "66bf2624-fb3bb5fa-210bed10";
        String seat_id = "23uAd50gKw";

        Account user = userRepository.findById(user_id).get();
        CinemaShow show = showRepository.findById(show_id).get();
        List<ShowSeat> seats = new ArrayList<>();
        seats.add(showSeatRepository.findById(seat_id).get());

        Booking booking = new Booking(user, show, seats);
        bookingRepository.save(booking);

        Booking updatedBooking = bookingRepository.findById(booking.getId()).get();
        Field field = Booking.class.getDeclaredField("create_at");
        field.setAccessible(true);

        LocalDateTime fakeTime = LocalDateTime.now().minusMinutes(16);
        Date fakeDate = Date.from(fakeTime.atZone(ZoneId.systemDefault()).toInstant());
        field.set(updatedBooking, fakeDate);
        bookingRepository.save(updatedBooking);

        Payment payment = new Payment(updatedBooking, 20000);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        bookingServiceImpl.autoCancleBooking();

        Booking changedBooking = bookingRepository.findById(booking.getId()).get();
        assertNotNull(changedBooking);
        assertEquals(BookingStatus.BOOKED, changedBooking.getStatus());
        for (ShowSeat seat : changedBooking.getSeats()) {
            assertEquals(ESeatStatus.BOOKED.name(), seat.getStatus());
        }

    }

    @Test
    void BOOK_045_autoCancelBooking_test4() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String show_id = "66bf2624-fb3bb5fa-210bed10";
        String seat_id = "23uAd50gKw";

        Account user = userRepository.findById(user_id).get();
        CinemaShow show = showRepository.findById(show_id).get();
        List<ShowSeat> seats = new ArrayList<>();
        seats.add(showSeatRepository.findById(seat_id).get());

        Booking booking = new Booking(user, show, seats);
        bookingRepository.save(booking);

        Booking updatedBooking = bookingRepository.findById(booking.getId()).get();
        Field field = Booking.class.getDeclaredField("create_at");
        field.setAccessible(true);

        LocalDateTime fakeTime = LocalDateTime.now().minusMinutes(14);
        Date fakeDate = Date.from(fakeTime.atZone(ZoneId.systemDefault()).toInstant());
        field.set(updatedBooking, fakeDate);
        bookingRepository.save(updatedBooking);

        Payment payment = new Payment(updatedBooking, 20000);
        payment.setStatus(PaymentStatus.PAID);
        paymentRepository.save(payment);

        bookingServiceImpl.autoCancleBooking();

        Booking changedBooking = bookingRepository.findById(booking.getId()).get();
        assertNotNull(changedBooking);
        assertEquals(BookingStatus.BOOKED, changedBooking.getStatus());
        for (ShowSeat seat : changedBooking.getSeats()) {
            assertEquals(ESeatStatus.BOOKED.name(), seat.getStatus());
        }

    }

    @Test
    void BOOK_046_autoCancelBooking_test5() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        String user_id = "fa19dcbd-241797c7-b83f36c5";
        String show_id = "66bf2624-fb3bb5fa-210bed10";
        String seat_id = "23uAd50gKw";

        Account user = userRepository.findById(user_id).get();
        CinemaShow show = showRepository.findById(show_id).get();
        List<ShowSeat> seats = new ArrayList<>();
        seats.add(showSeatRepository.findById(seat_id).get());

        Booking booking = new Booking(user, show, seats);
        bookingRepository.save(booking);

        Booking updatedBooking = bookingRepository.findById(booking.getId()).get();
        Field field = Booking.class.getDeclaredField("create_at");
        field.setAccessible(true);

        LocalDateTime fakeTime = LocalDateTime.now().minusMinutes(14);
        Date fakeDate = Date.from(fakeTime.atZone(ZoneId.systemDefault()).toInstant());
        field.set(updatedBooking, fakeDate);
        bookingRepository.save(updatedBooking);

        Payment payment = new Payment(updatedBooking, 20000);
        payment.setStatus(PaymentStatus.CANCLED);
        paymentRepository.save(payment);

        bookingServiceImpl.autoCancleBooking();

        Booking changedBooking = bookingRepository.findById(booking.getId()).get();
        assertNotNull(changedBooking);
        assertEquals(BookingStatus.CANCLED, changedBooking.getStatus());
        for (ShowSeat seat : changedBooking.getSeats()) {
            assertEquals(ESeatStatus.AVAILABLE.name(), seat.getStatus());
        }

    }

    @Test
    void BOOK_047_blacklistUsers_test1() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        SpamUserRepository spamUserRepositoryF = mock(SpamUserRepository.class);
        UserServiceImpl userServiceImplF = mock(UserServiceImpl.class);

        Field spamRepoField = BookingServiceImpl.class.getDeclaredField("spamREPO");
        spamRepoField.setAccessible(true);
        spamRepoField.set(bookingServiceImplMock, spamUserRepositoryF);

        Field userSerField = BookingServiceImpl.class.getDeclaredField("userSER");
        userSerField.setAccessible(true);
        userSerField.set(bookingServiceImplMock, userServiceImplF);

        Field queueField = BookingServiceImpl.class.getDeclaredField("spamUsers");
        queueField.setAccessible(true);
        Queue<Account> queue = new LinkedList<>();
        queueField.set(bookingServiceImplMock, queue);

        bookingServiceImplMock.blacklistUsers();

        verify(spamUserRepositoryF, never()).findByUserId(any());
        verify(userServiceImplF, never()).saveUser(any());

    }

    @Test
    void BOOK_048_blacklistUsers_test2() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        String user_id = "fa19dcbd-241797c7-b83f36c5";
        Account user = userRepository.findById(user_id).get();
        Queue<Account> queue = new LinkedList<>();
        queue.add(user);

        Field queueField = BookingServiceImpl.class.getDeclaredField("spamUsers");
        queueField.setAccessible(true);
        queueField.set(bookingServiceImpl, queue);

        SpamUser spamUser = new SpamUser(user);
        spamUser.setSpamTimes(2);
        spamUserRepository.save(spamUser);

        bookingServiceImpl.blacklistUsers();

        SpamUser expectedSpamUser = spamUserRepository.findByUserId(user_id).get();
        assertNotNull(expectedSpamUser);
        assertEquals(3, expectedSpamUser.getSpamTimes());
        assertNotEquals(UserStatus.BLACKLISTED, expectedSpamUser.getUser().getStatus());

    }

    @Test
    void BOOK_049_blacklistUsers_test3() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        String user_id = "fa19dcbd-241797c7-b83f36c5";
        Account user = userRepository.findById(user_id).get();
        Queue<Account> queue = new LinkedList<>();
        queue.add(user);

        Field queueField = BookingServiceImpl.class.getDeclaredField("spamUsers");
        queueField.setAccessible(true);
        queueField.set(bookingServiceImpl, queue);

        SpamUser spamUser = new SpamUser(user);
        spamUser.setSpamTimes(3);
        spamUserRepository.save(spamUser);

        bookingServiceImpl.blacklistUsers();

        SpamUser expectedSpamUser = spamUserRepository.findByUserId(user_id).get();
        assertNotNull(expectedSpamUser);
        assertEquals(4, expectedSpamUser.getSpamTimes());
        assertEquals(UserStatus.BLACKLISTED.name(), expectedSpamUser.getUser().getStatus());

    }

    @Test
    void BOOK_050_blacklistUsers_test4() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        String user_id = "fa19dcbd-241797c7-b83f36c5";
        Account user = userRepository.findById(user_id).get();
        Queue<Account> queue = new LinkedList<>();
        queue.add(user);

        Field queueField = BookingServiceImpl.class.getDeclaredField("spamUsers");
        queueField.setAccessible(true);
        queueField.set(bookingServiceImpl, queue);

        bookingServiceImpl.blacklistUsers();

        SpamUser expectedSpamUser = spamUserRepository.findByUserId(user_id).get();
        assertNotNull(expectedSpamUser);
        assertEquals(1, expectedSpamUser.getSpamTimes());

    }

}
