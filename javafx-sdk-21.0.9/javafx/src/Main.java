import java.io.Serializable;

class CareInitiative implements Serializable {
    private String initiativeId;
    private String title;
    private String category;
    private int requiredVolunteers;
    private boolean isActive;

    public CareInitiative() {}

    public String getInitiativeId() {
        return initiativeId;
    }

    public void setInitiativeId(String initiativeId) {
        this.initiativeId = initiativeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getRequiredVolunteers() {
        return requiredVolunteers;
    }

    public void setRequiredVolunteers(int requiredVolunteers) {
        this.requiredVolunteers = requiredVolunteers;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}

public class Main {
    public static void main(String[] args) {
        CareInitiative initiative = new CareInitiative();

        initiative.setInitiativeId("BLR-CARE-01");
        initiative.setTitle("Cubbon Park Clean-up Drive");
        initiative.setCategory("Environment");
        initiative.setRequiredVolunteers(25);
        initiative.setActive(true);

        System.out.println("Initiative: " + initiative.getTitle());
        System.out.println("Category: " + initiative.getCategory());
        System.out.println("Volunteers Needed: " + initiative.getRequiredVolunteers());
        System.out.println("Status: " + (initiative.isActive() ? "Open" : "Closed"));
    }
}