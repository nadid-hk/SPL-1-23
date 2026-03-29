public class DbContext {
    public final FileDatabase birthDB;
    public final FileDatabase postcodeDB;
    public final FileDatabase schoolAreaDB;
    public final FileDatabase schoolInfoDB;
    public final FileDatabase nidDB;
    public final FileDatabase referenceDB;
    public final FileDatabase classEligibilityDB;
    public final FileDatabase applicantDB;
    public final FileDatabase studentInfoDB;
    public final FileDatabase quotaChoiceDB;

    public DbContext(FileDatabase birthDB,
                     FileDatabase postcodeDB,
                     FileDatabase schoolAreaDB,
                     FileDatabase schoolInfoDB,
                     FileDatabase nidDB,
                     FileDatabase referenceDB,
                     FileDatabase classEligibilityDB,
                     FileDatabase applicantDB,
                     FileDatabase studentInfoDB,
                     FileDatabase quotaChoiceDB) {
        this.birthDB = birthDB;
        this.postcodeDB = postcodeDB;
        this.schoolAreaDB = schoolAreaDB;
        this.schoolInfoDB = schoolInfoDB;
        this.nidDB = nidDB;
        this.referenceDB = referenceDB;
        this.classEligibilityDB = classEligibilityDB;
        this.applicantDB = applicantDB;
        this.studentInfoDB = studentInfoDB;
        this.quotaChoiceDB = quotaChoiceDB;
    }
}
