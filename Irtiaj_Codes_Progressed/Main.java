// Main.java
import java.util.*;

public class Main {

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
            loggedInEIIN = loginSystem.login(input); // null if wrong
            if (loggedInEIIN == null) continue;

            // Fetch school name from schoolAreaDB (column "Name")
            Map<String, String> schoolArea = schoolAreaDB.find("EIIN", loggedInEIIN);
            if (schoolArea == null) {
                System.out.println("Login OK, but no school record found for EIIN: " + loggedInEIIN + "\n");
                continue;
            }

            loggedInSchoolName = schoolArea.get("Name");
            break;
        }

        // ===== MENU WORKFLOW =====
        boolean running = true;
        while (running) {
            System.out.println("Press 1 to add info ,Press 2 to show info ,Press 3 to Update totalSeatNumber,Press 4 to show Result");
            System.out.print("Choose option: ");

            int option;
            try {
                option = Integer.parseInt(input.nextLine().trim());
            } catch (Exception e) {
                System.out.println("Invalid option. Try again.\n");
                continue;
            }

            if (option == 1) {
                System.out.println("--- Add New Info ---");

                String eiin = loggedInEIIN;

                // User inputs ONLY these 4 fields
                System.out.print("Enter className: ");
                String className = input.nextLine().trim();

                System.out.print("Enter shift: ");
                String shift = input.nextLine().trim().toUpperCase();

                System.out.print("Enter seatGender: ");
                String seatGender = input.nextLine().trim().toUpperCase();

                System.out.print("Enter totalSeatNumber: ");
                String totalSeatNumber = input.nextLine().trim();

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

                if (existing != null) {
                    String existingSeatID = existing.get("SeatID");
                    System.out.println("\nSeatID already exists for this (Class, Shift, SeatGender).");
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
                    System.out.println("\nSuccessfully info inserted.");
                    System.out.println("School Name: " + loggedInSchoolName);
                    System.out.println("EIIN: " + eiin);
                    System.out.println("SeatID: " + seatID);
                    System.out.println("Class: " + className);
                    System.out.println("Shift: " + shift);
                    System.out.println("SeatGender: " + seatGender);
                    System.out.println("TotalSeatNumber: " + totalSeatNumber);
                    System.out.println();

                    System.out.println("Exiting...");
                    running = false;
                }
            }

            else if (option == 2) {
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
                    System.out.println("\nSchool Name: " + loggedInSchoolName);
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

                    System.out.println("Exiting...");
                    running = false;
                }
            }

            else if (option == 3) {
                System.out.println("--- Update Info ---");
                System.out.print("Enter class Name: ");
                String className = input.nextLine().trim();

                System.out.print("Enter Shift: ");
                String shift = input.nextLine().trim().toUpperCase();

                System.out.print("Enter SeatGender: ");
                String seatGender = input.nextLine().trim().toUpperCase();

                Map<String, String> target = null;
                for (Map<String, String> r : schoolInfoDB.readAll()) {
                    if(loggedInEIIN.equals(r.get("EIIN")) &&
                      className.equals(r.get("Class"))  &&
                      shift.equals(r.get("shift"))  &&
                       seatGender.equals(r.get("SeatGender"))){

                        target = r;
                        break;
                    }
                }

                if (target == null) {
                    System.out.println("No matching record found for your EIIN with given (Class, Shift, SeatGender). \n");
                    continue;
                }

                System.out.print("Enter new totalSeatNumber: ");
                String newSeatCount = input.nextLine().trim();

                String seatID = target.get("SeatID");
                Map<String, String> updateData = new LinkedHashMap<>();
                updateData.put("SeatAvailable", newSeatCount);

                schoolInfoDB.update("SeatID", seatID, updateData);
                System.out.println("Update completed.\n");

                System.out.println("Exiting...");
                running = false;
            }

            else if(option==4){
                SchoolResult obj = new SchoolResult(loggedInEIIN);
                obj.showSchoolWiseResult();

                System.out.println("Exiting...");
                running = false;
            }
                
            else {
                System.out.println("Invalid option. Try again.\n");
            }
        }

        input.close();
    }
}
