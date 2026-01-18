package birzeit.edu.a1203022_courseproject;


public class Category {
    private long id;
    private String name;
    private String type;
    private String userEmail;
    public Category(long id, String name, String type, String userEmail) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.userEmail = userEmail;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getUserEmail() { return userEmail; }

    public void setId(long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    @Override
    public String toString() {
        return name;
    }
}
