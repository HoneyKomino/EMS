package com.ems.employee_management.dto;

import com.ems.employee_management.model.TimeOffRequest;
import java.time.LocalDate;

public class TimeOffForm {

    private LocalDate start;
    private LocalDate end;
    private TimeOffRequest.Type type = TimeOffRequest.Type.VACATION; // default

    /* getters & setters */
    public LocalDate getStart()            { return start; }
    public void setStart(LocalDate start)  { this.start = start; }

    public LocalDate getEnd()              { return end; }
    public void setEnd(LocalDate end)      { this.end = end; }

    public TimeOffRequest.Type getType()            { return type; }
    public void setType(TimeOffRequest.Type type)   { this.type = type; }
}
