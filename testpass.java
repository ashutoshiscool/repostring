import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
public class testpass {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.matches("doctor123", "$2a$10$abcdef...")); 
    }
}
