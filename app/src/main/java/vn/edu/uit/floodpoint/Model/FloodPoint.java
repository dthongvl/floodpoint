package vn.edu.uit.floodpoint.Model;


import java.util.Date;

public class FloodPoint {
    private String name;
    private int level;
    private String comment;
    private String imageUrl;
    private double latitude;
    private double longitude;
    private Date expiredDate;

    public FloodPoint() {
    }

    public FloodPoint(String name, int level, String comment, double latitude, double longitude, Date expiredDate) {
        this.name = name;
        this.level = level;
        this.comment = comment;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Date getExpiredDate() {
        return expiredDate;
    }

    public void setExpiredDate(Date expiredDate) {
        this.expiredDate = expiredDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
