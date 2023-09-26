package com.ftl.collector.model.event;

import lombok.Data;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class EmulatorEvent extends ApplicationEvent {

    public EmulatorEvent(Object source) {
        super(source);
    }
}
