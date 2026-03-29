import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class ApplicationWorkflow {
    private final DbContext ctx;
    private final ConsoleIO io;

    private final BirthCertificateRepository birthRepo;
    private final StudentInfoRepository studentRepo;
    private final ApplicantRepository applicantRepo;
    private final NidRepository nidRepo;
    private final ReferenceRepository refRepo;
    private final PostcodeRepository pcRepo;
    private final SchoolRepository schoolRepo;
    private final ClassEligibilityRepository eligRepo;
    private final QuotaChoiceRepository quotaChoiceRepo;

    private final OtpService otp = new OtpService();
    private static final String BD_PHONE_REGEX = "^(?:\\+?880|0)1[3-9]\\d{8}$";

    public ApplicationWorkflow(DbContext ctx, ConsoleIO io) {
        this.ctx = ctx;
        this.io = io;

        this.birthRepo = new BirthCertificateRepository(ctx.birthDB);
        this.studentRepo = new StudentInfoRepository(ctx.studentInfoDB);
        this.applicantRepo = new ApplicantRepository(ctx.applicantDB);
        this.nidRepo = new NidRepository(ctx.nidDB);
        this.refRepo = new ReferenceRepository(ctx.referenceDB);
        this.pcRepo = new PostcodeRepository(ctx.postcodeDB);
        this.schoolRepo = new SchoolRepository(ctx.schoolAreaDB, ctx.schoolInfoDB);
        this.eligRepo = new ClassEligibilityRepository(ctx.classEligibilityDB);
        this.quotaChoiceRepo = new QuotaChoiceRepository(ctx.quotaChoiceDB);
    }

    public void run() {
        while (true) {
            runSingleApplication();
        }
    }

    public void runSingleApplication() {
        try {
            ApplicationForm form = new ApplicationForm();
            startWorkflow(form);
            io.println("\nApplication submitted successfully.");
            io.hr();
        } catch (BackToMainMenuSignal e) {
            io.println("\nReturned to main menu.");
            io.hr();
        } catch (RestartWorkflowException e) {
            io.println("\nRestarting application: " + e.getMessage());
            io.hr();
            runSingleApplication();
        } catch (WorkflowException e) {
            io.println("\nERROR: " + e.getMessage());
            io.println("Application was not completed.");
            io.hr();
        } catch (Exception e) {
            io.println("\nUnexpected error: " + e.getMessage());
            io.hr();
        }
    }

    private void startWorkflow(ApplicationForm f) throws WorkflowException {
        BirthRecord birth = askBirthCertificateAndPrefill(f);

        if (f.religion.isEmpty()) askReligion(f);
        if (f.mobile.isEmpty()) askMobileWithOtp(f, birth);
        // CHANGE START: Always normalize guardian info, but only force local guardian if both parents are skipped.
        if (f.fatherNid.isEmpty() && f.motherNid.isEmpty() && f.localGuardianNid.isEmpty()) {
            askGuardians(f, birth);
        } else {
            trySetGuardianNames(f);
        }
        // CHANGE END
        if (f.desiredClass < 0) askDesiredClassWithEligibilityCheck(f);

        if (f.present.postCode.isEmpty() || f.present.detailed.isEmpty()) f.present = askAddress("PRESENT ADDRESS");
        if (f.permanent.postCode.isEmpty() || f.permanent.detailed.isEmpty()) f.permanent = askAddress("PERMANENT ADDRESS");

        askApplyingAreaAndChoices(f);
        summaryAndEditLoop(f, birth);
        persist(f);
    }

    private BirthRecord askBirthCertificateAndPrefill(ApplicationForm f) throws WorkflowException {
        while (true) {
            try {
                String bcNo = io.promptNonEmpty("Enter Birth Certificate Number: ");
                f.birthCertNo = bcNo;

                Map<String, String> prevStudent = studentRepo.findByBcNo(bcNo);
                boolean hasPrevStudent = prevStudent != null;
                boolean hasPrevApplicant = !applicantRepo.findAllByBcNo(bcNo).isEmpty();

                BirthRecord birth = birthRepo.getByBcNo(bcNo);
                f.name = safeUpper(birth.name);
                f.dob = safeUpper(birth.dob);
                f.gender = safeUpper(birth.gender);

                if (hasPrevStudent || hasPrevApplicant) {
                    io.println("\nExisting student/application information found. Auto-filling locked fields.");

                    if (hasPrevStudent) {
                        f.religion = safeUpper(prevStudent.get("Religion"));
                        f.mobile = safeUpper(prevStudent.get("MobileNo"));
                        f.fatherNid = safeUpper(prevStudent.get("FatherNID"));
                        f.motherNid = safeUpper(prevStudent.get("MotherNID"));
                        f.localGuardianNid = safeUpper(prevStudent.get("LocalGurdianNID"));
                        trySetGuardianNames(f);

                        String admitted = safe(prevStudent.get("AdmittedClass"));
                        if (!admitted.isEmpty()) {
                            try { f.desiredClass = Integer.parseInt(admitted); } catch (NumberFormatException ignored) {}
                        }

                        f.present.postCode = safeUpper(prevStudent.get("PresentAdressPostcode"));
                        f.present.detailed = safeUpper(prevStudent.get("DetailedPresentAdress"));
                        f.permanent.postCode = safeUpper(prevStudent.get("ParmanentAdressPostcode"));
                        f.permanent.detailed = safeUpper(prevStudent.get("DetailedParmanentAdress"));
                        fillAdminAreaFromPostcode(f.present);
                        fillAdminAreaFromPostcode(f.permanent);
                    }

                    f.lockedFields.add("BirthCertNo");
                    f.lockedFields.add("Name");
                    f.lockedFields.add("DOB");
                    f.lockedFields.add("Gender");
                    if (hasPrevStudent) {
                        lockIfPresent(f, "Religion", f.religion);
                        lockIfPresent(f, "Mobile", f.mobile);
                        lockIfPresent(f, "FatherNID", f.fatherNid);
                        lockIfPresent(f, "MotherNID", f.motherNid);
                        lockIfPresent(f, "LocalGurdianNID", f.localGuardianNid);
                        if (f.desiredClass >= 0) lockIfPresent(f, "AdmittedClass", String.valueOf(f.desiredClass));
                        lockIfPresent(f, "PresentAdressPostcode", f.present.postCode);
                        lockIfPresent(f, "DetailedPresentAdress", f.present.detailed);
                        lockIfPresent(f, "ParmanentAdressPostcode", f.permanent.postCode);
                        lockIfPresent(f, "DetailedParmanentAdress", f.permanent.detailed);
                    }
                } else {
                    io.println("\nValid birth certificate found.");
                    io.println("Name   : " + f.name);
                    io.println("DOB    : " + f.dob);
                    io.println("Gender : " + f.gender);
                }
                io.hr();
                return birth;
            } catch (ValidationException | NotFoundException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void askReligion(ApplicationForm f) throws WorkflowException {
        while (true) {
            try {
                io.println("Select Religion:");
                io.println("  1) ISLAM");
                io.println("  2) HINDU");
                io.println("  3) BUDDHIST");
                io.println("  4) CHRISTIAN");
                io.println("  5) OTHER");
                int r = io.promptInt("Enter choice (1-5): ");
                switch (r) {
                    case 1: f.religion = "ISLAM"; break;
                    case 2: f.religion = "HINDU"; break;
                    case 3: f.religion = "BUDDHIST"; break;
                    case 4: f.religion = "CHRISTIAN"; break;
                    case 5: f.religion = "OTHER"; break;
                    default: throw new ValidationException("Invalid religion option.");
                }
                io.hr();
                return;
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void askMobileWithOtp(ApplicationForm f, BirthRecord birth) throws WorkflowException {
        while (true) {
            try {
                String mobile = io.promptNonEmpty("Enter Mobile Number: ");
                if (!mobile.matches(BD_PHONE_REGEX)) {
                    throw new ValidationException("Enter a valid Bangladeshi mobile number. Example: 017XXXXXXXX, 88017XXXXXXXX, or +88017XXXXXXXX.");
                }
                sendAndVerifyOtp(mobile);

                f.mobile = mobile;
                io.hr();
                return;
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void askGuardians(ApplicationForm f, BirthRecord birth) throws WorkflowException {
        f.fatherNid = "";
        f.motherNid = "";
        f.localGuardianNid = "";
        f.fatherName = "";
        f.motherName = "";
        f.localGuardianName = "";

        io.println("Enter guardian information. Press ENTER to skip a field. Type \\b anytime to return to main menu.");

        f.fatherNid = askParentNid("FATHER", birth.fatherName);
        if (!f.fatherNid.isEmpty()) f.fatherName = safeUpper(nidRepo.nameByNid(f.fatherNid));
        f.motherNid = askParentNid("MOTHER", birth.motherName);
        if (!f.motherNid.isEmpty()) f.motherName = safeUpper(nidRepo.nameByNid(f.motherNid));

        // CHANGE START: Local guardian is mandatory only when both father and mother are skipped.
        if (f.fatherNid.isEmpty() && f.motherNid.isEmpty()) {
            while (true) {
                try {
                    String lg = io.promptNonEmpty("Local Guardian NID (required if both father and mother are skipped): ");
                    validateNidDigitsOnly("LOCAL GUARDIAN", lg);
                    validateNidExists("LOCAL GUARDIAN", lg);
                    f.localGuardianNid = lg;
                    f.localGuardianName = safeUpper(nidRepo.nameByNid(lg));
                    break;
                } catch (ValidationException e) {
                    io.println("ERROR: " + e.getMessage());
                }
            }
        } else {
            f.localGuardianNid = "";
            f.localGuardianName = "";
        }
        // CHANGE END
        io.hr();
    }

    private String askParentNid(String label, String expectedName) throws WorkflowException {
        while (true) {
            try {
                String nid = io.promptOptional(label + " NID (press ENTER to skip): ");
                if (nid.isEmpty()) return "";
                validateNidDigitsOnly(label, nid);

                String nidName = nidRepo.nameByNid(nid);
                if (nidName == null) throw new ValidationException(label + " NID not found.");
                if (!safeUpper(nidName).equals(safeUpper(expectedName))) {
                    throw new ValidationException(label + " NID does not match the birth certificate " + label + " name. Enter the " + label + " NID again, or press ENTER to skip.");
                }
                return nid;
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void validateNidExists(String label, String nid) throws ValidationException {
        if (nid == null || nid.isEmpty()) return;
        String name = nidRepo.nameByNid(nid);
        if (name == null) throw new ValidationException(label + " NID not found.");
    }

    // CHANGE START: Centralized NID digit-only validation for clear error feedback.
    private void validateNidDigitsOnly(String label, String nid) throws ValidationException {
        if (nid == null || nid.isEmpty()) return;
        if (!nid.matches("\\d+")) {
            throw new ValidationException(label + " NID must contain digits only (no letters/symbols).");
        }
    }
    // CHANGE END

    private void trySetGuardianNames(ApplicationForm f) {
        if (!safe(f.fatherNid).isEmpty()) f.fatherName = safeUpper(nidRepo.nameByNid(f.fatherNid));
        if (!safe(f.motherNid).isEmpty()) f.motherName = safeUpper(nidRepo.nameByNid(f.motherNid));
        if (!safe(f.localGuardianNid).isEmpty()) f.localGuardianName = safeUpper(nidRepo.nameByNid(f.localGuardianNid));
    }

    private void askDesiredClassWithEligibilityCheck(ApplicationForm f) throws WorkflowException {
        while (true) {
            try {
                int cls = io.promptInt("Enter Desired Class (e.g., 1/3/6/9): ");
                Map<String, String> elig = eligRepo.forClass(cls);
                if (elig == null) throw new ValidationException("This class is not available in class_eligibility.db.");

                LocalDate dob = DateUtil.parseDMY(f.dob);
                LocalDate min = DateUtil.parseDMY(elig.get("MinDate"));
                LocalDate max = DateUtil.parseDMY(elig.get("MaxDate"));
                if (!DateUtil.isBetweenInclusive(dob, min, max)) {
                    boolean tooYoung = dob.isAfter(max);
                    throw new ValidationException("Not eligible (" + (tooYoung ? "too young" : "too old") + ") for this class.");
                }

                f.desiredClass = cls;
                io.hr();
                return;
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private Address askAddress(String title) throws WorkflowException {
        while (true) {
            try {
                io.println(title);
                Map<String, String> pcRec = selectPostcodeRecordByAdminArea();
                String detail = io.promptNonEmpty("Enter Detailed Address: ");

                Address a = new Address();
                a.postCode = safeUpper(pcRec.get("PostCode"));
                a.division = safeUpper(pcRec.get("Division"));
                a.district = safeUpper(pcRec.get("District"));
                a.thana = safeUpper(pcRec.get("Thana"));
                a.detailed = detail;
                io.hr();
                return a;
            } catch (ValidationException | NotFoundException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    public Map<String, String> selectPostcodeRecordByAdminArea() throws WorkflowException {
        String division = chooseFromList("DIVISION", pcRepo.uniqueDivisions());
        String district = chooseFromList("DISTRICT", pcRepo.districtsIn(division));
        String thana = chooseFromList("THANA", pcRepo.thanasIn(division, district));

        String fetchedPc = pcRepo.postcodeFor(division, district, thana);
        if (fetchedPc == null) throw new NotFoundException("No postcode found for this Division/District/Thana.");

        Map<String, String> pcRec = pcRepo.findByPostcode(fetchedPc);
        if (pcRec == null) throw new ValidationException("Invalid postcode record.");

        io.println("Selected Area:");
        io.println("  POSTCODE : " + safeUpper(pcRec.get("PostCode")));
        io.println("  DIVISION : " + safeUpper(pcRec.get("Division")));
        io.println("  DISTRICT : " + safeUpper(pcRec.get("District")));
        io.println("  THANA    : " + safeUpper(pcRec.get("Thana")));
        return pcRec;
    }

    private void fillAdminAreaFromPostcode(Address a) {
        if (a == null || a.postCode == null || a.postCode.isEmpty()) return;
        Map<String, String> r = pcRepo.findByPostcode(a.postCode);
        if (r == null) return;
        a.division = safeUpper(r.get("Division"));
        a.district = safeUpper(r.get("District"));
        a.thana = safeUpper(r.get("Thana"));
    }

    private void askApplyingAreaAndChoices(ApplicationForm f) throws WorkflowException {
        while (true) {
            try {
                io.println("APPLYING SCHOOL AREA + CHOICES");
                Map<String, String> pcRec = selectPostcodeRecordByAdminArea();
                f.schoolAreaPostCode = safeUpper(pcRec.get("PostCode"));

                List<Map<String, String>> seats = schoolRepo.seatsForAreaClassGender(f.schoolAreaPostCode, f.desiredClass, f.gender);
                if (seats.isEmpty()) throw new NotFoundException("No available seats for this area/class/gender.");

                List<SeatView> views = buildSeatViews(seats);
                printSeatViews(views);

                Set<String> previouslyUsed = previouslyUsedSeatIds(f.birthCertNo);
                int maxChoices = Math.min(5, views.size());
                io.println("You can choose up to " + maxChoices + " choices.");
                f.choices.clear();

                boolean prevAutoClear = io.isAutoClearAfterInput();
                io.setAutoClearAfterInput(false);
                try {
                    for (int rank = 1; rank <= maxChoices; rank++) {
                        while (true) {
                            String s = io.promptOptional("Enter number for choice " + rank + " (or press ENTER to stop): ");
                            if (s.isEmpty()) return;
                            int idx;
                            try {
                                idx = Integer.parseInt(s);
                            } catch (NumberFormatException e) {
                                io.println("ERROR: Please enter a valid number.");
                                continue;
                            }
                            if (idx < 1 || idx > views.size()) {
                                io.println("ERROR: Selection out of range.");
                                continue;
                            }

                            SeatView v = views.get(idx - 1);
                            boolean dup = false;
                            for (Choice existing : f.choices) if (existing.seatId.equals(v.seatId)) dup = true;
                            if (dup) {
                                io.println("ERROR: You already selected this seat in this application.");
                                continue;
                            }
                            if (previouslyUsed.contains(v.seatId)) {
                                io.println("ERROR: This SeatID was already used in a previous application of this student.");
                                continue;
                            }

                            Choice c = new Choice();
                            c.seatId = v.seatId;
                            c.schoolName = v.schoolName;
                            c.shift = v.shift;
                            applyQuotaRules(f, c);
                            f.choices.add(c);
                            io.println("Saved choice " + rank + ": " + v.schoolName + " || " + v.shift + " || " + c.quota);
                            io.hr();
                            break;
                        }
                    }
                } finally {
                    io.setAutoClearAfterInput(prevAutoClear);
                }
                return;
            } catch (ValidationException | NotFoundException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private void applyQuotaRules(ApplicationForm f, Choice c) throws WorkflowException {
        boolean localEligible = safe(f.present.postCode).equals(safe(f.schoolAreaPostCode));
        while (true) {
            try {
                io.println("Quota Options:");
                io.println("  1) FF");
                if (localEligible) io.println("  2) LOCAL SCHOOL CA");
                io.println("  3) GENERAL");
                int pick = io.promptInt("Choose quota: ");
                if (pick == 1) {
                    String refId = io.promptNonEmpty("Enter RefID: ");
                    String nid = refRepo.nidByRefId(refId);
                    if (nid == null) throw new ValidationException("RefID not found.");
                    boolean ok = nid.equals(safe(f.fatherNid)) || nid.equals(safe(f.motherNid));
                    if (!ok) throw new ValidationException("RefID must belong to father or mother NID.");
                    c.quota = "FF";
                    c.refId = refId;
                    return;
                }
                if (pick == 2) {
                    if (!localEligible) throw new ValidationException("Local quota is not available for this applicant.");
                    c.quota = "LOCAL";
                    c.refId = "";
                    return;
                }
                if (pick == 3) {
                    c.quota = localEligible ? "LOCAL" : "GN";
                    c.refId = "";
                    return;
                }
                throw new ValidationException("Invalid quota selection.");
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private Set<String> previouslyUsedSeatIds(String bcNo) {
        Set<String> usedSeats = new HashSet<>();
        List<Map<String, String>> previousApps = applicantRepo.findAllByBcNo(bcNo);
        for (Map<String, String> app : previousApps) {
            String appId = app.get("ApplicantID");
            List<Map<String, String>> choices = quotaChoiceRepo.findAllByApplicantId(appId);
            for (Map<String, String> choice : choices) usedSeats.add(choice.get("SeatID"));
        }
        return usedSeats;
    }

    private void summaryAndEditLoop(ApplicationForm f, BirthRecord birth) throws WorkflowException {
        while (true) {
            io.clearScreenNow();
            LinkedHashMap<Integer, String> idx = SummaryPrinter.printAndIndex(f, io);
            io.println("Enter a number to re-edit that field, or 0 to confirm and submit.");
            int pick;
            while (true) {
                try {
                    pick = io.promptInt("Your choice: ");
                    break;
                } catch (ValidationException e) {
                    io.println("ERROR: " + e.getMessage());
                }
            }
            if (pick == 0) return;
            if (!idx.containsKey(pick)) {
                io.println("ERROR: Invalid field number.");
                continue;
            }
            String label = idx.get(pick);
            if (isLockedLabel(f, label)) {
                throw new RestartWorkflowException("Locked field can only be changed by changing the Birth Certificate Number.");
            }
            handleEditByLabel(f, birth, label);
        }
    }

    private boolean isLockedLabel(ApplicationForm f, String label) {
        switch (label) {
            case "Name (auto)":
            case "Date of Birth (auto)":
            case "Gender (auto)":
            case "Birth Certificate No":
                return true;
            case "Religion": return f.isLocked("Religion");
            case "Mobile": return f.isLocked("Mobile");
            case "Father Info": return f.isLocked("FatherNID");
            case "Mother Info": return f.isLocked("MotherNID");
            case "Local Guardian Info": return f.isLocked("LocalGurdianNID");
            case "Desired Class": return f.isLocked("AdmittedClass");
            case "Present Address": return f.isLocked("PresentAdressPostcode") || f.isLocked("DetailedPresentAdress");
            case "Permanent Address": return f.isLocked("ParmanentAdressPostcode") || f.isLocked("DetailedParmanentAdress");
            default: return false;
        }
    }

    private void handleEditByLabel(ApplicationForm f, BirthRecord birth, String label) throws WorkflowException {
        switch (label) {
            case "Religion":
                askReligion(f);
                break;
            case "Mobile":
                f.mobile = "";
                askMobileWithOtp(f, birth);
                break;
            case "Father Info":
            case "Mother Info":
            case "Local Guardian Info":
                askGuardians(f, birth);
                break;
            case "Desired Class":
                f.desiredClass = -1;
                askDesiredClassWithEligibilityCheck(f);
                f.choices.clear();
                askApplyingAreaAndChoices(f);
                break;
            case "Present Address":
                f.present = askAddress("PRESENT ADDRESS (RE-EDIT)");
                f.choices.clear();
                break;
            case "Permanent Address":
                f.permanent = askAddress("PERMANENT ADDRESS (RE-EDIT)");
                break;
            case "Applying School Area Postcode":
                f.choices.clear();
                askApplyingAreaAndChoices(f);
                break;
            default:
                io.println("This field is not directly editable here.");
        }
    }

    private void persist(ApplicationForm f) throws WorkflowException {
        int seq = applicantRepo.findAllByBcNo(f.birthCertNo).size() + 1;
        f.applicantId = String.format("APP-%s-%03d", f.birthCertNo, seq);
        f.submissionTime = DateUtil.now();

        Map<String, String> appMap = new LinkedHashMap<>();
        appMap.put("ApplicantID", f.applicantId);
        appMap.put("BirthCertNo", f.birthCertNo);
        appMap.put("SchoolAreaPostCode", f.schoolAreaPostCode);
        appMap.put("SubmissionTime", f.submissionTime);
        applicantRepo.insert(toUpperMap(appMap));

        for (int i = 0; i < f.choices.size(); i++) {
            Choice c = f.choices.get(i);
            Map<String, String> choiceMap = new LinkedHashMap<>();
            choiceMap.put("ChoiceID", f.applicantId + "-C" + (i + 1));
            choiceMap.put("ApplicantID", f.applicantId);
            choiceMap.put("SeatID", c.seatId);
            choiceMap.put("Quota", c.quota);
            choiceMap.put("Preference", String.valueOf(i + 1));
            choiceMap.put("RefID", safe(c.refId));
            quotaChoiceRepo.insert(toUpperMap(choiceMap));
        }

        Map<String, String> prev = studentRepo.findByBcNo(f.birthCertNo);
        Map<String, String> stu = new LinkedHashMap<>();
        stu.put("BirthCertificateNo", f.birthCertNo);
        stu.put("Name", f.name);
        stu.put("DOB", f.dob);
        stu.put("Gender", f.gender);
        stu.put("Religion", f.religion);
        stu.put("MobileNo", f.mobile);
        stu.put("FatherNID", f.fatherNid);
        stu.put("MotherNID", f.motherNid);
        stu.put("LocalGurdianNID", f.localGuardianNid);
        stu.put("AdmittedClass", String.valueOf(f.desiredClass));
        stu.put("PresentAdressPostcode", f.present.postCode);
        stu.put("DetailedPresentAdress", f.present.detailed);
        stu.put("ParmanentAdressPostcode", f.permanent.postCode);
        stu.put("DetailedParmanentAdress", f.permanent.detailed);

        if (prev == null) studentRepo.insertNew(toUpperMap(stu));
        else studentRepo.updateByBcNo(f.birthCertNo, toUpperMap(stu));

        writeApplicationCardTxt(f);
        io.println("Saved application to applicant.db and student_info.db.");
        io.println("Generated application card: " + f.applicantId + ".txt");
    }

    private void writeApplicationCardTxt(ApplicationForm f) throws WorkflowException {
        String file = f.applicantId + ".txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("APPLICATION CARD\n");
            bw.write("APPLICANT ID: " + f.applicantId + "\n");
            bw.write("BIRTH CERTIFICATE NO: " + f.birthCertNo + "\n");
            bw.write("SUBMISSION TIME: " + f.submissionTime + "\n\n");
            bw.write("NAME: " + f.name + "\n");
            bw.write("DOB: " + f.dob + "\n");
            bw.write("GENDER: " + f.gender + "\n");
            bw.write("RELIGION: " + f.religion + "\n");
            bw.write("MOBILE: " + f.mobile + "\n\n");
            bw.write("FATHER: " + guardianCardLine(f.fatherName, f.fatherNid) + "\n");
            bw.write("MOTHER: " + guardianCardLine(f.motherName, f.motherNid) + "\n");
            bw.write("LOCAL GUARDIAN: " + guardianCardLine(f.localGuardianName, f.localGuardianNid) + "\n\n");
            bw.write("DESIRED CLASS: " + f.desiredClass + "\n\n");
            bw.write("PRESENT ADDRESS: " + formatAddressLine(f.present) + "\n\n");
            bw.write("PERMANENT ADDRESS: " + formatAddressLine(f.permanent) + "\n\n");
            bw.write("APPLYING SCHOOL AREA POSTCODE: " + f.schoolAreaPostCode + "\n\n");
            bw.write("CHOICES:\n");
            int rank = 1;
            for (Choice c : f.choices) {
                bw.write(String.format("  %d) %s || %s || QUOTA=%s%s\n", rank++, c.schoolName, c.shift, c.quota,
                        (c.refId == null || c.refId.isEmpty()) ? "" : (" (REFID=" + c.refId + ")")));
            }
        } catch (IOException e) {
            throw new WorkflowException("Failed to write application card file: " + e.getMessage());
        }
    }

    public void showVacantSeats() {
        // CHANGE START: Keep user in this feature on input errors instead of returning to main menu.
        while (true) {
            try {
                io.println("VACANT SEAT SEARCH");
                io.println("Instruction: We are selecting a school first. Start by choosing the school location (Division -> District -> Thana).");
                Map<String, String> pcRec = selectPostcodeRecordByAdminArea();
                String postCode = safeUpper(pcRec.get("PostCode"));

                Map<String, String> chosenSchool = selectSchoolInPostcode(postCode);
                String eiin = safeUpper(chosenSchool.get("EIIN"));
                String schoolName = safeUpper(chosenSchool.get("Name"));

                int cls = chooseClassInSchool(eiin);
                List<Map<String, String>> matches = schoolRepo.seatRowsByEiinAndClass(eiin, cls);
                if (matches.isEmpty()) throw new NotFoundException("No seat rows found for this school/class combination.");

                io.println("----------------------------------------------------------------------------------------");
                io.println("VACANT SEATS: " + schoolName + " | CLASS " + cls);
                io.println("----------------------------------------------------------------------------------------");
                io.println("SHIFT || SEAT GENDER || AVAILABLE SEATS");
                for (Map<String, String> row : matches) {
                    io.println(String.format("  %s || %s || %s",
                            safeUpper(row.get("Shift")),
                            safeUpper(row.get("SeatGender")),
                            safeUpper(row.get("SeatAvailable"))));
                }
                io.hr();
                return;
            } catch (BackToMainMenuSignal e) {
                io.println("\nReturned to main menu.");
                io.hr();
                return;
            } catch (ValidationException | NotFoundException e) {
                io.println("ERROR: " + e.getMessage());
            } catch (Exception e) {
                io.println("ERROR: " + e.getMessage());
                io.hr();
                return;
            }
        }
        // CHANGE END
    }

    public void recoverApplicantIds() {
        // CHANGE START: Recovery flow now retries on user errors instead of returning to menu.
        while (true) {
            try {
                String bcNo = io.promptNonEmpty("Enter Birth Certificate Number: ");
                Map<String, String> student = studentRepo.findByBcNo(bcNo);
                if (student == null) throw new NotFoundException("No student found for this Birth Certificate Number.");

                String mobile = safeUpper(student.get("MobileNo"));
                validateOtpForRecovery(mobile);

                List<Map<String, String>> apps = applicantRepo.findAllByBcNo(bcNo);
                if (apps.isEmpty()) throw new NotFoundException("No applicant ID found for this student.");

                io.println("APPLICANT ID | SCHOOL AREA NAME | SUBMISSION TIME");
                for (Map<String, String> app : apps) {
                    String postcode = safeUpper(app.get("SchoolAreaPostCode"));
                    String areaName = areaNameFromPostcode(postcode);
                    io.println(app.get("ApplicantID") + " | " + areaName + " | " + app.get("SubmissionTime"));
                }
                io.hr();
                return;
            } catch (BackToMainMenuSignal e) {
                io.println("\nReturned to main menu.");
                io.hr();
                return;
            } catch (ValidationException | NotFoundException e) {
                io.println("ERROR: " + e.getMessage());
            } catch (Exception e) {
                io.println("ERROR: " + e.getMessage());
                io.hr();
                return;
            }
        }
        // CHANGE END
    }

    public void deleteApplication() {
        // CHANGE START: Delete flow now retries on user errors instead of returning to menu.
        while (true) {
            try {
                String bcNo = io.promptNonEmpty("Enter Birth Certificate Number: ");
                Map<String, String> student = studentRepo.findByBcNo(bcNo);
                if (student == null) throw new NotFoundException("No student found for this Birth Certificate Number.");

                String mobile = safeUpper(student.get("MobileNo"));
                validateOtpForRecovery(mobile);

                List<Map<String, String>> apps = applicantRepo.findAllByBcNo(bcNo);
                if (apps.isEmpty()) throw new NotFoundException("No application found for this student.");

                io.println("Existing Applications:");
                for (int i = 0; i < apps.size(); i++) {
                    Map<String, String> app = apps.get(i);
                    io.println(String.format("  %d) %s | %s | %s", i + 1, app.get("ApplicantID"), areaNameFromPostcode(app.get("SchoolAreaPostCode")), app.get("SubmissionTime")));
                }
                int pick = io.promptInt("Select application number to delete: ");
                if (pick < 1 || pick > apps.size()) throw new ValidationException("Invalid selection.");

                Map<String, String> app = apps.get(pick - 1);
                String appId = app.get("ApplicantID");
                if (!io.confirm("Delete application " + appId + "?")) {
                    io.println("Deletion cancelled.");
                    io.hr();
                    return;
                }

                for (Map<String, String> choice : quotaChoiceRepo.findAllByApplicantId(appId)) {
                    ctx.quotaChoiceDB.delete("ChoiceID", choice.get("ChoiceID"));
                }
                ctx.applicantDB.delete("ApplicantID", appId);

                io.println("Application deleted successfully: " + appId);
                io.hr();
                return;
            } catch (BackToMainMenuSignal e) {
                io.println("\nReturned to main menu.");
                io.hr();
                return;
            } catch (ValidationException | NotFoundException e) {
                io.println("ERROR: " + e.getMessage());
            } catch (Exception e) {
                io.println("ERROR: " + e.getMessage());
                io.hr();
                return;
            }
        }
        // CHANGE END
    }

    public void showResultMenu() {
        // CHANGE START: Single result entry-point for teammate integration (StudentID/ApplicantID lookup).
        while (true) {
            try {
                io.println("RESULT LOOKUP");
                io.println("Plug your result-print function here (single flow, no school-wise submenu).");
                String studentOrApplicantId = io.promptNonEmpty("Enter StudentID or ApplicantID: ");
                io.println("Result implementation placeholder for ID: " + studentOrApplicantId);
                io.hr();
                return;
            } catch (BackToMainMenuSignal e) {
                io.println("\nReturned to main menu.");
                io.hr();
                return;
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
        // CHANGE END
    }

    private void validateOtpForRecovery(String mobile) throws WorkflowException {
        sendAndVerifyOtp(mobile);
    }

    private String areaNameFromPostcode(String postcode) {
        Map<String, String> area = pcRepo.findByPostcode(postcode);
        if (area == null) return postcode;
        return safeUpper(area.get("Thana")) + ", " + safeUpper(area.get("District")) + ", " + safeUpper(area.get("Division")) + " [" + safeUpper(area.get("PostCode")) + "]";
    }

    private String chooseFromList(String label, List<String> options) throws WorkflowException {
        if (options.isEmpty()) throw new NotFoundException("No " + label + " found.");
        while (true) {
            try {
                io.println("Select " + label + ":");
                for (int i = 0; i < options.size(); i++) io.println("  " + (i + 1) + ") " + safeUpper(options.get(i)));
                int pick = io.promptInt("Enter " + label + " number: ");
                if (pick < 1 || pick > options.size()) throw new ValidationException("Invalid " + label + " selection.");
                return safeUpper(options.get(pick - 1));
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private static class SeatView {
        String seatId;
        String schoolName;
        String shift;

        SeatView(String seatId, String schoolName, String shift) {
            this.seatId = seatId;
            this.schoolName = schoolName;
            this.shift = shift;
        }
    }

    private List<SeatView> buildSeatViews(List<Map<String, String>> seats) {
        List<SeatView> views = new ArrayList<>();
        for (Map<String, String> seat : seats) {
            String eiin = seat.get("EIIN");
            Map<String, String> sch = schoolRepo.schoolByEiin(eiin);
            String schoolName = sch == null ? ("EIIN-" + eiin) : safeUpper(sch.get("Name"));
            String shift = safeUpper(seat.get("Shift"));
            views.add(new SeatView(safeUpper(seat.get("SeatID")), schoolName, shift));
        }
        return views;
    }

    private void printSeatViews(List<SeatView> views) {
        io.println("NUM || SCHOOL NAME || SHIFT");
        int i = 1;
        for (SeatView v : views) io.println(String.format("%3d || %s || %s", i++, v.schoolName, v.shift));
        io.hr();
    }

    // CHANGE START: Shared school/class selection helpers for vacant seat flow.
    private Map<String, String> selectSchoolInPostcode(String postCode) throws WorkflowException {
        List<Map<String, String>> schools = schoolRepo.schoolsInPostcode(postCode);
        if (schools.isEmpty()) throw new NotFoundException("No schools found in this postcode.");

        io.println("Schools in postcode " + postCode + ":");
        for (int i = 0; i < schools.size(); i++) {
            Map<String, String> s = schools.get(i);
            io.println(String.format("  %d) %s || EIIN: %s", i + 1, safeUpper(s.get("Name")), safeUpper(s.get("EIIN"))));
        }
        while (true) {
            try {
                int pick = io.promptInt("Select school number: ");
                if (pick < 1 || pick > schools.size()) throw new ValidationException("Invalid school selection.");
                return schools.get(pick - 1);
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }

    private int chooseClassInSchool(String eiin) throws WorkflowException {
        while (true) {
            List<Integer> classes = schoolRepo.availableClassesByEiin(eiin);
            if (classes.isEmpty()) throw new NotFoundException("No classes found for this school.");

            io.println("Available Classes:");
            for (int i = 0; i < classes.size(); i++) {
                io.println(String.format("  %d) Class %d", i + 1, classes.get(i)));
            }
            try {
                int pick = io.promptInt("Select class number: ");
                if (pick < 1 || pick > classes.size()) throw new ValidationException("Invalid class selection.");
                return classes.get(pick - 1);
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
            }
        }
    }
    // CHANGE END

    // CHANGE START: Shared OTP helper reused by application/recovery/delete.
    private void sendAndVerifyOtp(String mobile) throws WorkflowException {
        String code = otp.generate6Digit();
        io.println("OTP sent to " + maskPhone(mobile));
        io.println("[Demo OTP: " + code + "]");
        String user = io.promptNonEmpty("Enter OTP: ");
        if (!code.equals(user)) throw new ValidationException("OTP mismatch.");
    }
    // CHANGE END

    private String guardianCardLine(String name, String nid) {
        if (safe(nid).isEmpty()) return "(NOT PROVIDED)";
        return safeUpper(name) + " (NID: " + safeUpper(nid) + ")";
    }

    private String formatAddressLine(Address a) {
        return safeUpper(a.detailed)
                + ", THANA: " + safeUpper(a.thana)
                + " (" + safeUpper(a.postCode) + ")"
                + ", DISTRICT: " + safeUpper(a.district)
                + ", DIVISION: " + safeUpper(a.division);
    }

    private String maskPhone(String mobile) {
        String digits = mobile.replaceAll("\\D", "");
        String tail = digits.length() >= 3 ? digits.substring(digits.length() - 3) : digits;
        return "*****" + tail;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static String safeUpper(String s) { return safe(s).toUpperCase(Locale.ROOT); }

    private Map<String, String> toUpperMap(Map<String, String> in) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : in.entrySet()) out.put(e.getKey(), safeUpper(e.getValue()));
        return out;
    }

    private void lockIfPresent(ApplicationForm f, String key, String value) {
        if (value != null && !value.trim().isEmpty()) f.lockedFields.add(key);
    }
}
