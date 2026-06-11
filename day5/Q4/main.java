import java.sql.*;
import java.time.LocalDateTime;

interface TelemetryService
{
    void printLatestLocations();
}

abstract class FleetDatabaseConnection
{
    private final String url = "jdbc:postgresql://localhost:5432/fleetdb";
    private final String username = "postgres";
    private final String password = "password";

    protected Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(url, username, password);
    }
}

class FleetRepository extends FleetDatabaseConnection implements TelemetryService
{
    @Override
    public void printLatestLocations()
    {
        String query =
                "WITH latest_pings AS (" +
                "SELECT ping_id, rider_id, latitude, longitude, recorded_at, " +
                "ROW_NUMBER() OVER (PARTITION BY rider_id ORDER BY recorded_at DESC) AS rn " +
                "FROM gps_pings ) " +
                "SELECT r.rider_name, r.bike_model, lp.latitude, lp.longitude, lp.recorded_at " +
                "FROM riders r " +
                "INNER JOIN latest_pings lp ON r.rider_id = lp.rider_id " +
                "WHERE lp.rn = 1";

        try (
                Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        )
        {
            while (rs.next())
            {
                String riderName = rs.getString("rider_name");
                String bikeModel = rs.getString("bike_model");
                double latitude = rs.getDouble("latitude");
                double longitude = rs.getDouble("longitude");
                LocalDateTime recordedAt =
                        rs.getObject("recorded_at", LocalDateTime.class);

                System.out.println(
                        riderName + " " +
                        bikeModel + " " +
                        latitude + " " +
                        longitude + " " +
                        recordedAt
                );
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}

public class Main
{
    public static void main(String[] args)
    {
        TelemetryService service = new FleetRepository();
        service.printLatestLocations();
    }
}
