package lk.ijse.dep10.app.model;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lk.ijse.dep10.app.util.Gender;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;

public class Student implements Serializable {
    private String id;
    private String name;
    private String address;
    private String contact;
    private Gender gender;
    private Blob picture;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Student() {
    }

    public Student(String id, String name, String address, String contact, Gender gender, Blob picture) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.contact = contact;
        this.gender = gender;
        this.picture = picture;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Blob getPicture() {
        return picture;
    }

    public void setPicture(Blob picture) {
        this.picture = picture;
    }

    public ImageView getStudentPicture(){
        ImageView imageView = null;
        try {
            InputStream is = picture.getBinaryStream();
            Image image = new Image(is);
            imageView = new ImageView(image);
            imageView.setFitWidth(30);
            imageView.setFitHeight(36);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return imageView;
    }
}
