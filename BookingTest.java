package cinema.ticket.booking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyConflictExecption;
import cinema.ticket.booking.model.Account;
import cinema.ticket.booking.model.Booking;
import cinema.ticket.booking.model.enumModel.BookingStatus;
import cinema.ticket.booking.repository.BookingRepository;
import cinema.ticket.booking.repository.UserRepository;
import cinema.ticket.booking.request.BookingRequest;
import cinema.ticket.booking.response.BookingResponse;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.service.impl.BookingServiceImpl;
import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional // tự động rollback db
public class BookingServiceTest {
    // Test tất cả các function trong interface BookingService
    @Autowired
    private BookingServiceImpl bookingService;
    
    @Autowired
    private UserRepository userREPO;


    @Autowired
    private BookingRepository bookingREPO;

    // Constants
    private final String username = "avart";
    private final String booking_id = "76027d91-63cf320f-4f35d314";

    // Function 1: createBooking()
    // Default: User exists in DB
    @Test
    void test001_testCreateBooking3Seats_test1() throws JsonMappingException, JsonProcessingException {
        // BookingServiceImpl.java line 129-130, no more than 4 tickets can be booked at once
        // Mocking the JSON input
        ObjectMapper objectMapper = new ObjectMapper();
        String json = """
        {
        "show_id": "719b41d4-07cf3ce5-794cb046",
        "seat_ids": ["049asw9yjp", "3hE9bEpble", "4GY1E6WzFg"] 
        }
        """; // variables are @JsonProperty and @NotNull
        
        // Parsing the JSON input to BookingRequest object
        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);
        // Another user
        String username = "user_1";
        BookingResponse response = bookingService.createBooking(username, request);
        
        assertNotNull(response);
        // Kiểm tra kĩ hơn các trường ko Null???
        assertNotNull(response.getId());
        assertNotNull(response.getShowId());
        assertNotNull(response.getFullname());
        assertNotNull(response.getPrice());
        assertNotNull(response.getSeats());
        assertNotNull(response.getMovieName());
        assertNotNull(response.getHallName());
        assertNotNull(response.getStartTime());
        assertNotNull(response.getCreateAt());
        assertNotNull(response.getStatus());
        // Tìm nó đúng, rồi có các seats_id, show_id gì gì đó???
        // Lôi lại từ DB
        Account account = userREPO.getByUsername(username).get();
        Booking booking = bookingREPO.findByIdAndUserId(response.getId(), account.getId()).orElse(null);
        
