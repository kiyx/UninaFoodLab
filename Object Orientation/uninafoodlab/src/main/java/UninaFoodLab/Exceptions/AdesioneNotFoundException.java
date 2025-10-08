package UninaFoodLab.Exceptions;

public class AdesioneNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public AdesioneNotFoundException(String msg) 
	{
        super(msg);
    }
}