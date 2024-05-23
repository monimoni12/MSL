import java.sql.*;
import java.util.Scanner;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class MSLHotelService {

    static String DB_URL = "jdbc:mysql://localhost:3306/";
    static String USER = "root";
    static String PASS = "";
    
    static Connection conn;
    
    private static String CustomerID;
	private static int ReservationID;
    
    private static Connection connect() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("(jdbc:mysql://localhost:3306/이_부분_입력) 접속할 주소를 입력하세요: ");
		DB_URL = DB_URL + scanner.nextLine();
		System.out.print("USER를 입력하세요: (Default USER: root)");
		if(scanner.nextLine() != "") {
			USER = scanner.nextLine();
		}
		System.out.print("비밀번호를 입력하세요: (없을 시 엔터)");
		PASS = scanner.nextLine();
	
		System.out.print("주소: " + DB_URL + " USER: " + USER + " Password: " + PASS);
		
		try {
			//드라이버 로드
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			return DriverManager.getConnection(DB_URL, USER, PASS);
		}
		catch(ClassNotFoundException e) {
			System.out.println("드라이버 로딩 실패 ");
			return null;
		} catch (SQLException e) {
			System.out.println("서버 연결 실패 ");
			return null;
		}
	}

    public static void main(String[] args) {
    	connect();
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
		        	insertReservation(conn);
		            break;
		        case 3:
		        	searchReservation(conn);
		            break;
		        case 4:
		        	updateReservation();
		        	break;
		        case 5:
		        	cancelReservation();
		        	break;
		        case 6:
		            searchCheckInDate(conn);
		            break;
		        case 7:
		            searchRoomType(conn);
		            break;
		        case 8:
		            searchRoomAvailability(conn);
		            break;
		        case 9:
		            return; // 프로그램 종료
		        default:
		            System.out.println("Invalid choice. Please try again.");
		    }
		}
    }

    static void displayMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Search Customer");
        System.out.println("2. Make Reservation");
        System.out.println("3. Search Reservation");
        System.out.println("4. Update Reservation");
        System.out.println("5. Cancel Reservation");
        System.out.println("6. Search Check-In Date");
        System.out.println("7. Search Room Type");
        System.out.println("8. Search Room Availability");
        System.out.println("9. Exit");
        System.out.print("Enter your choice: ");
    }

    public static int getCustomerIndex(Connection conn) throws SQLException {
        String sql = "SELECT MAX(`Index`) FROM Customers";
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        int index = 1;
        if (resultSet.next()) {
            index = resultSet.getInt(1) + 1;
        }
        return index;
    }

    //고객 검색
    public static void searchCustomer(Connection conn) {
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

    //예약하기
    public static void insertReservation(Connection conn) {
    	Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Enter Customer ID:");
            String customerID = scanner.nextLine();

            // 고객 ID 존재 여부 확인
            if (!customerExists(conn, customerID)) {
                System.out.println("Customer ID does not exist.");
                return;
            }
            
            System.out.print("Enter Room ID:");
            int roomID = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 소비

          //방값 계산
            int Room_Price = 0;
         // 방 ID 존재 여부 확인
            if (!roomExists(conn, roomID)) {
                System.out.println("Room ID does not exist.");
                return;
            }
            else {
            	String sql_2 = "SELECT Price FROM Room WHERE RoomID = ?";
            	try (PreparedStatement pstmt = conn.prepareStatement(sql_2)) {
    	            pstmt.setInt(1, roomID);
    	            
    	            ResultSet rs = pstmt.executeQuery();
    	            
    	            if (rs.next()) {
    	                
    	                Room_Price = rs.getInt("Price");
    	                
    	            }
    	        } catch (SQLException e) {
    	            e.printStackTrace();
    	        }
    		System.out.println("Room Price per Day:" + Room_Price);
            }
            
            System.out.print("Enter number of people: ");
            int peopleNum = scanner.nextInt();
            scanner.nextLine(); // 개행 문자 소비
            
            System.out.print("Enter Check-In Date (YYYY-MM-DD): ");
            String checkInDate = scanner.nextLine();

            System.out.print("Enter Check-Out Date (YYYY-MM-DD): ");
            String checkOutDate = scanner.nextLine();
            
    		
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            long daysBetween = ChronoUnit.DAYS.between(LocalDate.parse(checkInDate, formatter), LocalDate.parse(checkOutDate, formatter));
            int totalPrice = Room_Price * (int) daysBetween;
            
            System.out.print("Is Breakfast Included? (true/false):");
            boolean isBreakfast = scanner.nextBoolean();
            scanner.nextLine(); // 개행 문자 소비

            if (isRoomAvailable(roomID, checkInDate, checkOutDate)) {
                if (isBreakfast) {
                	totalPrice += 10000 * peopleNum; // Add breakfast cost
                }
                System.out.println("Total Price:" + totalPrice);

                // 예약 ID 생성
                int reservationID = 0;
                String sqlGetMaxReservationID = "SELECT MAX(ReservationID) AS MaxID FROM Reservation";
                try (PreparedStatement pstmtGetMaxReservationID = conn.prepareStatement(sqlGetMaxReservationID)) {
                    ResultSet rs = pstmtGetMaxReservationID.executeQuery();
                    if (rs.next()) {
                        reservationID = rs.getInt("MaxID") + 1;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
             // 예약 정보 데이터베이스에 삽입
                String sqlInsertReservation = "INSERT INTO Reservation (ReservationID, CustomerID, RoomID, CheckInDate, CheckOutDate, PeopleNum, TotalPrice, IsBreakfast) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmtInsertReservation = conn.prepareStatement(sqlInsertReservation)) {
                    pstmtInsertReservation.setInt(1, reservationID);
                    pstmtInsertReservation.setString(2, customerID);
                    pstmtInsertReservation.setInt(3, roomID);
                    pstmtInsertReservation.setString(4, checkInDate);
                    pstmtInsertReservation.setString(5, checkOutDate);
                    pstmtInsertReservation.setInt(6, peopleNum);
                    pstmtInsertReservation.setInt(7, totalPrice);
                    pstmtInsertReservation.setBoolean(8, isBreakfast);

                    int affectedRows = pstmtInsertReservation.executeUpdate();
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
    //예약 검색
    public static void searchReservation(Connection conn) {
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

    //예약 수정
    public static void updateReservation() {
    	
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("");
		System.out.println("<<<<<< Update Reservation >>>>>> ");
		System.out.print("Enter Customer ID:");
		CustomerID = scanner.next();
		System.out.print("Enter Reservation ID:");
		ReservationID = scanner.nextInt();
		
		displayReservationDetails(ReservationID);
		
		boolean reservationExist = displayReservationDetailsBool(ReservationID);
		
		if(reservationExist)
		{
			System.out.println("\nChoose to update:");
			System.out.println("1. Check-In Date & Check-Out Date");
			System.out.println("2. Breakfast");
			
			int choice = scanner.nextInt();
			
			switch(choice) {
			
			case 1:
				updateCheckInOutDate();
				break;
			case 2:
				updateBreakfast();
				break;
			default:
		        System.out.println("Invalid input. Please try again.");
			}
		}
	}
    
	//체크인, 체크아웃 변경 
	public static void updateCheckInOutDate() {
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Enter new Check-In Date (YYYY-MM-DD):");
		String checkInDate = scanner.next();
		System.out.println("Enter new Check-Out Date (YYYY-MM-DD):");
		String checkOutDate = scanner.next();
		
		if (isRoomAvailable(ReservationID, checkInDate, checkOutDate) == true) {
			
			int current_TotalPrice = computeTotalPrice(ReservationID, checkInDate, checkOutDate);
			
			String sql = "UPDATE Reservation SET CheckInDate = ?, CheckOutDate = ?, TotalPrice = ?  WHERE ReservationID = ?";
            try (
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, checkInDate);
                pstmt.setString(2, checkOutDate);
                pstmt.setInt(3, current_TotalPrice);
                pstmt.setInt(4, ReservationID);
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Reservation updated successfully.");
                   displayReservationDetails(ReservationID);
                    
                } else {
                    System.out.println("Reservation not updated");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Room is not available for the selected dates.");
        }
    }
	
	//체크인, 체크아웃 변경 시 방이 예약가능한 상태인지 확인 
	public static boolean isRoomAvailable(int RoomID, String checkInDate, String checkOutDate) {
	    try {
	        String sql = "SELECT * FROM Reservation WHERE RoomID = ? AND NOT (CheckOutDate <= ? OR CheckInDate >= ?)";
	        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setInt(1, RoomID);
	            pstmt.setString(2, checkOutDate);
	            pstmt.setString(3, checkInDate);
	            ResultSet rs = pstmt.executeQuery();
	            if (rs.next()) {
	            	System.out.println("boolean function false");
	                return false;  // Found a conflicting reservation
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return true;  // No conflicting reservation found
	}

		
	//체크인,체크아웃 날짜 변경 시 가격 다시 계
	public static int computeTotalPrice(int ReservationID, String checkInDate, String checkOutDate) {
		
		int result = 0;
		int Room_ID = 0;
		int Room_Price = 0;
		
		String sql = "SELECT RoomID FROM Reservation WHERE ReservationID = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setInt(1, ReservationID);
	            
	            ResultSet rs = pstmt.executeQuery();
	            if (rs.next()) {
	                
	                Room_ID = rs.getInt("RoomID");
	                
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		
		String sql_2 = "SELECT Price FROM Room WHERE RoomID = ?";
		try (PreparedStatement pstmt = conn.prepareStatement(sql_2)) {
	            pstmt.setInt(1, Room_ID);
	            
	            ResultSet rs = pstmt.executeQuery();
	            
	            if (rs.next()) {
	                
	                Room_Price = rs.getInt("Price");
	                
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		        
        // 문자열을 LocalDate 객체로 파싱
        LocalDate startDate = LocalDate.parse(checkInDate, formatter);
        LocalDate endDate = LocalDate.parse(checkOutDate, formatter);
        
        // 두 날짜 사이의 일수 차이 계산
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        result = Room_Price * (int) daysBetween;
        return result;      
	}
	
	//조식 여부 변경 
	public static void updateBreakfast() {
		
		Scanner scanner = new Scanner(System.in);
		boolean IsBreakfast = false; //초기화 
		int currentTotalPrice = 0;
		int numPeople = 0;
		
		String sql = "SELECT IsBreakfast,TotalPrice, PeopleNum FROM Reservation WHERE ReservationID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ReservationID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                IsBreakfast = rs.getBoolean("IsBreakfast");
                currentTotalPrice = rs.getInt("TotalPrice");
                numPeople = rs.getInt("PeopleNum");
            } else {
            	System.out.println("Reservation not found");
            	return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        System.out.println("Do you want to change? (Y/N)");
        String answer = scanner.next().toUpperCase(); // 사용자 응답 입력받음

        if (answer.equals("Y")) {
        	IsBreakfast = !IsBreakfast; // 아침 식사 옵션 반전
        	
        	int priceChange = IsBreakfast ? 10000 : -10000;
            int updatedTotalPrice = currentTotalPrice + priceChange * numPeople;
            
            String updateSql = "UPDATE Reservation SET IsBreakfast = ?, TotalPrice = ? WHERE ReservationID = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setBoolean(1, IsBreakfast);
                pstmt.setInt(2, updatedTotalPrice);
                pstmt.setInt(3,  ReservationID);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                	System.out.println("Reservation updated successfully.");
                    System.out.println("Breakfast option updated to: " + (IsBreakfast ? "Included" : "Not included"));
                    System.out.println("TotalPrice updated to: "+ updatedTotalPrice);
                    displayReservationDetails(ReservationID);
                } else {
                    System.out.println("Failed to update breakfast option.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No changes made to the breakfast option.");
        }
	}
	
	private static void displayReservationDetails(int reservationID) {
        String sqlQuery = "SELECT * FROM Reservation WHERE ReservationID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            pstmt.setInt(1, reservationID);
            ResultSet resultSet = pstmt.executeQuery();
            
            if (resultSet.next()) {
                System.out.println("Reservation Details:");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-15s | %-10s | %-10s | %-15s | %-15s | %-10s | %-10s | %-10s\n",
                        "ReservationID", "CustomerID", "RoomID", "CheckInDate", "CheckOutDate", "PeopleNum", "IsBreakfast", "TotalPrice");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                do {
                    System.out.printf("%-15d | %-10s | %-10s | %-15s | %-15s | %-10d | %-10b | %-10d\n",
                            resultSet.getInt("ReservationID"),
                            resultSet.getString("CustomerID"),
                            resultSet.getInt("RoomID"),
                            resultSet.getString("CheckInDate"),
                            resultSet.getString("CheckOutDate"),
                            resultSet.getInt("PeopleNum"),
                            resultSet.getBoolean("IsBreakfast"),
                            resultSet.getInt("TotalPrice"));
                    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------");
                } while (resultSet.next());
            } else {
                System.out.println("No reservations found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
	
	
	private static boolean displayReservationDetailsBool(int reservationID) {
        String sqlQuery = "SELECT * FROM Reservation WHERE ReservationID = ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
            pstmt.setInt(1, reservationID);
            ResultSet resultSet = pstmt.executeQuery();
            
            if (resultSet.next()) {
                    return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
		return false;
    }
	
	//예약 취소
	public static void cancelReservation() {
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("");
		System.out.println("<<<<<< Cancel Reservation >>>>>> ");
		System.out.print("Enter Customer ID:");
		CustomerID = scanner.next();
		System.out.print("Enter Reservation ID:");
		ReservationID = scanner.nextInt();
		scanner.nextLine(); // 개행 문자 소비
		
		String sql = "DELETE FROM Reservation WHERE CustomerID = ? and ReservationID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, CustomerID);
            pstmt.setInt(2, ReservationID);
            
            int affectedRows = pstmt.executeUpdate(); // Use executeUpdate for DELETE, INSERT, and UPDATE statements

            if (affectedRows > 0) {
                System.out.println("\nReservation canceled successfully.");
            } else {
                System.out.println("\nNo reservation found with the provided IDs.");
            }
        } catch (Exception e) {
            System.out.println("Error while canceling the reservation: " + e.getMessage());
        }
    }
    
    public static void searchCheckInDate(Connection conn) {
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

    public static void searchRoomType(Connection conn) {
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

    public static void searchRoomAvailability(Connection conn) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter RoomID to search its availability during a period: ");
            int roomID = scanner.nextInt();
            scanner.nextLine();  // Consume newline
            
            if (!roomExists(conn, roomID)) {
                System.out.println("Room ID does not exist.");
                return;
            }
            
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
	//고객 ID 존재 여부 확인 함수
	 static boolean customerExists(Connection conn, String customerID) throws SQLException {
	 String sql = "SELECT COUNT(*) FROM Customers WHERE CustomerID = ?";
	 try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	     pstmt.setString(1, customerID);
	     ResultSet rs = pstmt.executeQuery();
	     if (rs.next()) {
	         return rs.getInt(1) > 0;
	     }
	 }
	 return false;
	}
	//방 ID 존재 여부 확인 함수
	 static boolean roomExists(Connection conn, int roomID) throws SQLException {
	 String sql = "SELECT COUNT(*) FROM Room WHERE RoomID = ?";
	 try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
	     pstmt.setInt(1, roomID);
	     ResultSet rs = pstmt.executeQuery();
	     if (rs.next()) {
	         return rs.getInt(1) > 0;
	     }
	 }
	 return false;
	}
}