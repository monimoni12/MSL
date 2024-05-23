

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class HotelBookingSystem {
	
	private static String CustomerID;
	private static int ReservationID;
	

	private static Connection connect() {
		String url = "jdbc:mysql://localhost:3306/HotelBookingService";
        String user = "root";
        String password = "dPfksdl!1357";
	
	
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			return DriverManager.getConnection(url, user, password);
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
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            displayMenu();
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    updateReservation();
                    break;
                case 2:
                    cancelReservation();
                    break;
                case 0:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid input. Please try again.");
            }
        }
    }
	
	public static void displayMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Update Reservation");
        System.out.println("2. Cancel Reservation");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }

	public static void updateReservation() {
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("");
		System.out.println("<<<<<< Update Reservation >>>>>> ");
		System.out.print("Enter Customer ID:");
		CustomerID = scanner.next();
		System.out.print("Enter Reservation ID:");
		ReservationID = scanner.nextInt();
		
		displayReservationDetails(ReservationID);
		
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
            try (Connection conn = connect();
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
	public static boolean isRoomAvailable(int ReservationID, String checkInDate, String checkOutDate) {
		
		
		int RoomID = -1; // 초기값은 유효하지 않은 RoomID로 설정
        String findRoomIDSql = "SELECT RoomID FROM Reservation WHERE ReservationID = ?";
        try (Connection conn = connect();
             PreparedStatement pstmtFindRoomId = conn.prepareStatement(findRoomIDSql)) {
            pstmtFindRoomId.setInt(1, ReservationID);
            ResultSet rs = pstmtFindRoomId.executeQuery();
            if (rs.next()) {
                RoomID = rs.getInt("RoomID");
            } else {
                return false; // ReservationID가 잘못되었거나 찾을 수 없음
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
		 String sql = "SELECT * FROM Reservation WHERE RoomID = ? AND ReservationID <> ? AND NOT (CheckOutDate <= ? OR CheckInDate >= ?)";
	        try (Connection conn = connect();
	             PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setInt(1, RoomID);
	            pstmt.setInt(2, ReservationID);
	            pstmt.setString(3, checkInDate);
	            pstmt.setString(4, checkOutDate);
	            ResultSet rs = pstmt.executeQuery();
	            if (rs.next()) {
	                return false;  // Found a conflicting reservation
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
		try (Connection conn = connect();
	             PreparedStatement pstmt = conn.prepareStatement(sql)) {
	            pstmt.setInt(1, ReservationID);
	            
	            ResultSet rs = pstmt.executeQuery();
	            if (rs.next()) {
	                
	                Room_ID = rs.getInt("RoomID");
	                
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		
		String sql_2 = "SELECT Price FROM Room WHERE RoomID = ?";
		try (Connection conn = connect();
	            PreparedStatement pstmt = conn.prepareStatement(sql_2)) {
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
		
		String sql = "SELECT IsBreakfast,TotalPrice FROM Reservation WHERE ReservationID = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ReservationID);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                IsBreakfast = rs.getBoolean("IsBreakfast");
                currentTotalPrice = rs.getInt("TotalPrice");
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
            int updatedTotalPrice = currentTotalPrice + priceChange;
            
            String updateSql = "UPDATE Reservation SET IsBreakfast = ?, TotalPrice = ? WHERE ReservationID = ?";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
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
        
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {
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
	
	public static void cancelReservation() {
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("");
		System.out.println("<<<<<< Cancel Reservation >>>>>> ");
		System.out.print("Enter Customer ID:");
		CustomerID = scanner.next();
		System.out.print("Enter Reservation ID:");
		ReservationID = scanner.nextInt();
		
		String sql = "DELETE FROM Reservation WHERE CustomerID = ? and ReservationID = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
		
		
		
	}
		
	
	






