import java.util.*;

public class DatabaseSetup {

    public static FileDatabase birthDB;
    public static FileDatabase postcodeDB;
    public static FileDatabase schoolAreaDB;
    public static FileDatabase schoolInfoDB;

    public static FileDatabase nidDB;
    public static FileDatabase referenceDB;

    public static FileDatabase classEligibilityDB;
    public static FileDatabase authorityLoginDB;

    // Keep EMPTY (only schema/attributes)
    public static FileDatabase applicantDB;     // ApplicantID, BirthCertNo
    public static FileDatabase studentInfoDB;   // attributes only

    public static void initAndSeed() {

        birthDB = new FileDatabase(
                "birthcertificate.db",
                Arrays.asList("BirthCertificateNo", "Name", "BirthDate", "FatherName", "MotherName", "Gender", "Postcode")
        );

        postcodeDB = new FileDatabase(
                "postcode.db",
                Arrays.asList("PostCode", "Division", "District", "Thana")
        );

        schoolAreaDB = new FileDatabase(
                "schoolarea.db",
                Arrays.asList("EIIN", "Name", "Postcode")
        );

        schoolInfoDB = new FileDatabase(
                "seatinfo.db",
                Arrays.asList("SeatID", "EIIN", "Class", "Shift", "SeatGender", "SeatAvailable")
        );

        nidDB = new FileDatabase(
                "nid.db",
                Arrays.asList("NID", "Name")
        );

        referenceDB = new FileDatabase(
                "reference.db",
                Arrays.asList("RefID", "NID")
        );

        classEligibilityDB = new FileDatabase(
                "class_eligibility.db",
                Arrays.asList("Class", "MinDate", "MaxDate")
        );

        authorityLoginDB = new FileDatabase(
                "authority_login.db",
                Arrays.asList("EIIN", "Password")
        );

        // APPLICANT DATABASE INSERTION
        
        applicantDB = new FileDatabase(
                "applicant.db",
                Arrays.asList(
                        "ApplicantID",
                        "BirthCertNo",
                        "SchoolAreaPostCode",
                        "Choice1SeatID", "Choice1Quota", "Choice1RefID",
                        "Choice2SeatID", "Choice2Quota", "Choice2RefID",
                        "Choice3SeatID", "Choice3Quota", "Choice3RefID",
                        "Choice4SeatID", "Choice4Quota", "Choice4RefID",
                        "Choice5SeatID", "Choice5Quota", "Choice5RefID"
                )
        );

        
        studentInfoDB = new FileDatabase(
                "student_info.db",
                Arrays.asList(
                        "BirthCertificateNo",
                        "Name",
                        "DOB",
                        "Gender",
                        "Religion",
                        "MobileNo",
                        "FatherNID",
                        "MotherNID",
                        "LocalGurdianNID",
                        "AdmittedClass",
                        "PresentAdressPostcode",
                        "DetailedPresentAdress",
                        "ParmanentAdressPostcode",
                        "DetailedParmanentAdress"
                )
        );

        seedIfEmpty(); 
    }

