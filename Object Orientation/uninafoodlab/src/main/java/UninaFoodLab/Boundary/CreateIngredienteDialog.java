package UninaFoodLab.Boundary;

import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;

public class CreateIngredienteDialog extends JXDialog {

	public CreateIngredienteDialog(JXPanel parent)
	{
		super(parent, "Cambia Password", true);
		this.parent=parent;
        initComponents();
        initListeners();
        setSize(380,300);
        setLocationRelativeTo(parent);
        setResizable(false);
	}
}
