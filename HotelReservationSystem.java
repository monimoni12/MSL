import java.sql.*;
import java.util.Scanner;

public class HotelReservationSystem {
    
    // Database connection details
    private static final String URL = "jdbc:mysql://localhost:3306/hotel";
    private static final String USER = "testuser";
    private static final String PASSWORD = "testpw";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to the database.");
            
            // Menu options
            System.out.println("Select an option:");
            System.out.println("1. 방 종류별 평균 가격 조회");
            System.out.println("2. 특정 기간 동안 예약된 방 종류별 예약 수와 총 수입");
            System.out.println("3. 예약 날짜별 예약 수 조회");
            System.out.println("4. 특정 고객의 총 예약 수와 총 지출 조회");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    queryAverageRoomPrice(connection, scanner);
                    break;
                case 2:
                    queryReservationsByRoomType(connection, scanner);
                    break;
                case 3:
                    queryReservationsByDate(connection, scanner);
                    break;
                case 4:
                    queryCustomerReservations(connection, scanner);
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    private static void queryAverageRoomPrice(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter room type: ");
        String roomType = scanner.nextLine();

        String sql = "SELECT rt.RoomTypeName, AVG(rm.Price) AS AveragePrice " +
                     "FROM Room rm " +
                     "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                     "WHERE rt.RoomTypeName = ? " +
                     "GROUP BY rt.RoomTypeName";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, roomType);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String typeName = resultSet.getString("RoomTypeName");
                    double averagePrice = resultSet.getDouble("AveragePrice");

                    System.out.println("Room Type: " + typeName);
                    System.out.println("Average Price: " + averagePrice);
                } else {
                    System.out.println("No results found for the specified room type.");
                }
            }
        }
    }

    private static void queryReservationsByRoomType(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter start date (YYYY-MM-DD): ");
        String startDate = scanner.nextLine();
        System.out.print("Enter end date (YYYY-MM-DD): ");
        String endDate = scanner.nextLine();

        String sql = "SELECT rt.RoomTypeName, COUNT(r.ReservationID) AS NumberOfReservations, SUM(r.TotalPrice) AS TotalRevenue " +
                     "FROM Reservation r " +
                     "JOIN Room rm ON r.RoomID = rm.RoomID " +
                     "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                     "WHERE r.CheckInDate BETWEEN ? AND ? " +
                     "GROUP BY rt.RoomTypeName";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, startDate);
            statement.setString(2, endDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String roomTypeName = resultSet.getString("RoomTypeName");
                    int numberOfReservations = resultSet.getInt("NumberOfReservations");
                    double totalRevenue = resultSet.getDouble("TotalRevenue");

                    System.out.println("Room Type: " + roomTypeName);
                    System.out.println("Number of Reservations: " + numberOfReservations);
                    System.out.println("Total Revenue: " + totalRevenue);
                }
            }
        }
    }

    private static void queryReservationsByDate(Connection connection, Scanner scanner) throws SQLException {
        System.out.print("Enter check-in date (YYYY-MM-DD): ");
        String checkInDate = scanner.nextLine();

        String sql = "SELECT r.CheckInDate, COUNT(*) AS NumberOfReservations " +
                     "FROM Reservation r " +
                     "WHERE r.CheckInDate = ? " +
                     "GROUP BY r.CheckInDate";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, checkInDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String date = resultSet.getString("CheckInDate");
                    int numberOfReservations = resultSet.getInt("NumberOfReservations");

                    System.out.println("Check-In Date: " + date);
                    System.out.println("Number of Reservations: " + numberOfReservations);
                } else {
                    System.out.println("No reservations found for the specified date.");
               
