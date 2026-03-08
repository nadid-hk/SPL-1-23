// Keep exceptions in one file (default package) to simplify compiling with javac *.java.

class WorkflowException extends Exception {
    public WorkflowException(String message) { super(message); }
}

class ValidationException extends WorkflowException {
    public ValidationException(String message) { super(message); }
}

class NotFoundException extends WorkflowException {
    public NotFoundException(String message) { super(message); }
}

class LockedFieldException extends WorkflowException {
    public LockedFieldException(String message) { super(message); }
}

class RestartWorkflowException extends WorkflowException {
    public RestartWorkflowException(String message) { super(message); }
}

class BackToMainMenuSignal extends RuntimeException {
    public BackToMainMenuSignal() { super("Returned to main menu."); }
}
