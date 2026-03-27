import java.io.Console;
import java.util.*;

public class Login {
    private final FileDatabase userDB;

    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RESET = "\u001B[0m";

    public Login(FileDatabase userDB) {
        this.userDB = userDB;
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
        }

        System.out.print("\u001b[H\u001b[2J\u001b[3J");
        System.out.flush();
    }

    private void showHeader() {
        System.out.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "      SCHOOL AUTHORITY PORTAL" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
    }

    // Returns EIIN if login success, otherwise null
    public String login(Scanner input) {
        showHeader();
        System.out.println("==== LOGIN ====");

        System.out.print("Enter EIIN: ");
        String eiin = input.nextLine().trim();
        clearScreen();
        showHeader();
        System.out.println("==== LOGIN ====");

        String password = "";
        Console console = System.console();

        if (console != null) {
            char[] array = console.readPassword("Enter Password: ");
            password = new String(array).trim();
        } else {
            System.out.print("Enter Password: ");
            password = input.nextLine().trim();
        }
        clearScreen();
        showHeader();

        // authorityLoginDB schema: EIIN, Password
        Map<String, String> user = userDB.find("EIIN", eiin);

        if (user == null) {
            System.out.println("Incorrect EIIN or password.\n");
            return null;
        }

        String correctPassword = user.get("Password");
        String enteredhashed = PasswordUtil.hashPassword(password);
        if (correctPassword != null && correctPassword.equals(enteredhashed)) {
            System.out.println("Login successful.\n");
            return eiin;
        }

        System.out.println("Incorrect EIIN or password.\n");
        return null;
    }
}
