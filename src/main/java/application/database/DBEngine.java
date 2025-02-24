package application.database;

import application.models.Dragon;
import application.models.Element;
import application.models.Rarity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBEngine {

    private Connection connection;

    public DBEngine() {
        connection = connect();
    }

    public boolean isConnected() {
        return (connection != null);
    }

    private Connection connect() {
        String url = "jdbc:mysql://localhost:3306/dragonDB" +
                "?useUnicode=yes&characterEncoding=UTF-8";

        Properties properties = new Properties();
        properties.put("user", System.getenv("DB_USER"));
        properties.put("password", System.getenv("DB_PASSWORD"));

        try {
            return DriverManager.getConnection(url, properties);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Dragon findDragonByName(String searchName) {
        String query = "SELECT * FROM " + DBHelper.TABLE_DRAGON + " WHERE unique_name = ?";

        Dragon result = null;

        try {
            // Statement statement = connection.createStatement();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, searchName);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                // getXXX("column_name_in_DB")
                long id = resultSet.getLong("id");        // resultSet.getLong(1);
                String name = resultSet.getString("unique_name");
                String text = resultSet.getString("dragon_text");
                String rarityFromDB = resultSet.getString("rarity").toUpperCase();
                Rarity rarity = Rarity.valueOf(rarityFromDB);
                // Rarity rarity = Rarity.find(rarityFromDB);

                result = new Dragon(id, name, text, rarity);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Dragon> listAllDragons() {
        String query = "SELECT * FROM " + DBHelper.TABLE_DRAGON;

        List<Dragon> dragons = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                // getXXX("column_name_in_DB")
                long id = resultSet.getLong("id");        // resultSet.getLong(1);
                String name = resultSet.getString("unique_name");
                String text = resultSet.getString("dragon_text");
                String rarityFromDB = resultSet.getString("rarity").toUpperCase();
                Rarity rarity = Rarity.valueOf(rarityFromDB);
                // Rarity rarity = Rarity.find(rarityFromDB);

                Dragon dragon = new Dragon(id, name, text, rarity);
                dragon.setElements(findDragonsElement(id));

                dragons.add(dragon);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return dragons;
    }

    public Element findElementByName(String name) {
        String query = "SELECT * FROM " + DBHelper.TABLE_ELEMENT + " WHERE element_name = ?";

        Element element = null;

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, name);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String elementName = resultSet.getString("element_name");

                element = new Element(elementName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return element;
    }

    public List<Element> findDragonsElement(long dragonId) {
        String query = "SELECT * FROM " + DBHelper.TABLE_DRAGONS_ELEMENT + " WHERE dragon_id = ?";

        List<Element> elements = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setLong(1, dragonId);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                String elementName = resultSet.getString("element_name");
                Element element = findElementByName(elementName);
                elements.add(element);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return elements;
    }

    public boolean addDragonToDB(Dragon dragon) {
        String query = "INSERT INTO dragon (unique_name, dragon_text, rarity) VALUES (?, ?, ?);";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setString(1, dragon.getUniqueName());
            ps.setString(2, dragon.getDragonText());
            ps.setInt(3, dragon.getRarity().getDBIndex());

            ps.executeUpdate();
            ps.close();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
