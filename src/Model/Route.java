package Model;

public class Route
{
	private String start,
				   end,
				   rider,
				   rating;
				   
	private int fare,
			   minutes;
				   
	public Route(String s, String e, int m, int f)
	{
		start  = s;
		end    = e;
		fare   = f;
		minutes = m;
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
		
	public String getRider()
	{
		return rider;
	}
	
	public String getRating()
	{
		return rating;
	}
	
	public void setStart(String s)
	{
		start = s;
	}
	
	public void setEnd(String e)
	{
		end = e;
	}
	
	public void setRider(String r)
	{
		rider = r;
	}
	
	public void setRating(String r)
	{
		rating = r;
	}
	
	public void setFare(int f)
	{
		fare = f;
	}
	
	public void setMinutes(int m)
	{
		minutes = m;
	}
}