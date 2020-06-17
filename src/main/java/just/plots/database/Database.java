package just.plots.database;

import java.sql.*;

public abstract class Database {

    private Connection connection = null;

    public abstract Connection openConnection() throws SQLException;

    private void checkConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = openConnection();
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkConnection();

        return connection.prepareStatement(sql);
    }

    public void createTables() {
        try {
            checkConnection();

            try (Statement statement = connection.createStatement()) {
                statement.addBatch("CREATE TABLE IF NOT EXISTS justplots_plots ("
                        + "world VARCHAR(45) NOT NULL,"
                        + "x INT NOT NULL,"
                        + "z INT NOT NULL,"
                        + "owner CHAR(36) NOT NULL,"
                        + "creation timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                        + "PRIMARY KEY (world, x, z))");
                statement.addBatch("CREATE TABLE IF NOT EXISTS justplots_added ("
                        + "world VARCHAR(45) NOT NULL,"
                        + "x INT NOT NULL,"
                        + "z INT NOT NULL,"
                        + "uuid CHAR(36) NOT NULL,"
                        + "UNIQUE (world, x, z, uuid))");

                statement.executeBatch();
                statement.clearBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
