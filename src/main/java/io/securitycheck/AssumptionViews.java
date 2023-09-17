package io.securitycheck;

import general.entities.GraphAssumption;

/**
 * Views that can be used to restrict certain
 * {@link GraphAssumption} properties from being serialized by the object mapper.
 */
public class AssumptionViews {
    /**
     * View that contains only the properties potentially relevant for a security analysis.
     */
    public static class SecurityCheckAnalysisView {
    }

    /**
     * View that contains all properties.
     */
    public static class AssumptionGraphAnalysisView extends SecurityCheckAnalysisView {
    }
}
