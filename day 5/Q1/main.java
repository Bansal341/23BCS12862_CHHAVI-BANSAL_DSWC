import java.sql.*;

interface ReportGenerator
{
    void printDelayedReport();
}

abstract class DatabaseRepository
{
    private final String url = "jdbc:postgresql://localhost:5432/cargologix";
    private final String username = "postgres";
    private final String password = "password";

    protected Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(url, username, password);
    }
}

class LogisticsRepository extends DatabaseRepository implements ReportGenerator
{
    @Override
    public void printDelayedReport()
    {
        String sql = """
                SELECT s.shipment_id, c.company_name, s.dispatch_date
                FROM shipments s
                JOIN couriers c ON s.courier_id = c.courier_id
                WHERE s.status = ?
                ORDER BY s.dispatch_date DESC
                """;

        try (
                Connection con = getConnection();
                PreparedStatement ps = con.prepareStatement(sql)
        )
        {
            ps.setString(1, "DELAYED");

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    int shipmentId = rs.getInt("shipment_id");
                    String companyName = rs.getString("company_name");
                    Date dispatchDate = rs.getDate("dispatch_date");

                    System.out.println(
                            shipmentId + " " +
                            companyName + " " +
                            dispatchDate
                    );
                }
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
        ReportGenerator report = new LogisticsRepository();
        report.printDelayedReport();
    }
}
