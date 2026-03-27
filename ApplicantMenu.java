import java.util.*;

public class ApplicantMenu {
    private final ConsoleIO io;
    private final ApplicationWorkflow workflow;

    // CHANGE START: Result readiness now depends on lottery run status written by admin flow.
    private static final String LOTTERY_STATUS_FILE = "lottery_status.db";
    // CHANGE END

    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_RESET = "\u001B[0m";

    public ApplicantMenu(ConsoleIO io, ApplicationWorkflow workflow) {
        this.io = io;
        this.workflow = workflow;
    }

    public void run() {
        while (true) {
            try {
                showHeader();
                showOptions();
                int option = io.promptInt("Choose option: ");
                boolean resultReady = isResultReady();
                switch (option) {
                    case 1:
                        if(resultReady){
                            io.println("Time's Up! The application window has expired.");
                            io.hr();
                        }
                        else {
                        ensureWindowOpen();
                        workflow.runSingleApplication();
                        }
                        break;
                    case 2:
                        ensureWindowOpen();
                        workflow.showVacantSeats();
                        break;
                    case 3:
                        ensureWindowOpen();
                        workflow.recoverApplicantIds();
                        break;
                    case 4:
                        ensureWindowOpen();
                        workflow.deleteApplication();
                        break;
                    case 5:
                        if (!resultReady) {
                            io.println("Result module is not ready yet!! ");
                            io.hr();
                        } else {
                            new ApplicantResult();
                        }
                        break;
                    case 6:
                        if (io.confirm("Are you sure you want to exit")) {
                            io.println("Exiting program.");
                            return;
                        }
                        break;
                    default:
                        io.println("Invalid option.");
                        io.hr();
                }
            } catch (BackToMainMenuSignal e) {
                io.println("\nReturned to main menu.");
                io.hr();
            } catch (ValidationException e) {
                io.println("ERROR: " + e.getMessage());
                io.hr();
            }
        }
    }

    private void ensureWindowOpen() throws ValidationException {
        if (isResultReady()) {
            throw new ValidationException("This module is currently unavailable because the application window has expired.");
        }
    }

    // CHANGE START
    private boolean isResultReady() {
        FileDatabase statusDB = new FileDatabase(LOTTERY_STATUS_FILE, Arrays.asList("Key", "Value"));
        Map<String, String> row = statusDB.find("Key", "RESULT_READY");
        return row != null && "TRUE".equalsIgnoreCase(row.get("Value"));
    }
    // CHANGE END

    private void showHeader() {
        io.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
        io.println(ANSI_CYAN + "      SCHOOL ADMISSION PORTAL" + ANSI_RESET);
        io.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
        io.println("Type \\b during any input to return to this menu.");
    }

    private void showOptions() {
        io.println("1. New application");
        io.println("2. See vacant seat");
        io.println("3. Recover applicant ID");
        io.println("4. Delete application");
        io.println("5. Show result");
        io.println("6. Exit");
    }
}
