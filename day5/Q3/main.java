import java.sql.*;

interface PortfolioManager
{
    void restructurePortfolio(long investorId);
}

abstract class FinancialDatabaseConfig
{
    private final String url = "jdbc:postgresql://localhost:5432/firedb";
    private final String username = "postgres";
    private final String password = "password";

    protected Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(url, username, password);
    }
}

class PortfolioRepository extends FinancialDatabaseConfig implements PortfolioManager
{
    @Override
    public void restructurePortfolio(long investorId)
    {
        String selectQuery =
                "SELECT h.asset_class, SUM(h.current_value) AS total_value " +
                "FROM investors i " +
                "INNER JOIN holdings h ON i.investor_id = h.investor_id " +
                "WHERE i.investor_id = ? " +
                "GROUP BY h.asset_class";

        String updateDebt =
                "UPDATE holdings SET current_value = current_value - ? " +
                "WHERE investor_id = ? AND asset_class = 'Debt'";

        String updateEquity =
                "UPDATE holdings SET current_value = current_value + ? " +
                "WHERE investor_id = ? AND asset_class = 'Equity'";

        double shiftAmount = 50000.0;

        Connection conn = null;

        try
        {
            conn = getConnection();
            conn.setAutoCommit(false);

            try (
                    PreparedStatement ps1 = conn.prepareStatement(selectQuery);
                    PreparedStatement ps2 = conn.prepareStatement(updateDebt);
                    PreparedStatement ps3 = conn.prepareStatement(updateEquity)
            )
            {
                ps1.setLong(1, investorId);

                try (ResultSet rs = ps1.executeQuery())
                {
                    while (rs.next())
                    {
                        System.out.println(
                                rs.getString("asset_class") + " " +
                                rs.getDouble("total_value")
                        );
                    }
                }

                ps2.setDouble(1, shiftAmount);
                ps2.setLong(2, investorId);
                ps2.executeUpdate();

                ps3.setDouble(1, shiftAmount);
                ps3.setLong(2, investorId);
                ps3.executeUpdate();

                conn.commit();
            }
        }
        catch (Exception e)
        {
            try
            {
                if (conn != null)
                {
                    conn.rollback();
                }
            }
            catch (SQLException ex)
            {
                ex.printStackTrace();
            }

            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (conn != null)
                {
                    conn.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }
}

public class Main
{
    public static void main(String[] args)
    {
        PortfolioManager manager = new PortfolioRepository();
        manager.restructurePortfolio(101);
    }
}
