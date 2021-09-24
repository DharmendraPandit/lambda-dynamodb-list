package example;

import com.google.gson.Gson;

import java.util.Date;
import java.util.UUID;

public class Announcement {
    private String id;
    private String title;
    private String description;
    private String date;

    public Announcement(String json) {
        Gson gson = new Gson();
        Announcement request = gson.fromJson(json, Announcement.class);
        this.id = UUID.randomUUID().toString();
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.date = request.getDate();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Announcement{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
