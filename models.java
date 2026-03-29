import java.util.*;

// Keep simple models together to ease compilation.

class BirthRecord {
    public final String bcNo;
    public final String name;
    public final String dob; // dd-MM-yyyy
    public final String fatherName;
    public final String motherName;
    public final String gender; // MALE/FEMALE
    public final String postcode;

    public BirthRecord(String bcNo, String name, String dob, String fatherName, String motherName, String gender, String postcode) {
        this.bcNo = bcNo;
        this.name = name;
        this.dob = dob;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.gender = gender;
        this.postcode = postcode;
    }
}

class Address {
    public String postCode = "";
    public String division = "";
    public String district = "";
    public String thana = "";
    public String detailed = "";
}

class Choice {
    public String seatId = "";
    public String schoolName = "";
    public String shift = "";
    
    public String quota = "GN"; // FF / LOCAL / GN for now! needs extendable
    public String refId = "";   // only for FF
}

class ApplicationForm {
    public String applicantId = "";
    public String birthCertNo = "";
    public String submissionTime = "";

    // auto-filled from BirthRecord
    public String name = "";
    public String dob = "";
    public String gender = "";

    // user-provided (some may be prefilled from student info)
    public String religion = "";
    public String mobile = "";

    public String fatherNid = "";
    public String fatherName = "";
    public String motherNid = "";
    public String motherName = "";
    public String localGuardianNid = "";
    public String localGuardianName = "";

    public int desiredClass = -1;

    public Address present = new Address();
    public Address permanent = new Address();

    // Applying school area postcode (used to list seats)
    public String schoolAreaPostCode = "";

    // Previous school (not persisted in provided schema; kept for TXT card only)
    public String prevSchoolAreaPostCode = "";
    public String previousSchoolName = "";

    public List<Choice> choices = new ArrayList<>(); // up to 5

    // lock controls (for "previous info found and filled")
    public final Set<String> lockedFields = new HashSet<>();

    public boolean isLocked(String key) {
        return lockedFields.contains(key);
    }
}
