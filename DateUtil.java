import java.time.*;
import java.time.format.*;

public class DateUtil {
    public static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd-MM-uuuu");
    public static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("hh:mm:ss a, dd MMMM, uuuu");

    public static String now() {
        return LocalDateTime.now().format(FULL);
    }

    public static LocalDate parseDMY(String s) throws ValidationException {
        try {
            return LocalDate.parse(s, DMY);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Use dd-MM-yyyy (e.g., 24-07-2015).");
        }
    }

    public static boolean isBetweenInclusive(LocalDate x, LocalDate min, LocalDate max) {
        return (x.isEqual(min) || x.isAfter(min)) && (x.isEqual(max) || x.isBefore(max));
    }
}
