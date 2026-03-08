import java.util.*;

public class ApplicationCLI {
    public static void main(String[] args) {
        DatabaseSetup.initAndSeed();
        ConsoleIO io = new ConsoleIO(new Scanner(System.in));

        DbContext ctx = new DbContext(
                DatabaseSetup.birthDB,
                DatabaseSetup.postcodeDB,
                DatabaseSetup.schoolAreaDB,
                DatabaseSetup.schoolInfoDB,
                DatabaseSetup.nidDB,
                DatabaseSetup.referenceDB,
                DatabaseSetup.classEligibilityDB,
                DatabaseSetup.applicantDB,
                DatabaseSetup.studentInfoDB,
                DatabaseSetup.quotaChoiceDB
        );

        ApplicationWorkflow workflow = new ApplicationWorkflow(ctx, io);
        ApplicantMenu menu = new ApplicantMenu(io, workflow);
        menu.run();
    }
}
