package UninaFoodLab.Exceptions;

public class IscrizioneNotFoundException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public IscrizioneNotFoundException(String msg) 
    {
        super(msg);
    }
}