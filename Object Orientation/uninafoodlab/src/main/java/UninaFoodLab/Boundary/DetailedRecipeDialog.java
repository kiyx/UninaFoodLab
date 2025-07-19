package UninaFoodLab.Boundary;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class DetailedRecipeDialog extends JDialog
{

	
	private static final long serialVersionUID = 1L;
	
	private JXPanel panel;
	private JXLabel nomeLabel, provenienzaLabel, calorieLabel, difficoltaLabel, ingredientiLabel, utilizzoLabel;
	private JXButton modifica;
	private MyRecipesFrame parent;
	private ActionListener modificaListener;
	private String nomeRicetta;
	private String provenienzaRicetta;
	private int calorieRicetta;
	private int difficoltaRicetta;
	private ArrayList<String> nomiIngredienti;
	private ArrayList<Integer> quantitaIngredienti;
	private ArrayList<String> udmIngredienti;
	
	public DetailedRecipeDialog(MyRecipesFrame parent, String nomeRicetta, String provenienzaRicetta, int calorieRicetta, int difficoltaRicetta, ArrayList<String> nomiIngredienti, ArrayList<Integer> quantitaIngredienti, ArrayList<String> udmIngredienti)
	{
		
		super(parent, "Cambia Password", true);
		this.parent=parent;
		this.nomeRicetta=nomeRicetta;
		this.provenienzaRicetta=provenienzaRicetta;
		this.calorieRicetta=calorieRicetta;
		this.difficoltaRicetta=difficoltaRicetta;
		this.nomiIngredienti=nomiIngredienti;
		this.quantitaIngredienti=quantitaIngredienti;
		this.udmIngredienti=udmIngredienti;
		
        initComponents();
        initListeners();
        setSize(300, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
	}
		
	private void initComponents() 
	{
	    setTitle("Dettagli ricetta");

	    panel = new JXPanel(new MigLayout("wrap 2", "[grow,fill]", "[]30[]5[]5[]5[grow, fill]"));
	    panel.setBackground(Color.WHITE);
	    panel.setBorder(new EmptyBorder(20, 25, 20, 25));
	    setContentPane(panel);

	    Font labelFont = new Font("Segoe UI", Font.BOLD, 14);
	    Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

	    nomeLabel = new JXLabel(nomeRicetta);
	    nomeLabel.setFont(labelFont);
	    nomeLabel.setForeground(new Color(50, 50, 50));
	    panel.add(nomeLabel, "span 2, left");

	    provenienzaLabel = new JXLabel("Provenienza:" + provenienzaRicetta);
	    provenienzaLabel.setFont(inputFont);
	    provenienzaLabel.setForeground(new Color(50, 50, 50));
	    panel.add(provenienzaLabel, "span 2, left");

	    calorieLabel = new JXLabel("Calorie:" + calorieRicetta);
	    calorieLabel.setFont(inputFont);
	    calorieLabel.setForeground(new Color(50, 50, 50));
	    panel.add(calorieLabel, "span 2, left");
	    
	    difficoltaLabel = new JXLabel("Difficolta:" + difficoltaRicetta);
	    difficoltaLabel.setFont(inputFont);
	    difficoltaLabel.setForeground(new Color(50, 50, 50));
	    panel.add(difficoltaLabel, "span 2, left");
	    
	    ingredientiLabel = new JXLabel("Ingredienti");
	    ingredientiLabel.setFont(labelFont);
	    ingredientiLabel.setForeground(new Color(50, 50, 50));
	    panel.add(ingredientiLabel, "span 2, left");
	    
	    for(int i=0; i<nomiIngredienti.size(); i++)
	    {
	    	utilizzoLabel = new JXLabel(nomiIngredienti.get(i)+" "+quantitaIngredienti.get(i)+" "+udmIngredienti.get(i));
	    	utilizzoLabel.setFont(inputFont);
	    	utilizzoLabel.setForeground(new Color(50, 50, 50));
		    panel.add(utilizzoLabel, "span 2, left");
	    }

	    modifica = new JXButton("Conferma");
	    modifica.setFont(new Font("Segoe UI", Font.BOLD, 16));
	    modifica.setPreferredSize(new Dimension(140, 36));
	    modifica.setBackground(new Color(225, 126, 47, 220));
	    modifica.setForeground(Color.WHITE);
	    modifica.setOpaque(true);
	    modifica.setFocusPainted(false);
	    modifica.setCursor(new Cursor(Cursor.HAND_CURSOR));

	    panel.add(modifica, "span 2, center");
	}

	private void initListeners()
	{
		modificaListener = new ActionListener()
		   {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					Controller.getController().showChangeRecipeDialog(parent, nomeRicetta, provenienzaRicetta, calorieRicetta, difficoltaRicetta, nomiIngredienti, quantitaIngredienti, udmIngredienti);
					dispose();
				}
		   };    		   
		   modifica.addActionListener(modificaListener);
	}
	
	private void disposeListeners() 
    {
	   
		 if (modifica != null && modificaListener != null)
			 modifica.removeActionListener(modificaListener);

    }
	@Override
    public void dispose()
    {
    	disposeListeners();
        super.dispose();
    }

}
