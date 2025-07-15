package UninaFoodLab.Exceptions;

public class RequiredSessioneException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public RequiredSessioneException()
	{
		super("Il Corso deve avere almeno una sessione associata");
	}
}