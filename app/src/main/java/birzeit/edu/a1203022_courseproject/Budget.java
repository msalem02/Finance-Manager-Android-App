package birzeit.edu.a1203022_courseproject;


public class Budget {
    private long id;
    private String userEmail;
    private long categoryId;
    private int month;
    private int year;
    private double limit;
    private String categoryName;
    private double spent;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public double getLimit() { return limit; }
    public void setLimit(double limit) { this.limit = limit; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public double getSpent() { return spent; }
    public void setSpent(double spent) { this.spent = spent; }
}
