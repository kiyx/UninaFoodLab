package UninaFoodLab.Exceptions;

public class RicettaNotFoundException extends RecordNotFoundException
{
	private static final long serialVersionUID = 1L;

	public RicettaNotFoundException(String msg)
	{
		super(msg);
	}
}