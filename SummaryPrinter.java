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

        n = line(idx, io, n, "Father NID", f.fatherNid);
        n = line(idx, io, n, "Mother NID", f.motherNid);
        n = line(idx, io, n, "Local Guardian NID", f.localGuardianNid);

        n = line(idx, io, n, "Desired Class", String.valueOf(f.desiredClass));

        n = line(idx, io, n, "Present Address Postcode", f.present.postCode);
        n = line(idx, io, n, "Present Detailed Address", f.present.detailed);
        n = line(idx, io, n, "Permanent Address Postcode", f.permanent.postCode);
        n = line(idx, io, n, "Permanent Detailed Address", f.permanent.detailed);

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
}
