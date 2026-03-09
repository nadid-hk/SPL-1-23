import java.util.*;

public class SummaryPrinter {

    public static LinkedHashMap<Integer, String> printAndIndex(ApplicationForm f, ConsoleIO io) {
        io.hr();
        io.println("APPLICATION SUMMARY (numbered for re-edit)");
        io.hr();

        LinkedHashMap<Integer, String> idx = new LinkedHashMap<>();
        int n = 1;

        n = line(idx, io, n, "Birth Certificate No", f.birthCertNo);
        n = line(idx, io, n, "Name (auto)", f.name);
        n = line(idx, io, n, "Date of Birth (auto)", f.dob);
        n = line(idx, io, n, "Gender (auto)", f.gender);

        n = line(idx, io, n, "Religion", f.religion);
        n = line(idx, io, n, "Mobile", f.mobile);

        n = line(idx, io, n, "Father Info", guardianInfo(f.fatherName, f.fatherNid));
        n = line(idx, io, n, "Mother Info", guardianInfo(f.motherName, f.motherNid));
        n = line(idx, io, n, "Local Guardian Info", guardianInfo(f.localGuardianName, f.localGuardianNid));

        n = line(idx, io, n, "Desired Class", String.valueOf(f.desiredClass));

        n = line(idx, io, n, "Present Address", formatAddress(f.present));
        n = line(idx, io, n, "Permanent Address", formatAddress(f.permanent));

        n = line(idx, io, n, "Applying School Area Postcode", f.schoolAreaPostCode);

        io.hr();
        io.println("Choices (max 5):");
        if (f.choices.isEmpty()) {
            io.println("  (no choices yet)");
        } else {
            int rank = 1;
            for (Choice c : f.choices) {
                io.println(String.format("  %d) %s || %s || Quota=%s%s",
                        rank++, c.schoolName, c.shift, 
                        c.quota,
                        (c.refId == null || c.refId.isEmpty()) ? "" : (" (RefID=" + c.refId + ")")));
            }
        }
        io.hr();

        return idx;
    }

    private static int line(LinkedHashMap<Integer, String> idx, ConsoleIO io, int n, String label, String value) {
        io.println(String.format("%2d) %-30s : %s", n, label, value));
        idx.put(n, label);
        return n + 1;
    }

    private static String guardianInfo(String name, String nid) {
        String safeName = safe(name);
        String safeNid = safe(nid);
        if (safeNid.isEmpty()) return "(NOT PROVIDED)";
        return safeName + " (NID: " + safeNid + ")";
    }

    private static String formatAddress(Address a) {
        return safe(a.detailed)
                + ", THANA: " + safe(a.thana)
                + " (" + safe(a.postCode) + ")"
                + ", DISTRICT: " + safe(a.district)
                + ", DIVISION: " + safe(a.division);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
