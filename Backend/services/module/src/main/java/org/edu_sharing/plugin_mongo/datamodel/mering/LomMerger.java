package org.edu_sharing.plugin_mongo.datamodel.mering;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LomMerger extends BaseMerger {

    public LomMerger() {
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
    private Result contributeHandler(Object from, Object to) {
        List<Map<String, Object>> fromList = (List<Map<String, Object>>) from;
        List<Map<String, Object>> toList = (List<Map<String, Object>>) to;

        Map<String, Map> toContributors = toList.stream().collect(Collectors.toMap(x->(String)x.get("role"), x->x));
        for (Map<String, Object> fromContributor : fromList){
            String role = (String) fromContributor.get("role");
            Result diff = mergHelper(fromContributor, toContributors.get(role));
            if(!diff.isEmpty()) {
                toList.add(fromContributor);
            }
        }

        return Result.empty();
    }

    @SuppressWarnings("unchecked")
    private Result classificationHandler(Object from, Object to) {
        List<Map<String, Object>> fromList = (List<Map<String, Object>>) from;
        List<Map<String, Object>> toList = (List<Map<String, Object>>) to;

        Map<String, Map> toContributors = toList.stream().collect(Collectors.toMap(x->(String)x.get("purpose"), x->x));

        for (Map<String, Object> fromContributor : fromList){
            String purpose = (String) fromContributor.get("purpose");
            Result diff = mergHelper(fromContributor, toContributors.get(purpose));
            if(!diff.isEmpty()) {
                toList.add(fromContributor);
            }
        }

        return Result.empty();
    }

    @SuppressWarnings("unchecked")
    private Result rangedValueListHandler(Object from, Object to){
        List<Map<String, Object>> fromList = (List<Map<String, Object>>) from;
        List<Map<String, Object>> toList = (List<Map<String, Object>>) to;

        toList.clear();
        toList.addAll(fromList);

        return Result.empty();
    }

}
