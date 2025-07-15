package UninaFoodLab.Exceptions;

public class RequiredCorsoException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public RequiredCorsoException()
	{
		super("La Sessione non può essere creata senza un corso");
	}
}