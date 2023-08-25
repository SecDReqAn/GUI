package general.entities;

import org.jetbrains.annotations.NotNull;

public class AnalysisResult implements Cloneable {
    /**
     * The title of the {@link AnalysisResult} (by default the current date and time in the format dd.mm.yy hh:mm:ss).
     *
     * <p>
     * Note: The title attribute uniquely identifies an {@link AnalysisResult} within a {@link Configuration} (i.e., is is strictly forbidden for two {@link AnalysisResult}s to share the same title).
     * </p>
     */
    private @NotNull String title;
    private @NotNull String result;

    /**
     * Default constructor as <b>required</b> for Jackson deserialization.
     */
    @SuppressWarnings("unused")
    private AnalysisResult() {
        title = result = "";
    }

    public AnalysisResult(@NotNull String title, @NotNull String result) {
        this.title = title;
        this.result = result;
    }

    public @NotNull String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title){
        this.title = title;
    }

    public @NotNull String getResult() {
        return result;
    }

    @Override
    public @NotNull AnalysisResult clone() {
        try {
            AnalysisResult clone = (AnalysisResult) super.clone();

            clone.title = this.title;
            clone.result = this.result;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
