import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public class Inventory implements inventoryControl {
	Connection connect=null;
	PreparedStatement ps=null;
	PreparedStatement ps1=null;
	PreparedStatement ps2=null;
	PreparedStatement reorder=null;
	PreparedStatement suppliercount=null;
	ResultSet rs=null;
	ResultSet rs1=null;
	ResultSet rs3=null;
	int count = 0;
	int productID;
	int unitsinstock;
	int orderedquantity;
	String date;
	int daysale;
	int pID;
	int day_end_inv;
	int sale_end_inv;
	int day_start_inv;
	int reordered_units;
	int reorder_level;
	int counter=0;
	int unit_on_order;
	int temp=0;
	int discontinued;
	int sum;
	float cost;
	double unitcost;
	int internal_order_reference=0;
	int sID;
	int units;
	int unitins;
	int add;
	void getconnection(Connection connect)
	{
		this.connect=connect;
	}
	public void Ship_order(int orderNumber) throws OrderException 
	{
		int oid=orderNumber;
		try
		{
			ps=connect.prepareStatement("update orders set ShippedDate= curdate() where OrderID='"+oid+"';");
			ps.executeUpdate();
			ps2=connect.prepareStatement("select od.ProductID as Product_id, p.UnitsInStock as UnitsinStock , od.Quantity as Ordered_Quantity from orderdetails as od, products as p where od.OrderID=11045 and od.ProductID=p.ProductID;");
			rs=ps2.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount=rsmd.getColumnCount();
			if(!rs.next())
			{
				throw new OrderException("Order Id not in DB");
			}
			while(rs.next())
			{
				for(int i=1;i<colcount;i++)
				{
					String columnName=rsmd.getColumnLabel(i);
					if(columnName.equals("Product_id"))
					{
						productID=rs.getInt(i);
					}
					else if(columnName.equals("UnitsinStock"))
					{
						unitsinstock=rs.getInt(i);
					}
					else if(columnName.equals("Ordered_Quantity"))
					{
						orderedquantity=rs.getInt(i);
					}
				}
				if(unitsinstock>orderedquantity)
				{
					int updatediff=unitsinstock-orderedquantity;
					ps1=connect.prepareStatement("update products set UnitsinStock= '"+updatediff+"' where ProductID='"+productID+"'");
					ps1.executeUpdate();	
				}
				else
				{
					throw new OrderException("Units in stock is less than ordered quantity");
				}
			}

		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
	}

	@Override
	public int Issue_reorders(int year, int month, int day) 
	{
		String oyear=Integer.toString(year);
		String omonth=Integer.toString(month);
		String oday=Integer.toString(day);
		String datess=oyear+omonth+oday;
		String orrddate=oyear+"-"+omonth+"-"+oday;
		try
		{
			reorder=connect.prepareStatement("select p.productid as Product_ID,p.SupplierID as SupplierID,p.unitsinstock as Day_End_Inventory, p.unitsonorder as Reordered_Units, p.reorderlevel as Reorder_Level, o.shippeddate as Shipped_Date, p.Discontinued as Discontinued from products p join orderdetails od using (productid) join orders o using (orderid) where o.ShippedDate is not null and o.ShippedDate = '"+orrddate+"' group by p.productid,o.shippeddate order by p.productid,o.shippeddate desc;");
			rs1=reorder.executeQuery();
			ResultSetMetaData rsmd1=rs1.getMetaData();
			int colcount=rsmd1.getColumnCount();
			while(rs1.next())
			{
				temp=sID;
				for(int i=1;i<=colcount;i++)
				{
					String columnName=rsmd1.getColumnLabel(i);
					if(columnName.equals("Product_ID"))
					{
						pID = rs1.getInt(i);
					}
					else if(columnName.equals("SupplierID"))
					{
						sID = rs1.getInt(i);
					}
					else if(columnName.equals("Day_End_Inventory"))
					{
						day_end_inv = rs1.getInt(i);
					}
					else if(columnName.equals("Reordered_Units"))
					{
						reordered_units = rs1.getInt(i);
					}
					else if(columnName.equals("Reorder_Level"))
					{
						reorder_level = rs1.getInt(i);
					}
					else if(columnName.equals("Shipped_Date"))
					{
						date = rs1.getString(i);
					} 
					else if(columnName.equals("Day_Sales"))
					{
						daysale = rs1.getInt(i);
					}
					else if(columnName.equals("Discontinued"))
					{
						discontinued=rs1.getInt(i);
					}
				}
				String p_id=Integer.toString(pID);
				String iof=datess+p_id;
				internal_order_reference=Integer.parseInt(iof);
				if(reordered_units==0 && discontinued!=1)
				{
					if(reorder_level==0)
					{
						reorder_level=(day_end_inv/4);
					}
					if(day_end_inv<=(reorder_level*4))
					{
						int sum=(reorder_level*4)-day_end_inv;
						PreparedStatement ps3=connect.prepareStatement("insert into Reorder (ProductID, SupplierID,Internal_order_reference, OrderedDate, Reordered_units, Order_status) values ('"+pID+"','"+sID+"','"+internal_order_reference+"','"+date+"','"+sum+"','ordered');");
						ps3.executeUpdate();
					}
				}
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
		try
		{
			suppliercount=connect.prepareStatement("select count(distinct(SupplierID)) as count from Reorder;");
			rs3=suppliercount.executeQuery();
			ResultSetMetaData rsmd2=rs3.getMetaData();
			int colcount=rsmd2.getColumnCount();
			while(rs3.next())
			{
				for(int i=1;i<=colcount;i++)
				{
					String columnName=rsmd2.getColumnLabel(i);
					if(columnName.equals("count"))
					{
						count=rs3.getInt(i);
					}
				}
			}
			System.out.println(count);
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}
		return count;
	}

	@Override
	public void Receive_order(int internal_order_reference) throws OrderException {
		// TODO Auto-generated method stub
		int id=internal_order_reference;
		int pid = 0;
		try
		{
			PreparedStatement receive=null;
			ResultSet rs4=null;
			PreparedStatement prpstmt=null;
			receive=connect.prepareStatement("update Reorder set Order_status='Received' where Internal_order_reference="+id+";");
			receive.executeUpdate();
			prpstmt=connect.prepareStatement("select ProductID as ProductID,Reordered_Units as reordered_units from Reorder where Internal_order_reference="+id+";");
			rs4=prpstmt.executeQuery();
			ResultSetMetaData rsmd=rs4.getMetaData();
			int colcount=rsmd.getColumnCount();
			while(rs4.next())
			{
				for(int i=1;i<=colcount;i++)
				{
					String columnName=rsmd.getColumnLabel(i);
					if(columnName.equals("ProductID"))
					{
						pid=rs4.getInt(i);
					}
					if(columnName.equals("reordered_units"))
					{
						units=rs4.getInt(i);
					}
				}
				PreparedStatement prd=connect.prepareStatement("select UnitsInStock as reorderedunits from products where ProductID="+pid+";");
				ResultSet rs5=prd.executeQuery();
				ResultSetMetaData rsmd3=rs5.getMetaData();
				int colcount1=rsmd3.getColumnCount();
				while(rs5.next())
				{
					for(int i=1;i<=colcount1;i++)
					{
						String columnName=rsmd3.getColumnLabel(i);
						if(columnName.equals("reorderedunits"))
						{
							unitins=rs5.getInt(i);
						}
					}
				}
				add=units+unitins;
				PreparedStatement prpst=connect.prepareStatement("update products set UnitsInStock="+add+" where ProductID="+pid+";");
				prpst.executeUpdate();
			}
		}
		catch(Exception ex)
		{
			System.out.println(ex);
		}

	}

}
