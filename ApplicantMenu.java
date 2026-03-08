public class ApplicantMenu {
    private final ConsoleIO io;
    private final ApplicationWorkflow workflow;

    private static final boolean APPLICATION_WINDOW_OPEN = true;
    private static final boolean RESULT_READY = false;

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
                switch (option) {
                    case 1:
                        ensureWindowOpen();
                        workflow.runSingleApplication();
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
                        if (!RESULT_READY) {
                            io.println("Result module is not ready yet. Enable it after lottery implementation.");
                            io.hr();
                        } else {
                            workflow.showResultMenu();
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
        if (!APPLICATION_WINDOW_OPEN) {
            throw new ValidationException("This module is currently unavailable because the application window has expired.");
        }
    }

    private void showHeader() {
        io.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
        io.println(ANSI_CYAN + "     YAHOO SCHOOL ADMISSION PORTAL" + ANSI_RESET);
        io.println(ANSI_CYAN + "==========================================" + ANSI_RESET);
        io.println("Type \\b during any input to return to this menu.");
    }

    private void showOptions() {
        io.println("1. New application");
        io.println("2. See vacant seat");
        io.println("3. Recover applicant ID");
        io.println("4. Delete application");
        io.println("5. Show result");
        io.println("   i) Show result by ApplicantID or StudentID");
        io.println("   ii) Show a school's result");
        io.println("6. Exit");
    }
}
