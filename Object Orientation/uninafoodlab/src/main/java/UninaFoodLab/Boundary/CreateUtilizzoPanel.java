package UninaFoodLab.Boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import com.github.lgooddatepicker.optionalusertools.DateChangeListener;
import com.github.lgooddatepicker.optionalusertools.TimeChangeListener;
import com.github.lgooddatepicker.zinternaltools.DateChangeEvent;
import com.github.lgooddatepicker.zinternaltools.TimeChangeEvent;

import UninaFoodLab.Controller.Controller;
import UninaFoodLab.DTO.FrequenzaSessioni;
import UninaFoodLab.DTO.Ingrediente;
import UninaFoodLab.DTO.LivelloDifficolta;
import UninaFoodLab.DTO.Ricetta;
import UninaFoodLab.DTO.UnitaDiMisura;
import net.miginfocom.swing.MigLayout;

public class CreateUtilizzoPanel extends JXPanel {

	private Font fieldFont = new Font("Segoe UI", Font.PLAIN, 12);
	
	private JXButton removeBtn;
	private CreateRecipesDialog parent;
	private JComboBox<Ingrediente> ingredientiList;
	private JComboBox<UnitaDiMisura> misuraList;
	private JFormattedTextField quantitaField;
	private boolean focusSet = false;
	
	private JXLabel clearButton;
	private ButtonGroup group;
	private List<Ingrediente> ingredienti;
	private JPanel ingredientiPanel;
	private List<JRadioButton> ingredientiChecks = new ArrayList<>();
	private JScrollPane scrollIngredienti;
	private JXTextField ricercaIngredientiField;
	
	private ActionListener removeBtnActionListener;
	private DocumentListener ricercaIngredientiFieldListener;
	private MouseAdapter clearButtonListener;
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

		    // Inizializzazione e popolamento lista ingredienti 

		    group = new ButtonGroup();
		    
		    ingredienti = Controller.getController().loadIngredienti();
		    for(Ingrediente r : ingredienti)
		    {
		        JRadioButton cb = new JRadioButton(r.getNome());
		        
		        // Impostazioni base RadioButton (font, focus, cursor)
		        cb.setFont(fieldFont);
		        cb.setFocusPainted(false);
		        cb.setOpaque(false);
		        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		        
		        ingredientiChecks.add(cb);
		        group.add(cb);
		        ingredientiPanel.add(cb, "growx");
		    }

		    scrollIngredienti = new JScrollPane(ingredientiPanel);
		    scrollIngredienti.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		    scrollIngredienti.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		    scrollIngredienti.setBorder(BorderFactory.createEmptyBorder());
		    scrollIngredienti.getViewport().setBackground(Color.WHITE);
		    scrollIngredienti.setBackground(Color.WHITE);

		    add(new JXLabel("Ingredienti esistenti:"), "span, growx");
		    add(scrollIngredienti, "span, growx, growy, hmin 100, hmax 160");
			
	        quantitaField = new JFormattedTextField(quantitaFormatter());
	        quantitaField.setValue(0.0);
	        add(new JLabel("Quantità per una porzione:"));
	        add(quantitaField, "h 30!");
	        
	        misuraList = new JComboBox<>(UnitaDiMisura.values());
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
		/**
		 * Listener associato al pulsante di rimozione dell'utilizzo.
		 * <p>Invoca {@link CreateRecipesDialog#removeUtilizzoCard(CreateUtilizzoPanel)} per eliminare dinamicamente il pannello dal dialogo genitore.
		 */
	    removeBtnActionListener = new ActionListener()
								  {
								      @Override
								      public void actionPerformed(ActionEvent e)
								      {
								          parent.removeUtilizzoCard(CreateUtilizzoPanel.this);
								      }
								  };
	    removeBtn.addActionListener(removeBtnActionListener);

	        /*addressListener = new DocumentListener()
					          {
					              @Override
					              public void insertUpdate(DocumentEvent e) { checkAddress(); }
					              @Override
					              public void removeUpdate(DocumentEvent e) { checkAddress(); }
					              @Override
					              public void changedUpdate(DocumentEvent e) { checkAddress(); }
				
					              private void checkAddress()
					              {
					            	  showError(addressField, addressField.getText().trim().isEmpty(), "Indirizzo obbligatorio");
					              }
					          };
	        addressField.getDocument().addDocumentListener(addressListener);*/

	        /**
	         * Listener che attiva la validazione al termine del focus sul campo indirizzo.
	         */
	        /*addressFocusListener = new FocusAdapter()
							       {
							           @Override
							           public void focusLost(FocusEvent e)
							           {
							        	   showError(addressField, addressField.getText().trim().isEmpty(), "Indirizzo obbligatorio");
							           }
							       };
	        addressField.addFocusListener(addressFocusListener);
	    }*/

	    /**
	     * Listener su label x che svuota il campo di ricerca degli ingredienti
	     */
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

			    	  // Prima inserisco quelli matching
			    	  for(int i = 0; i < ingredienti.size(); i++)
			    	  {
			    	      Ingrediente r = ingredienti.get(i);
			    	      JRadioButton cb = ingredientiChecks.get(i);
			    	      boolean match = r.getNome().toLowerCase().contains(filtro);
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
	    focusSet = false;
	    boolean valido = true;

	    /*valido &= validateData();
	    valido &= validateTime();
	    valido &= validateDurata();

	    if(pratica)
	    {
	        valido &= validateRicetta();
	        valido &= validateAddress();
	    }
	    else
	    	valido &= validateLink();*/

	    return valido;
	}
	
    private NumberFormatter quantitaFormatter()
    {
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getNumberInstance(Locale.ITALY));
        formatter.setValueClass(Double.class);
        formatter.setMinimum(0.0);
        formatter.setAllowsInvalid(false);
        return formatter;
    }
}
