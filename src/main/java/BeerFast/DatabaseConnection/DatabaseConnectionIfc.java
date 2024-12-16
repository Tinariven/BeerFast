package BeerFast.DatabaseConnection;
public interface DatabaseConnectionIfc {
    Object getConnection();
    void disconnect();
}

