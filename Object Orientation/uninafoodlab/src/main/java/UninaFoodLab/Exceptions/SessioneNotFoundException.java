package UninaFoodLab.Exceptions;

public class SessioneNotFoundException extends RecordNotFoundException
{
	private static final long serialVersionUID = 1L;

	public SessioneNotFoundException(String msg) 
    {
        super(msg);
    }
}