import java.util.*;

public class ConsoleIO {
    private final Scanner sc;

    public ConsoleIO(Scanner sc) {
        this.sc = sc;
    }

    public void println(String s) {
        System.out.println(s);
    }

    public void print(String s) {
        System.out.print(s);
    }

    public String promptLine(String prompt) {
        print(prompt);
        String raw = sc.nextLine().trim();
        if (raw.equalsIgnoreCase("\\b")) throw new BackToMainMenuSignal();
        return raw.toUpperCase(Locale.ROOT);
    }

    public int promptInt(String prompt) throws ValidationException {
        String s = promptLine(prompt);
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new ValidationException("Please enter a valid number.");
        }
    }

    public String promptNonEmpty(String prompt) throws ValidationException {
        String s = promptLine(prompt);
        if (s.isEmpty()) throw new ValidationException("This field cannot be empty.");
        return s;
    }

    public String promptOptional(String prompt) {
        return promptLine(prompt);
    }

    public boolean confirm(String prompt) {
        while (true) {
            String s = promptLine(prompt + " (Y/N): ");
            if ("Y".equals(s)) return true;
            if ("N".equals(s)) return false;
            println("Please enter Y or N.");
        }
    }

    public void hr() {
        println("------------------------------------------");
    }
}
