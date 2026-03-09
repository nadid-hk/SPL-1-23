import java.util.*;

public class SchoolResult {

    private final FileDatabase resultDB;
    private final String eiin;

    public SchoolResult(String eiin) {
        DatabaseSetup.initAndSeed();

        this.resultDB = new FileDatabase(
                "result.db",
                Arrays.asList("ApplicantID", "StudentID", "AdmittedSeatID")
        );
        this.eiin = eiin == null ? "" : eiin.trim();
    }

    public void showSchoolWiseResult() {
        if (eiin.isEmpty()) {
            System.out.println("Invalid EIIN. School result cannot be shown.");
            return;
        }

        Map<String, String> schoolRow = DatabaseSetup.schoolAreaDB.find("EIIN", eiin);
        String schoolName = (schoolRow == null) ? "(School name not found)" : schoolRow.get("Name");

        List<Map<String, String>> seatRowsForSchool = new ArrayList<>();
        Set<String> schoolSeatIds = new LinkedHashSet<>();

        for (Map<String, String> seatRow : DatabaseSetup.schoolInfoDB.readAll()) {
            if (eiin.equals(seatRow.get("EIIN"))) {
                seatRowsForSchool.add(seatRow);
                String seatId = seatRow.get("SeatID");
                if (seatId != null && !seatId.trim().isEmpty()) {
                    schoolSeatIds.add(seatId.trim());
                }
            }
        }

        System.out.println("\n######## SCHOOL RESULT ########");
        System.out.println("School Name : " + schoolName);
        System.out.println("EIIN        : " + eiin);

        if (seatRowsForSchool.isEmpty()) {
            System.out.println("No seat information found for this school.");
            System.out.println("###############################\n");
            return;
        }

        List<Map<String, String>> resultRows = resultDB.readAll();
        if (resultRows.isEmpty()) {
            System.out.println("result.db is empty. Run lottery first.");
            System.out.println("###############################\n");
            return;
        }

        Map<String, List<Map<String, String>>> groupedAdmissions = new LinkedHashMap<>();
        int totalAdmitted = 0;

        for (Map<String, String> seatRow : seatRowsForSchool) {
            String seatId = safe(seatRow.get("SeatID"));
            if (seatId.isEmpty()) {
                continue;
            }

            String className = safe(seatRow.get("Class"));
            String shift = safe(seatRow.get("Shift"));
            String seatGender = safe(seatRow.get("SeatGender"));
            String groupKey = className + "|" + shift + "|" + seatGender;

            List<Map<String, String>> admittedList = new ArrayList<>();

            for (Map<String, String> resultRow : resultRows) {
                String admittedSeatId = safe(resultRow.get("AdmittedSeatID"));
                if (!seatId.equals(admittedSeatId)) {
                    continue;
                }

                String applicantId = safe(resultRow.get("ApplicantID"));
                String studentId = safe(resultRow.get("StudentID"));
                String name = safe(DatabaseSetup.studentInfoDB.getValueByPrimaryKey(
                        "BirthCertificateNo",
                        studentId,
                        "Name"
                ));
                String mobileNo = safe(DatabaseSetup.studentInfoDB.getValueByPrimaryKey(
                        "BirthCertificateNo",
                        studentId,
                        "MobileNo"
                ));

                Map<String, String> admittedInfo = new LinkedHashMap<>();
                admittedInfo.put("ApplicantID", applicantId.isEmpty() ? "(Applicant ID not found)" : applicantId);
                admittedInfo.put("Name", name.isEmpty() ? "(Name not found)" : name);
                admittedInfo.put("MobileNo", mobileNo.isEmpty() ? "(Mobile not found)" : mobileNo);
                admittedInfo.put("StudentID", studentId.isEmpty() ? "(Student ID not found)" : studentId);
                admittedInfo.put("SeatID", seatId);

                admittedList.add(admittedInfo);
                totalAdmitted++;
            }

            groupedAdmissions.put(groupKey, admittedList);
        }

        System.out.println("Total Admitted Students : " + totalAdmitted);

        if (totalAdmitted == 0) {
            System.out.println("No admitted students found for this school.");
            System.out.println("###############################\n");
            return;
        }

        for (Map<String, String> seatRow : seatRowsForSchool) {
            String className = safe(seatRow.get("Class"));
            String shift = safe(seatRow.get("Shift"));
            String seatGender = safe(seatRow.get("SeatGender"));
            String groupKey = className + "|" + shift + "|" + seatGender;

            if (!groupedAdmissions.containsKey(groupKey)) {
                continue;
            }

            List<Map<String, String>> admittedList = groupedAdmissions.get(groupKey);
            if (admittedList == null || admittedList.isEmpty()) {
                continue;
            }

            System.out.println();
            System.out.println("Class      : " + className);
            System.out.println("Shift      : " + shift);
            System.out.println("SeatGender : " + seatGender);
            System.out.printf("%-18s %-30s %-18s%n", "ApplicantID", "Name", "MobileNumber");
            System.out.println("--------------------------------------------------------------------------");

            for (Map<String, String> admitted : admittedList) {
                System.out.printf(
                        "%-18s %-30s %-18s%n",
                        admitted.get("ApplicantID"),
                        admitted.get("Name"),
                        admitted.get("MobileNo")
                );
            }
        }

        System.out.println("###############################\n");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
