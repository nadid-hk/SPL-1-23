// Main.java
import java.util.*;

public class SchoolAuthority {

    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RESET = "\u001B[0m";

    private static void clearScreen() {
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

    private static void showHeader(String schoolName) {
        System.out.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "      SCHOOL AUTHORITY PORTAL" + ANSI_RESET);
        System.out.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
        if (schoolName != null && !schoolName.trim().isEmpty()) {
            System.out.println("School: " + schoolName);
        }
    }

    public static void main(String[] args) {

        // 1) Setup + seed DB files first
        DatabaseSetup.initAndSeed();

        // DB references created by DatabaseSetup
        FileDatabase schoolAreaDB = DatabaseSetup.schoolAreaDB;          // EIIN, Name, Postcode
        FileDatabase schoolInfoDB = DatabaseSetup.schoolInfoDB;          // SeatID, EIIN, Class, Shift, SeatGender, SeatAvailable
        FileDatabase authorityLoginDB = DatabaseSetup.authorityLoginDB;  // EIIN, Password

        Scanner input = new Scanner(System.in);
        Login loginSystem = new Login(authorityLoginDB);

        // ===== CONTINUOUS LOGIN UNTIL SUCCESS =====
        String loggedInEIIN;
        String loggedInSchoolName;

        while (true) {
            clearScreen();
            // showHeader(null);
            loggedInEIIN = loginSystem.login(input); // null if wrong
            if (loggedInEIIN == null) continue;

            // Fetch school name from schoolAreaDB (column "Name")
            Map<String, String> schoolArea = schoolAreaDB.find("EIIN", loggedInEIIN);
            if (schoolArea == null) {
                System.out.println("Login OK, but no school record found for EIIN: " + loggedInEIIN + "\n");
                continue;
            }

            loggedInSchoolName = schoolArea.get("Name");
            clearScreen();
            break;
        }

        // ===== MENU WORKFLOW =====
        boolean running = true;
        while (running) {
            showHeader(loggedInSchoolName);
            System.out.println("1. Add info");
            System.out.println("2. Show info");
            System.out.println("3. Update total seat number");
            System.out.println("4. Show result");
            System.out.println("0. Exit");
            System.out.print("Choose option: ");

            int option;
            try {
                option = Integer.parseInt(input.nextLine().trim());
                clearScreen();
            } catch (Exception e) {
                clearScreen();
                System.out.println("Invalid option. Try again.\n");
                continue;
            }

            if (option == 1) {
                showHeader(loggedInSchoolName);
                System.out.println("--- Add New Info ---");

                String eiin = loggedInEIIN;

                // User inputs ONLY these 4 fields
                System.out.print("Enter className: ");
                String className = input.nextLine().trim();
                clearScreen();
                showHeader(loggedInSchoolName);
                System.out.println("--- Add New Info ---");

                System.out.print("Enter shift: ");
                String shift = input.nextLine().trim().toUpperCase();
                clearScreen();
                showHeader(loggedInSchoolName);
                System.out.println("--- Add New Info ---");

                System.out.print("Enter seatGender: ");
                String seatGender = input.nextLine().trim().toUpperCase();
                clearScreen();
                showHeader(loggedInSchoolName);
                System.out.println("--- Add New Info ---");

                System.out.print("Enter totalSeatNumber: ");
                String totalSeatNumber = input.nextLine().trim();
                clearScreen();

                List<Map<String, String>> allRows = schoolInfoDB.readAll();

                // 1) If same 4-tuple exists (EIIN, Class, Shift, SeatGender), update instead
                Map<String, String> existing = null;
                for (Map<String, String> r : allRows) {
                    if (eiin.equals(r.get("EIIN")) &&
                            className.equals(r.get("Class")) &&
                            shift.equals(r.get("Shift")) &&
                            seatGender.equals(r.get("SeatGender"))) {
                        existing = r;
                        break;
                    }
                }

                showHeader(loggedInSchoolName);
                if (existing != null) {
                    String existingSeatID = existing.get("SeatID");
                    System.out.println("SeatID already exists for this (Class, Shift, SeatGender).");
                    System.out.println("Existing SeatID: " + existingSeatID);
                    System.out.println("Updating totalSeatNumber using update()...\n");

                    Map<String, String> updateData = new LinkedHashMap<>();
                    updateData.put("SeatAvailable", totalSeatNumber);
                    schoolInfoDB.update("SeatID", existingSeatID, updateData);

                    // Print final info
                    System.out.println("School Name: " + loggedInSchoolName);
                    System.out.println("EIIN: " + eiin);
                    System.out.println("SeatID: " + existingSeatID);
                    System.out.println("Class: " + className);
                    System.out.println("Shift: " + shift);
                    System.out.println("SeatGender: " + seatGender);
                    System.out.println("TotalSeatNumber: " + totalSeatNumber);
                    System.out.println();

                } else {
                    // 2) Generate UNIQUE SeatID: S-[8 digits]
                    Set<String> usedSeatIDs = new HashSet<>();
                    for (Map<String, String> r : allRows) {
                        String sid = r.get("SeatID");
                        if (sid != null && !sid.isEmpty()) usedSeatIDs.add(sid);
                    }

                    Random rand = new Random();
                    String seatID;
                    while (true) {
                        int num = rand.nextInt(100_000_000); // 0..99,999,999
                        seatID = "S-" + String.format("%08d", num);
                        if (!usedSeatIDs.contains(seatID)) break;
                    }

                    // Insert new row
                    Map<String, String> seatRow = new LinkedHashMap<>();
                    seatRow.put("SeatID", seatID);
                    seatRow.put("EIIN", eiin);
                    seatRow.put("Class", className);
                    seatRow.put("Shift", shift);
                    seatRow.put("SeatGender", seatGender);
                    seatRow.put("SeatAvailable", totalSeatNumber);

                    schoolInfoDB.insert(seatRow);

                    // Print final info + generated seatID
                    System.out.println("Successfully info inserted.");
                    System.out.println("School Name: " + loggedInSchoolName);
                    System.out.println("EIIN: " + eiin);
                    System.out.println("SeatID: " + seatID);
                    System.out.println("Class: " + className);
                    System.out.println("Shift: " + shift);
                    System.out.println("SeatGender: " + seatGender);
                    System.out.println("TotalSeatNumber: " + totalSeatNumber);
                    System.out.println();

                }
            }

            else if (option == 2) {
                showHeader(loggedInSchoolName);
                System.out.println("--- Show Info ---");

                List<Map<String, String>> allRows = schoolInfoDB.readAll();
                List<Map<String, String>> rows = new ArrayList<>();

                for (Map<String, String> r : allRows) {
                    if (loggedInEIIN.equals(r.get("EIIN"))) {
                        rows.add(r);
                    }
                }

                if (rows.isEmpty()) {
                    System.out.println("No record found for EIIN: " + loggedInEIIN + "\n");
                } else {
                    System.out.println("School Name: " + loggedInSchoolName);
                    System.out.println("EIIN: " + loggedInEIIN);
                    System.out.println("Classes & Shifts:");

                    for (Map<String, String> r : rows) {
                        System.out.println(
                                "  SeatID: " + r.get("SeatID") +
                                        " ,Class: " + r.get("Class") +
                                        " ,Shift: " + r.get("Shift") +
                                        " ,SeatGender: " + r.get("SeatGender") +
                                        " ,TotalSeat: " + r.get("SeatAvailable")
                        );
                    }
                    System.out.println();

                }
            }

            else if (option == 3) {
                showHeader(loggedInSchoolName);
                System.out.println("--- Update Info ---");
                System.out.print("Enter class Name: ");
                String className = input.nextLine().trim();
                clearScreen();
                showHeader(loggedInSchoolName);
                System.out.println("--- Update Info ---");

                System.out.print("Enter Shift: ");
                String shift = input.nextLine().trim().toUpperCase();
                clearScreen();
                showHeader(loggedInSchoolName);
                System.out.println("--- Update Info ---");

                System.out.print("Enter SeatGender: ");
                String seatGender = input.nextLine().trim().toUpperCase();
                clearScreen();

                Map<String, String> target = null;
                for (Map<String, String> r : schoolInfoDB.readAll()) {
                    if(loggedInEIIN.equals(r.get("EIIN")) &&
                            className.equals(r.get("Class"))  &&
                            shift.equals(r.get("shift"))  &&
                            seatGender.equals(r.get("seatGender"))){

                        target = r;
                        break;
                    }
                }

                if (target == null) {
                    showHeader(loggedInSchoolName);
                    System.out.println("No matching record found for your EIIN with given (Class, Shift, SeatGender). \n");
                    continue;
                }

                showHeader(loggedInSchoolName);
                System.out.print("Enter new totalSeatNumber: ");
                String newSeatCount = input.nextLine().trim();
                clearScreen();

                String seatID = target.get("SeatID");
                Map<String, String> updateData = new LinkedHashMap<>();
                updateData.put("SeatAvailable", newSeatCount);

                schoolInfoDB.update("SeatID", seatID, updateData);
                showHeader(loggedInSchoolName);
                System.out.println("Update completed.\n");

            }

            else if(option==4){
                clearScreen();
                SchoolResult obj = new SchoolResult(loggedInEIIN);
                obj.showSchoolWiseResult();

            }

            else if(option==0){
                System.out.println("Exiting...");
                running = false;
            }

            else {
                showHeader(loggedInSchoolName);
                System.out.println("Invalid option. Try again.\n");
            }
        }

        input.close();
    }
}
