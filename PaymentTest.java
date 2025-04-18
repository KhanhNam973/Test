package cinema.ticket.booking;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.Payment;
import cinema.ticket.booking.model.enumModel.PaymentStatus;
import cinema.ticket.booking.repository.BookingRepository;
import cinema.ticket.booking.repository.PaymentRepository;
import cinema.ticket.booking.request.PaymentRequest;
import cinema.ticket.booking.response.PaymentResponse;
import cinema.ticket.booking.service.impl.PaymentServiceImpl;
import cinema.ticket.booking.utils.VNPay;

import javax.servlet.ServletException;


import cinema.ticket.booking.model.Booking;
import cinema.ticket.booking.model.enumModel.BookingStatus;
import cinema.ticket.booking.response.MyApiResponse;
import jakarta.transaction.Transactional;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Transactional
public class PaymentServiceTest {

    @Autowired
    private PaymentServiceImpl paymentServiceImpl;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void PAY_001_createPayment_test1() {

        String username = "user_1";
        String booking_id = "1";
        String ip_address = "127.0.0.1";

        PaymentRequest request = new PaymentRequest(booking_id, "");

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> paymentServiceImpl.create(username, request, ip_address)
        );

        String message = "Ticket ID 1 is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_002_createPayment_test2() {

        String username = "user_1";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String ip_address = "127.0.0.1";

