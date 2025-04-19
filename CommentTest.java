package cinema.ticket.booking;
import cinema.ticket.booking.exception.MyBadRequestException;
import cinema.ticket.booking.exception.MyNotFoundException;
import cinema.ticket.booking.model.Comment;
import cinema.ticket.booking.repository.CommentRepository;
import cinema.ticket.booking.request.AddCommentRequest;
import cinema.ticket.booking.request.EditCommentRequest;
import cinema.ticket.booking.response.CommentResponse;
import cinema.ticket.booking.response.MyApiResponse;
import cinema.ticket.booking.service.impl.CommentServiceImpl;
import jakarta.transaction.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.Arrays;
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
    public void COM_001_testAddComment_Success() throws Exception {
        int a=commentRepo.findAll().size();
        AddCommentRequest request = new AddCommentRequest();
           
        Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
        movieID.setAccessible(true);
        movieID.set(request,4L);
        
        Field commentField=AddCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"Great movie!");       
         
        Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
        ratingField.setAccessible(true);
        ratingField.setInt(request, 5);

        commentService.addComment("avart", request);
        int b=commentRepo.findAll().size();
        assertEquals(a, b);        
    }
    
    // test add comment with invalid user
    @Test
    public void COM_002_testAddComment_UserNotInDatabase() throws Exception {
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
            commentService.addComment("Hao", request);// user not in database
        });
        int b=commentRepo.findAll().size();
        assertEquals(a,b);
        assertEquals(exception.getMessage(), "User not found");
    }

    // test add comment with invalid movie
    @Test
    public void COM_003_testAddComment_MovieNotInDatabase() throws Exception {
        int a=commentRepo.findAll().size();
        AddCommentRequest request = new AddCommentRequest();
        
        Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
        movieID.setAccessible(true);
        movieID.set(request,100L);// movie id not in database
        
        Field commentField=AddCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"Great movie!");       
            
        Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
        ratingField.setAccessible(true);
        ratingField.setInt(request, 5);

        MyNotFoundException exception = assertThrows(
            MyNotFoundException.class, () -> {
            commentService.addComment("avart", request);
        });
        int b=commentRepo.findAll().size();
        assertEquals(a,b);
        assertEquals(exception.getMessage(), "Movie not found");
    }

    // tesst add comment with user not booking this movie
    @Test
    public void COM_004_testAddComment_UserNotBooking() throws Exception {
        int a=commentRepo.findAll().size();
        AddCommentRequest request = new AddCommentRequest();
        
        Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
        movieID.setAccessible(true);
        movieID.set(request,6L);
        
        Field commentField=AddCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"Great movie!");       
            
        Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
        ratingField.setAccessible(true);
        ratingField.setInt(request, 5);
        
        MyBadRequestException exception =
         assertThrows(
            MyBadRequestException.class, () -> {
            commentService.addComment("avart", request); // user not booking this movie
        });
        int b=commentRepo.findAll().size();
        assertEquals(a,b);
        assertEquals(exception.getMessage(), "You must buy ticket for this movie before reviewing.");
    }

    // test add comment with user has reviewed this movie
    @Test
    public void COM_005_testAddComment_UserReviewed() throws Exception {
        int a=commentRepo.findAll().size();
        AddCommentRequest request = new AddCommentRequest();

        Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
        movieID.setAccessible(true);
        movieID.set(request,2L);
        
        Field commentField=AddCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"Phim này không đáng xem. Cốt truyện nhạt nhẽo và diễn xuất kém. Mình không thấy hứng thú khi xem.");       
            
        Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
        ratingField.setAccessible(true);
        ratingField.setInt(request, 1);

        MyBadRequestException exception = assertThrows(
            MyBadRequestException.class, () -> {
            commentService.addComment("avart", request); // user has reviewed this movie before
        });

        
        int b=commentRepo.findAll().size();
        assertEquals(a,b);
        assertEquals(exception.getMessage(), "You already have reviewed this movie");
    }

    // test add comment with invalid rating
    @Test
    public void COM_006_testAddComment_InvalidRating() throws Exception {
        int a=commentRepo.findAll().size();
        AddCommentRequest request = new AddCommentRequest();

        Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
        movieID.setAccessible(true);
        movieID.set(request,4L);
        
        Field commentField=AddCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"Great movie!");       
            
        Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
        ratingField.setAccessible(true);
        ratingField.setInt(request, 6);

        MyBadRequestException exception = assertThrows(
            MyBadRequestException.class, () -> {
            commentService.addComment("avart", request); // rating > 5
        });
        int b=commentRepo.findAll().size();
        assertEquals(a,b);
        assertEquals(exception.getMessage(), "Rating number must be in range 0 and 5");
    }

    // test with valid data
    @Test
    public void COM_007_testaddListComment_Success() throws Exception {
        int a=commentRepo.findAllByUsername("avart").size();

        AddCommentRequest request = new AddCommentRequest();
        Field movieID=AddCommentRequest.class.getDeclaredField("movie_id");
        movieID.setAccessible(true);
        movieID.set(request,1L);
        
        Field commentField=AddCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"Great movie!");       
            
        Field ratingField=AddCommentRequest.class.getDeclaredField("rated_stars");
        ratingField.setAccessible(true);
        ratingField.setInt(request, 5);

        AddCommentRequest request1 = new AddCommentRequest();
        Field movieID1=AddCommentRequest.class.getDeclaredField("movie_id");
        movieID1.setAccessible(true);
        movieID1.set(request1,3L);
        
        Field commentField1=AddCommentRequest.class.getDeclaredField("comment");
        commentField1.setAccessible(true);
        commentField1.set(request1,"Great movie!");       
            
        Field ratingField1=AddCommentRequest.class.getDeclaredField("rated_stars");
        ratingField1.setAccessible(true);
        ratingField1.setInt(request1, 5);

        List<AddCommentRequest> requests = Arrays.asList(request, request1);
        commentService.addListComments("avart", requests);
        int b=commentRepo.findAllByUsername("avart").size();
        assertEquals(a+2,b);
    }

    // test edit comment with valid data
    @Test
    public void COM_008_testEditComment_Success() throws Exception {
        EditCommentRequest request = new EditCommentRequest();
        Field commentField=EditCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"test comment");
        
        Field ratingField=EditCommentRequest.class.getDeclaredField("rating_stars");
        ratingField.setAccessible(true);   
        ratingField.setInt(request, 5);

        commentService.editComment("avart", "fd564807-b6262c59-92d7e4c2", request);
        String comment = commentRepo.findById("fd564807-b6262c59-92d7e4c2").get().getComment();
        assertEquals(comment, "test comment");
    }

    // test edit comment with invalid comment_id
    @Test
    public void COM_009_testEditComment_invalidcommentid() throws Exception {
        EditCommentRequest request = new EditCommentRequest();
        Field commentField=EditCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"test comment");
        
        Field ratingField=EditCommentRequest.class.getDeclaredField("rating_stars");
        ratingField.setAccessible(true);   
        ratingField.setInt(request, 5);

        MyNotFoundException exception = assertThrows(
            MyNotFoundException.class, () -> {
            commentService.editComment("avart", "fd564807-b6262c59-92d7e4c3", request); // comment_id not in database
        });
        String comment = commentRepo.findById("fd564807-b6262c59-92d7e4c2").get().getComment();
        assertNotEquals(comment, "test comment");
        assertEquals(exception.getMessage(), "Comment not found");
    }

    // test edit comment with invalid user
    @Test
    public void COM_010_testEditComment_invalidusername() throws Exception {
        EditCommentRequest request = new EditCommentRequest();
        Field commentField=EditCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"test comment");
        
        Field ratingField=EditCommentRequest.class.getDeclaredField("rating_stars");
        ratingField.setAccessible(true);   
        ratingField.setInt(request, 5);

        MyBadRequestException exception = assertThrows(
            MyBadRequestException.class, () -> {
            commentService.editComment("user_1", "fd564807-b6262c59-92d7e4c2", request); // user not in database
        });
        String comment = commentRepo.findById("fd564807-b6262c59-92d7e4c2").get().getComment();
        assertNotEquals(comment, "test comment");
        assertEquals(exception.getMessage(), "This comment is not belonged to you");
    }

    // test edit comment with invalid rating
    @Test
    public void COM_011_testEditComment_invalidrating() throws Exception {
        EditCommentRequest request = new EditCommentRequest();
        Field commentField=EditCommentRequest.class.getDeclaredField("comment");
        commentField.setAccessible(true);
        commentField.set(request,"test comment");
        
        Field ratingField=EditCommentRequest.class.getDeclaredField("rating_stars");
        ratingField.setAccessible(true);   
        ratingField.setInt(request, 9); // rating > 5

        MyBadRequestException exception = assertThrows(
            MyBadRequestException.class, () -> {
            commentService.editComment("avart", "fd564807-b6262c59-92d7e4c2", request);
        });
        String comment = commentRepo.findById("fd564807-b6262c59-92d7e4c2").get().getComment();
        assertNotEquals(comment, "test comment");
        assertEquals(exception.getMessage(), "Rating number must be in range 0 and 5");
        
    }

    // test delete comment with valid data
    @Test   
    public void COM_012_testDeleteComment_Success() throws Exception {
        int a=commentRepo.findAll().size();
        commentService.deleteCommentById("fd564807-b6262c59-92d7e4c2");
        int b=commentRepo.findAll().size();
        assertEquals(a-1,b);
    }
    
    // test delete comment with invalid comment_id
    @Test
    public void COM_013_testDeleteComment_invalidcommentid() throws Exception {
        int a=commentRepo.findAll().size();
        MyNotFoundException exception = assertThrows(
            MyNotFoundException.class, () -> {
            commentService.deleteCommentById("fd564807-b6262c59-92d7e4c3"); // comment_id not in database
        });
        int b=commentRepo.findAll().size();
        assertEquals(a,b);
        assertEquals(exception.getMessage(), "Comment not found");
    }

    // test delete comment with valid data
    @Test   
    public void COM_014_testDeleteCommentByUsername_Success() throws Exception {
        int a=commentRepo.findAll().size();
        commentService.deleteCommentByUsername("avart", "fd564807-b6262c59-92d7e4c2");
        int b=commentRepo.findAll().size();
        assertEquals(a-1,b);
    }

    // test delete comment with invalid username
    @Test
    public void COM_015_testDeleteCommentByUsername_invalidusername() throws Exception {
        int a=commentRepo.findAll().size();
        MyBadRequestException exception = assertThrows(
            MyBadRequestException.class, () -> {
            commentService.deleteCommentByUsername("user_1", "fd564807-b6262c59-92d7e4c2"); // user not in database
        });
        int b=commentRepo.findAll().size();
        assertEquals(a,b);
        assertEquals(exception.getMessage(), "This comment is not belonged to you");
    }

    // test delete comment with invalid comment_id
    @Test   
    public void COM_016_testDeleteCommentByUsername_invalidcommentid() throws Exception {
        int a=commentRepo.findAll().size();
        MyNotFoundException exception = assertThrows(
            MyNotFoundException.class, () -> {
            commentService.deleteCommentByUsername("avart", "fd564807-b6262c59-92d7e4c3"); // comment_id not in database
        });
        int b=commentRepo.findAll().size();
        assertEquals(a,b);
        assertEquals(exception.getMessage(), "Comment not found");
    }

    // test get one comment with valid data
    @Test
    public void COM_017_testGetOne_Success() throws Exception {
        CommentResponse c=commentService.getOne("avart", "fd564807-b6262c59-92d7e4c2");
        assertEquals(c.getComment(), "good");
    }

    // test get one comment with invalid username
    @Test
    public void COM_018_testGetOne_invalidusername() throws Exception {
        MyBadRequestException exception = assertThrows(
            MyBadRequestException.class, () -> {
            commentService.getOne("user_1", "fd564807-b6262c59-92d7e4c2"); // user not in database
        });
        assertEquals(exception.getMessage(), "This comment is not belonged to you");
    }

    // test get one comment with invalid comment_id
    @Test
    public void COM_019_testGetOne_invalidcommentid() throws Exception {
        MyNotFoundException exception = assertThrows(
            MyNotFoundException.class, () -> {
            commentService.getOne("avart", "fd564807-b6262c59-92d7e4c3"); // comment_id not in database
        });
        assertEquals(exception.getMessage(), "Comment not found");
    }

    // test get all comments success
    @Test
    public void COM_020_testGetAll_Success() throws Exception {
        List<CommentResponse> c=commentService.getAllComments();
        assertEquals(c.size(),201);
    }

    //test get all comment from movie id with valid data
    @Test
    public void COM_021_testGetAllFromMovieId_Success() throws Exception {
        List<CommentResponse> c=commentService.getAllCommentsFromMovieId(1L);
        assertEquals(c.size(), 8);
    }

    //test get all comment from user id with valid data
    @Test
    public void COM_022_testGetAllFromUserId_Success() throws Exception {
        List<CommentResponse> c=commentService.getAllCommentsFromUserId("7540f518-84b36801-ec034014");
        assertEquals(c.size(), 1);
    }

    //test get all comment from username with valid data
    @Test
    public void COM_023_testGetAllFromUsername_Success() throws Exception {
        List<CommentResponse> c=commentService.getAllCommentsFromusername("avart");
        assertEquals(c.size(), 1);
    }
  
    // test get all comment from username with invalid username
    @Test
    public void COM_024_testGetAllFromUsername_invalidusername() throws Exception {
        MyNotFoundException exception = assertThrows(
            MyNotFoundException.class, () -> {
            commentService.getAllCommentsFromusername("haha"); // user not in database
        });
        assertEquals(exception.getMessage(), "User not found");
    }

    //Test add like with valid data
    @Test 
        public void COM_025_testAddLike_Success() throws Exception {    
            commentService.addLike("admin.1234", 1L);
            List<Comment> comments = commentRepo.findAllByUsername("admin.1234");
            List<Comment> comments2 = comments.stream()
            .filter(comment -> 1L==(comment.getMovie().getId()))
            .toList();
            assertEquals(1,comments2.get(0).getLiked());
        }

        //Test add dislike with valid data
    @Test 
    public void COM_026_testAddDislike_Success() throws Exception {    
        commentService.addDisLike("admin.1234", 1L);
        List<Comment> comments = commentRepo.findAllByUsername("admin.1234");
        List<Comment> comments2 = comments.stream()
        .filter(comment -> 1L==(comment.getMovie().getId()))
        .toList();
        assertEquals(1,comments2.get(0).getLiked());
    }
}
