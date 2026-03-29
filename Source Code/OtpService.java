import java.util.*;

public class OtpService {
    private final Random rnd = new Random();

    public String generate6Digit() {
        int code = 100000 + rnd.nextInt(900000);
        return String.valueOf(code);
    }
}
