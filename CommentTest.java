package cinema.ticket.booking;
import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.Comment;
import cinema.ticket.booking.repository.CommentRepository;
import cinema.ticket.booking.request.AddCommentRequest;
import cinema.ticket.booking.response.CommentResponse;
import cinema.ticket.booking.service.impl.CommentServiceImpl;
import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Transactional
public class CommentTest {

    @Autowired
    private CommentServiceImpl commentService;
    @Autowired
    private CommentRepository commentRepo;
// test with valid data
    @Test
    public void testAddComment_Success() throws Exception {
           AddCommentRequest request = new AddCommentRequest();
           
           Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
           movieID.setAccessible(true);
           movieID.set(request,2L);
           
           Field commentField=AddCommentRequest.class.getDeclaredField("comment");
           commentField.setAccessible(true);
           commentField.set(request,"Great movie!");       
            
           Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
           ratingField.setAccessible(true);
           ratingField.setInt(request, 5);

           CommentResponse c=commentService.addComment("avart", request);
           assertEquals(c.getComment(), "Great movie!");
           assertEquals(c.getUsername(), "avart");
           //null update-time because in CommentResponse class, update-time is not check null when the comment is just created
        }

// test with valid data
@Test
public void testAddComment_NullUserName() throws Exception {
       int a=commentRepo.findAll().size();
       AddCommentRequest request = new AddCommentRequest();
       
       Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
       movieID.setAccessible(true);
       movieID.set(request,2L);
       
       Field commentField=AddCommentRequest.class.getDeclaredField("comment");
       commentField.setAccessible(true);
       commentField.set(request,"Great movie!");       
        
       Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
       ratingField.setAccessible(true);
       ratingField.setInt(request, 5);

       MyNotFoundException e=assertThrows(MyNotFoundException.class, ()->commentService.addComment(null, request));
       int b=commentRepo.findAll().size();
       assertEquals(a, b);
       assertEquals(e.getMessage(), "User not found");
    }
//test with user not in database
@Test
        public void testAddComment_UserNotInDatabase() throws Exception {
            int a=commentRepo.findAll().size();
            AddCommentRequest request = new AddCommentRequest();
               
            Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
            movieID.setAccessible(true);
            movieID.set(request,2L);
               
            Field commentField=AddCommentRequest.class.getDeclaredField("comment");
            commentField.setAccessible(true);
            commentField.set(request,"Great movie!");       
                
            Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
            ratingField.setAccessible(true);
            ratingField.setInt(request, 5);

            MyNotFoundException exception = assertThrows(
                MyNotFoundException.class, () -> {
                commentService.addComment("Hao", request);
            });
            int b=commentRepo.findAll().size();
            assertEquals(a,b);
            assertEquals(exception.getMessage(), "User not found");
}
@Test
        public void testAddComment_UserNotBooking() throws Exception {
            int a=commentRepo.findAll().size();
            AddCommentRequest request = new AddCommentRequest();
               
            Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
            movieID.setAccessible(true);
            movieID.set(request,3L);
               
            Field commentField=AddCommentRequest.class.getDeclaredField("comment");
            commentField.setAccessible(true);
            commentField.set(request,"Great movie!");       
                
            Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
            ratingField.setAccessible(true);
            ratingField.setInt(request, 5);

            assertThrows(
                MyBadRequestException.class, () -> {
                commentService.addComment("avart", request);
            });
            int b=commentRepo.findAll().size();
            assertEquals(a,b);
        }

//Test add like and dislike with valid data
@Test 
    public void testAddLike_Success() throws Exception {    
        commentService.addLike("admin.1234", 1L);
        List<Comment> comments = commentRepo.findAllByUsername("admin.1234");
        List<Comment> comments2 = comments.stream()
        .filter(comment -> 1L==(comment.getMovie().getId()))
        .toList();
        assertEquals(1,comments2.get(0).getLiked());
    }
}
