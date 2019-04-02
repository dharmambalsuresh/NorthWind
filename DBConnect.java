import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnect {
	public DBConnect()
    {
	Connection connect=null;
	try 
		{
			Class.forName("com.mysql.cj.jdbc.Driver");
			connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306/dharmambal?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", "dharmambal", "B00824492");
			PurchaseHistory ph=new PurchaseHistory();
          ph.buildhistory(connect);
			Inventory iv=new Inventory();
			iv.getconnection(connect);
			iv.Ship_order(11405);
			iv.Issue_reorders(2019,04, 01);
			iv.Receive_order(1);
		}
	
		catch (Exception e) 
		{
			System.out.println(e);
			System.exit(0);
		}
    }
		
}
