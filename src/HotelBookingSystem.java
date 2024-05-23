import java.sql.*;
import java.util.Scanner;

public class HotelBookingSystem {

    private static Connection connect() {
        String url = "jdbc:mysql://localhost:3306/HotelBookingService";
        String user = "testuser";
        String password = "testpw";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로딩 실패");
            return null;
        } catch (SQLException e) {
            System.out.println("서버 연결 실패");
            return null;
        }
    }

    public static void insertReservation(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter Customer ID:");
            String customerID = scanner.nextLine();

            System.out.println("Enter Room ID:");
            int roomID = scanner.nextInt();
            scanner.nextLine(); // consume newline

            System.out.println("Enter Check-In Date (YYYY-MM-DD):");
            String checkInDate = scanner.nextLine();

            System.out.println("Enter Check-Out Date (YYYY-MM-DD):");
            String checkOutDate = scanner.nextLine();

            System.out.println("Enter Total Price:");
            int totalPrice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            System.out.println("Is Breakfast Included? (true/false):");
            boolean isBreakfast = scanner.nextBoolean();
            scanner.nextLine(); // consume newline

            if (isRoomAvailable(connection, roomID, checkInDate, checkOutDate)) {
                if (isBreakfast) {
                    totalPrice += 10000; // Add breakfast cost
                }

                String sql = "INSERT INTO Reservation (CustomerID, RoomID, CheckInDate, CheckOutDate, TotalPrice, IsBreakfast) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.setString(1, customerID);
                    pstmt.setInt(2, roomID);
                    pstmt.setString(3, checkInDate);
                    pstmt.setString(4, checkOutDate);
                    pstmt.setInt(5, totalPrice);
                    pstmt.setBoolean(6, isBreakfast);

                    int affectedRows = pstmt.executeUpdate();
                    if (affectedRows > 0) {
                        System.out.println("Reservation inserted successfully.");
                    } else {
                        System.out.println("Failed to insert reservation.");
                    }
                }
            } else {
                System.out.println("Room is not available for the selected dates.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean isRoomAvailable(Connection connection, int roomID, String checkInDate, String checkOutDate) {
        String sql = "SELECT * FROM Reservation WHERE RoomID = ? AND NOT (CheckOutDate <= ? OR CheckInDate >= ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, roomID);
            pstmt.setString(2, checkInDate);
            pstmt.setString(3, checkOutDate);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return false; // Found a conflicting reservation
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true; // No conflicting reservation found
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = connect();
        if (connection != null) {
            insertReservation(connection, scanner);
        }
    }
}
