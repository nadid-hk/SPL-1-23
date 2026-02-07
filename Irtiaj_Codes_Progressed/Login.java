// Login.java
import java.util.*;

public class Login {
    private final FileDatabase userDB;

    public Login(FileDatabase userDB) {
        this.userDB = userDB;
    }

    // Returns EIIN if login success, otherwise null
    public String login(Scanner input) {
        System.out.println("==== LOGIN ====");

        System.out.print("Enter EIIN: ");
        String eiin = input.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = input.nextLine().trim();

        // authorityLoginDB schema: EIIN, Password
        Map<String, String> user = userDB.find("EIIN", eiin);

        if (user == null) {
            System.out.println("Incorrect EIIN or password.\n");
            return null;
        }

        String correctPassword = user.get("Password");
        if (correctPassword != null && correctPassword.equals(password)) {
            System.out.println("Login successful.\n");
            return eiin;
        }

        System.out.println("Incorrect EIIN or password.\n");
        return null;
    }
}
