package org.advancedproductivity.gable.framework.thread;

import com.fasterxml.jackson.databind.node.ObjectNode;

public interface ThreadListener {
    void onFinished(ObjectNode item);
}
