import java.util.*;

public class ConsoleIO {
    private final Scanner sc;
    private boolean autoClearAfterInput;

    public ConsoleIO(Scanner sc) {
        this.sc = sc;
        this.autoClearAfterInput = false;
    }

    public void setAutoClearAfterInput(boolean enabled) {
        this.autoClearAfterInput = enabled;
    }

    private void clearScreen() {
        // ANSI clear for most terminals; newline fallback keeps behavior usable elsewhere.
        System.out.print("\u001b[H\u001b[2J");
        System.out.flush();
        for (int i = 0; i < 6; i++) {
            System.out.println();
        }
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
        if (autoClearAfterInput) clearScreen();
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
