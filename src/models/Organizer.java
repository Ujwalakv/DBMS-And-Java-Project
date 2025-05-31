package models;
public class Organizer extends User {
    private String organizerId;
    private String[] eventsManaged;

    public Organizer(String username, String password, String organizerId, String[] eventsManaged) {
        super(username, password, "Organizer");
        this.organizerId = organizerId;
        this.eventsManaged = eventsManaged;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String[] getEventsManaged() {
        return eventsManaged;
    }

    public void setEventsManaged(String[] eventsManaged) {
        this.eventsManaged = eventsManaged;
    }

    public void addEvent(String event) {
        // Logic to add an event to the eventsManaged array
    }

    public void removeEvent(String event) {
        // Logic to remove an event from the eventsManaged array
    }
}