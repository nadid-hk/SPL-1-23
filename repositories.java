import java.util.*;

class BirthCertificateRepository {
    private final FileDatabase db;
    public BirthCertificateRepository(FileDatabase db) { this.db = db; }

    public BirthRecord getByBcNo(String bcNo) throws NotFoundException {
        Map<String,String> r = db.find("BirthCertificateNo", bcNo);
        if (r == null) throw new NotFoundException("Birth certificate not found in birthcertificate.db.");
        return new BirthRecord(
                r.get("BirthCertificateNo"),
                r.get("Name"),
                r.get("BirthDate"),
                r.get("FatherName"),
                r.get("MotherName"),
                r.get("Gender"),
                r.get("Postcode")
        );
    }
}

class StudentInfoRepository {
    private final FileDatabase db;
    public StudentInfoRepository(FileDatabase db) { this.db = db; }

    public Map<String,String> findByBcNo(String bcNo) {
        return db.find("BirthCertificateNo", bcNo);
    }

    public void insertNew(Map<String,String> record) {
        db.insert(record);
    }

    public void updateByBcNo(String bcNo, Map<String,String> newData) {
        db.update("BirthCertificateNo", bcNo, newData);
    }
}

class ApplicantRepository {
    private final FileDatabase db;
    public ApplicantRepository(FileDatabase db) { this.db = db; }

    public List<Map<String,String>> findAllByBcNo(String bcNo) {
        List<Map<String,String>> out = new ArrayList<>();
        for (Map<String,String> r : db.readAll()) {
            if (bcNo.equals(r.get("BirthCertNo"))) out.add(r);
        }
        return out;
    }

    public void insert(Map<String,String> record) {
        db.insert(record);
    }
}

class NidRepository {
    private final FileDatabase db;
    public NidRepository(FileDatabase db) { this.db = db; }

    public String nameByNid(String nid) {
        Map<String,String> r = db.find("NID", nid);
        return r == null ? null : r.get("Name");
    }
}

class ReferenceRepository {
    private final FileDatabase db;
    public ReferenceRepository(FileDatabase db) { this.db = db; }

    public String nidByRefId(String refId) {
        Map<String,String> r = db.find("RefID", refId);
        return r == null ? null : r.get("NID");
    }
}

class PostcodeRepository {
    private final FileDatabase db;
    public PostcodeRepository(FileDatabase db) { this.db = db; }

    public Map<String,String> findByPostcode(String pc) {
        return db.find("PostCode", pc);
    }

    public List<Map<String,String>> readAll() {
        return db.readAll();
    }

    public String postcodeFor(String division, String district, String thana) {
        for (Map<String,String> r : db.readAll()) {
            if (division.equals(r.get("Division")) && district.equals(r.get("District")) && thana.equals(r.get("Thana"))) {
                return r.get("PostCode");
            }
        }
        return null;
    }

    public List<String> uniqueDivisions() {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        for (Map<String,String> r : db.readAll()) s.add(r.get("Division"));
        return new ArrayList<>(s);
    }

    public List<String> districtsIn(String division) {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        for (Map<String,String> r : db.readAll()) if (division.equals(r.get("Division"))) s.add(r.get("District"));
        return new ArrayList<>(s);
    }

    public List<String> thanasIn(String division, String district) {
        LinkedHashSet<String> s = new LinkedHashSet<>();
        for (Map<String,String> r : db.readAll()) {
            if (division.equals(r.get("Division")) && district.equals(r.get("District"))) s.add(r.get("Thana"));
        }
        return new ArrayList<>(s);
    }
}

class ClassEligibilityRepository {
    private final FileDatabase db;
    public ClassEligibilityRepository(FileDatabase db) { this.db = db; }

    public Map<String,String> forClass(int cls) {
        return db.find("Class", String.valueOf(cls));
    }
}

class SchoolRepository {
    private final FileDatabase schoolAreaDB;
    private final FileDatabase seatDB;

    public SchoolRepository(FileDatabase schoolAreaDB, FileDatabase seatDB) {
        this.schoolAreaDB = schoolAreaDB;
        this.seatDB = seatDB;
    }

    public Map<String,String> schoolByEiin(String eiin) {
        return schoolAreaDB.find("EIIN", eiin);
    }

    public List<Map<String,String>> schoolsInPostcode(String pc) {
        List<Map<String,String>> out = new ArrayList<>();
        for (Map<String,String> r : schoolAreaDB.readAll()) {
            if (pc.equals(r.get("Postcode"))) out.add(r);
        }
        return out;
    }

    public List<Map<String,String>> seatRowsByEiinAndClass(String eiin, int cls) {
        List<Map<String,String>> out = new ArrayList<>();
        for (Map<String,String> seat : seatDB.readAll()) {
            if (eiin.equals(seat.get("EIIN")) && String.valueOf(cls).equals(seat.get("Class"))) {
                out.add(seat);
            }
        }
        return out;
    }

    public List<Map<String,String>> seatsForAreaClassGender(String pc, int cls, String studentGender) {
        // Find EIIN list for area
        Set<String> eiins = new HashSet<>();
        for (Map<String,String> s : schoolAreaDB.readAll()) {
            if (pc.equals(s.get("Postcode"))) eiins.add(s.get("EIIN"));
        }

        List<Map<String,String>> out = new ArrayList<>();
        for (Map<String,String> seat : seatDB.readAll()) {
            String eiin = seat.get("EIIN");
            if (!eiins.contains(eiin)) continue;

            if (!String.valueOf(cls).equals(seat.get("Class"))) continue;

            // gender rule: BOTH is acceptable for everyone; otherwise must match student's gender
            String seatGender = seat.get("SeatGender");
            if (!"BOTH".equalsIgnoreCase(seatGender) && !seatGender.equalsIgnoreCase(studentGender)) continue;

            // must have available seat count > 0
            try {
                int avail = Integer.parseInt(seat.get("SeatAvailable"));
                if (avail <= 0) continue;
            } catch (NumberFormatException ignored) {}

            out.add(seat);
        }
        return out;
    }
}


class QuotaChoiceRepository {
    private final FileDatabase db;

    public QuotaChoiceRepository(FileDatabase db) {
        this.db = db;
    }

    public void insert(Map<String, String> record) {
        db.insert(record);
    }

    // This is crucial for checking previously used seats across multiple applications
    public List<Map<String, String>> findAllByApplicantId(String appId) {
        List<Map<String, String>> out = new ArrayList<>();
        for (Map<String, String> r : db.readAll()) {
            if (appId.equals(r.get("ApplicantID"))) {
                out.add(r);
            }
        }
        return out;
    }
}
