package org.edu_sharing.plugin_mongo.datamodel.remover;


import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LomRemover extends BaseRemover {

    public LomRemover() {
        functionHooks.put("contribute", this::contributeHandler);
        functionHooks.put("classification", this::classificationHandler);


        functionHooks.put("context", this::rangedValueListHandler);
        functionHooks.put("intendedEndUserRoles", this::rangedValueListHandler);
        functionHooks.put("learningResourceTypes", this::rangedValueListHandler);
        functionHooks.put("curriculum", this::rangedValueListHandler);
        functionHooks.put("typicalLearningTime", this::rangedValueListHandler);
        functionHooks.put("interactivityType", this::rangedValueListHandler);
        functionHooks.put("editorial", this::rangedValueListHandler);
    }

    @SuppressWarnings("unchecked")
    private boolean contributeHandler(Object from, Object to) {
        List<Map<String, Object>> fromList = (List<Map<String, Object>>) from;
        List<Map<String, Object>> toList = (List<Map<String, Object>>) to;

        for (Map<String, Object> fromContributor : fromList) {
            String role = (String) fromContributor.get("role");
            toList.removeIf(x ->  Objects.equals(x.get("role"), role));
        }

        return toList.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private boolean classificationHandler(Object from, Object to) {
        List<Map<String, Object>> fromList = (List<Map<String, Object>>) from;
        List<Map<String, Object>> toList = (List<Map<String, Object>>) to;

        for (Map<String, Object> fromContributor : fromList) {
            String purpose = (String) fromContributor.get("purpose");
            toList.removeIf(x -> Objects.equals(x.get("purpose"), purpose));
        }

        return toList.isEmpty();
    }

    @SuppressWarnings("unchecked")
    private boolean rangedValueListHandler(Object from, Object to) {
        if (scalarHasNullString(from)) {
            List<Map<String, Object>> toList = (List<Map<String, Object>>) to;
            toList.clear();
            return true;
        }

        return false;
    }
}