    private static void seedIfEmpty() {

        //    POSTCODES DATABASE INSERTION
        if (postcodeDB.readAll().isEmpty()) {
            insertPost("1000", "DHAKA", "DHAKA", "MOTIJHEEL");
            insertPost("1217", "DHAKA", "DHAKA", "RAMNA");
            insertPost("1206", "DHAKA", "DHAKA", "CANTONMENT & TEJGAON");
        }

        // NID DATABASE  INSERTION
        if (nidDB.readAll().isEmpty()) {

            // Fathers
            insertNid("19901234567890123", "MD. KAMAL HASAN");
            insertNid("19887654321098765", "MD. SELIM UDDIN");
            insertNid("19775553331112223", "MD. NURUL ISLAM");
            insertNid("19881234123412345", "MD. AZIZUL HAQUE");
            insertNid("19993322110099887", "MD. RAFIQUL ISLAM");
            insertNid("19790011223344556", "MD. IMRAN HOSSAIN");
            insertNid("19850099887766554", "MD. MAHBUB ALAM");
            insertNid("19970055667788990", "MD. SAIFUR RAHMAN");
            insertNid("19830011220033445", "MD. SHAFIQUR RAHMAN");
            insertNid("19760088990011223", "MD. ANISUR RAHMAN");

            // Mothers 
            insertNid("19901111222233334", "FARHANA AKTER");
            insertNid("19880000111122223", "RUKEYA BEGUM");
            insertNid("19902222333344445", "SALMA KHATUN");
            insertNid("19887777666655554", "NASRIN SULTANA");
            insertNid("19905556667778889", "MST. SHAHINUR BEGUM");
            insertNid("19896666555544443", "MST. MORIUM BEGUM");
            insertNid("19914443332221110", "MAHFUZA AKTER");
            insertNid("19873322110099876", "JANNATARA BEGUM");
            insertNid("19926667778889990", "AFROZA BEGUM");
            insertNid("19841112223334445", "NUSRAT JAHAN");

            //  LocalGurdianNID testing IF NO PARENT FOUND 
            insertNid("19770123456789012", "MD. SHAHIDUL ISLAM");
            insertNid("19890111223344556", "MST. TAHMINA AKTER");
            insertNid("19785554443332221", "MD. MOSHIUR RAHMAN");
        }

        // REFERENCES DATABASE INSERTION
        if (referenceDB.readAll().isEmpty()) {
            insertRef("REF001", "19901234567890123");
            insertRef("REF002", "19887654321098765");
            insertRef("REF003", "19775553331112223");
            insertRef("REF004", "19881234123412345");
            insertRef("REF005", "19901111222233334");
            insertRef("REF006", "19880000111122223");
        }

        // BIRTH CERTIFICATE DATABASE INSERTION
        if (birthDB.readAll().isEmpty()) {
            String[] postcodes = {"1000", "1217", "1206"};

            insertBirth("BC3001", "RAHIM HASAN",   "24-07-2017", "MD. KAMAL HASAN",    "FARHANA AKTER",      "MALE",   postcodes[0]);
            insertBirth("BC3002", "KAMRUN NAHAR",  "11-02-2016", "MD. SELIM UDDIN",    "RUKEYA BEGUM",       "FEMALE", postcodes[1]);
            insertBirth("BC3003", "SAMIUL ISLAM",  "05-09-2018", "MD. NURUL ISLAM",    "SALMA KHATUN",       "MALE",   postcodes[2]);
            insertBirth("BC3004", "NUSRAT JAHAN",  "18-03-2017", "MD. AZIZUL HAQUE",   "NASRIN SULTANA",     "FEMALE", postcodes[0]);
            insertBirth("BC3005", "TASNIM AKTER",  "09-01-2019", "MD. RAFIQUL ISLAM",  "MST. SHAHINUR BEGUM", "FEMALE", postcodes[1]);
            insertBirth("BC3006", "ARIF HOSSAIN",  "12-11-2016", "MD. IMRAN HOSSAIN",  "MST. MORIUM BEGUM",  "MALE",   postcodes[2]);
            insertBirth("BC3007", "MEHEDI HASAN",  "20-05-2017", "MD. MAHBUB ALAM",    "MAHFUZA AKTER",      "MALE",   postcodes[0]);
            insertBirth("BC3008", "SADIA ISLAM",   "02-08-2018", "MD. SAIFUR RAHMAN",  "JANNATARA BEGUM",    "FEMALE", postcodes[1]);
            insertBirth("BC3009", "RIFAT AHMED",   "27-12-2015", "MD. SHAFIQUR RAHMAN","AFROZA BEGUM",       "MALE",   postcodes[2]);
            insertBirth("BC3010", "SUMAIYA KHATUN","14-04-2016", "MD. ANISUR RAHMAN",  "NUSRAT JAHAN",       "FEMALE", postcodes[0]);

            insertBirth("BC3011", "FAHIM RAHMAN",  "06-06-2017", "MD. SELIM UDDIN",    "SALMA KHATUN",       "MALE",   postcodes[1]);
            insertBirth("BC3012", "MIM AKTER",     "19-09-2018", "MD. NURUL ISLAM",    "NASRIN SULTANA",     "FEMALE", postcodes[2]);
            insertBirth("BC3013", "NAFISA ISLAM",  "01-02-2017", "MD. AZIZUL HAQUE",   "MST. SHAHINUR BEGUM", "FEMALE", postcodes[0]);
            insertBirth("BC3014", "SABIT HASAN",   "23-03-2019", "MD. RAFIQUL ISLAM",  "MST. MORIUM BEGUM",  "MALE",   postcodes[1]);
            insertBirth("BC3015", "TANVIR HOSSAIN","10-10-2016", "MD. IMRAN HOSSAIN",  "MAHFUZA AKTER",      "MALE",   postcodes[2]);
        }

        // SCHOOL AREAS DATABASE INSERTION
        if (schoolAreaDB.readAll().isEmpty()) {
            insertSchoolArea("108001", "Adamjee Cantonment School", "1206");
            insertSchoolArea("108002", "B.A.F. Shaheen College", "1206");
            insertSchoolArea("108003", "Shaheed Anwar Girls' School", "1206");
            insertSchoolArea("108004", "BD International School", "1206");

            insertSchoolArea("108101", "Motijheel Govt. Boys' High School", "1000");
            insertSchoolArea("108102", "Motijheel Govt. Girls' High School", "1000");
            insertSchoolArea("108103", "Motijheel Model School & College", "1000");
            insertSchoolArea("108104", "Bangladesh Bank High School", "1000");
            insertSchoolArea("108105", "Motijheel Colony High School", "1000");

            insertSchoolArea("108201", "Viqarunnisa Noon School & College", "1217");
            insertSchoolArea("108202", "Willes Little Flower School", "1217");
            insertSchoolArea("108203", "BIAM Model School & College", "1217");
            insertSchoolArea("108204", "Provati Uchya Bidyalaya", "1217");
            insertSchoolArea("108205", "Eskaton Garden High School", "1217");
        }

        // CLASS ELIGIBILITY DATABASE INSERTION
        if (classEligibilityDB.readAll().isEmpty()) {
            insertEligibility("1", "01-01-2019", "31-12-2020");
            insertEligibility("3", "01-01-2017", "31-12-2018");
            insertEligibility("6", "01-01-2014", "31-12-2015");
            insertEligibility("9", "01-01-2011", "31-12-2012");
        }

        //  AUTHORITY LOGIN DATABASE INSERTION
        if (authorityLoginDB.readAll().isEmpty()) {
            insertAuthorityLogin("108001", "482193");
            insertAuthorityLogin("108002", "716204");
            insertAuthorityLogin("108003", "903517");
            insertAuthorityLogin("108004", "128649");

            insertAuthorityLogin("108101", "640275");
            insertAuthorityLogin("108102", "551809");
            insertAuthorityLogin("108103", "334712");
            insertAuthorityLogin("108104", "875126");
            insertAuthorityLogin("108105", "209884");

            insertAuthorityLogin("108201", "770341");
            insertAuthorityLogin("108202", "491660");
            insertAuthorityLogin("108203", "612938");
            insertAuthorityLogin("108204", "058417");
            insertAuthorityLogin("108205", "965703");
        }

        // SEAT INFO DATABASE INSERTION
        if (schoolInfoDB.readAll().isEmpty()) {
            addSeat("108001", 1, "MORNING", "BOTH", 60);
            addSeat("108001", 1, "DAY",     "BOTH", 55);
            addSeat("108001", 6, "MORNING", "BOTH", 30);
            addSeat("108001", 9, "DAY",     "BOTH", 22);

            addSeat("108002", 1, "MORNING", "BOTH", 75);
            addSeat("108002", 1, "DAY",     "BOTH", 70);
            addSeat("108002", 6, "MORNING", "BOTH", 25);
            addSeat("108002", 9, "DAY",     "BOTH", 18);

            addSeat("108003", 1, "MORNING", "FEMALE", 55);
            addSeat("108003", 1, "DAY",     "FEMALE", 50);
            addSeat("108003", 6, "MORNING", "FEMALE", 20);
            addSeat("108003", 9, "DAY",     "FEMALE", 15);

            addSeat("108004", 1, "DAY", "BOTH", 25);
            addSeat("108004", 6, "DAY", "BOTH", 20);
            addSeat("108004", 9, "DAY", "BOTH", 18);

            addSeat("108101", 3, "MORNING", "MALE", 70);
            addSeat("108101", 3, "DAY",     "MALE", 65);
            addSeat("108101", 6, "MORNING", "MALE", 55);
            addSeat("108101", 9, "DAY",     "MALE", 12);

            addSeat("108102", 3, "MORNING", "FEMALE", 60);
            addSeat("108102", 3, "DAY",     "FEMALE", 58);
            addSeat("108102", 6, "MORNING", "FEMALE", 40);
            addSeat("108102", 9, "DAY",     "FEMALE", 10);

            addSeat("108103", 1, "MORNING", "BOTH", 90);
            addSeat("108103", 1, "DAY",     "BOTH", 85);
            addSeat("108103", 6, "MORNING", "BOTH", 60);
            addSeat("108103", 9, "DAY",     "BOTH", 50);

            addSeat("108104", 1, "MORNING", "BOTH", 45);
            addSeat("108104", 6, "DAY",     "BOTH", 40);

            addSeat("108105", 1, "MORNING", "BOTH", 35);
            addSeat("108105", 6, "DAY",     "BOTH", 30);

            addSeat("108201", 1, "MORNING", "FEMALE", 120);
            addSeat("108201", 1, "DAY",     "FEMALE", 110);
            addSeat("108201", 6, "MORNING", "FEMALE", 70);
            addSeat("108201", 9, "DAY",     "FEMALE", 55);

            addSeat("108202", 1, "MORNING", "BOTH", 70);
            addSeat("108202", 1, "DAY",     "BOTH", 65);
            addSeat("108202", 6, "MORNING", "BOTH", 55);
            addSeat("108202", 9, "DAY",     "BOTH", 50);

            addSeat("108203", 1, "MORNING", "BOTH", 40);
            addSeat("108203", 6, "DAY",     "BOTH", 38);

            addSeat("108204", 1, "MORNING", "BOTH", 45);
            addSeat("108204", 6, "DAY",     "BOTH", 42);

            addSeat("108205", 1, "MORNING", "BOTH", 35);
            addSeat("108205", 6, "DAY",     "BOTH", 30);
        }

        
    }

