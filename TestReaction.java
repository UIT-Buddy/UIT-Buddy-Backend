
// Test file to verify Reaction entity changes
import com.uit.buddy.entity.social.Reaction;
import com.uit.buddy.entity.social.Post;
import com.uit.buddy.entity.user.Student;

public class TestReaction {
    public static void main(String[] args) {
        Student student = new Student();
        student.setMssv("22100001");

        Post post = new Post();
        // Set post properties

        Reaction reaction = Reaction.builder()
                .student(student)
                .post(post)
                .build();

        // These should work now
        System.out.println("Student MSSV: " + reaction.getStudent().getMssv());
        System.out.println("Post ID: " + reaction.getPost().getId());

        // These methods should NOT exist anymore
        // reaction.getMssv() - REMOVED
        // reaction.getPostId() - REMOVED
        // reaction.setMssv() - REMOVED
        // reaction.setPostId() - REMOVED
    }
}