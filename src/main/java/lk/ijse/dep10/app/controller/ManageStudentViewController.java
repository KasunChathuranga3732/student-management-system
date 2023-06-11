package lk.ijse.dep10.app.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import lk.ijse.dep10.app.db.DBConnection;
import lk.ijse.dep10.app.model.Student;
import lk.ijse.dep10.app.util.Gender;

import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.*;

public class ManageStudentViewController {

    public Button btnBrowse;
    public Button btnClear;
    public Button btnDelete;
    public Button btnNewStudent;
    public Button btnSave;
    public ImageView imgStudent;
    public Label lblGender;
    public RadioButton rdoFemale;
    public RadioButton rdoMale;
    public ToggleGroup set;
    public TableView<Student> tblStudent;
    public TextField txtAddress;
    public TextField txtContact;
    public TextField txtId;
    public TextField txtName;
    public TextField txtSearchStudent;


    public void initialize(){
        tblStudent.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("studentPicture"));
        tblStudent.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("id"));
        tblStudent.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudent.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("address"));
        tblStudent.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("contact"));
        tblStudent.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("gender"));



        loadAllStudents();

        tblStudent.getSelectionModel().selectedItemProperty().addListener((ov, prev, current)->{
            btnDelete.setDisable(current == null);

            if (current == null) return;

            txtId.setText(current.getId());
            txtName.setText(current.getName());
            txtAddress.setText(current.getAddress());
            txtContact.setText(current.getContact());
            if (current.getGender() == Gender.MALE) rdoMale.getToggleGroup().selectToggle(rdoMale);
            if (current.getGender() == Gender.FEMALE) rdoFemale.getToggleGroup().selectToggle(rdoFemale);
            Blob picture = current.getPicture();

            if(picture != null) {
                try {
                    Image image = new Image(picture.getBinaryStream());
                    imgStudent.setImage(image);
                    btnClear.setDisable(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }else{
                btnClear.fire();
            }
        });

    }

    private void loadAllStudents() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM Student");
            PreparedStatement stmPicture = connection.prepareStatement(
                    "SELECT * FROM Picture WHERE student_id = ?");

            while (rst.next()){
                String id = rst.getString("id");
                String name = rst.getString("name");
                String address = rst.getString("address");
                String contact = rst.getString("contact");
                String gender = rst.getString("gender");
                Blob picture = null;

                stmPicture.setString(1, id);
                ResultSet rstPicture = stmPicture.executeQuery();
                if(rstPicture.next()){
                    picture = rstPicture.getBlob("picture");
                } else {
                    Image image = new Image("/image/No-photo.jpg");
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ImageIO.write(bufferedImage, "png", bos);

                    byte[] bytes = bos.toByteArray();
                    picture = new SerialBlob(bytes);
                }

                Student student = new Student(id, name, address, contact, Gender.valueOf(gender), picture);
                tblStudent.getItems().add(student);

            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load Students, try again!...").showAndWait();
            Platform.exit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void btnBrowseOnAction(ActionEvent event) throws MalformedURLException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select the Student picture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files",
                "*.jpg", "*.png", "*.jpeg", "*.gif", "*.bmp"));
        File file = fileChooser.showOpenDialog(btnBrowse.getScene().getWindow());
        if(file != null){
            Image image = new Image(file.toURI().toURL().toString(), 200, 200, true, true);
            imgStudent.setImage(image);
            btnClear.setDisable(false);
        }
    }

    
    public void btnClearOnAction(ActionEvent event) {
        Image image = new Image("/image/No-photo.jpg");
        imgStudent.setImage(image);
        btnClear.setDisable(true);
    }

    
    public void btnDeleteOnAction(ActionEvent event) {
        Student selectedStudent = tblStudent.getSelectionModel().getSelectedItem();

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            connection.setAutoCommit(false);
            PreparedStatement stmPicture = connection.prepareStatement(
                    "DELETE FROM Picture WHERE student_id = ?");
            PreparedStatement stmItem = connection.prepareStatement(
                    "DELETE FROM Student WHERE id = ?");

            stmPicture.setString(1, selectedStudent.getId());
            stmPicture.executeUpdate();

            stmItem.setString(1, selectedStudent.getId());
            stmItem.executeUpdate();

            connection.commit();
            tblStudent.getItems().remove(selectedStudent);
            tblStudent.getSelectionModel().clearSelection();
            if(tblStudent.getItems().isEmpty()) btnNewStudent.fire();

        } catch (Throwable e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Failed to delete the Student").show();
        }finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void tblStudentOnKeyReleased(KeyEvent event) {
        if(event.getCode() == KeyCode.DELETE) btnDelete.fire();
    }

    
    public void btnNewStudentOnAction(ActionEvent event) {
        txtId.setText(generateId());
        removeStyleClasses();
        txtName.clear();
        txtAddress.clear();
        txtContact.clear();
        rdoFemale.getToggleGroup().selectToggle(null);
        txtSearchStudent.clear();
        btnClear.fire();
        tblStudent.getSelectionModel().clearSelection();
        txtName.requestFocus();
    }

    private String generateId() {
        ObservableList<Student> studentList = tblStudent.getItems();
        if(studentList.isEmpty()) return "S-001";
        int id = Integer.parseInt(studentList.get(studentList.size() - 1).getId().substring(2));
        String newId = String.format("%s%03d",("S-"), (id + 1));
        return newId;
    }

    
    public void btnSaveOnAction(ActionEvent event) {
        if(!isDataValidate()) return;

        String id = txtId.getText();
        String name = txtName.getText();
        String address = txtAddress.getText();
        String contact = txtContact.getText();
        String gender = rdoMale.isSelected()? Gender.MALE.name():Gender.FEMALE.name();


        Connection connection = DBConnection.getInstance().getConnection();
        Student selectedStudent = tblStudent.getSelectionModel().getSelectedItem();
        int selectedIndex = tblStudent.getSelectionModel().getSelectedIndex();

        if(selectedStudent == null) {
            try {
                connection.setAutoCommit(false);

                PreparedStatement stm = connection.prepareStatement(
                        "INSERT INTO Student(id, name, address, contact, gender) VALUES (?,?,?,?,?)");
                stm.setString(1, id);
                stm.setString(2, name);
                stm.setString(3, address);
                stm.setString(4, contact);
                stm.setString(5, gender);
                stm.executeUpdate();

                Student student = new Student(id, name, address, contact,
                        Gender.valueOf(gender), null);

                if (!btnClear.isDisable()) {
                    PreparedStatement stm2 = connection.prepareStatement
                            ("INSERT INTO Picture (student_id, picture) VALUES (?, ?)");

                    Blob picture = convertPicture();
                    stm2.setString(1, id);
                    stm2.setBlob(2, picture);
                    stm2.executeUpdate();
                    student.setPicture(picture);
                }else{
                    Blob picture = convertPicture();
                    student.setPicture(picture);
                }

                connection.commit();
                tblStudent.getItems().add(student);
                btnNewStudent.fire();

            } catch (Throwable e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to save the student").show();
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        } else{
            try {
                connection.setAutoCommit(false);

                PreparedStatement stm = connection.prepareStatement(
                        "UPDATE Student SET name=?, address=?, contact=?, gender=? WHERE id = ?");
                stm.setString(1, name);
                stm.setString(2, address);
                stm.setString(3, contact);
                stm.setString(4, gender);
                stm.setString(5, id);
                stm.executeUpdate();

                Student student = new Student(id, name, address, contact,
                        Gender.valueOf(gender), null);

                Statement stmPicSelect = connection.createStatement();
                String sql = String.format("SELECT * FROM Picture WHERE student_id = '%s'", id);

                if (!btnClear.isDisable()) {
                    PreparedStatement stm2 = connection.prepareStatement
                            ("INSERT INTO Picture (student_id, picture) VALUES (?, ?)");
                    PreparedStatement stm3 = connection.prepareStatement(
                            "UPDATE Picture SET picture = ? WHERE student_id = ?");

                    Blob picture = convertPicture();
                    student.setPicture(picture);

                    if(!stmPicSelect.executeQuery(sql).next()) {
                        stm2.setString(1, id);
                        stm2.setBlob(2, picture);
                        stm2.executeUpdate();

                    } else {
                        stm3.setBlob(1, picture);
                        stm3.setString(2, id);
                        stm3.executeUpdate();
                    }

                } else if(stmPicSelect.executeQuery(sql).next()){
                    Statement stmDeletePic = connection.createStatement();
                    String sqlDelete = String.format("DELETE FROM Picture WHERE student_id = '%s'", id);
                    stmDeletePic.executeUpdate(sqlDelete);
                    Blob picture = convertPicture();
                    student.setPicture(picture);

                }

                connection.commit();
                tblStudent.getItems().set(selectedIndex, student);
                btnNewStudent.fire();

            } catch (Throwable e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Failed to update the student").show();
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private boolean isDataValidate() {
        boolean isDataValid = true;
        removeStyleClasses();


        if(rdoMale.getToggleGroup().getSelectedToggle()==null){
            isDataValid = false;
            rdoMale.requestFocus();
            lblGender.setTextFill(Color.RED);
        }

        if(!txtContact.getText().matches("\\d{3}-\\d{7}")) {
            txtContact.getStyleClass().add("invalid");
            txtContact.requestFocus();
            isDataValid = false;
        }

        if(txtAddress.getText().isBlank()){
            isDataValid=false;
            txtAddress.getStyleClass().add("invalid");
            txtAddress.requestFocus();
            txtAddress.selectAll();
        }

        if(txtName.getText().isBlank()){
            isDataValid=false;
            txtName.getStyleClass().add("invalid");
            txtName.requestFocus();
            txtName.selectAll();
        }
        return isDataValid;
    }

    private void removeStyleClasses() {
        txtName.getStyleClass().remove("invalid");
        txtAddress.getStyleClass().remove("invalid");
        txtContact.getStyleClass().remove("invalid");
        lblGender.setTextFill(Color.BLACK);
    }

    private Blob convertPicture() throws IOException, SQLException {
        Image image = imgStudent.getImage();
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", bos);

        byte[] bytes = bos.toByteArray();
        return new SerialBlob(bytes);
    }


    public void rdoGentOnAction(ActionEvent event) {

    }
}
