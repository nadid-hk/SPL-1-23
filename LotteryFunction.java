import java.util.*;

public class LotteryFunction {
private FileDatabase quotaChoiceDB;
private FileDatabase seatLotteryDB;
private FileDatabase resultDB;
private FileDatabase applicantDB;
private FileDatabase schoolAreaDB;

private Random random = new Random();

public LotteryFunction(FileDatabase quotaChoiceDB,
                       FileDatabase seatLotteryDB,
                       FileDatabase resultDB,
                       FileDatabase applicantDB,
                       FileDatabase schoolAreaDB) {

    this.quotaChoiceDB = quotaChoiceDB;
    this.seatLotteryDB = seatLotteryDB;
    this.resultDB = resultDB;
    this.applicantDB = applicantDB;
    this.schoolAreaDB = schoolAreaDB;
}

// Main Lottery Function

public void runLottery() {

    boolean progressMade;

    do {

        progressMade = false;

        List<Map<String, String>> seats = seatLotteryDB.readAll();

        for (Map<String, String> selectedSeat : seats) {

            // Reload choices each time so deletions from prior winners are reflected
            List<Map<String, String>> choices = quotaChoiceDB.readAll();

            String seatID = selectedSeat.get("SeatID");

            if (totalSeatOf(selectedSeat) <= 0)
                continue;

            List<Map<String, String>> ffList = new ArrayList<>();
            List<Map<String, String>> areaList = new ArrayList<>();
            List<Map<String, String>> generalList = new ArrayList<>();

            for (Map<String, String> choice : choices) {

                if (!choice.get("SeatID").equals(seatID))
                    continue;

                String quota = choice.get("Quota");

                if ("FF".equals(quota)) {

                    ffList.add(choice);

                } else if ("AreaQuota".equals(quota)) {

                    if (isAreaMatched(choice.get("StudentID"), seatID)) {
                        areaList.add(choice);
                    }

                } else {

                    generalList.add(choice);
                }
            }

            Map<String, String> winner = null;
            String quotaColumn = null;

            if (!ffList.isEmpty() &&
                    Integer.parseInt(selectedSeat.get("FFSeat")) > 0) {

                winner = randomPick(ffList);
                quotaColumn = "FFSeat";

            } else if (!areaList.isEmpty() &&
                    Integer.parseInt(selectedSeat.get("AreaSeat")) > 0) {

                winner = randomPick(areaList);
                quotaColumn = "AreaSeat";

            } else if (!generalList.isEmpty() &&
                    Integer.parseInt(selectedSeat.get("GeneralSeat")) > 0) {

                winner = randomPick(generalList);
                quotaColumn = "GeneralSeat";
            }

            if (winner != null) {

                decreaseSeat(seatID, quotaColumn);

                String studentID = winner.get("StudentID");
                String applicantID = winner.get("ApplicantID");

                updateResult(applicantID, studentID, seatID);

                deleteAllChoices(studentID);

                progressMade = true;
            }
        }

    } while (progressMade);

    markWaitingApplicants();
    // CHANGE START (cross-team integration): expose result-ready state for applicant menu.
    markResultReady();
    // CHANGE END
}

// Helper Methods

private int totalSeatOf(Map<String, String> seat) {

    return Integer.parseInt(seat.get("FFSeat")) +
            Integer.parseInt(seat.get("AreaSeat")) +
            Integer.parseInt(seat.get("GeneralSeat"));
}

private void decreaseSeat(String seatID, String quotaColumn) {

    Map<String, String> seat =
            seatLotteryDB.find("SeatID", seatID);

    int newValue =
            Integer.parseInt(seat.get(quotaColumn)) - 1;

    Map<String, String> newData = new HashMap<>();
    newData.put(quotaColumn, String.valueOf(newValue));

    seatLotteryDB.update("SeatID", seatID, newData);
}

private Map<String, String> randomPick(List<Map<String, String>> list) {

    return list.get(random.nextInt(list.size()));
}

private void deleteAllChoices(String studentID) {

    List<Map<String, String>> all =
            quotaChoiceDB.readAll();

    for (Map<String, String> row : all) {

        if (row.get("StudentID").equals(studentID)) {

            quotaChoiceDB.delete(
                    "ChoiceID",
                    row.get("ChoiceID"));
        }
    }
}

// Area match check

private boolean isAreaMatched(String studentID, String seatID) {

    Map<String, String> applicantRow =
            applicantDB.find("BirthCertNo", studentID);

    String applicantPost =
            (applicantRow != null) ? applicantRow.get("SchoolAreaPostCode") : null;

    String eiin =
            seatLotteryDB.getValueByPrimaryKey(
                    "SeatID",
                    seatID,
                    "EIIN");

    String schoolPost =
            schoolAreaDB.getValueByPrimaryKey(
                    "EIIN",
                    eiin,
                    "Postcode");

    return applicantPost != null &&
            applicantPost.equals(schoolPost);
}

// Insert or Update result

private void updateResult(
        String applicantID,
        String studentID,
        String seatID) {

    Map<String, String> row =
            resultDB.find("StudentID", studentID);

    if (row == null) {

        Map<String, String> newRow =
                new LinkedHashMap<>();

        newRow.put("ApplicantID", applicantID);
        newRow.put("StudentID", studentID);
        newRow.put("AdmittedSeatID", seatID);

        resultDB.insert(newRow);

    } else {

        Map<String, String> newData =
                new HashMap<>();

        newData.put("AdmittedSeatID", seatID);

        resultDB.update(
                "StudentID",
                studentID,
                newData);
    }
}

// Mark Waiting Applicants

private void markWaitingApplicants() {

    for (Map<String, String> row :
            resultDB.readAll()) {

        String seat = row.get("AdmittedSeatID");

        if (seat == null || seat.trim().isEmpty()) {

            Map<String, String> waiting =
                    new HashMap<>();

            waiting.put("AdmittedSeatID", "WAITING");

            resultDB.update(
                    "ApplicantID",
                    row.get("ApplicantID"),
                    waiting);
        }
    }
}

// CHANGE START (cross-team integration): isolated status marker for applicant menu gating.
private void markResultReady() {
    FileDatabase statusDB = new FileDatabase("lottery_status.db", Arrays.asList("Key", "Value"));
    Map<String, String> existing = statusDB.find("Key", "RESULT_READY");
    if (existing == null) {
        Map<String, String> row = new LinkedHashMap<>();
        row.put("Key", "RESULT_READY");
        row.put("Value", "TRUE");
        statusDB.insert(row);
    } else {
        Map<String, String> update = new HashMap<>();
        update.put("Value", "TRUE");
        statusDB.update("Key", "RESULT_READY", update);
    }
}
// CHANGE END

}