    /* -------------------- Insert Helpers -------------------- */

    private static void insertBirth(String bcNo, String name, String bdate,
                                    String father, String mother, String gender, String postcode) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("BirthCertificateNo", bcNo);
        r.put("Name", name);
        r.put("BirthDate", bdate);
        r.put("FatherName", father);
        r.put("MotherName", mother);
        r.put("Gender", gender);
        r.put("Postcode", postcode);
        birthDB.insert(r);
    }

    private static void insertPost(String pc, String district, String division, String thana) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("PostCode", pc);
        r.put("District", district);
        r.put("Division", division);
        r.put("Thana", thana);
        postcodeDB.insert(r);
    }

    private static void insertSchoolArea(String eiin, String name, String postcode) {
        Map<String, String> r = new LinkedHashMap<>();
        r.put("EIIN", eiin);
        r.put("Name", name);
        r.put("Postcode", postcode);
        schoolAreaDB.insert(r);
    }

    // Generate a unique SeatID: Using random]
    private static void addSeat(String eiin, int classNo, String shift, String seatGender, int seatAvail) {
        // Collect all existing SeatIDs so we avoid duplicates
        Set<String> used = new HashSet<>();
        for (Map<String, String> row : schoolInfoDB.readAll()) {
            String sid = row.get("SeatID");
            if (sid != null && !sid.isEmpty()) used.add(sid);
        }

        // Generate a unique SeatID: S-[8 digit]
        Random rand = new Random();
        String seatId;
        while (true) {
            int num = rand.nextInt(100_000_000); // 0..99,999,999
            seatId = "S-" + String.format("%08d", num);
            if (!used.contains(seatId)) break;
        }

    Map<String, String> r = new LinkedHashMap<>();
    r.put("SeatID", seatId);
    r.put("EIIN", eiin);
    r.put("Class", String.valueOf(classNo));
    r.put("Shift", shift);
    r.put("SeatGender", seatGender);
    r.put("SeatAvailable", String.valueOf(seatAvail));

    schoolInfoDB.insert(r);
}

private static void insertNid(String nid, String name) {
    Map<String, String> r = new LinkedHashMap<>();
    r.put("NID", nid);
    r.put("Name", name);
    nidDB.insert(r);
}

private static void insertRef(String refId, String nid) {
    Map<String, String> r = new LinkedHashMap<>();
    r.put("RefID", refId);
    r.put("NID", nid);
    referenceDB.insert(r);
}

private static void insertEligibility(String cls, String minDate, String maxDate) {
    Map<String, String> r = new LinkedHashMap<>();
    r.put("Class", cls);
    r.put("MinDate", minDate);
    r.put("MaxDate", maxDate);
    classEligibilityDB.insert(r);
}

private static void insertAuthorityLogin(String eiin, String password6Digit) {
    Map<String, String> r = new LinkedHashMap<>();
    r.put("EIIN", eiin);
    r.put("Password", password6Digit);
    authorityLoginDB.insert(r);
}
}