package com.kairos.activity.spec;

import com.kairos.activity.persistence.model.task.Task;

/**
 * Created by prabjot on 28/11/17.
 */
public class MergeTaskSpecification extends AbstractTaskSpecification<Task> {

    private boolean mainTask;

    public MergeTaskSpecification(boolean mainTask) {
        this.mainTask = mainTask;
    }

    @Override
    public boolean isSatisfied(Task task) {
        return mainTask == !task.getSubTaskIds().isEmpty();
    }
}
