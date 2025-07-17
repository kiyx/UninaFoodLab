package UninaFoodLab.Exceptions;

public class RequiredIngredienteException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public RequiredIngredienteException()
	{
		super("Deve essere inserito un ingrediente.");
	}
}
