package Model;

public class Transaction
{
	private String start,
				   end,
				   uberType,
				   riderName,
				   driverName,
				   date;
				   
	private int fare,
				   minutes,
				   rate;
	
	public Transaction(String d, String rN, String dN, String uT, String s, String e, int m, int f)
	{
		date 	   = d;
		riderName  = rN;
		driverName = dN;
		uberType   = uT;
		start  	   = s;
		end    	   = e;
		fare  	   = f;
		minutes    = m;
	}
	
	public String getDate()
	{
		return date;
	}
		
	public int getRate()
	{
		return rate;
	}
	
	public String getRiderName()
	{
		return riderName;
	}
	
	public String getDriverName()
	{
		return driverName;
	}
	
	public String getUberType()
	{
		return uberType;
	}
	
	public int getFare()
	{
		return fare;
	}
	
	public int getMinutes()
	{
		return minutes;
	}
	
	public String getStart()
	{
		return start;
	}
	
	public String getEnd()
	{
		return end;
	}
	
	public void setDate(String d)
	{
		date = d;
	}
	
	public void setDriverName(String dN)
	{
		driverName = dN;
	}
	
	public void setRiderName(String rN)
	{
		riderName = rN;
	}
		
	public void setUberType(String uT)
	{
		uberType = uT;
	}
	
	public void setStart(String s)
	{
		start = s;
	}
	
	public void setEnd(String e)
	{
		end = e;
	}
	
	public void setFare(int f)
	{
		fare = f;
	}
	
	public void setRate(int f)
	{
		rate = f;
	}
	
	public void setMinutes(int m)
	{
		minutes = m;
	}
}