import java.sql.*;
import java.util.Scanner;

public class HotelReservationSystem {

    // 데이터베이스 연결 세부 사항
    private static final String URL = "jdbc:mysql://localhost:3306/hotel";
    private static final String USER = "testuser";
    private static final String PASSWORD = "testpw";

    // 메인 메소드: 프로그램의 진입점
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("데이터베이스에 연결되었습니다.");

            boolean exit = false;
            while (!exit) {
                // 메뉴 옵션
                System.out.println("메뉴를 선택하세요:");
                System.out.println("1. 방의 타입에 따라 최저, 평균 가격을 조회합니다.");
                System.out.println("2. 방의 인원 수에 따라 최저, 평균 가격을 조회합니다.");
                System.out.println("3. 방의 침대 타입에 따라 최저, 평균 가격을 조회합니다.");
                System.out.println("4. 방의 뷰 타입에 따라 최저, 평균 가격을 조회합니다.");
                System.out.println("5. 고객님의 총 예약 수, 방 수, 총 지출을 조회합니다.");
                System.out.println("6. 프로그램을 종료합니다.");

                int choice = scanner.nextInt();
                scanner.nextLine(); // 줄바꿈

                switch (choice) {
                    case 1:
                        queryRoomTypePriceStats(connection, scanner);
                        break;
                    case 2:
                        queryRoomCapacityPriceStats(connection, scanner);
                        break;
                    case 3:
                        queryBedTypePriceStats(connection, scanner);
                        break;
                    case 4:
                        queryViewTypePriceStats(connection, scanner);
                        break;
                    case 5:
                        queryCustomerReservationsByPhone(connection, scanner);
                        break;
                    case 6:
                        exit = true;
                        break;
                    default:
                        System.out.println("잘못된 메뉴를 선택하였습니다.");
                        break;
                }

                // 메뉴 출력 후 한 줄 띄우기
                System.out.println();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    // 방 타입에 따라 최저, 평균 가격을 조회하는 메소드
    private static void queryRoomTypePriceStats(Connection connection, Scanner scanner) {
        try {
            System.out.print("조회할 방 종류를 입력하세요: ");
            String roomType = scanner.nextLine();

            String sql = "SELECT MIN(r.Price) AS LowestPrice, AVG(r.Price) AS AveragePrice " +
                    "FROM Room r " +
                    "JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "WHERE rt.RoomTypeName = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, roomType);

                try (ResultSet resultSet = statement.executeQuery()) {
                    System.out.println("방 종류\t\t\t최저 가격\t\t평균 가격");
                    while (resultSet.next()) {
                        double lowestPrice = resultSet.getDouble("LowestPrice");
                        double averagePrice = resultSet.getDouble("AveragePrice");

                        System.out.printf("%-20s%-20.2f%-20.2f\n", roomType, lowestPrice, averagePrice);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 방 인원에 따라 최저, 평균 가격을 조회하는 메소드
    private static void queryRoomCapacityPriceStats(Connection connection, Scanner scanner) {
        try {
            System.out.print("조회할 방 인원을 입력하세요: ");
            int capacity = scanner.nextInt();
            scanner.nextLine(); // 줄바꿈 문자 소비

            String sql = "SELECT MIN(r.Price) AS LowestPrice, AVG(r.Price) AS AveragePrice " +
                    "FROM Room r " +
                    "JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "WHERE rt.Capacity = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, capacity);

                try (ResultSet resultSet = statement.executeQuery()) {
                    System.out.println("방 인원\t\t\t최저 가격\t\t평균 가격");
                    while (resultSet.next()) {
                        double lowestPrice = resultSet.getDouble("LowestPrice");
                        double averagePrice = resultSet.getDouble("AveragePrice");

                        System.out.printf("%-20s%-20.2f%-20.2f\n", capacity, lowestPrice, averagePrice);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 침대 타입에 따라 방의 최저, 평균 가격을 조회하는 메소드
    private static void queryBedTypePriceStats(Connection connection, Scanner scanner) {
        try {
            System.out.print("조회할 침대 종류를 입력하세요: ");
            String bedType = scanner.nextLine();

            String sql = "SELECT MIN(r.Price) AS LowestPrice, AVG(r.Price) AS AveragePrice " +
                    "FROM Room r " +
                    "JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "WHERE rt.BedType = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, bedType);

                try (ResultSet resultSet = statement.executeQuery()) {
                    System.out.println("침대 종류\t\t최저 가격\t\t평균 가격");
                    while (resultSet.next()) {
                        double lowestPrice = resultSet.getDouble("LowestPrice");
                        double averagePrice = resultSet.getDouble("AveragePrice");

                        System.out.printf("%-20s%-20.2f%-20.2f\n", bedType, lowestPrice, averagePrice);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 방의 뷰에 따라 최저, 평균 가격을 조회하는 메소드
    private static void queryViewTypePriceStats(Connection connection, Scanner scanner) {
        try {
            System.out.print("조회할 view 종류를 입력하세요: ");
            String viewType = scanner.nextLine();

            String sql = "SELECT MIN(r.Price) AS LowestPrice, AVG(r.Price) AS AveragePrice " +
                    "FROM Room r " +
                    "JOIN RoomType rt ON r.RoomTypeID = rt.RoomTypeID " +
                    "WHERE rt.View = ?";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, viewType);

                try (ResultSet resultSet = statement.executeQuery()) {
                    System.out.println("View 종류\t\t최저 가격\t\t평균 가격");
                    while (resultSet.next()) {
                        double lowestPrice = resultSet.getDouble("LowestPrice");
                        double averagePrice = resultSet.getDouble("AveragePrice");

                        System.out.printf("%-20s%-20.2f%-20.2f\n", viewType, lowestPrice, averagePrice);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // 특정 고객의 총 예약 수와 총 예약한 방 수, 총 지출을 조회하는 메소드 (고객 전화번호로 검색)
    private static void queryCustomerReservationsByPhone(Connection connection, Scanner scanner) {
        try {
            System.out.print("고객 전화번호를 입력하세요: ");
            String phoneNumber = scanner.nextLine();

            String sql = "SELECT g.CustomerName, COUNT(r.ReservationID) AS NumberOfReservations, " +
                    "COUNT(DISTINCT r.RoomID) AS NumberOfRooms, SUM(r.TotalPrice) AS TotalSpent " +
                    "FROM Reservation r " +
                    "JOIN Customers g ON r.CustomerID = g.CustomerID " +
                    "WHERE g.PhoneNumber = ? " +
                    "GROUP BY g.CustomerName";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, phoneNumber);

                try (ResultSet resultSet = statement.executeQuery()) {
                    System.out.println("고객 이름\t\t총 예약 수\t\t총 예약한 방 수\t\t총 지출");
                    while (resultSet.next()) {
                        String name = resultSet.getString("CustomerName");
                        int numberOfReservations = resultSet.getInt("NumberOfReservations");
                        int numberOfRooms = resultSet.getInt("NumberOfRooms");
                        double totalSpent = resultSet.getDouble("TotalSpent");

                        System.out.printf("%-20s%-20d%-20d%-20.2f\n", name, numberOfReservations, numberOfRooms, totalSpent);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
