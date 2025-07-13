package UninaFoodLab.Boundary;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import UninaFoodLab.DTO.FrequenzaSessioni;
import UninaFoodLab.DTO.Ingrediente;
import UninaFoodLab.DTO.LivelloDifficolta;
import UninaFoodLab.DTO.UnitaDiMisura;
import net.miginfocom.swing.MigLayout;

public class CreateUtilizzoPanel extends JXPanel {

	private JXButton removeBtn;
	private JXButton aggiungiIngredienteBtn;
	private CreateRecipesDialog parent;
	private JComboBox<Ingrediente> ingredientiList;
	private JPanel ingredientiPanel;
	private JScrollPane scrollIngredienti;
	private JComboBox<UnitaDiMisura> misuraList;
	private JTextField quantitaField;
	
	public CreateUtilizzoPanel(CreateRecipesDialog parent)
	{
		this.parent = parent;

		initComponents();
		initListeners();
	}
	
	private void initComponents()
	{
		setLayout(new MigLayout("wrap 2, insets 15, gap 10 15", "[right][grow,fill]"));
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createCompoundBorder(
			    BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(220, 220, 220, 50)),
			    BorderFactory.createCompoundBorder(
			        BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
			        BorderFactory.createEmptyBorder(10, 10, 10, 10)
			    )
			));
			setOpaque(true);
			
			ingredientiList = new JComboBox<>();
	        ingredientiPanel = new JPanel(new GridLayout(1, 1));
	        ingredientiPanel.setOpaque(false);

	        aggiungiIngredienteBtn = new JXButton("Nuovo ingrediente");
	        ingredientiPanel.add(aggiungiIngredienteBtn);
	        
	        for(Ingrediente a : Controller.getController().loadIngredienti())
	        {
	            JCheckBox cb = new JCheckBox(a.getNome());
	            ingredientiList.add(cb);
	            ingredientiPanel.add(cb);
	        }

	        	        
	        scrollIngredienti = new JScrollPane(ingredientiPanel);
	        scrollIngredienti.setPreferredSize(new Dimension(200, 100));
	        scrollIngredienti.setOpaque(false);
	        scrollIngredienti.getViewport().setOpaque(false);
	        scrollIngredienti.getVerticalScrollBar().setUnitIncrement(14);

	        add(new JLabel("Ingrediente:"));
	        add(scrollIngredienti, "span 2");
			
	        quantitaField = new JXTextField();
	        add(new JLabel("Quantità per una porzione:"));
	        add(quantitaField, "h 30!");
	        
	        misuraList = new JComboBox<>(UnitaDiMisura.values());
	        add(new JLabel("Unità di misura:"));
	        add(misuraList, "h 30!");

			removeBtn = new JXButton("Rimuovi sessione");
			removeBtn.putClientProperty("JButton.buttonType", "roundRect");
			removeBtn.putClientProperty("JButton.focusWidth", 0);          
			removeBtn.putClientProperty("JButton.hoverBackground", new Color(180, 40, 50));
			removeBtn.setBackground(new Color(220, 53, 69)); 
			removeBtn.setForeground(Color.WHITE);           
			removeBtn.setBorder(new EmptyBorder(6, 18, 6, 18)); 
			removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));  
			removeBtn.setFocusPainted(false);          
			removeBtn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13)); 
			removeBtn.setIcon(FontIcon.of(MaterialDesign.MDI_CLOSE, 16));  
			removeBtn.setIconTextGap(8); 
			add(removeBtn, "span 2, center, gaptop 15");
	}
	
	private void initListeners()
	{
		
	}
}
