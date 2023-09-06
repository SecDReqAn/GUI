package general.entities;

import org.jetbrains.annotations.NotNull;

/**
 * Data type associating an analysis output log with a unique title.
 */
public class AnalysisResult implements Cloneable {
    /**
     * The title of the {@link AnalysisResult} (by default the current date and time in the format dd.mm.yy hh:mm:ss).
     *
     * <p>
     * Note: The title attribute uniquely identifies an {@link AnalysisResult} within a {@link Configuration}
     * (i.e., is is strictly forbidden for two {@link AnalysisResult}s to share the same title).
     * </p>
     */
    private @NotNull String title;
    /**
     * The output log received from of the analysis.
     */
    private @NotNull String result;

    /**
     * Default constructor as <b>required</b> for Jackson deserialization.
     */
    @SuppressWarnings("unused")
    private AnalysisResult() {
        title = result = "";
    }

    /**
     * Initializing constructor.
     *
     * <p><b>Note</b>: <code>title</code> has to be unique within a {@link Configuration}</p>
     *
     * @param title  The <code>title</code> that should be set.
     * @param result The <code>result</code> log that should be set.
     */
    public AnalysisResult(@NotNull String title, @NotNull String result) {
        this.title = title;
        this.result = result;
    }

    /**
     * Gets the <code>title</code> of this {@link AnalysisResult}.
     *
     * @return The <code>title</code> of this {@link AnalysisResult}.
     */
    public @NotNull String getTitle() {
        return title;
    }

    /**
     * Sets the <code>title</code> of this {@link AnalysisResult}.
     *
     * @param title The <code>title</code> that should be set. (<b>Note</b>: Has to be unique within a
     *              {@link Configuration}).
     */
    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    /**
     * Gets the <code>result</code> of this {@link AnalysisResult}.
     *
     * @return The <code>result</code> log of this {@link AnalysisResult}.
     */
    public @NotNull String getResult() {
        return result;
    }

    /**
     * Clones this {@link AnalysisResult} instance.
     *
     * @return The created clone.
     */
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
