import java.util.*;

public class ApplicantResult {

    private final FileDatabase resultDB;

    public ApplicantResult(String studentId) {
        DatabaseSetup.initAndSeed();

        this.resultDB = new FileDatabase(
                "result.db",
                Arrays.asList("ApplicantID", "StudentID", "AdmittedSeatID")
        );

        showApplicantResult(studentId);
    }

    public ApplicantResult() {
        DatabaseSetup.initAndSeed();

        this.resultDB = new FileDatabase(
                "result.db",
                Arrays.asList("ApplicantID", "StudentID", "AdmittedSeatID")
        );

        Scanner sc = new Scanner(System.in);
        System.out.print("Enter Student ID (Birth Certificate No): ");
        String studentId = sc.nextLine().trim();
        showApplicantResult(studentId);
    }

    private void showApplicantResult(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            System.out.println("Student ID cannot be empty.");
            return;
        }

        studentId = studentId.trim();

        Map<String, String> resultRow = resultDB.find("StudentID", studentId);
        if (resultRow == null) {
            System.out.println("No result found for Student ID: " + studentId);
            return;
        }

        String applicantId = resultRow.get("ApplicantID");
        String seatId = resultRow.get("AdmittedSeatID");

        if (seatId == null || seatId.isEmpty()) {
            System.out.println("Student is not admitted (SeatID missing).");
            return;
        }

        String studentName = DatabaseSetup.studentInfoDB.getValueByPrimaryKey(
                "BirthCertificateNo",
                studentId,
                "Name"
        );

        if (studentName == null || studentName.isEmpty()) {
            studentName = "(Name not found in student_info.db)";
        }

        String eiin = DatabaseSetup.schoolInfoDB.getValueByPrimaryKey(
                "SeatID",
                seatId,
                "EIIN"
        );

        if (eiin == null || eiin.isEmpty()) {
            System.out.println("SeatID found, but EIIN not found for SeatID: " + seatId);
            return;
        }

        String schoolName = DatabaseSetup.schoolAreaDB.getValueByPrimaryKey(
                "EIIN",
                eiin,
                "Name"
        );

        if (schoolName == null || schoolName.isEmpty()) {
            schoolName = "(School name not found in schoolarea.db)";
        }

        System.out.println("\n#### APPLICANT RESULT ####");
        System.out.println("1. Applicant ID          : " + applicantId);
        System.out.println("2. Student ID            : " + studentId);
        System.out.println("3. Name of the student   : " + studentName);
        System.out.println("4. Admitted school name  : " + schoolName);
        System.out.println("############################\n");
    }
}
