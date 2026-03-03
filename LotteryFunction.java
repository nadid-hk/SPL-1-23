import java.util.*;

public class LotteryFunction {

    private FileDatabase quotaChoiceDB;      // quota_choice_extended.db
    private FileDatabase seatLotteryDB;      // seat_lottery.db
    private FileDatabase resultDB;           // result.db
    private FileDatabase applicantDB;        // existing
    private FileDatabase schoolAreaDB;       // existing

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

   
    // MAIN LOTTERY FUNCTION

    public void runLottery() {

        boolean progressMade;

        do {
            progressMade = false;

            List<Map<String, String>> seats = seatLotteryDB.readAll();

            for (Map<String, String> selectedSeat : seats) {

                String seatID = selectedSeat.get("SeatID");

                if (totalSeatOf(selectedSeat) <= 0)
                    continue;

                List<Map<String, String>> choices = quotaChoiceDB.readAll();

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

                    updateResult(studentID, seatID);
                    deleteAllChoices(studentID);

                    progressMade = true;
                }
            }

        } while (progressMade);

        markWaitingApplicants();
    }

    // HELPER METHODS
    

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

    private Map<String, String> randomPick(
            List<Map<String, String>> list) {

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

    // AREA MATCH CHECK (using BirthCertificateNo)

    private boolean isAreaMatched(
            String studentID,
            String seatID) {

        String applicantPost =
                applicantDB.getValueByPrimaryKey(
                        "BirthCertNo",
                        studentID,
                        "SchoolAreaPostCode");

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

    private void updateResult(
            String studentID,
            String seatID) {

        Map<String, String> newData =
                new HashMap<>();

        newData.put("AdmittedSeatID", seatID);

        resultDB.update(
                "StudentID",
                studentID,
                newData);
    }

    private void markWaitingApplicants() {

        for (Map<String, String> row :
                resultDB.readAll()) {

            if (row.get("AdmittedSeatID") == null ||
                row.get("AdmittedSeatID").isEmpty()) {

                Map<String, String> waiting =
                        new HashMap<>();

                waiting.put("AdmittedSeatID", "WAITING");

                resultDB.update(
                        "StudentID",
                        row.get("StudentID"),
                        waiting);
            }
        }
    }
}