package lk.ijse.dep10.app.db;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection instance;
    private final Connection connection;

    private DBConnection(){
        try {
            File file = new File("application.properties");
            Properties properties = new Properties();
            FileReader fr = new FileReader(file);
            properties.load(fr);
            fr.close();

            String host = properties.getProperty("mysql.host", "dep10.lk");
            String port = properties.getProperty("mysql.port", "3306");
            String database = properties.getProperty("mysql.database", "studentManagement");
            String username = properties.getProperty("mysql.username", "root");
            String password = properties.getProperty("mysql.password", "");

            String queryString = "createDatabaseIfNotExist=true&allowMultiQueries=true";
            String url = "jdbc:mysql://" +host+ ":" +port+ "/" +database+ "?" + queryString;

            connection = DriverManager.getConnection(url, username, password);

        } catch (FileNotFoundException e) {
            new Alert(Alert.AlertType.ERROR, "Configuration file doesn't exist").showAndWait();
            throw new RuntimeException(e);

        } catch (IOException e){
            new Alert(Alert.AlertType.ERROR, "Failed to read configurations").showAndWait();
            throw new RuntimeException(e);

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to establish connection, try again. I the problem persist please contact technical team").showAndWait();
            throw new RuntimeException(e);
        }
    }

    public static DBConnection getInstance(){
        return (instance == null)? instance = new DBConnection() : instance;
    }

    public Connection getConnection(){
        return connection;
    }
}
