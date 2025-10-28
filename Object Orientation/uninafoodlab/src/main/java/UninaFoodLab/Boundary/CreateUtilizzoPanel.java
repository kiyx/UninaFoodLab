package UninaFoodLab.Boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class CreateUtilizzoPanel extends JXPanel {

	private static final long serialVersionUID = 1L;
	
	private Font fieldFont = new Font("Segoe UI", Font.PLAIN, 12);
	
	private JXButton removeBtn;
	private JDialog parent;
	private JComboBox<String> misuraList;
	private JFormattedTextField quantitaField;
	
	private JXLabel clearButton;
	private ButtonGroup group;
	private JPanel ingredientiPanel;
	private List<JRadioButton> ingredientiChecks = new ArrayList<>();
	private JScrollPane scrollIngredienti;
	private JXTextField ricercaIngredientiField;
	
	private ActionListener removeBtnActionListener;
	private DocumentListener ricercaIngredientiFieldListener;
	private MouseAdapter clearButtonListener;
	private int ingSelezionato = -1;
	
	private ArrayList<String> nomiIngredienti = new ArrayList<>();
	private ArrayList<Integer> idIngredienti = new ArrayList<>();
	
	private ArrayList<String> nomiIngredientiUtil = new ArrayList<>();
	private ArrayList<Integer> idIngredientiUtil = new ArrayList<>();
	
	public CreateUtilizzoPanel(JDialog parent)
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
			

		    ricercaIngredientiField = new JXTextField();
		    ricercaIngredientiField.setFont(fieldFont);
		    ricercaIngredientiField.putClientProperty("JTextField.placeholderText", "Cerca ingrediente...");
		    ricercaIngredientiField.setLayout(new BorderLayout());

		    clearButton = new JXLabel(FontIcon.of(MaterialDesign.MDI_CLOSE_CIRCLE_OUTLINE, 16, new Color(150, 150, 150)));
		    clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		    clearButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		    clearButton.setVisible(false); // appare solo quando c'è testo

		    ricercaIngredientiField.add(clearButton, BorderLayout.EAST);
		    add(ricercaIngredientiField, "span, growx, gaptop 10, gapbottom 5");

		    ingredientiPanel = new JPanel(new MigLayout("wrap 1, insets 0, gap 4", "[grow,fill]"));
		    ingredientiPanel.setAutoscrolls(true);
		    ingredientiPanel.setOpaque(false);
		    ingredientiPanel.setBackground(Color.WHITE);

		    group = new ButtonGroup();
		    
		    if(parent instanceof CreateRecipesDialog)
		    	((CreateRecipesDialog)parent).getIngredienti(nomiIngredienti, idIngredienti);
		    else
		    	((ChangeRecipeDialog)parent).getIngredienti(nomiIngredienti, idIngredienti);

		    for(int i=0; i<nomiIngredienti.size(); i++)
		    {
		        JRadioButton cb = new JRadioButton(nomiIngredienti.get(i));
		        
		        cb.setFont(fieldFont);
		        cb.setFocusPainted(false);
		        cb.setOpaque(false);
		        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		        
		        itemListener(cb, idIngredienti.get(i));
		        
		        ingredientiChecks.add(cb);
		        group.add(cb);
		        ingredientiPanel.add(cb, "growx");

		        if(parent instanceof CreateRecipesDialog)
			    	((CreateRecipesDialog)parent).getIngredientiUtil(nomiIngredientiUtil, idIngredientiUtil);
			    else
			    	((ChangeRecipeDialog)parent).getIngredientiUtil(nomiIngredientiUtil, idIngredientiUtil);

		        for(int z=0; z<nomiIngredientiUtil.size(); z++)
		        	if(idIngredientiUtil.get(z)==idIngredienti.get(i))
		        		cb.setEnabled(false);
		    }

		    scrollIngredienti = new JScrollPane(ingredientiPanel);
		    scrollIngredienti.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		    scrollIngredienti.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		    scrollIngredienti.getVerticalScrollBar().setUnitIncrement(20);
		    scrollIngredienti.setBorder(BorderFactory.createEmptyBorder());
		    scrollIngredienti.getViewport().setBackground(Color.WHITE);
		    scrollIngredienti.setBackground(Color.WHITE);

		    add(new JXLabel("Ingredienti esistenti:"), "span, growx");
		    add(scrollIngredienti, "span, growx, growy, hmin 100, hmax 160");
			
	        quantitaField = new JFormattedTextField(quantitaFormatter());
	        quantitaField.setValue(0.0);
	        add(new JLabel("Quantità per una porzione:"));
	        add(quantitaField, "h 30!");
	        
	        misuraList = new JComboBox<>(Controller.getController().loadUnita());
	        add(new JLabel("Unità di misura:"));
	        add(misuraList, "h 30!");

			removeBtn = new JXButton("Rimuovi ingrediente");
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
	    removeBtnActionListener = new ActionListener()
								  {
								      @Override
								      public void actionPerformed(ActionEvent e)
								      {
									        if(parent instanceof CreateRecipesDialog)
										    	((CreateRecipesDialog)parent).removeUtilizzoCard(CreateUtilizzoPanel.this);
										    else
										    	((ChangeRecipeDialog)parent).removeUtilizzoCard(CreateUtilizzoPanel.this);

								      }
								  };
	    removeBtn.addActionListener(removeBtnActionListener);

	    if(clearButton != null)
	    {
	    	clearButton.addMouseListener(new MouseAdapter()
								    	 {
								    	     @Override
								    	     public void mouseClicked(MouseEvent e)
								    	     {
								    	    	 ricercaIngredientiField.setText("");
								    	     }
								    	 });

	        clearButton.addMouseListener(clearButtonListener);
	    }
	    
	    if(ricercaIngredientiField!=null)
	    {
	    	ricercaIngredientiFieldListener = new DocumentListener()
			  {
			   	   private void aggiornaFiltro()
			   	   {
			    	  String filtro = ricercaIngredientiField.getText().trim().toLowerCase();
			    	  clearButton.setVisible(!filtro.isEmpty());

			    	  ingredientiPanel.removeAll();

			    	  for(int i = 0; i < nomiIngredienti.size(); i++)
			    	  {
			    	      String r = nomiIngredienti.get(i);
			    	      JRadioButton cb = ingredientiChecks.get(i);
			    	      boolean match = r.toLowerCase().contains(filtro);
			    	      if(match)
			    	      {
			    	          ingredientiPanel.add(cb, "growx");
			    	          cb.setVisible(true);
			    	          group.add(cb);
			    	      }
			    	  }

			    	  ingredientiPanel.revalidate();
			    	  ingredientiPanel.repaint();
			    	}
			        @Override public void insertUpdate(DocumentEvent e) { aggiornaFiltro(); }
			        @Override public void removeUpdate(DocumentEvent e) { aggiornaFiltro(); }
			        @Override public void changedUpdate(DocumentEvent e) { aggiornaFiltro(); }
			    };
			    ricercaIngredientiField.getDocument().addDocumentListener(ricercaIngredientiFieldListener);
	    }
	    
	}
	public void disposeListeners()
	{
	    if(removeBtn != null && removeBtnActionListener != null)
	    {
	        removeBtn.removeActionListener(removeBtnActionListener);
	        removeBtnActionListener = null;
	    }
	}
	
	public boolean isValidUtilizzo()
	{
	    boolean valido = true;

	    valido &= validateIngrediente();
	    valido &=validateQuantita();

	    return valido;
	}
	
	public void refresh(String ingNome, int ingId)
	{
        JRadioButton cb = new JRadioButton(ingNome);
        cb.setFont(fieldFont);
        cb.setFocusPainted(false);
        cb.setOpaque(false);
        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));       
        
        nomiIngredienti.add(ingNome);
        idIngredienti.add(ingId);
        itemListener(cb, ingId);
        
        ingredientiChecks.add(cb);
        group.add(cb);
        ingredientiPanel.add(cb, "growx");
	}
	
	private boolean validateIngrediente()
	{
		boolean check = true;
		
		if(getSelectedRadioText(group)==null)
		{
			check = false;
		}
		
		return check;
	}
	
	private boolean validateQuantita()
	{
		boolean check = true;
		
		if((double)quantitaField.getValue()==0)
		{
			check = false;
		}
		
		return check;
	}
	
    private NumberFormatter quantitaFormatter()
    {
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getNumberInstance(Locale.ITALY));
        formatter.setValueClass(Double.class);
        formatter.setMinimum(0.0);
        formatter.setAllowsInvalid(false);
        return formatter;
    }
    
    public String getSelectedRadioText(ButtonGroup group) 
    {
        for(java.util.Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) 
        {
            AbstractButton button = buttons.nextElement();
            if(button.isSelected()) 
            {
                return button.getText();
            }
        }
        return null; 
    }
    
    public int getIngrediente()
    {
    	return ingSelezionato;
    }
    
    public String getUnita()
    {
    	return (String)misuraList.getSelectedItem();
    }
    
    public Double getQuantita()
    {
    	return (Double)quantitaField.getValue();
    }
    
    public void setResearchNeutral()
    {
    	ricercaIngredientiField.setText(ricercaIngredientiField.getText());   	
    }
  
    public void init(int idIngrediente, String nomeIngrediente, double quantitaIngrediente, String udmIngrediente)
    {	
	    for(JRadioButton r: ingredientiChecks)
	    {
	    	if(r.getText().equals(nomeIngrediente))
	    	{
	    		r.setSelected(true);
	    	}
	    	r.setEnabled(false);
	    }
	    	    
	    quantitaField.setValue(quantitaIngrediente);
	    quantitaField.setEnabled(true);
	    
	    misuraList.setSelectedItem(udmIngrediente);
	    misuraList.setEnabled(true);
    }
    
    public void disabilitaIngrediente(String oldIng, String newIng)
    {
    	for(int i=0; i<ingredientiChecks.size(); i++)
	    {
	        JRadioButton cb = ingredientiChecks.get(i);
	        
	        if(oldIng!=null && cb.getText().equals(oldIng))
	        	cb.setEnabled(true);	        
	        if(cb.getText().equals(newIng))
	        	cb.setEnabled(false);
	    }

    }
    
    public void enableIngrediente(String Ing)
    {
    	for(int i=0; i<ingredientiChecks.size(); i++)
	    {
	        JRadioButton cb = ingredientiChecks.get(i);
	        
	        if(cb.getText().equals(Ing))
	        	cb.setEnabled(true);	        
	    }

    }
    
     private void itemListener(JRadioButton cb, int id)
        {
        	cb.addItemListener(new ItemListener() 
 	       {
 	           @Override
 	           public void itemStateChanged(ItemEvent e) 
 	           {
 	        	   if(cb.isSelected())
 	        	   {
 	        		  if(parent instanceof CreateRecipesDialog)
					    	((CreateRecipesDialog)parent).addListaUtilizzi(ingSelezionato, id, CreateUtilizzoPanel.this);
					  else
					    	((ChangeRecipeDialog)parent).addListaUtilizzi(ingSelezionato, id, CreateUtilizzoPanel.this);
 	        		   ingSelezionato = id;
 	        	   }
 	        	   
 	           }
 	           
 	        });
        }     
}