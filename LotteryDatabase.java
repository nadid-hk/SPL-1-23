import java.util.*;

public class LotteryDatabase {

    public void initializeDatabase() {

        createSeatLotteryDB();          // from seatinfo
        createResultDB();               // simple result db
        createQuotaChoiceExtendedDB();  // integrated version

        System.out.println("Lottery database integration completed.");
    }

    
    //SEAT LOTTERY DB (Copy of seatinfo + extension)
    

    private void createSeatLotteryDB() {

        // Load original seatinfo database
        FileDatabase seatInfoDB =
                new FileDatabase("seatinfo.db", Arrays.asList());

        // Create extended seat lottery database
        FileDatabase seatLotteryDB =
                new FileDatabase("seat_lottery.db",
                        Arrays.asList("SeatID","EIIN","Class",
                                "Shift","SeatGender",
                                "SeatAvailable",
                                "FFSeat","AreaSeat","GeneralSeat"));

        for(Map<String,String> row : seatInfoDB.readAll()) {

            int totalSeat =
                    Integer.parseInt(row.get("SeatAvailable"));

            int ffSeat   = (int)Math.round(totalSeat * 0.20);
            int areaSeat = (int)Math.round(totalSeat * 0.10);
            int generalSeat = totalSeat - ffSeat - areaSeat;

            Map<String,String> newRow = new LinkedHashMap<>();

            newRow.put("SeatID", row.get("SeatID"));
            newRow.put("EIIN", row.get("EIIN"));
            newRow.put("Class", row.get("Class"));
            newRow.put("Shift", row.get("Shift"));
            newRow.put("SeatGender", row.get("SeatGender"));
            newRow.put("SeatAvailable", row.get("SeatAvailable"));

            newRow.put("FFSeat", String.valueOf(ffSeat));
            newRow.put("AreaSeat", String.valueOf(areaSeat));
            newRow.put("GeneralSeat", String.valueOf(generalSeat));

            seatLotteryDB.insert(newRow);
        }
    }

    private void createResultDB() {

        FileDatabase applicantDB =
                new FileDatabase("applicant.db", Arrays.asList());

        FileDatabase resultDB =
                new FileDatabase("result.db",
                        Arrays.asList("ApplicantID","StudentID","AdmittedSeatID"));

        for(Map<String,String> applicant : applicantDB.readAll()) {

            Map<String,String> row = new LinkedHashMap<>();
            row.put("ApplicantID",applicant.get("ApplicantID"));

            // StudentID = BirthCertificateNo
            row.put("StudentID",
                    applicant.get("BirthCertNo"));

            row.put("AdmittedSeatID","");

            resultDB.insert(row);
        }
    }

    // Quota Choice Extended DB (Integrated Version)

    private void createQuotaChoiceExtendedDB() {

        FileDatabase quotaChoiceDB =
                new FileDatabase("quota_choice.db",
                        Arrays.asList());

        FileDatabase applicantDB =
                new FileDatabase("applicant.db",
                        Arrays.asList());

        FileDatabase extendedDB =
                new FileDatabase("quota_choice_extended.db",
                        Arrays.asList("ChoiceID",
                                "StudentID",
                                "SeatID",
                                "Quota"));

        for(Map<String,String> choice : quotaChoiceDB.readAll()) {

            String applicantID =
                    choice.get("ApplicantID");

            // find BirthCertificateNo from applicant.db
            String birthCertNo = "";

            for(Map<String,String> applicant :
                    applicantDB.readAll()) {

                if(applicant.get("ApplicantID")
                        .equals(applicantID)) {

                    birthCertNo =
                            applicant.get("BirthCertNo");
                    break;
                }
            }

            Map<String,String> newRow =
                    new LinkedHashMap<>();

            newRow.put("ChoiceID",
                    choice.get("ChoiceID"));

            newRow.put("StudentID",
                    birthCertNo);

            newRow.put("SeatID",
                    choice.get("SeatID"));

            newRow.put("Quota",
                    choice.get("Quota"));

            extendedDB.insert(newRow);
        }
    }
}
