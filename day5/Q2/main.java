import java.sql.*;

interface RegistrationManager
{
    void enrollAtRiskStudents();
}

abstract class DatabaseConnectionProvider
{
    private final String url = "jdbc:postgresql://localhost:5432/edixo";
    private final String username = "postgres";
    private final String password = "password";

    protected Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(url, username, password);
    }
}

class EdixoRegistrationRepository extends DatabaseConnectionProvider implements RegistrationManager
{
    @Override
    public void enrollAtRiskStudents()
    {
        String selectQuery =
                "SELECT s.student_id, s.full_name " +
                "FROM students s " +
                "LEFT JOIN course_registrations cr " +
                "ON s.student_id = cr.student_id " +
                "WHERE cr.student_id IS NULL";

        String insertQuery =
                "INSERT INTO course_registrations(student_id, course_code, semester) " +
                "VALUES (?, ?, ?)";

        try (
                Connection con = getConnection();
                PreparedStatement selectStmt = con.prepareStatement(selectQuery);
                ResultSet rs = selectStmt.executeQuery();
                PreparedStatement insertStmt = con.prepareStatement(insertQuery)
        )
        {
            int count = 0;

            while (rs.next())
            {
                int studentId = rs.getInt("student_id");

                insertStmt.setInt(1, studentId);
                insertStmt.setString(2, "Orientation 101");
                insertStmt.setString(3, "Fall 2026");

                insertStmt.addBatch();
                count++;

                if (count % 1000 == 0)
                {
                    insertStmt.executeBatch();
                }
            }

            insertStmt.executeBatch();
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
        RegistrationManager manager = new EdixoRegistrationRepository();
        manager.enrollAtRiskStudents();
    }
}
