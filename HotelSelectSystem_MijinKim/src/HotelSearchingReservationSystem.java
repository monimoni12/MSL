import java.sql.*;
import java.util.Scanner;

public class HotelSearchingReservationSystem {

    static final String DB_URL = "jdbc:mysql://localhost:3306/team_MijinKim";
    static final String USER = "root";
    static final String PASS = "";

    public static void main(String[] args) {
        try {
            // 드라이버 로드
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 연결
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);

            // 나머지 코드
            while (true) {
                displayMenu();
                Scanner scanner = new Scanner(System.in);
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        searchCustomer(conn);
                        break;
                    case 2:
                        searchReservation(conn);
                        break;
                    case 3:
                        searchCheckInDate(conn);
                        break;
                    case 4:
                        searchRoomType(conn);
                        break;
                    case 5:
                        searchRoomAvailability(conn);
                        break;
                    case 6:
                        return; // 프로그램 종료
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    static void displayMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Search Customer");
        System.out.println("2. Search Reservation");
        System.out.println("3. Search Check-In Date");
        System.out.println("4. Search Room Type");
        System.out.println("5. Search Room Availability");
        System.out.println("6. Exit");
        System.out.print("Enter your choice: ");
    }

    static int getCustomerIndex(Connection conn) throws SQLException {
        String sql = "SELECT MAX(`Index`) FROM Customers";
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        int index = 1;
        if (resultSet.next()) {
            index = resultSet.getInt(1) + 1;
        }
        return index;
    }

    static void searchCustomer(Connection conn) {
        try {
        	Scanner scanner = new Scanner(System.in);
            System.out.print("Enter CustomerName to search (use * to show all): ");
            String searchName = scanner.nextLine();

            String sql;
            if ("*".equals(searchName)) {
                sql = "SELECT CustomerID, CustomerName, DateOfBirth, Address, PhoneNumber, `Index` FROM Customers";
            } else {
                sql = "SELECT CustomerID, CustomerName, DateOfBirth, Address, PhoneNumber, `Index` FROM Customers WHERE CustomerName = ?";
            }

            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            if (!"*".equals(searchName)) {
                preparedStatement.setString(1, searchName);
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("Customer Details:");
            System.out.println("-----------------------------------------------------------------------------------------------------------");
            System.out.printf("%-15s | %-20s | %-25s | %-10s | %-15s | %-10s\n",
                    "CustomerID", "CustomerName", "DateOfBirth", "Address", "PhoneNumber", "Index");
            System.out.println("-----------------------------------------------------------------------------------------------------------");
            while (resultSet.next()) {
                String customerID = resultSet.getString("CustomerID");
                String customerName = resultSet.getString("CustomerName");
                String dateOfBirth = resultSet.getString("DateOfBirth");
                String address = resultSet.getString("Address");
                String phoneNumber = resultSet.getString("PhoneNumber");
                int index = resultSet.getInt("Index");
                System.out.printf("%-15s | %-20s | %-25s | %-10s | %-15s | %-10s\n",
                                    customerID, customerName, dateOfBirth, address, phoneNumber, index);
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void searchReservation(Connection conn) {
        try {
        	Scanner scanner = new Scanner(System.in);
            System.out.print("Enter CustomerID or CustomerName to search reservation (use * to show all): ");
            String search = scanner.nextLine();

         // 뷰 생성
            String createViewSQL;
            if ("*".equals(search)) {
                createViewSQL = "CREATE OR REPLACE VIEW SearchReservation AS " +
                        "SELECT r.ReservationID, r.CheckInDate, r.CheckOutDate, r.PeopleNum, r.TotalPrice, c.CustomerID, " +
                        "rt.RoomTypeName, rt.BedType " +
                        "FROM Reservation r " +
                        "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                        "JOIN Room rm ON r.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID";
            } else {
                createViewSQL = "CREATE OR REPLACE VIEW SearchReservation AS " +
                        "SELECT r.ReservationID, r.CheckInDate, r.CheckOutDate, r.PeopleNum, r.TotalPrice, c.CustomerID, " +
                        "rt.RoomTypeName, rt.BedType " +
                        "FROM Reservation r " +
                        "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                        "JOIN Room rm ON r.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                        "WHERE c.CustomerID = ? OR c.CustomerName = ?";
            }

            PreparedStatement createViewStmt = conn.prepareStatement(createViewSQL);
            if (!"*".equals(search)) {
                createViewStmt.setString(1, search);
                createViewStmt.setString(2, search);
            }
            createViewStmt.executeUpdate(); // executeUpdate를 사용하여 뷰 생성
            
            // 뷰 생성 여부 확인
            String checkViewSQL = "SELECT * FROM information_schema.VIEWS WHERE TABLE_NAME = 'SearchReservation'";
            Statement checkViewStmt = conn.createStatement();
            ResultSet checkViewResultSet = checkViewStmt.executeQuery(checkViewSQL);
            
            if (checkViewResultSet.next()) {
                System.out.println("View 'SearchReservation' created successfully.");
            } else {
                System.out.println("Failed to create view 'SearchReservation'.");
            }

            // 뷰 조회
            String selectSQL = "SELECT * FROM SearchReservation";
            Statement selectStmt = conn.createStatement();
            ResultSet resultSet = selectStmt.executeQuery(selectSQL);

            if (resultSet.next()) {
                System.out.println("Reservation Details:");
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-15s | %-10s | %-20s | %-20s | %-10s | %-10s | %-15s | %-10s\n",
                        "ReservationID","CustomerID" ,"CheckInDate", "CheckOutDate", "PeopleNum", "TotalPrice", "RoomTypeName", "BedType");
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                do {
                    int reservationID = resultSet.getInt("ReservationID");
                    String customerID = resultSet.getString("CustomerID");
                    String checkInDate = resultSet.getString("CheckInDate");
                    String checkOutDate = resultSet.getString("CheckOutDate");
                    int peopleNum = resultSet.getInt("PeopleNum");
                    int totalPrice = resultSet.getInt("TotalPrice");
                    String roomTypeName = resultSet.getString("RoomTypeName");
                    String bedType = resultSet.getString("BedType");

                    System.out.printf("%-15s | %-10s | %-20s | %-20s | %-10s | %-10s | %-15s | %-10s\n",
                            reservationID, customerID,checkInDate, checkOutDate, peopleNum, totalPrice, roomTypeName, bedType);
                } while (resultSet.next());
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
            } else {
                System.out.println("No reservations found for the given CustomerID or CustomerName.");
            }

            // 뷰 삭제
            String dropViewSQL = "DROP VIEW IF EXISTS SearchReservation";
            Statement dropViewStmt = conn.createStatement();
            dropViewStmt.executeUpdate(dropViewSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void searchCheckInDate(Connection conn) {
        try {
        	Scanner scanner = new Scanner(System.in);
            System.out.print("Enter CheckIn Date (YYYY-MM-DD) to search reservations (use * to show all): ");
            String checkInDate = scanner.nextLine();

            String createViewSQL;
            if ("*".equals(checkInDate)) {
                createViewSQL = "CREATE OR REPLACE VIEW ReservationByCheckIn AS " +
                        "SELECT r.ReservationID, c.CustomerID, c.CustomerName, r.RoomID, rt.RoomTypeName, " +
                        "r.CheckInDate, r.CheckOutDate, r.PeopleNum, r.TotalPrice " +
                        "FROM Reservation r " +
                        "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                        "JOIN Room rm ON r.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID";
            } else {
                createViewSQL = "CREATE OR REPLACE VIEW ReservationByCheckIn AS " +
                        "SELECT r.ReservationID, c.CustomerID, c.CustomerName, r.RoomID, rt.RoomTypeName, " +
                        "r.CheckInDate, r.CheckOutDate, r.PeopleNum, r.TotalPrice " +
                        "FROM Reservation r " +
                        "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                        "JOIN Room rm ON r.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                        "WHERE r.CheckInDate = ?";
            }

            PreparedStatement createViewStmt = conn.prepareStatement(createViewSQL);
            if (!"*".equals(checkInDate)) {
                createViewStmt.setString(1, checkInDate);
            }
            createViewStmt.executeUpdate();
            
         // 뷰 생성 여부 확인
            String checkViewSQL = "SELECT * FROM information_schema.VIEWS WHERE TABLE_NAME = 'ReservationByCheckIn'";
            Statement checkViewStmt = conn.createStatement();
            ResultSet checkViewResultSet = checkViewStmt.executeQuery(checkViewSQL);

            if (checkViewResultSet.next()) {
                System.out.println("View 'ReservationByCheckIn' created successfully.");
            } else {
                System.out.println("Failed to create view 'ReservationByCheckIn'.");
            }

            // 뷰 조회
            String selectSQL = "SELECT * FROM ReservationByCheckIn";
            Statement selectStmt = conn.createStatement();
            ResultSet resultSet = selectStmt.executeQuery(selectSQL);

            if (resultSet.next()) {
                System.out.println("Reservation Details:");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-15s | %-10s | %-15s | %-10s | %-15s | %-20s | %-20s | %-10s\n",
                        "ReservationID", "CustomerID", "CustomerName", "RoomID", "RoomTypeName", "CheckInDate", "CheckOutDate", "PeopleNum", "TotalPrice");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                do {
                    int reservationID = resultSet.getInt("ReservationID");
                    String customerID = resultSet.getString("CustomerID");
                    String customerName = resultSet.getString("CustomerName");
                    int roomID = resultSet.getInt("RoomID");
                    String roomTypeName = resultSet.getString("RoomTypeName");
                    String realCheckInDate = resultSet.getString("CheckInDate");
                    String checkOutDate = resultSet.getString("CheckOutDate");
                    int peopleNum = resultSet.getInt("PeopleNum");
                    int totalPrice = resultSet.getInt("TotalPrice");

                    System.out.printf("%-15s | %-10s | %-15s | %-10s | %-15s | %-20s | %-20s | %-10s\n",
                            reservationID, customerID, customerName, roomID, roomTypeName, realCheckInDate, checkOutDate, peopleNum, totalPrice);
                } while (resultSet.next());
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
            } else {
                System.out.println("No reservations found for the given CheckIn Date.");
            }

            // 뷰 삭제
            String dropViewSQL = "DROP VIEW IF EXISTS ReservationByCheckIn";
            Statement dropViewStmt = conn.createStatement();
            dropViewStmt.executeUpdate(dropViewSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void searchRoomType(Connection conn) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter Room Type Name to search reservations (use * to show all): ");
            String roomTypeName = scanner.nextLine();

            String createViewSQL;
            if ("*".equals(roomTypeName)) {
                createViewSQL = "CREATE OR REPLACE VIEW RoomAndRoomType AS " +
                        "SELECT r.ReservationID, c.CustomerID, c.CustomerName, r.RoomID, rt.RoomTypeName, " +
                        "r.CheckInDate, r.CheckOutDate, r.PeopleNum, r.TotalPrice " +
                        "FROM Reservation r " +
                        "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                        "JOIN Room rm ON r.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID";
            } else {
                createViewSQL = "CREATE OR REPLACE VIEW RoomAndRoomType AS " +
                        "SELECT r.ReservationID, c.CustomerID, c.CustomerName, r.RoomID, rt.RoomTypeName, " +
                        "r.CheckInDate, r.CheckOutDate, r.PeopleNum, r.TotalPrice " +
                        "FROM Reservation r " +
                        "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                        "JOIN Room rm ON r.RoomID = rm.RoomID " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                        "WHERE rt.RoomTypeName = ?";
            }

            PreparedStatement createViewStmt = conn.prepareStatement(createViewSQL);
            if (!"*".equals(roomTypeName)) {
                createViewStmt.setString(1, roomTypeName);
            }
            createViewStmt.executeUpdate();

         // 뷰 생성 여부 확인
            String checkViewSQL = "SELECT * FROM information_schema.VIEWS WHERE TABLE_NAME = 'RoomAndRoomType'";
            Statement checkViewStmt = conn.createStatement();
            ResultSet checkViewResultSet = checkViewStmt.executeQuery(checkViewSQL);

            if (checkViewResultSet.next()) {
                System.out.println("View 'RoomAndRoomType' created successfully.");
            } else {
                System.out.println("Failed to create view 'RoomAndRoomType'.");
            }

            
            // 뷰 조회
            String selectSQL = "SELECT * FROM RoomAndRoomType";
            Statement selectStmt = conn.createStatement();
            ResultSet resultSet = selectStmt.executeQuery(selectSQL);

            if (resultSet.next()) {
                System.out.println("Reservation Details:");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-15s | %-15s | %-10s | %-15s | %-10s | %-20s | %-20s | %-10s\n",
                        "RoomTypeName","ReservationID", "CustomerID", "CustomerName", "RoomID", "CheckInDate", "CheckOutDate", "PeopleNum", "TotalPrice");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                do {
                    int reservationID = resultSet.getInt("ReservationID");
                    String customerID = resultSet.getString("CustomerID");
                    String customerName = resultSet.getString("CustomerName");
                    int roomID = resultSet.getInt("RoomID");
                    String roomType = resultSet.getString("RoomTypeName");
                    String checkInDate = resultSet.getString("CheckInDate");
                    String checkOutDate = resultSet.getString("CheckOutDate");
                    int peopleNum = resultSet.getInt("PeopleNum");
                    int totalPrice = resultSet.getInt("TotalPrice");

                    System.out.printf("%-15s | %-15s | %-10s | %-15s | %-10s | %-20s | %-20s | %-10s\n",
                            roomType, reservationID, customerID, customerName, roomID, checkInDate, checkOutDate, peopleNum, totalPrice);
                } while (resultSet.next());
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
            } else {
                System.out.println("No reservations found.");
            }

            // 뷰 삭제
            String dropViewSQL = "DROP VIEW IF EXISTS RoomAndRoomType";
            Statement dropViewStmt = conn.createStatement();
            dropViewStmt.executeUpdate(dropViewSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void searchRoomAvailability(Connection conn) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter RoomID to search its availability during a period: ");
            int roomID = scanner.nextInt();
            scanner.nextLine();  // Consume newline
            System.out.print("Enter start date (YYYY-MM-DD): ");
            String startDate = scanner.nextLine();
            System.out.print("Enter end date (YYYY-MM-DD): ");
            String endDate = scanner.nextLine();

            String createViewSQL = "CREATE OR REPLACE VIEW RoomAvailability AS " +
                    "SELECT r.ReservationID, c.CustomerID, c.CustomerName, r.RoomID, rt.RoomTypeName, " +
                    "r.CheckInDate, r.CheckOutDate, r.PeopleNum, r.TotalPrice " +
                    "FROM Reservation r " +
                    "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                    "JOIN Room rm ON r.RoomID = rm.RoomID " +
                    "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                    "WHERE r.RoomID = ? AND ((r.CheckInDate < ? AND r.CheckOutDate > ?) OR (r.CheckInDate >= ? AND r.CheckInDate < ?))";

            PreparedStatement createViewStmt = conn.prepareStatement(createViewSQL);
            createViewStmt.setInt(1, roomID);
            createViewStmt.setString(2, endDate);
            createViewStmt.setString(3, startDate);
            createViewStmt.setString(4, startDate);
            createViewStmt.setString(5, endDate);
            createViewStmt.executeUpdate();

         // 뷰 생성 여부 확인
            String checkViewSQL = "SELECT * FROM information_schema.VIEWS WHERE TABLE_NAME = 'RoomAvailability'";
            Statement checkViewStmt = conn.createStatement();
            ResultSet checkViewResultSet = checkViewStmt.executeQuery(checkViewSQL);

            if (checkViewResultSet.next()) {
                System.out.println("View 'RoomAvailability' created successfully.");
            } else {
                System.out.println("Failed to create view 'RoomAvailability'.");
            }

            
            String selectSQL = "SELECT * FROM RoomAvailability";
            Statement selectStmt = conn.createStatement();
            ResultSet resultSet = selectStmt.executeQuery(selectSQL);

            if (resultSet.next()) {
                System.out.println("This room is currently reserved. Reservation details:");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-15s | %-10s | %-10s | %-15s | %-20s | %-20s | %-10s\n",
                        "ReservationID", "CustomerID", "RoomID", "RoomTypeName", "CheckInDate", "CheckOutDate", "PeopleNum", "TotalPrice");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");

                do {
                    int reservationID = resultSet.getInt("ReservationID");
                    String customerID = resultSet.getString("CustomerID");
                    String customerName = resultSet.getString("CustomerName");
                    String roomTypeName = resultSet.getString("RoomTypeName");
                    String checkInDate = resultSet.getString("CheckInDate");
                    String checkOutDate = resultSet.getString("CheckOutDate");
                    int peopleNum = resultSet.getInt("PeopleNum");
                    int totalPrice = resultSet.getInt("TotalPrice");
                    System.out.printf("%-15s | %-10s | %-10s | %-15s | %-20s | %-20s | %-10s\n",
                            reservationID, customerID, roomID, roomTypeName, checkInDate, checkOutDate, peopleNum, totalPrice);                  
                } while (resultSet.next());
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");

                // Fetch available rooms during the same period
                String availableRoomsSQL = "SELECT rm.RoomID, rt.RoomTypeName, rt.BedType, rm.Price " +
                        "FROM Room rm " +
                        "JOIN RoomType rt ON rm.RoomTypeID = rt.RoomTypeID " +
                        "WHERE rm.RoomID NOT IN (" +
                        "  SELECT r.RoomID FROM Reservation r " +
                        "  WHERE (r.CheckInDate < ? AND r.CheckOutDate > ?) OR (r.CheckInDate >= ? AND r.CheckInDate < ?)" +
                        ") AND rm.RoomID != ?";

                PreparedStatement availableRoomsStmt = conn.prepareStatement(availableRoomsSQL);
                availableRoomsStmt.setString(1, endDate);
                availableRoomsStmt.setString(2, startDate);
                availableRoomsStmt.setString(3, startDate);
                availableRoomsStmt.setString(4, endDate);
                availableRoomsStmt.setInt(5, roomID);

                ResultSet availableRoomsResultSet = availableRoomsStmt.executeQuery();

                System.out.println("\nThe following rooms are available for booking during the specified period:");
                System.out.println("---------------------------------------------------");
                System.out.printf("%-10s | %-15s | %-10s | %-10s\n", "RoomID", "RoomTypeName", "BedType", "Price");
                System.out.println("---------------------------------------------------");
                while (availableRoomsResultSet.next()) {
                    int availableRoomID = availableRoomsResultSet.getInt("RoomID");
                    String availableRoomTypeName = availableRoomsResultSet.getString("RoomTypeName");
                    String availableBedType = availableRoomsResultSet.getString("BedType");
                    int availableRoomPrice = availableRoomsResultSet.getInt("Price");

                    System.out.printf("%-10s | %-15s | %-10s | %-10s\n", availableRoomID, availableRoomTypeName, availableBedType, availableRoomPrice);
                }
                System.out.println("---------------------------------------------------");
            } else {
                System.out.println("The room is available for booking.");
            }

            String dropViewSQL = "DROP VIEW IF EXISTS RoomAvailability";
            Statement dropViewStmt = conn.createStatement();
            dropViewStmt.executeUpdate(dropViewSQL);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}