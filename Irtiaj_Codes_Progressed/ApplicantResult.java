import java.util.*;

public class ApplicantResult {

    private final FileDatabase resultDB;


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

        if (seatId == null || seatId.isEmpty() || "WAITING".equals(seatId)) {
            System.out.println("You didn't get chance this time. Better luck next time.");
            return;
        }

        String studentName = DatabaseSetup.studentInfoDB.getValueByPrimaryKey(
                "BirthCertificateNo",
                studentId,
                "Name"
        );


        String eiin = DatabaseSetup.schoolInfoDB.getValueByPrimaryKey(
                "SeatID",
                seatId,
                "EIIN"
        );


        String schoolName = DatabaseSetup.schoolAreaDB.getValueByPrimaryKey(
                "EIIN",
                eiin,
                "Name"
        );

        String Shift = DatabaseSetup.schoolInfoDB.getValueByPrimaryKey(
                "SeatID",
                seatId,
                "Shift"
        );

        System.out.println("\n#### APPLICANT RESULT ####");
        System.out.println("1. Applicant ID          : " + applicantId);
        System.out.println("2. Student ID            : " + studentId);
        System.out.println("3. Name of the student   : " + studentName);
        System.out.println("4. Admitted school name  : " + schoolName);
        System.out.println("5. Admitted Shift        : " + Shift);
        System.out.println("############################\n");
    }
}
