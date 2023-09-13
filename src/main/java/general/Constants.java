package general;

public class Constants {
    public static final String USER_HOME_PATH = System.getProperty("user.home");
    public static final String FILE_SYSTEM_SEPARATOR = System.getProperty("file.separator");

    public static final String APPLICATION_NAME = "Assumption Analyzer";
    public static final String DEFAULT_SAVE_FILE_NAME = "NewAssumptionSet.json";
    public static final String DEFAULT_SAVE_LOCATION = Constants.USER_HOME_PATH + Constants.FILE_SYSTEM_SEPARATOR + Constants.DEFAULT_SAVE_FILE_NAME;
    public static final String DEFAULT_STAGE_TITLE = Constants.APPLICATION_NAME + " — " + Constants.DEFAULT_SAVE_FILE_NAME;
    public static final String DEFAULT_ANALYSIS_PATH = "http://localhost:2406/";
    public static final int DEFAULT_ANALYSIS_GRAPH_API_PORT = 2407;
    public static final String DOCUMENTATION_URL = "https://git.scc.kit.edu/i43/stud/praktika/sose2023/timnorbertbaechle";

    public static final String CONNECTION_SUCCESS_TEXT = "✓";
    public static final String CONNECTION_FAILURE_TEXT = "❌";
}
