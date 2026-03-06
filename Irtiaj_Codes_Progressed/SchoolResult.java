import java.util.*;

public class SchoolResult {

    private final FileDatabase resultDB;
    private final PostcodeRepository pcRepo;

    public SchoolResult() {
        DatabaseSetup.initAndSeed();

        this.resultDB = new FileDatabase(
                "result.db",
                Arrays.asList("ApplicantID", "StudentID", "AdmittedSeatID")
        );

        this.pcRepo = new PostcodeRepository(DatabaseSetup.postcodeDB);

        showSchoolWiseResult();
    }

    private void showSchoolWiseResult() {
        Scanner sc = new Scanner(System.in);

        List<Map<String, String>> results = resultDB.readAll();
        if (results.isEmpty()) {
            System.out.println("result.db is empty. Run lottery first.");
            return;
        }

        System.out.println("Step A: Choose (Division -> District -> Thana) to fetch Postcode.");

        String division = chooseFromList(sc, "Division", pcRepo.uniqueDivisions());
        if (division == null) return;

        String district = chooseFromList(sc, "District", pcRepo.districtsIn(division));
        if (district == null) return;

        String thana = chooseFromList(sc, "Thana", pcRepo.thanasIn(division, district));
        if (thana == null) return;

        String fetchedPc = pcRepo.postcodeFor(division, district, thana);
        if (fetchedPc == null) {
            System.out.println("No postcode found for this (Division, District, Thana).");
            return;
        }

        Map<String, String> pcRec = pcRepo.findByPostcode(fetchedPc);
        if (pcRec == null) {
            System.out.println("Invalid Postcode. Not found in postcode.db.");
            return;
        }

        System.out.println("Selected Address Admin Area:");
        System.out.println("  Postcode  : " + fetchedPc);
        System.out.println("  Division  : " + pcRec.get("Division"));
        System.out.println("  District  : " + pcRec.get("District"));
        System.out.println("  Thana     : " + pcRec.get("Thana"));

        List<Map<String, String>> schools = new ArrayList<>();
        for (Map<String, String> row : DatabaseSetup.schoolAreaDB.readAll()) {
            if (fetchedPc.equals(row.get("Postcode"))) {
                schools.add(row);
            }
        }

        if (schools.isEmpty()) {
            System.out.println("No school found under this postcode.");
            return;
        }

        System.out.println("\nSchools under this Postcode:");
        for (int i = 0; i < schools.size(); i++) {
            Map<String, String> school = schools.get(i);
            System.out.println((i + 1) + ". " + school.get("Name") + " (EIIN: " + school.get("EIIN") + ")");
        }

        System.out.print("Choose school: ");
        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid choice.");
            return;
        }

        if (choice < 1 || choice > schools.size()) {
            System.out.println("Invalid school choice.");
            return;
        }

        Map<String, String> selectedSchool = schools.get(choice - 1);
        String selectedEIIN = selectedSchool.get("EIIN");
        String selectedSchoolName = selectedSchool.get("Name");

        System.out.println("\n######## SCHOOL RESULT ########");
        System.out.println("School Name : " + selectedSchoolName);
        System.out.println("EIIN        : " + selectedEIIN);
        System.out.println("Postcode    : " + fetchedPc);

        int totalAdmitted = 0;

        for (Map<String, String> row : results) {
            String seatId = row.get("AdmittedSeatID");
            if (seatId == null || seatId.isEmpty()) continue;

            Map<String, String> seatRow = DatabaseSetup.schoolInfoDB.find("SeatID", seatId);
            if (seatRow == null) continue;

            String eiin = seatRow.get("EIIN");
            if (selectedEIIN.equals(eiin)) {
                totalAdmitted++;
            }
        }

        System.out.println("Total Admitted Students : " + totalAdmitted);

        if (totalAdmitted == 0) {
            System.out.println("No admitted students found.");
            System.out.println("###############################");
            return;
        }

        System.out.println("\nAdmitted Students:");
        System.out.printf("%-15s %-20s %-30s%n", "Applicant ID", "Student ID", "Student Name");
        System.out.println("---------------------------------------------------------------------");

        for (Map<String, String> row : results) {
            String seatId = row.get("AdmittedSeatID");
            if (seatId == null || seatId.isEmpty()) continue;

            Map<String, String> seatRow = DatabaseSetup.schoolInfoDB.find("SeatID", seatId);
            if (seatRow == null) continue;

            String eiin = seatRow.get("EIIN");
            if (!selectedEIIN.equals(eiin)) continue;

            String applicantId = row.get("ApplicantID");
            String studentId = row.get("StudentID");

            String studentName = DatabaseSetup.studentInfoDB.getValueByPrimaryKey(
                    "BirthCertificateNo",
                    studentId,
                    "Name"
            );

            if (studentName == null || studentName.isEmpty()) {
                studentName = "(Name not found)";
            }

            System.out.printf("%-15s %-20s %-30s%n", applicantId, studentId, studentName);
        }

        System.out.println("###############################");
    }

    private String chooseFromList(Scanner sc, String title, List<String> options) {
        if (options == null || options.isEmpty()) {
            System.out.println("No options found for " + title);
            return null;
        }

        System.out.println("\n" + title + ":");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }

        System.out.print("Enter choice: ");
        int choice;
        try {
            choice = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid input.");
            return null;
        }

        if (choice < 1 || choice > options.size()) {
            System.out.println("Invalid choice.");
            return null;
        }

        return options.get(choice - 1);
    }
}