        PaymentRequest request = new PaymentRequest(booking_id, "");

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> paymentServiceImpl.create(username, request, ip_address)
        );

        String message = "This ticket have been already paid or canceled before.";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_003_createPayment_test3() {

        String username = "user_1";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String ip_address = "127.0.0.1";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        PaymentRequest request = new PaymentRequest(booking_id, "");

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> paymentServiceImpl.create(username, request, ip_address)
        );

        String message = "This ticket have been already pending for payment.";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_004_createPayment_test4() {

        String username = "";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String ip_address = "127.0.0.1";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        List<Payment> paymentList = paymentRepository.findAllByBookingId(booking_id);
        for (Payment payment : paymentList) {
            paymentRepository.deleteById(payment.getId());
        }

        PaymentRequest request = new PaymentRequest(booking_id, "");

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> paymentServiceImpl.create(username, request, ip_address)
        );

        String message = "Username must not empty";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_005_createPayment_test5() {

        String username = "user_3";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String ip_address = "127.0.0.1";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        List<Payment> paymentList = paymentRepository.findAllByBookingId(booking_id);
        for (Payment payment : paymentList) {
            paymentRepository.deleteById(payment.getId());
        }

        PaymentRequest request = new PaymentRequest(booking_id, "");

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> paymentServiceImpl.create(username, request, ip_address)
        );

        String message = "User is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_006_createPayment_test6() {

        String username = "user_1";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String ip_address = "127.0.0.1";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        List<Payment> paymentList = paymentRepository.findAllByBookingId(booking_id);
        for (Payment payment : paymentList) {
            paymentRepository.deleteById(payment.getId());
        }

        PaymentRequest request = new PaymentRequest(booking_id, "");

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> paymentServiceImpl.create(username, request, ip_address)
        );

        String message = "This ticket is not belongs to user_1";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_007_createPayment_test7() throws NoSuchFieldException, SecurityException {

        String username = "avart";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String ip_address = "127.0.0.1";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        List<Payment> paymentList = paymentRepository.findAllByBookingId(booking_id);
        for (Payment payment : paymentList) {
            paymentRepository.deleteById(payment.getId());
        }

        PaymentRequest request = new PaymentRequest(booking_id, "");

        try (MockedStatic<VNPay> mockedStatic = mockStatic(VNPay.class)) {

            mockedStatic.when(() -> VNPay.createPay(any(), any(), any())).thenThrow(new ServletException("An error occurred"));

            PaymentResponse response = paymentServiceImpl.create(username, request, ip_address);

            assertEquals(PaymentStatus.CANCLED, response.getStatus());
            assertEquals("none", response.getPaymentUrl());

        }

    }

    @Test
    void PAY_008_createPayment_test8() throws NoSuchFieldException, SecurityException {

        String username = "avart";
        String booking_id = "76027d91-63cf320f-4f35d314";
        String ip_address = "127.0.0.1";

        Booking booking = bookingRepository.findById(booking_id).get();
        booking.setStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        List<Payment> paymentList = paymentRepository.findAllByBookingId(booking_id);
        for (Payment payment : paymentList) {
            paymentRepository.deleteById(payment.getId());
        }

        PaymentRequest request = new PaymentRequest(booking_id, "");

        PaymentResponse response = paymentServiceImpl.create(username, request, ip_address);

        assertNotNull(response);
        assertEquals(PaymentStatus.PENDING.name(), response.getStatus());
        assertNotEquals("none", response.getPaymentUrl());

    }

    @Test
    void PAY_009_getFromId_test1() {

        String username = "avart";
        String payment_id = "1";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> paymentServiceImpl.getFromId(username, payment_id)
        );

        String message = "Payment is not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_010_getFromId_test2() {

        String username = "";
        String payment_id = "f1d2859f-7086d6a9-5bed308f";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> paymentServiceImpl.getFromId(username, payment_id)
        );

        String message = "Username must not empty";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_011_getFromId_test3() {

        String username = "user_3";
        String payment_id = "f1d2859f-7086d6a9-5bed308f";

        Exception exception = assertThrows(MyNotFoundException.class,
                () -> paymentServiceImpl.getFromId(username, payment_id)
        );

        String message = "User not found";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_012_getFromId_test4() {

        String username = "user_1";
        String payment_id = "f1d2859f-7086d6a9-5bed308f";

        Exception exception = assertThrows(MyBadRequestException.class,
                () -> paymentServiceImpl.getFromId(username, payment_id)
        );

        String message = "Payment not belongs to user user_1";
        assertEquals(message, exception.getMessage());

    }

    @Test
    void PAY_013_getFromId_test5() {

        String username = "avart";
        String payment_id = "f1d2859f-7086d6a9-5bed308f";

        PaymentResponse response = paymentServiceImpl.getFromId(username, payment_id);

        assertNotNull(response);
        assertEquals(payment_id, response.getId());

        Payment payment = paymentRepository.findById(response.getId()).get();
        assertEquals(username, payment.getBooking().getUser().getUsername());

    }

    @Test
    void PAY_014_verifyPayment_test1() {

        String username = "avart";
        String payment_id = "f1d2859f-7086d6a9-5bed308f";

        Payment payment = paymentRepository.findById(payment_id).get();
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        try (MockedStatic<VNPay> mockedStatic = mockStatic(VNPay.class)) {

            mockedStatic.when(() -> VNPay.verifyPay(any())).thenThrow(new ServletException("An unknown error occurred"));

            Exception exception = assertThrows(ServletException.class,
                    () -> paymentServiceImpl.verifyPayment(username, payment_id)
            );

            String message = "An unknown error occurred";
            assertEquals(message, exception.getMessage());

        }

    }

    @Test
    void PAY_015_verifyPayment_test2() {

        String username = "avart";
        String payment_id = "f1d2859f-7086d6a9-5bed308f";

        Payment payment = paymentRepository.findById(payment_id).get();
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        try (MockedStatic<VNPay> mockedStatic = mockStatic(VNPay.class)) {

            mockedStatic.when(() -> VNPay.verifyPay(any())).thenReturn(0);

            MyApiResponse response = paymentServiceImpl.verifyPayment(username, payment_id);

            assertNotNull(response);
            assertEquals("Ticket is paid. You will receive this email", response.getMessage());

            Payment checkPayment = paymentRepository.findById(payment_id).get();
            assertEquals(PaymentStatus.PAID, checkPayment.getStatus());

        }

    }

    @Test
    void PAY_016_verifyPayment_test3() {

        String username = "avart";
        String payment_id = "f1d2859f-7086d6a9-5bed308f";

        Payment payment = paymentRepository.findById(payment_id).get();
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        try (MockedStatic<VNPay> mockedStatic = mockStatic(VNPay.class)) {

            mockedStatic.when(() -> VNPay.verifyPay(any())).thenReturn(2);

            MyApiResponse response = paymentServiceImpl.verifyPayment(username, payment_id);

            assertNotNull(response);
            assertEquals("Ticket is unpaid", response.getMessage());

            Payment checkPayment = paymentRepository.findById(payment_id).get();
            assertEquals(PaymentStatus.CANCLED, checkPayment.getStatus());

        }

    }

    @Test
    void PAY_017_getAllPaymentsOfUser() {

        String username = "avart";

        List<PaymentResponse> paymentList = paymentServiceImpl.getAllPaymentsOfUser(username);

        assertNotNull(paymentList);
        assertEquals(1, paymentList.size());

    }

}