        // Ktra showID, bookingID
        assertNotNull(booking);
        assertEquals(response.getShowId(), booking.getShow().getId());
        assertEquals(response.getId(), booking.getId());
        assertEquals(response.getSeats(), booking.getNameOfSeats());
    }

    @Test
    void test002_testCreateBooking5Seats_test1() throws JsonMappingException, JsonProcessingException {
        // BookingServiceImpl.java line 129-130, no more than 4 tickets can be booked at once
        // Mocking the JSON input
        ObjectMapper objectMapper = new ObjectMapper();
        String json = """
        {
        "show_id": "719b41d4-07cf3ce5-794cb046",
        "seat_ids": ["049asw9yjp", "3hE9bEpble", "4GY1E6WzFg", "4HUJxILBP9", "6Od4dvpor1"] 
        }
        """; // variables are @JsonProperty and @NotNull
        
        // Parsing the JSON input to BookingRequest object
        BookingRequest request = objectMapper.readValue(json, BookingRequest.class);
        // Another user
        String username = "user_1";
        assertThrows(MyBadRequestException.class, () -> bookingService.createBooking(username, request));
        
    }

    // Function 2: cancleBooking()
    // Default: Cancel bookings that have already existed in DB
    // 2.1. If user did not book?
    @Test
    void test003_testCancelBooking_test2() {
        // username == "user_1"
        String username = "user_1";
        assertThrows(MyConflictExecption.class, () -> bookingService.cancleBooking(username, booking_id));
    }

    // 2.2. If user has already booked / cancelled
    @Test
    void test004_testCancelBooking_test2() {
        // username == "avast"
        // String username = "avart";
        assertThrows(MyBadRequestException.class, () -> bookingService.cancleBooking(username, booking_id));
    }

    // 2.3. If user is pending
    // Default: mock createBooking()
    @Test
    void test005_testCancelBooking_test2() throws JsonMappingException, JsonProcessingException {
        // Prep data
        // String username = "avart";
        // String booking_id = "76027d91-63cf320f-4f35d314";
        
        Account account = userREPO.getByUsername(username).get();
        Booking booking = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        booking.setStatus(BookingStatus.PENDING);
        bookingREPO.save(booking);

        // cancleBooking()
        MyApiResponse cancel = bookingService.cancleBooking(username, booking_id);
        assertNotNull(cancel);
        Booking booking2 = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        assertEquals(BookingStatus.CANCLED, booking2.getStatus());
    }

    // Function 3: getBookingFromId()
    @Test
    void test006_testGetBookingFromId_test3() throws JsonMappingException, JsonProcessingException {
        // username == "avart"
        // String username = "avart";
        Account account = userREPO.getByUsername(username).get();
        Booking booking = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        BookingResponse response = new BookingResponse(booking);
        assertNotNull(response);
        assertNotNull(response.getId());
        assertNotNull(response.getShowId());
        assertNotNull(response.getFullname());
        assertNotNull(response.getPrice());
        assertNotNull(response.getSeats());
        assertNotNull(response.getMovieName());
        assertNotNull(response.getHallName());
        assertNotNull(response.getStartTime());
        assertNotNull(response.getCreateAt());
        assertNotNull(response.getStatus());
    }

    // Function 4: listOfBooking()
    @Test
    void test007_testListOfBooking_test4() {
        // username == "avart"   
        List<BookingResponse> bookingList = bookingService.listOfBooking(username);
        // A bit difficult so I check this
        assertNotNull(bookingList);
        assertFalse(bookingList.isEmpty());
    }

    // Function 5: setBookingStatus()
    // 5.1. Status to PENDING
    @Test
    void test008_testSetBookingStatusPending_test5() {
        // Supposed result
        Account account = userREPO.getByUsername(username).get();
        Booking booking = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        booking.setStatus(BookingStatus.PENDING);
        bookingREPO.save(booking);

        String status = "PENDING";

        // If already in that 
        //assertThrows(MyBadRequestException.class, () -> booking.getStatus().name().equals(status));

        // Test new status change
        MyApiResponse changeStatus = bookingService.setBookingStatus(username, booking_id, status);
        assertNotNull(changeStatus);
        Booking booking2 = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        assertEquals(changeStatus.getStatus(), booking2.getStatus());
    }

    // 5.2. Status to BOOKED
    @Test
    void test009_testSetBookingStatusBooked_test5() {
        // Supposed result
        Account account = userREPO.getByUsername(username).get();
        Booking booking = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        booking.setStatus(BookingStatus.BOOKED);
        bookingREPO.save(booking);

        String status = "BOOKED";

        // If already in that 
        //assertThrows(MyBadRequestException.class, () -> booking.getStatus().name().equals(status));

        // Test new status change
        MyApiResponse changeStatus = bookingService.setBookingStatus(username, booking_id, status);
        assertNotNull(changeStatus);
        Booking booking2 = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        assertEquals(changeStatus.getStatus(), booking2.getStatus());
    }

    // 5.3. Status to CANCLED
    @Test
    void test010_testSetBookingStatusCancelled_test5() {
        // Supposed result
        Account account = userREPO.getByUsername(username).get();
        Booking booking = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        booking.setStatus(BookingStatus.CANCLED);
        bookingREPO.save(booking);

        String status = "CANCLED";

        // If already in that 
        //assertThrows(MyBadRequestException.class, () -> booking.getStatus().name().equals(status));

        // Test new status change
        MyApiResponse changeStatus = bookingService.setBookingStatus(username, booking_id, status);
        assertNotNull(changeStatus);
        Booking booking2 = bookingREPO.findByIdAndUserId(booking_id, account.getId()).orElse(null);
        assertEquals(changeStatus.getStatus(), booking2.getStatus());
    }

}
