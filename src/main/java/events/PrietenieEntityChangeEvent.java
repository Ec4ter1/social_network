package events;

import domain.Entity;
import domain.Prietenie;
import domain.Utilizator;

public class PrietenieEntityChangeEvent implements observer.Event {
    private ChangeEventType type;

    public PrietenieEntityChangeEvent(ChangeEventType type) {
        this.type = type;
    }

    public ChangeEventType getType() {
        return type;
    }

}