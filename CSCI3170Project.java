import java.util.Scanner;
import java.sql.*;
import java.io.*;
public class CSCI3170Project {

	public static String dbAddress = "jdbc:mysql://projgw.cse.cuhk.edu.hk:2312/db37";
	public static String dbUsername = "Group37";
	public static String dbPassword = "csci3170gp37";
	// mysql --host=projgw --port=2312 -u Group37 -p

	public static Connection connectToMySQL(){
		Connection con = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
		} catch (ClassNotFoundException e){
			System.out.println("[Error]: Java MySQL DB Driver not found!!");
			System.exit(0);
		} catch (SQLException e){
			System.out.println(e);
		}
		return con;
	}

	public static void createTables(Connection mySQLDB) throws SQLException{
		String neaSQL = "CREATE TABLE NEA (";
		neaSQL += "NID VARCHAR(10) NOT NULL,";
		neaSQL += "Distance DOUBLE(11,3) UNSIGNED NOT NULL,";
		neaSQL += "Family VARCHAR(6) NOT NULL,";
		neaSQL += "Duration INT(3) UNSIGNED NOT NULL,";
		neaSQL += "Energy DOUBLE(11,3) UNSIGNED NOT NULL,";
		neaSQL += "Resources VARCHAR(2),";
		neaSQL += "PRIMARY KEY (NID))";

		String resourceSQL = "CREATE TABLE Resource (";
		resourceSQL += "Type VARCHAR(2) NOT NULL,";
		resourceSQL += "Density DOUBLE(11,3) UNSIGNED NOT NULL,";
		resourceSQL += "Value DOUBLE(11,3) UNSIGNED NOT NULL,";
		resourceSQL += "PRIMARY KEY (Type))";

		String spacecraftSQL = "CREATE TABLE SpacecraftModel (";
		spacecraftSQL += "Agency VARCHAR(4) NOT NULL,";
		spacecraftSQL += "MID VARCHAR(4) NOT NULL,";
		spacecraftSQL += "Num INT(2) UNSIGNED NOT NULL,";
		spacecraftSQL += "Type VARCHAR(1) NOT NULL,";
		spacecraftSQL += "Energy DOUBLE(11,3) UNSIGNED NOT NULL,";
		spacecraftSQL += "T INT(3) UNSIGNED NOT NULL,";
		spacecraftSQL += "Capacity INT(2) UNSIGNED,";
		spacecraftSQL += "Charge INT(5) UNSIGNED NOT NULL,";
		spacecraftSQL += "PRIMARY KEY (Agency, MID))";

		String rentalrecordSQL = "CREATE TABLE RentalRecord (";
		rentalrecordSQL += "Agency VARCHAR(4) NOT NULL,";
		rentalrecordSQL += "MID VARCHAR(4) NOT NULL,";
		rentalrecordSQL += "SNum INT(2) UNSIGNED NOT NULL,";
		rentalrecordSQL += "CheckoutDate DATE NOT NULL,";
		rentalrecordSQL += "ReturnDate DATE,";
		rentalrecordSQL += "PRIMARY KEY (Agency, MID, SNum))";

		Statement stmt  = mySQLDB.createStatement();
		System.out.print("Processing...");

		//System.err.println("Creating Near-Earth Asteroids Table.");
		stmt.execute(neaSQL);

		//System.err.println("Creating Resources Details Table.");
		stmt.execute(resourceSQL);
		
		//System.err.println("Creating Space Agencies\' Spacecrafts Table.");
		stmt.execute(spacecraftSQL);

		//System.err.println("Creating Spacecraft Rental Records Table.");
		stmt.execute(rentalrecordSQL);

		System.out.println("Done! Database is initialized!");
		stmt.close();
	}

	public static void deleteTables(Connection mySQLDB) throws SQLException{
		Statement stmt  = mySQLDB.createStatement();
		System.out.print("Processing...");
		stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
		stmt.execute("DROP TABLE IF EXISTS NEA");
		stmt.execute("DROP TABLE IF EXISTS Resource");
		stmt.execute("DROP TABLE IF EXISTS SpacecraftModel");
		stmt.execute("DROP TABLE IF EXISTS RentalRecord");
		stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
		System.out.println("Done! Database is removed!");
		stmt.close();
	}

	public static void loadTables(Scanner menuAns, Connection mySQLDB) throws SQLException{

		String filePath = "";
		Statement stmt  = mySQLDB.createStatement();

		while(true){
			System.out.println("");
			System.out.print("Type in the Source Data Folder Path: ");
			filePath = menuAns.nextLine();
			if((new File(filePath)).isDirectory()) break;
			else System.out.println("[Error]: Source Data Folder not found!");
		}

		String loadDataSQL = "LOAD DATA LOCAL INFILE \"./";
		loadDataSQL += filePath;

		String neaSQL = loadDataSQL + "/Near-Earth Asteroids.txt\" ";
		neaSQL += "INTO TABLE NEA ";
		neaSQL += "FIELDS TERMINATED BY '\t' ";
		neaSQL += "LINES TERMINATED BY '\n' ";
		neaSQL += "IGNORE 1 ROWS ";
		neaSQL += "(NID, Distance, Family, Duration, Energy, @resource) ";
		neaSQL += "SET ";
		neaSQL += "Resources = NULLIF(@resource, 'null') ";

		String resourceSQL = loadDataSQL + "/Resources Details.txt\" ";
		resourceSQL += "INTO TABLE Resource ";
		resourceSQL += "FIELDS TERMINATED BY '\t' ";
		resourceSQL += "LINES TERMINATED BY '\n' ";
		resourceSQL += "IGNORE 1 ROWS ";
		resourceSQL += "(Type, Density, Value) ";

		String spacecraftSQL = loadDataSQL + "/Space Agencies\' Spacecrafts.txt\" ";
		spacecraftSQL += "INTO TABLE SpacecraftModel ";
		spacecraftSQL += "FIELDS TERMINATED BY '\t' ";
		spacecraftSQL += "LINES TERMINATED BY '\n' ";
		spacecraftSQL += "IGNORE 1 ROWS ";
		spacecraftSQL += "(Agency, MID, Num, Type, Energy, T, @capacity, Charge) ";
		spacecraftSQL += "SET ";
		spacecraftSQL += "Capacity = NULLIF(@capacity, 'null') ";

		String rentalrecordSQL = loadDataSQL + "/Spacecraft Rental Records.txt\" ";
		rentalrecordSQL += "INTO TABLE RentalRecord ";
		rentalrecordSQL += "FIELDS TERMINATED BY '\t' ";
		rentalrecordSQL += "LINES TERMINATED BY '\n' ";
		rentalrecordSQL += "IGNORE 1 ROWS ";
		rentalrecordSQL += "(Agency, MID, SNum, @checkoutdate, @returndate) ";
		rentalrecordSQL += "SET ";
		rentalrecordSQL += "CheckoutDate = STR_TO_DATE(@checkoutdate,'%d-%m-%Y'), ";
		rentalrecordSQL += "ReturnDate = NULLIF(STR_TO_DATE(@returndate,'%d-%m-%Y'), 'null') ";

		System.out.print("Processing...");

		try{
			stmt.execute(neaSQL);
			stmt.execute(resourceSQL);
			stmt.execute(spacecraftSQL);
			stmt.execute(rentalrecordSQL);
			stmt.close();
			System.out.println("Done! Data is inputted to the database!");
		} catch (Exception e){
			System.out.println(e);
		}

	}

	public static void showTables(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String[] table_name = {"NEA", "Resource", "SpacecraftModel", "RentalRecord"};

		System.out.println("Number of records in each table:");
		for (int i = 0; i < 4; i++){
			Statement stmt  = mySQLDB.createStatement();
			ResultSet rs = stmt.executeQuery("select count(*) from "+table_name[i]);

			rs.next();
			System.out.println(table_name[i]+": "+rs.getString(1));
			rs.close();
			stmt.close();
		}
	}

	public static void adminMenu(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String answer = null;

		while(true){
			System.out.println();
			System.out.println("-----Operations for administrator menu-----");
			System.out.println("What kinds of operation would you like to perform?");
			System.out.println("1. Create all tables");
			System.out.println("2. Delete all tables");
			System.out.println("3. Load from datafile");
			System.out.println("4. Show number of records in each table");
			System.out.println("0. Return to the main menu");
			System.out.print("Enter Your Choice: ");
			answer = menuAns.nextLine();

			if(answer.equals("1")||answer.equals("2")||answer.equals("3")||answer.equals("4")||answer.equals("0"))
				break;
			System.out.println("[Error]: Wrong Input, Type in again!!!");
		}

		if(answer.equals("1")){
			createTables(mySQLDB);
		}
		else if(answer.equals("2")){
			deleteTables(mySQLDB);
		}
		else if(answer.equals("3")){
			loadTables(menuAns, mySQLDB);
		}
		else if(answer.equals("4")){
			showTables(menuAns, mySQLDB);
		}
	}

	public static void searchNEA(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String ans = null, keyword = null, method = null;
		String searchSQL = "";
		PreparedStatement stmt = null;

		searchSQL += "SELECT NID, Distance, Family, Duration, Energy, Resources ";
		searchSQL += "FROM NEA ";
		searchSQL += "WHERE ";

		while(true){
			System.out.println("Choose the Search criterion:");
			System.out.println("1. ID");
			System.out.println("2. Family");
			System.out.println("3. Resource type");
			System.out.print("My criterion: ");
			ans = menuAns.nextLine();
			if(ans.equals("1")||ans.equals("2")||ans.equals("3")) break;
		}
		method = ans;

		while(true){
			System.out.print("Type in the search keyword: ");
			ans = menuAns.nextLine();
			if(!ans.isEmpty()) break;
		}
		keyword = ans;

		if(method.equals("1")){
			searchSQL += " NID = ? ";
		}
		else if(method.equals("2")){
			searchSQL += " Family LIKE ? ";
		}
		else if(method.equals("3")){
			searchSQL += " Resources LIKE ? ";
		}

		stmt = mySQLDB.prepareStatement(searchSQL);
		if(method.equals("1")){
			stmt.setString(1, keyword);
		}
		else{
			stmt.setString(1, "%" + keyword + "%");
		}

		ResultSet resultSet = stmt.executeQuery();
		if(!resultSet.next()){
			System.out.println("No query result returned based on the provided keyword! Returning to the main menu...");
		}
		else{
			String[] field_name = {"ID", "Distance", "Family", "Duration", "Energy", "Resources"};
			System.out.print(String.format("| %10s ", field_name[0]));  //ID
			System.out.print(String.format("| %10s ", field_name[1]));  //Distance
			System.out.print(String.format("| %6s ", field_name[2]));  //Family
			System.out.print(String.format("| %8s ", field_name[3]));  //Duration
			System.out.print(String.format("| %10s ", field_name[4])); //Energy
			System.out.print(String.format("| %9s ", field_name[5]));  //Resources
			// for (int i = 0; i < 6; i++){
			// 	 System.out.print("| " + field_name[i] + " ");
			// }
			System.out.println("|");

			do{
				System.out.print(String.format("| %10s ", resultSet.getString(1)));  //ID
				System.out.print(String.format("| %10s ", resultSet.getString(2)));  //Distance
				System.out.print(String.format("| %6s ", resultSet.getString(3)));  //Family
				System.out.print(String.format("| %8s ", resultSet.getString(4)));  //Duration
				System.out.print(String.format("| %10s ", resultSet.getString(5))); //Energy
				System.out.print(String.format("| %9s ", resultSet.getString(6)));  //Resources
				// for (int i = 1; i <= 6; i++){
				// 	System.out.print("| " + resultSet.getString(i) + " ");
				// } 
				System.out.println("|");
			} while(resultSet.next());

			System.out.println("End of Query");
		}
		resultSet.close();
		stmt.close();
	}

	public static void searchSpacecraft(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String ans = null, keyword = null, method = null;
		String searchSQL = "";
		PreparedStatement stmt = null;
		// select S.Agency, S.MID, R.SNum, S.Type, S.Energy, S.T, S.Capacity, S.Charge from SpacecraftModel S LEFT JOIN RentalRecord R ON S.Agency = R.Agency AND S.MID = R.MID;
		searchSQL += "SELECT S.Agency, S.MID, R.SNum, S.Type, S.Energy, S.T, S.Capacity, S.Charge ";
		searchSQL += "from SpacecraftModel S ";
		searchSQL += "LEFT JOIN RentalRecord R ON S.Agency = R.Agency AND S.MID = R.MID ";
		searchSQL += "WHERE ";

		while(true){
			System.out.println("Choose the Search criterion:");
			System.out.println("1. Agency Name");
			System.out.println("2. Type");
			System.out.println("3. Least energy [km/s]");
			System.out.println("4. Least working time [days]");
			System.out.println("5. Least capacity [m^3]");
			System.out.print("My criterion: ");
			ans = menuAns.nextLine();
			if(ans.equals("1")||ans.equals("2")||ans.equals("3")||ans.equals("4")||ans.equals("5")) break;
		}
		method = ans;

		while(true){
			System.out.print("Type in the search keyword: ");
			ans = menuAns.nextLine();
			if(!ans.isEmpty()) break;
		}
		keyword = ans;

		if(method.equals("1")){
			searchSQL += " S.Agency = ? ";
		}
		else if(method.equals("2")){
			searchSQL += " S.Type = ? ";
		}
		else if(method.equals("3")){
			searchSQL += " S.Energy = ? ";
		}
		else if(method.equals("4")){
			searchSQL += " S.Capacity = ? ";
		}
		else if(method.equals("5")){
			searchSQL += " S.Charge = ? ";
		}

		stmt = mySQLDB.prepareStatement(searchSQL);
		stmt.setString(1, keyword);

		ResultSet resultSet = stmt.executeQuery();
		if(!resultSet.next()){
			System.out.println("No query result returned based on the provided keyword! Returning to the main menu...");
		}
		else{
			String[] field_name = {"Agency", "MID", "SNum", "Type", "Energy", "T", "Capacity", "Charge"};
			System.out.print(String.format("| %6s ", field_name[0]));  //Agency
			System.out.print(String.format("| %4s ", field_name[1]));  //MID
			System.out.print(String.format("| %4s ", field_name[2]));  //SNum
			System.out.print(String.format("| %4s ", field_name[3]));  //Type
			System.out.print(String.format("| %10s ", field_name[4])); //Energy
			System.out.print(String.format("| %3s ", field_name[5]));  //T
			System.out.print(String.format("| %8s ", field_name[6]));  //Capacity
			System.out.print(String.format("| %6s ", field_name[7]));  //Charge
			// for (int i = 0; i < 8; i++){
			// 	 System.out.print(String.format("| %8s ", field_name[i]));
			// }
			System.out.println("|");

			do{
				System.out.print(String.format("| %6s ", resultSet.getString(1)));  //Agency
				System.out.print(String.format("| %4s ", resultSet.getString(2)));  //MID
				System.out.print(String.format("| %4s ", resultSet.getString(3)));  //SNum
				System.out.print(String.format("| %4s ", resultSet.getString(4)));  //Type
				System.out.print(String.format("| %10s ", resultSet.getString(5))); //Energy
				System.out.print(String.format("| %3s ", resultSet.getString(6)));  //T
				System.out.print(String.format("| %8s ", resultSet.getString(7)));  //Capacity
				System.out.print(String.format("| %6s ", resultSet.getString(8)));  //Charge
				// for (int i = 1; i <= 8; i++){
				// 	System.out.print(String.format("| %8s ", resultSet.getString(i)));
				// }
				System.out.println("|");
			} while(resultSet.next());

			System.out.println("End of Query");
		}
		resultSet.close();
		stmt.close();
	}

	public static void searchCertainMissionDesign(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String ans = null, nid = null;
		String searchSQL = "";
		PreparedStatement stmt = null;

		searchSQL += "SELECT S.Agency, S.MID, RentalRecord.SNum, S.Charge * NEA.Duration AS 'Cost', R.Value * R.Density * S.Capacity - S.Charge * NEA.Duration AS 'Benefit'";
		searchSQL += "FROM NEA LEFT JOIN Resource R ON NEA.Resources = R.Type, ";
		searchSQL += "SpacecraftModel S LEFT JOIN RentalRecord ON S.Agency = RentalRecord.Agency AND S.MID = RentalRecord.MID ";
		searchSQL += "WHERE S.Type = 'A' ";
		searchSQL += "AND S.Energy > NEA.Energy ";
		searchSQL += "AND S.T > NEA.Duration ";
		searchSQL += "AND NEA.Resources IS NOT NULL ";
		searchSQL += "AND RentalRecord.ReturnDate IS NOT NULL ";
		searchSQL += "AND NEA.NID = ? ";
		searchSQL += "ORDER BY Benefit DESC";

		while(true){
			System.out.print("Type in the NEA ID: ");
			ans = menuAns.nextLine();
			if(!ans.isEmpty()) break;
		}
		nid = ans;

		stmt = mySQLDB.prepareStatement(searchSQL);
		stmt.setString(1, nid);

		ResultSet resultSet = stmt.executeQuery();
		if(!resultSet.next()){
			System.out.println("No query result returned based on the provided keyword! Returning to the main menu...");
		}
		else{
			String[] field_name = {"Agency", "MID", "SNum", "Cost", "Benefit"};
			System.out.print(String.format("| %6s ", field_name[0]));  //Agency
			System.out.print(String.format("| %4s ", field_name[1]));  //MID
			System.out.print(String.format("| %4s ", field_name[2]));  //SNum
			System.out.print(String.format("| %10s ", field_name[3])); //Cost
			System.out.print(String.format("| %13s ", field_name[4])); //Benefit
			// for (int i = 0; i < 5; i++){
			// 	 System.out.print(String.format("| %8s ", field_name[i]));
			// }
			System.out.println("|");
			do{
				System.out.print(String.format("| %6s ", resultSet.getString(1)));  //Agency
				System.out.print(String.format("| %4s ", resultSet.getString(2)));  //MID
				System.out.print(String.format("| %4s ", resultSet.getString(3)));  //SNum
				System.out.print(String.format("| %10s ", resultSet.getString(4))); //Cost
				System.out.print(String.format("| %13s ", resultSet.getString(5))); //Benefit
				// for (int i = 1; i <= 8; i++){
				// 	System.out.print(String.format("| %8s ", resultSet.getString(i)));
				// }
				System.out.println("|");
			} while(resultSet.next());

			System.out.println("End of Query");
		}
		resultSet.close();
		stmt.close();
	}

	public static void searchBeneficialMissionDesign(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String ans = null, budget = null, resource = null;
		String searchSQL = "";
		PreparedStatement stmt = null;

		searchSQL += "SELECT NEA.NID, NEA.Family, S.Agency, S.MID, RentalRecord.SNum, NEA.Duration, ";
		searchSQL += "S.Charge * NEA.Duration AS 'Cost', R.Value * R.Density * S.Capacity - S.Charge * NEA.Duration AS 'Benefit' ";
		searchSQL += "FROM NEA LEFT JOIN Resource R ON NEA.Resources = R.Type, ";
		searchSQL += "SpacecraftModel S LEFT JOIN RentalRecord ON S.Agency = RentalRecord.Agency AND S.MID = RentalRecord.MID ";
		searchSQL += "WHERE S.Type = 'A' ";
		searchSQL += "AND S.Energy > NEA.Energy ";
		searchSQL += "AND S.T > NEA.Duration ";
		searchSQL += "AND RentalRecord.ReturnDate IS NOT NULL ";
		searchSQL += "AND NEA.Resources = ? ";
		searchSQL += "AND R.Value * R.Density * S.Capacity - S.Charge * NEA.Duration <= ? ";
		searchSQL += "ORDER BY Benefit DESC ";
		searchSQL += "LIMIT 0, 1";

		while(true){
			System.out.print("Type in your budget [$]: ");
			ans = menuAns.nextLine();
			if(!ans.isEmpty()) break;
		}
		budget = ans;
		while(true){
			System.out.print("Type in the source type: ");
			ans = menuAns.nextLine();
			if(!ans.isEmpty()) break;
		}
		resource = ans;

		stmt = mySQLDB.prepareStatement(searchSQL);
		stmt.setString(1, resource);
		stmt.setDouble(2, Double.parseDouble(budget));

		ResultSet resultSet = stmt.executeQuery();
		if(!resultSet.next()){
			System.out.println("No query result returned based on the provided keyword! Returning to the main menu...");
		}
		else{
			String[] field_name = {"NEA ID", "Family", "Agency", "MID", "SNum", "Duration", "Cost", "Benefit"};
			System.out.print(String.format("| %10s ", field_name[0]));  //NEA ID
			System.out.print(String.format("| %6s ", field_name[1]));  //Family
			System.out.print(String.format("| %6s ", field_name[2]));  //Agency
			System.out.print(String.format("| %4s ", field_name[3]));  //MID
			System.out.print(String.format("| %4s ", field_name[4]));  //SNum
			System.out.print(String.format("| %8s ", field_name[5])); //Duration
			System.out.print(String.format("| %10s ", field_name[6])); //Cost
			System.out.print(String.format("| %13s ", field_name[7])); //Benefit
			// for (int i = 0; i < 5; i++){
			// 	 System.out.print(String.format("| %8s ", field_name[i]));
			// }
			System.out.println("|");
			do{
				System.out.print(String.format("| %10s ", resultSet.getString(1)));  //NEA ID
				System.out.print(String.format("| %6s ", resultSet.getString(2)));  //Family
				System.out.print(String.format("| %6s ", resultSet.getString(3)));  //Agency
				System.out.print(String.format("| %4s ", resultSet.getString(4)));  //MID
				System.out.print(String.format("| %4s ", resultSet.getString(5)));  //SNum
				System.out.print(String.format("| %8s ", resultSet.getString(6))); //Duration
				System.out.print(String.format("| %10s ", resultSet.getString(7))); //Cost
				System.out.print(String.format("| %13s ", resultSet.getString(8))); //Benefit
				// for (int i = 1; i <= 8; i++){
				// 	System.out.print(String.format("| %8s ", resultSet.getString(i)));
				// }
				System.out.println("|");
			} while(resultSet.next());

			System.out.println("End of Query");
		}
		resultSet.close();
		stmt.close();
	}

	public static void customerMenu(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String answer = "";

		while(true){
			System.out.println();
			System.out.println("-----Operations for exploration companies (rental customers)-----");
			System.out.println("What kinds of operation would you like to perform?");
			System.out.println("1. Search for NEAs based on some criteria");
			System.out.println("2. Search for spacecrafts based on some criteria");
			System.out.println("3. A certain NEA exploration mission design");
			System.out.println("4. The most beneficial NEA exploration mission design");
			System.out.println("0. Return to the main menu");
			System.out.print("Enter Your Choice: ");
			answer = menuAns.nextLine();
			
			if(answer.equals("1")||answer.equals("2")||answer.equals("3")||answer.equals("4")||answer.equals("0"))
				break;
			System.out.println("[Error]: Wrong Input, Type in again!!!");
		}
		
		if(answer.equals("1")){
			searchNEA(menuAns, mySQLDB);
		}
		else if(answer.equals("2")){
			searchSpacecraft(menuAns, mySQLDB);
		}
		else if(answer.equals("3")){
			searchCertainMissionDesign(menuAns, mySQLDB);
		}
		else if(answer.equals("4")){
			searchBeneficialMissionDesign(menuAns, mySQLDB);
		}
	}

	public static void countSalespersonRecord(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String recordSQL = "SELECT S.s_id, S.s_name, S.s_experience, COUNT(T.t_id) ";
		recordSQL += "FROM transaction T, salesperson S ";
		recordSQL += "WHERE T.s_id = S.s_id AND S.s_experience >= ? AND S.s_experience <= ? ";
		recordSQL += "GROUP BY S.s_id, S.s_name, S.s_experience ";
		recordSQL += "ORDER BY S.s_id DESC";
		
		String expBegin = null, expEnd = null;

		while(true){
			System.out.print("Type in the lower bound for years of experience: ");
			expBegin = menuAns.nextLine();
			if(!expBegin.isEmpty()) break;
		}

		while(true){
			System.out.print("Type in the upper bound for years of experience: ");
			expEnd = menuAns.nextLine();
			if(!expEnd.isEmpty()) break;
		}

		PreparedStatement stmt  = mySQLDB.prepareStatement(recordSQL);
		stmt.setInt(1, Integer.parseInt(expBegin));
		stmt.setInt(2, Integer.parseInt(expEnd));
		
		ResultSet resultSet = stmt.executeQuery();

		System.out.println("Transaction Record:");
		
		System.out.println("| ID | Name | Years of Experience | Number of Transaction |");
		while(resultSet.next()){
			for (int i = 1; i <= 4; i++){
				System.out.print("| " + resultSet.getString(i) + " ");
			}
			System.out.println("|");
		}
		System.out.println("End of Query");
	}

	public static void showPopularPart(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String ans;
		int booknum = 0, i = 0;
		String sql = "SELECT P.p_id, P.p_name, count(*) "+
					 "FROM part P, transaction T "+
					 "WHERE P.p_id = T.p_id "+
					 "GROUP BY P.p_id, P.p_name "+
					 "ORDER BY count(*) DESC";

		while(true){
			System.out.print("Type in the number of parts: ");
			ans = menuAns.nextLine();
			if(!ans.isEmpty()) break;
		}

		booknum = Integer.parseInt(ans);
		Statement stmt  = mySQLDB.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		System.out.println("| Part ID | Part Name | No. of Transaction |");
		while(resultSet.next() && i < booknum){
			System.out.println( "| " + resultSet.getString(1) + " " +
								"| " + resultSet.getString(2) + " " +
								"| " + resultSet.getString(3) + " " +
								"|");
			i++;
		}
		System.out.println("End of Query");
		stmt.close();
	}

	public static void showTotalSales(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String sql = "SELECT M.m_id, M.m_name, SUM(P.p_price) as total_sum "+
					 "FROM transaction T, part P, manufacturer M " +
					 "WHERE T.p_id = P.p_id AND P.m_id = M.m_id " +
					 "GROUP BY M.m_id, M.m_name " + 
					 "ORDER by total_sum DESC";

		Statement stmt  = mySQLDB.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
	
		System.out.println("| Manufacturer ID | Manufacturer Name | Total Sales Value |");
		while(resultSet.next()){
			System.out.println(	"| " + resultSet.getString(1) + " " +
								"| " + resultSet.getString(2) + " " +          
								"| " + resultSet.getString(3) + " " + 
								"|"); 
		}
		System.out.println("End of Query");
		stmt.close();
	}

	public static void staffMenu(Scanner menuAns, Connection mySQLDB) throws SQLException{
		String answer = "";

		while(true){
			System.out.println();
			System.out.println("-----Operations for spacecraft rental staff-----");
			System.out.println("What kinds of operation would you like to perform?");
			System.out.println("1. Rent a spacecraft");
			System.out.println("2. Return a spacecraft");
			System.out.println("3. List all spacecraft currently rented out (on a mission) for a certain period");
			System.out.println("4. List the number of spacecrafts currently rented out by each Agency");
			System.out.println("0. Return to the main menu");
			System.out.print("Enter Your Choice: ");
			answer = menuAns.nextLine();

			if(answer.equals("1")||answer.equals("2")||answer.equals("3")||answer.equals("4")||answer.equals("0"))
				break;
			System.out.println("[Error]: Wrong Input, Type in again!!!");
		}

		if(answer.equals("1")){
			countSalespersonRecord(menuAns, mySQLDB);
		}
		else if(answer.equals("2")){
			showTotalSales(menuAns, mySQLDB);
		}
		else if(answer.equals("3")){
			showPopularPart(menuAns, mySQLDB);
		}
		else if(answer.equals("4")){
			showPopularPart(menuAns, mySQLDB);
		}
	}

	public static void main(String[] args) {
		Scanner menuAns = new Scanner(System.in);
		System.out.println("Welcome to sales system!");

		while(true){
			try{
				Connection mySQLDB = connectToMySQL();
				System.out.println();
				System.out.println("-----Main menu-----");
				System.out.println("What kinds of operation would you like to perform?");
				System.out.println("1. Operations for administrator");
				System.out.println("2. Operations for exploration companies (rental customers)");
				System.out.println("3. Operations for spacecraft rental staff");
				System.out.println("0. Exit this program");
				System.out.print("Enter Your Choice: ");

				String answer = menuAns.nextLine();

				if(answer.equals("1")){
					adminMenu(menuAns, mySQLDB);
				}
				else if(answer.equals("2")){
					customerMenu(menuAns, mySQLDB);
				}
				else if(answer.equals("3")){
					staffMenu(menuAns, mySQLDB);
				}
				else if(answer.equals("0")){
					break;
				}
				else{
					System.out.println("[Error]: Wrong Input, Type in again!!!");
				}
			}catch (SQLException e){
				System.out.println(e);
			}
		}

		menuAns.close();
		System.exit(0);
	}
}