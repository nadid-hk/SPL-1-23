import java.util.*;

public class MasterAdmin {

    // Fixed for master admin login
    private static final String MASTER_ID = "MASTER";
    private static final String MASTER_PASS = "999999";

    public static FileDatabase authorityLoginDB;

    public static void main(String[] args) {

        authorityLoginDB = new FileDatabase(
                "authority_login.db",
                Arrays.asList("EIIN", "Password")
        );

        DatabaseSetup.initAndSeed();
        ensureMasterAccount();

        Scanner input = new Scanner(System.in);

        // Master Login
        while (true) {
            System.out.println("  MASTER ADMIN LOGIN  ");

            System.out.print("Enter MasterID: ");
            String masterId = input.nextLine().trim();

            System.out.print("Enter Password: ");
            String pass = input.nextLine().trim();

            Map<String, String> row = DatabaseSetup.authorityLoginDB.find("EIIN", masterId);

            if (row != null && MASTER_ID.equals(masterId) && MASTER_PASS.equals(pass)) {
                System.out.println("Login successful.\n");
                break;
            }
            System.out.println("Incorrect MasterID or password.\n");
        }

        // MASTER MENU
        while (true) {
            System.out.println("  MASTER ADMIN MENU  ");
            System.out.println("1. Create School");
            System.out.println("2. Show All Schools");
            System.out.println("3. Delete School");
            System.out.println("4. Run Lottery");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = input.nextLine().trim();

            if ("1".equals(choice)) {
                createSchool(input);
            } else if ("2".equals(choice)) {
                showAllSchools();
            } else if("3".equals(choice)){
                deleteSchool(input);
            }
            else if("4".equals(choice)){
                runLottery(input);
            }
            else if ("0".equals(choice)) {
                System.out.println("Exiting MASTER...");
                break;
            } else {
                System.out.println("Invalid option.\n");
            }
        }

        input.close();
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

    private static void createSchool(Scanner input) {

        System.out.println("\n--- CREATE SCHOOL ---");

        System.out.print("Enter School Name: ");
        String schoolName = input.nextLine().trim();
        if (schoolName.isEmpty()) {
            System.out.println("School name cannot be empty.");
            return;
        }

        System.out.print("Enter Postcode: ");
        String postcode = input.nextLine().trim();
        if (DatabaseSetup.postcodeDB.find("PostCode", postcode) == null) {
            System.out.println("Invalid postcode.");
            return;
        }

        // Generate EIIN
        String eiin = generateUniqueEIIN();

        // Insert into schoolarea.db
        Map<String, String> r = new LinkedHashMap<>();
        r.put("EIIN", eiin);
        r.put("Name", schoolName);
        r.put("Postcode", postcode);
        DatabaseSetup.schoolAreaDB.insert(r);

        // Generate password and write to authority_login.db
        String password = generateRandomPassword();
        insertAuthorityLogin(eiin, password);

        System.out.println("\nSchool created successfully!");
        System.out.println("EIIN: " + eiin);
        System.out.println("Password: " + password);
    }


    private static void  deleteSchool(Scanner input){
         System.out.println("\n--- DELETE SCHOOL ---");

        System.out.print("Enter School EIIN: ");
        String schoolEiin = input.nextLine().trim();
        if (schoolEiin.isEmpty()) {
            System.out.println("Deletion is not possible without eiin.");
            return;
        }
        String check=DatabaseSetup.schoolAreaDB.getValueByPrimaryKey("EIIN",schoolEiin,schoolEiin);
        if(schoolEiin==check){
        DatabaseSetup.schoolAreaDB.delete("EIIN",schoolEiin);
        DatabaseSetup.schoolInfoDB.delete("EIIN",schoolEiin);
        System.out.println("Deletion Successfully Everything");
    }
        else{
            System.out.println("EIIN is not found in the database.");
        }

    };






























    private static void runLottery(Scanner input){
        LotteryDatabase initializer = new LotteryDatabase();
        initializer.initializeDatabase();
        FileDatabase quotaChoiceDB =
                new FileDatabase("quota_choice_extended.db",
                        Arrays.asList("ChoiceID","StudentID","SeatID","Quota"));

        FileDatabase seatLotteryDB =
                new FileDatabase("seat_lottery.db",
                        Arrays.asList("SeatID","EIIN","Class","Shift",
                                "SeatGender","SeatAvailable","FFSeat","AreaSeat","GeneralSeat"));

        FileDatabase resultDB =
                new FileDatabase("result.db",
                        Arrays.asList("StudentID","AdmittedSeatID"));

        FileDatabase applicantDB =
                new FileDatabase("applicant.db", Arrays.asList());

        FileDatabase schoolAreaDB =
                new FileDatabase("school_area.db", Arrays.asList());

        System.out.println("Databases Loaded Successfully!");

        LotteryFunction engine =
                new LotteryFunction(quotaChoiceDB,
                        seatLotteryDB,
                        resultDB,
                        applicantDB,
                        schoolAreaDB);

        engine.runLottery();

        System.out.println("Lottery Completed Successfully!");

        // Optional: Print results for verification
        // System.out.println("----- Lottery Results -----");
        // for (Map<String,String> row : resultDB.readAll()) {
        //     System.out.println(row.get("StudentID") + " -> " + row.get("AdmittedSeatID"));
        // }

    }




























    private static void insertAuthorityLogin(String eiin, String password6Digit) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("EIIN", eiin);
        r.put("Password", password6Digit);
        authorityLoginDB.insert(r);
    }

    private static void showAllSchools() {
        System.out.println("\n--- ALL SCHOOLS ---");
        for (Map<String, String> r : DatabaseSetup.schoolAreaDB.readAll()) {
            System.out.println(
                    "EIIN: " + r.get("EIIN") +" | Name: " + r.get("Name") +" | Postcode: " + r.get("Postcode")
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
