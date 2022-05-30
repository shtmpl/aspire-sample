package me.sample.domain;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.function.Consumer;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ImportTask implements Runnable {
    MutableInt percent;
    Consumer<MutableInt> task;

    public static ImportTask of(Consumer<MutableInt> task) {
        return new ImportTask(task);
    }

    private ImportTask(Consumer<MutableInt> task) {
        this.task = task;
        this.percent = new MutableInt();
    }

    @Override
    public void run() {
        task.accept(percent);
    }

    public Integer getPercent() {
        return percent.getValue();
    }
}
