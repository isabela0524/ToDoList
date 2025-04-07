package models;

import java.io.Serializable;
import java.time.LocalDate;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private String description;
    private boolean done;
    private LocalDate date;

    public Task(String description) {
        this.description = description;
        this.done = false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    public boolean isDoane() {
        return done;
    }

    public void setDone(boolean done) {
        System.out.println("Se schimbă starea task-ului: " + this.description + " la " + (done ? "finalizat" : "nefinalizat"));
        this.done = done;
    }

    public LocalDate getDate() {
        return date;
    }
    @Override
    public String toString() {
        return description + (done ? " (Done)" : "(Not Done)");
    }
}
