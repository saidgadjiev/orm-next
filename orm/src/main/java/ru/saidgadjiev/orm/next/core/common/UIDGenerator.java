package ru.saidgadjiev.orm.next.core.common;

import java.util.concurrent.atomic.AtomicInteger;

public class UIDGenerator {

    private AtomicInteger generator = new AtomicInteger();

    public String nextUID() {
        return "uid:" + generator.getAndIncrement();
    }
}
