package UninaFoodLab.Boundary;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class CreateIngredienteDialog extends JDialog 
{

	private static final long serialVersionUID = 1L;
	
	private JXLabel nomeLabel, origineLabel;
	private JTextField nomeField;
	
	private CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
			new LineBorder(Color.LIGHT_GRAY, 1),
		  	new EmptyBorder(0, 6, 0, 0));
	private CompoundBorder errorBorder = BorderFactory.createCompoundBorder(
        	new LineBorder(Color.RED, 1),
        	new EmptyBorder(0, 6, 0, 0));
	
	private JXButton conferma;
	private JComboBox<String> origineList;
	
	private JXPanel panel;
	private JDialog parent;
	
	private DocumentListener nomeListener;
	private ActionListener ConfermaBtnActionListener;
	
	public CreateIngredienteDialog(JDialog parent)
	{
		super(parent, "Cambia Password", true);
		this.parent=parent;
        initComponents();
        initListeners();
        setSize(380,300);
        setLocationRelativeTo(parent);
        setResizable(false);
	}
	
	private void initComponents() {
	    setTitle("Crea Ingrediente");

	    panel = new JXPanel(new MigLayout("wrap 2", "[grow][grow,fill]", "[]5[]5[]5[]30[]"));
	    panel.setBackground(Color.WHITE);
	    panel.setBorder(new EmptyBorder(20, 25, 20, 25));
	    setContentPane(panel);

	    Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
	    Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

	    nomeLabel = new JXLabel("Nome ingrediente:");
	    nomeLabel.setFont(labelFont);
	    nomeLabel.setForeground(new Color(50, 50, 50));
	    panel.add(nomeLabel, "span 2, left");

	    nomeField = new JTextField();
	    nomeField.setFont(inputFont);
	    nomeField.setPreferredSize(new Dimension(250, 36));
	    nomeField.setBorder(BorderFactory.createCompoundBorder(
	    		defaultBorder,
	        new EmptyBorder(6, 8, 6, 8)
	    ));
	    panel.add(nomeField, "span 2, growx");

	    origineLabel = new JXLabel("Natura dell'ingrediente:");
	    origineLabel.setFont(labelFont);
	    origineLabel.setForeground(new Color(50, 50, 50));
	    panel.add(origineLabel, "span 2, left");

	    origineList = new JComboBox<>(Controller.getController().loadOrigine());
	    origineList.setFont(inputFont);
	    origineList.setPreferredSize(new Dimension(250, 36));
	    origineList.setBackground(Color.WHITE);
	    origineList.setBorder(BorderFactory.createCompoundBorder(
	        new LineBorder(new Color(200, 200, 200), 1, true),
	        new EmptyBorder(2, 8, 2, 8)
	    ));
	    panel.add(origineList, "span 2, growx");

	    conferma = new JXButton("Conferma");
	    conferma.setFont(new Font("Segoe UI", Font.BOLD, 16));
	    conferma.setPreferredSize(new Dimension(140, 36));
	    conferma.setBackground(new Color(225, 126, 47, 220));
	    conferma.setForeground(Color.WHITE);
	    conferma.setOpaque(true);
	    conferma.setFocusPainted(false);
	    conferma.setCursor(new Cursor(Cursor.HAND_CURSOR));
	    conferma.setBorder(BorderFactory.createCompoundBorder(
	    		defaultBorder,
	        new EmptyBorder(6, 12, 6, 12)
	    ));
	    panel.add(conferma, "span 2, center");
	}
	
	private void initListeners()
	{
        nomeListener = new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e) { checkNome(); }
            @Override
            public void removeUpdate(DocumentEvent e) { checkNome(); }
            @Override
            public void changedUpdate(DocumentEvent e) { checkNome(); }
        };
        nomeField.getDocument().addDocumentListener(nomeListener);
	
	
			ConfermaBtnActionListener = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					boolean nomeValid = checkNome();
					boolean origineValid = checkOrigine();

					if(!nomeValid)
					{
						nomeField.requestFocus();
					} 
					else if(!origineValid) 
					{
						origineList.requestFocus();
					} 
					else 
					{
			        	conferma.setEnabled(false);
			        	Controller.getController().createNewIngredient(CreateIngredienteDialog.this, parent, nomeField.getText(), (String)origineList.getSelectedItem());
			        }
			    }
			};
			conferma.addActionListener(ConfermaBtnActionListener);
	}
	
   private void disposeListeners() 
    {
	    if(nomeField != null && nomeListener != null)
			nomeField.getDocument().removeDocumentListener(nomeListener);

        if(conferma != null && ConfermaBtnActionListener != null)
        	conferma.removeActionListener(ConfermaBtnActionListener);
		
    }
	@Override
    public void dispose()
    {
    	disposeListeners();
        super.dispose();
    }

    private boolean checkNome()
    {
    	boolean ret = true;
    	if(nomeField.getText().trim().isEmpty())
    	{
    		nomeField.setBorder(errorBorder);
    		showError( "Nome obbligatorio");
    		ret = false;
    	}
    	else
    		nomeField.setBorder(defaultBorder);
    	return ret;
  	  		
    }
    
    private boolean checkOrigine()
    {
    	boolean ret = true;
    	if(origineList.getSelectedItem()==null)
    	{
    		showError( "Natura dell'ingrediente obbligatoria");
    		ret = false;
    	}
    	return ret;
  	  		
    }    
    
	public void showError(String msg)
	{
		JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
		conferma.setEnabled(true);
	}
}