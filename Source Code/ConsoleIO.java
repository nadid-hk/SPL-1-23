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

    public boolean isAutoClearAfterInput() {
        return autoClearAfterInput;
    }

    private void clearScreen() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\u001b[H\u001b[2J\u001b[3J");
                System.out.flush();
            }
            return;
        } catch (Exception ignored) {
            // Fall through to ANSI fallback.
        }

        System.out.print("\u001b[H\u001b[2J\u001b[3J");
        System.out.flush();
    }

    public void clearScreenNow() {
        clearScreen();
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
