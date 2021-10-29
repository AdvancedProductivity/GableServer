package org.advancedproductivity.gable.framework.thread;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.advancedproductivity.gable.framework.config.IntegrateField;
import org.advancedproductivity.gable.framework.config.IntegrateStepStatus;
import org.advancedproductivity.gable.web.service.IntegrateService;

/**
 * @author zzq
 */
@Slf4j
public class IntegrateEnTrustRun extends Thread {
    private boolean isStop = false;
    private ObjectNode item;
    private ThreadListener listener;

    public IntegrateEnTrustRun(ObjectNode item, ThreadListener listener) {
        this.item = item;
        this.listener = listener;
    }

    public void makeStop(){
        this.isStop = true;
    }

    @Override
    public void run() {
        item.put(IntegrateField.STATUS, IntegrateStepStatus.RUNNING.getValue());
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            item.put(IntegrateField.STATUS, IntegrateStepStatus.SUCCESS.getValue());
        }
        this.listener.onFinished(item);
    }
}
