package org.edu_sharing.plugin_mongo.qa;

import org.edu_sharing.service.qa.domain.QAEntry;

import java.util.List;

public interface QAEntriesOnly {
    List<QAEntry> getEntries();
}
