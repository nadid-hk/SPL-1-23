import java.util.*;

public class MasterAdmin {

    private static final String MASTER_ID = "MASTER";
    private static final String MASTER_PASS = "999999";

    public static FileDatabase authorityLoginDB;

    private static ConsoleIO io;

    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RESET = "\u001B[0m";

    public static void main(String[] args) {

        authorityLoginDB = new FileDatabase(
                "authority_login.db",
                Arrays.asList("EIIN", "Password")
        );

        DatabaseSetup.initAndSeed();
        ensureMasterAccount();

        io = new ConsoleIO(new Scanner(System.in));

        // Enable auto clear
        io.setAutoClearAfterInput(true);

        // MASTER LOGIN
        while (true) {
            showHeader("MASTER ADMIN LOGIN");

            String masterId = io.promptLine("Enter MasterID: ");
            String pass = io.promptLine("Enter Password: ");

            if (MASTER_ID.equals(masterId) && MASTER_PASS.equals(pass)) {
                io.println("Login successful.\n");
                break;
            }
            io.println("Incorrect MasterID or password.\n");
        }

        //  Show header ONLY ONCE
        io.clearScreenNow();
        showHeader("MASTER ADMIN PORTAL");

        // MASTER MENU
        while (true) {

            io.println("1. Create School");
            io.println("2. Show All Schools");
            io.println("3. Delete School");
            io.println("4. Run Lottery");
            io.println("0. Exit");

            String choice = io.promptLine("Choose: ");

            if ("1".equals(choice)) {
                createSchool();
            } 
            else if ("2".equals(choice)) {
                showAllSchools();
            } 
            else if ("3".equals(choice)) {
                deleteSchool();
            } 
            else if ("4".equals(choice)) {
                runLottery();
            } 
            else if ("0".equals(choice)) {
                if (io.confirm("Are you sure you want to exit")) {
                    io.println("Exiting MASTER...");
                    break;
                }
            } 
            else {
                io.println("Invalid option.\n");
            }
        }
    }

    private static void showHeader(String title) {
        io.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
        io.println(ANSI_CYAN + "      " + title + ANSI_RESET);
        io.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
    }

    private static void ensureMasterAccount() {
        Map<String, String> existing = DatabaseSetup.authorityLoginDB.find("EIIN", MASTER_ID);
        if (existing == null) {
            Map<String, String> r = new LinkedHashMap<>();
            r.put("EIIN", MASTER_ID);
            r.put("Password", MASTER_PASS);
            DatabaseSetup.authorityLoginDB.insert(r);
        }
    }

    private static void createSchool() {

        io.println("\n--- CREATE SCHOOL ---");

        try {
            String schoolName = io.promptNonEmpty("Enter School Name: ");
            String postcode = io.promptNonEmpty("Enter Postcode: ");

            if (DatabaseSetup.postcodeDB.find("PostCode", postcode) == null) {
                io.println("Invalid postcode.");
                return;
            }

            String eiin = generateUniqueEIIN();

            Map<String, String> r = new LinkedHashMap<>();
            r.put("EIIN", eiin);
            r.put("Name", schoolName);
            r.put("Postcode", postcode);
            DatabaseSetup.schoolAreaDB.insert(r);

            String password = generateRandomPassword();
            insertAuthorityLogin(eiin, password);

            io.println("\nSchool created successfully!");
            io.println("EIIN: " + eiin);
            io.println("Password: " + password);

        } catch (ValidationException e) {
            io.println("ERROR: " + e.getMessage());
        }
    }

    private static void deleteSchool() {

        io.println("\n--- DELETE SCHOOL ---");

        try {
            String schoolEiin = io.promptNonEmpty("Enter School EIIN: ");

            String check = DatabaseSetup.schoolAreaDB
                    .getValueByPrimaryKey("EIIN", schoolEiin, schoolEiin);

            if (schoolEiin.equals(check)) {
                DatabaseSetup.schoolAreaDB.delete("EIIN", schoolEiin);
                DatabaseSetup.schoolInfoDB.delete("EIIN", schoolEiin);
                io.println("Deletion Successfully Everything");
            } else {
                io.println("EIIN is not found in the database.");
            }

        } catch (ValidationException e) {
            io.println("ERROR: " + e.getMessage());
        }
    }

    private static void runLottery() {

        LotteryDatabase initializer = new LotteryDatabase();
        initializer.initializeDatabase();

        FileDatabase quotaChoiceDB =
                new FileDatabase("quota_choice_extended.db",
                        Arrays.asList("ChoiceID","ApplicantID","StudentID","SeatID","Quota"));

        FileDatabase seatLotteryDB =
                new FileDatabase("seat_lottery.db",
                        Arrays.asList("SeatID","EIIN","Class","Shift",
                                "SeatGender","SeatAvailable","FFSeat","AreaSeat","GeneralSeat"));

        FileDatabase resultDB =
                new FileDatabase("result.db",
                        Arrays.asList("ApplicantID","StudentID","AdmittedSeatID"));

        FileDatabase applicantDB =
                new FileDatabase("applicant.db", Arrays.asList());

        FileDatabase schoolAreaDB =
                new FileDatabase("school_area.db", Arrays.asList());

        io.println("Databases Loaded Successfully!");

        LotteryFunction engine =
                new LotteryFunction(quotaChoiceDB,
                        seatLotteryDB,
                        resultDB,
                        applicantDB,
                        schoolAreaDB);

        engine.runLottery();

        io.println("Lottery Completed Successfully!");
    }

    private static void insertAuthorityLogin(String eiin, String password6Digit) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("EIIN", eiin);
        r.put("Password", password6Digit);
        authorityLoginDB.insert(r);
    }

    private static void showAllSchools() {
        io.println("\n--- ALL SCHOOLS ---");
        for (Map<String, String> r : DatabaseSetup.schoolAreaDB.readAll()) {
            io.println(
                    "EIIN: " + r.get("EIIN") +
                    " | Name: " + r.get("Name") +
                    " | Postcode: " + r.get("Postcode")
            );
        }
    }

    private static String generateUniqueEIIN() {
        Set<String> used = new HashSet<>();
        for (Map<String, String> row : DatabaseSetup.schoolAreaDB.readAll()) {
            used.add(row.get("EIIN"));
        }

        Random rand = new Random();
        while (true) {
            String eiin = String.valueOf(rand.nextInt(900000) + 100000);
            if (!used.contains(eiin)) return eiin;
        }
    }

    private static String generateRandomPassword() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }
}
