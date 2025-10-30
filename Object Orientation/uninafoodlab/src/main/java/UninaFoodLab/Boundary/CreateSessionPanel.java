package UninaFoodLab.Boundary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;

import org.jdesktop.swingx.*;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.*;

import com.github.lgooddatepicker.components.*;
import com.github.lgooddatepicker.optionalusertools.*;
import com.github.lgooddatepicker.zinternaltools.*;

import UninaFoodLab.Controller.Controller;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;


public class CreateSessionPanel extends JXPanel
{
	private static final long serialVersionUID = 1L;

	private Font fieldFont = new Font("Segoe UI", Font.PLAIN, 12);
	private int numero;
	private boolean pratica;
	private CreateCourseDialog parent;

	private JXPanel durataPanel;
	private JXLabel numeroLabel, clearButton;
	private DatePicker datePicker;
	private TimePicker timePicker;
	private JSpinner oreSpinner, minutiSpinner;
	private JXButton removeBtn;
	private JXTextField addressField, linkField;
	private boolean focusSet = false;

	private ActionListener removeBtnActionListener;
	private DocumentListener addressListener, linkListener, ricercaRicetteFieldListener;
	private ChangeListener durataChangeListener;
	private TimeChangeListener timeListener;
	private DateChangeListener dateListener, dateChangeListener;
	private FocusListener addressFocusListener, linkFocusListener, dateFocusListener, timeFocusListener,
						  oreFocusListener, minutiFocusListener;
	private MouseAdapter clearButtonListener;

	private JPanel ricettePanel;
	private JScrollPane scrollRicette;
	private JXTextField ricercaRicetteField;
	
	private List<JCheckBox> ricettaChecks = new ArrayList<>();	
	private List<Integer> idsRecipes = new ArrayList<>();
	private List<String> namesRecipes = new ArrayList<>();
	private List<Integer> idsSelectedRecipes = new ArrayList<>();


	public CreateSessionPanel(int numero, boolean pratica, CreateCourseDialog parent)
	{
		this.numero = numero;
		this.pratica = pratica;
		this.parent = parent;
		
		initComponents();
		initListeners();
	}
	

	public String getTipo()
	{
		return pratica ? "Pratica" : "Online";
	}

	public LocalDate getDataSessione()
	{
		return datePicker.getDate();
	}
	
	public void setData(LocalDate data)
	{
	    datePicker.setDate(data);
	}


	public DatePicker getDatePicker()
	{
		return datePicker;
	}
	
	/**
	 * Restituisce il listener registrato per gli eventi di cambio data associato a questo pannello.
	 *
	 */
	public DateChangeListener getDateChangeListener()
	{
		return dateChangeListener;
	}
	
	/**
	 * Restituisce l'orario selezionato per la sessione.
	 *
	 */
	public LocalTime getOrario()
	{
		return timePicker.getTime();
	}

	/**
	 * Restituisce la durata totale della sessione in minuti.
	 *
	 */
	public int getDurata()
	{
	    try 
	    {
	        int ore = (int) oreSpinner.getValue();
	        int minuti = (int) minutiSpinner.getValue();
	        return Math.max(0, ore * 60 + minuti);
	    } 
	    catch (Exception e) 
	    {
	        return 0;
	    }
	}

	/**
	 * Restituisce la lista degli ID delle ricette selezionate (solo per sessioni pratiche).
	 *
	 */
	public List<Integer> getIdRicetteSelezionate()
	{
	    if(!pratica)
	        throw new IllegalStateException("La sessione non è pratica: nessuna ricetta selezionabile.");
	    return idsSelectedRecipes;
	}

	/**
	 * Restituisce il link della riunione (solo per sessioni online).
	 *
	 */
	public String getLinkRiunione()
	{
		return !pratica ? linkField.getText().trim() : null;
	}

	/**
	 * Restituisce l'indirizzo della sessione (solo per sessioni pratiche).
	 *
	 */
	public String getIndirizzo()
	{
		return pratica && addressField != null ? addressField.getText().trim() : null;
	}

	/**
     * Inizializza i componenti grafici del pannello.
     */
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

		numeroLabel = createLabel("Sessione #" + numero);
		numeroLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		add(numeroLabel, "span 2, center");

		add(createLabel("Tipo:"));
		add(createLabel(pratica ? "Pratica" : "Online"));

		add(createLabel("Data:"));
		DateVetoPolicy vetoPolicy = new DateVetoPolicyMinimumMaximumDate(LocalDate.now().plusDays(1), null);
        DatePickerSettings settings = new DatePickerSettings();
		datePicker = new DatePicker(settings);
        settings.setVetoPolicy(vetoPolicy);
        datePicker.setFont(fieldFont);
		add(datePicker, "h 30!");

		add(createLabel("Orario:"));
		timePicker = new TimePicker();
		timePicker.setFont(fieldFont);
		add(timePicker, "h 30!");

		oreSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 23, 1));
		oreSpinner.setFont(fieldFont);
		((JSpinner.DefaultEditor) oreSpinner.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		oreSpinner.setToolTipText("Ore di durata");

		minutiSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
		((JSpinner.DefaultEditor) minutiSpinner.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		minutiSpinner.setFont(fieldFont);
		minutiSpinner.setToolTipText("Minuti di durata");

		durataPanel = new JXPanel(new MigLayout("insets 0", "[]5[]5[]", "[]"));
		durataPanel.setBackground(Color.WHITE);
		durataPanel.add(oreSpinner, "w 50!");
		durataPanel.add(createLabel("h"));
		durataPanel.add(minutiSpinner, "w 50!");
		durataPanel.add(createLabel("m"));

		add(createLabel("Durata:"), "aligny center");
		add(durataPanel);

		if(pratica)
		{
		    add(createLabel("Indirizzo:"));
		    addressField = new JXTextField();
		    addressField.setFont(fieldFont);
		    addressField.putClientProperty("JTextField.placeholderText", "Inserisci indirizzo...");
		    add(addressField, "h 30!");

		    ricercaRicetteField = new JXTextField();
		    ricercaRicetteField.setFont(fieldFont);
		    ricercaRicetteField.putClientProperty("JTextField.placeholderText", "Cerca ricetta...");
		    ricercaRicetteField.setLayout(new BorderLayout());

		    clearButton = new JXLabel(FontIcon.of(MaterialDesign.MDI_CLOSE_CIRCLE_OUTLINE, 16, new Color(150, 150, 150)));
		    clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		    clearButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		    clearButton.setVisible(false);

		    ricercaRicetteField.add(clearButton, BorderLayout.EAST);
		    add(ricercaRicetteField, "span, growx, gaptop 10, gapbottom 5");

		    ricettePanel = new JXPanel(new MigLayout("wrap 1, insets 0, gap 4", "[grow,fill]"));
		    ricettePanel.setAutoscrolls(true);
		    ricettePanel.setOpaque(false);
		    ricettePanel.setBackground(Color.WHITE);

		    Controller.getController().loadRicette(idsRecipes, namesRecipes);

		    for(int i = 0; i < idsRecipes.size(); i++) 
		    {
		        final int j = i;
		        JCheckBox cb = new JCheckBox(namesRecipes.get(i));
		        
		        cb.setFont(fieldFont);
		        cb.setFocusPainted(false);
		        cb.setOpaque(false);
		        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		        cb.addItemListener(new ItemListener() 
							        {
							            @Override
							            public void itemStateChanged(ItemEvent e) 
							            {  	
							                int recipeId = idsRecipes.get(j);
							                
							                if(cb.isSelected() && !idsSelectedRecipes.contains(recipeId)) 
							                	idsSelectedRecipes.add(recipeId);
							                else 
							                {
							                    int index = idsSelectedRecipes.indexOf(recipeId);
							                    if(index >= 0) 
							                    	idsSelectedRecipes.remove(index);
							                }
							            }
							        });

		        ricettaChecks.add(cb);
		        ricettePanel.add(cb, "growx");
		    }

		    scrollRicette = new JScrollPane(ricettePanel);
		    scrollRicette.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		    scrollRicette.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		    scrollRicette.setBorder(BorderFactory.createEmptyBorder());
		    scrollRicette.getViewport().setBackground(Color.WHITE);
		    scrollRicette.setBackground(Color.WHITE);

		    add(new JXLabel("Ricette da svolgere:"), "span, growx");
		    add(scrollRicette, "span, growx, growy, hmin 100, hmax 160");
		}
		else
		{
			add(createLabel("Link riunione:"));
			linkField = new JXTextField();
			linkField.setFont(fieldFont);
			linkField.putClientProperty("JTextField.placeholderText", "Inserisci il link della riunione...");
			add(linkField, "h 30!");
		}
		
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
		
	    removeBtnActionListener = new ActionListener()
								  {
								      @Override
								      public void actionPerformed(ActionEvent e)
								      {
								    	  Controller.getController().callRemoveSessionCard(parent, CreateSessionPanel.this);		          
								      }
								  };
	    removeBtn.addActionListener(removeBtnActionListener);

	    if(pratica && addressField != null)
	    {
	    	
	        addressListener = new DocumentListener()
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
	        addressField.getDocument().addDocumentListener(addressListener);

	        /**
	         * Listener che attiva la validazione al termine del focus sul campo indirizzo.
	         */
	        addressFocusListener = new FocusAdapter()
							       {
							           @Override
							           public void focusLost(FocusEvent e)
							           {
							        	   showError(addressField, addressField.getText().trim().isEmpty(), "Indirizzo obbligatorio");
							           }
							       };
	        addressField.addFocusListener(addressFocusListener);
	    }

	    if(!pratica && linkField != null)
	    {
	    	
	        linkListener = new DocumentListener()
					       {
					           @Override
					           public void insertUpdate(DocumentEvent e) { checkLink(); }
					           @Override
					           public void removeUpdate(DocumentEvent e) { checkLink(); }
					           @Override
					           public void changedUpdate(DocumentEvent e) { checkLink(); }
				
					           private void checkLink()
					           {
					               showError(linkField, linkField.getText().trim().isEmpty(), "Link riunione obbligatorio");
					           }
					        };
	        linkField.getDocument().addDocumentListener(linkListener);

	        /**
	         * Listener che attiva la validazione al termine del focus sul campo link riunione.
	         */
	        linkFocusListener = new FocusAdapter()
						        {
						            @Override
						            public void focusLost(FocusEvent e)
						            {
						                showError(linkField, linkField.getText().trim().isEmpty(), "Link riunione obbligatorio");
						            }
						        };
	        linkField.addFocusListener(linkFocusListener);
	    }

	 
	    durataChangeListener = new ChangeListener()
							   {
							       @Override
							       public void stateChanged(ChangeEvent e)
							       {
							           showError(oreSpinner, getDurata() <= 0, "Durata non valida");
							           showError(minutiSpinner, getDurata() <= 0, "Durata non valida");
							       }
							    };
	    oreSpinner.addChangeListener(durataChangeListener);
	    minutiSpinner.addChangeListener(durataChangeListener);

	    /**
	     * Listener che attiva la validazione al termine del focus sul campo ore (durata).
	     */
	    oreFocusListener = new FocusAdapter()
						   {
						       @Override
						       public void focusLost(FocusEvent e)
						       {
						    	   showError(oreSpinner, getDurata() <= 0, "Durata non valida");
						       }
						   };
	   /**
	    * Listener che attiva la validazione al termine del focus sul campo minuti (durata).
	    */				   
	    minutiFocusListener = new FocusAdapter()
							  {
							      @Override
							      public void focusLost(FocusEvent e)
							      {
							          showError(minutiSpinner, getDurata() <= 0, "Durata non valida");
							      }
							  };
	    oreSpinner.addFocusListener(oreFocusListener);
	    minutiSpinner.addFocusListener(minutiFocusListener);

	  
	    dateListener = new DateChangeListener()
					   {
					       @Override
					       public void dateChanged(DateChangeEvent event)
					       {
					          showError(datePicker, datePicker.getDate() == null, "Data obbligatoria");
					       }
					   };
	    datePicker.addDateChangeListener(dateListener);


		/**
		 * Listener che attiva la validazione al termine del focus sul campo data.
		 */
	    dateFocusListener = new FocusAdapter()
						    {
						        @Override
						        public void focusLost(FocusEvent e)
						        {
						        	showError(datePicker, datePicker.getDate() == null, "Data obbligatoria");
						        }
						    };
	    // Nota: getComponent(0) è il campo testo
	    datePicker.getComponent(0).addFocusListener(dateFocusListener);

	 
	    timeListener = new TimeChangeListener()
					   {
					       @Override
					       public void timeChanged(TimeChangeEvent event)
					       {
					           showError(timePicker, timePicker.getTime() == null, "Orario obbligatorio");
					       }
					   };
	    timePicker.addTimeChangeListener(timeListener);

	    /**
	     * Listener che attiva la validazione al termine del focus sul campo orario.
	     */
	    timeFocusListener = new FocusAdapter()
						    {
						        @Override
						        public void focusLost(FocusEvent e)
						        {
						        	showError(timePicker, timePicker.getTime() == null, "Orario obbligatorio");
						        }
						    };
	    timePicker.getComponent(0).addFocusListener(timeFocusListener);

	    /**
	     * Listener su label x che svuota il campo di ricerca delle ricette
	     */
	    if(pratica && clearButton != null)
	    {
	    	clearButtonListener = new MouseAdapter()
								    	 {
								    	     @Override
								    	     public void mouseClicked(MouseEvent e)
								    	     {
								    	         ricercaRicetteField.setText("");
								    	     }
								    	 };
	        clearButton.addMouseListener(clearButtonListener);
	    }
	    
	
	    if(pratica && ricercaRicetteField != null)
	    {
	    	ricercaRicetteFieldListener = new DocumentListener()
										  {
										   	   private void aggiornaFiltro()
										   	   {
										    	  String filtro = ricercaRicetteField.getText().trim().toLowerCase();
										    	  clearButton.setVisible(!filtro.isEmpty());
								
										    	  ricettePanel.removeAll();
								
										    	  // Prima inserisco quelli matching
										    	  for(int i = 0; i < namesRecipes.size(); i++)
										    	  {
										    	      String name = namesRecipes.get(i);
										    	      JCheckBox cb = ricettaChecks.get(i);

										    	      if(name.toLowerCase().contains(filtro))
										    	      {
										    	          ricettePanel.add(cb, "growx");
										    	          cb.setVisible(true);
										    	      }
										    	  }
								
										    	  ricettePanel.revalidate();
										    	  ricettePanel.repaint();
										    	}
										        @Override public void insertUpdate(DocumentEvent e) { aggiornaFiltro(); }
										        @Override public void removeUpdate(DocumentEvent e) { aggiornaFiltro(); }
										        @Override public void changedUpdate(DocumentEvent e) { aggiornaFiltro(); }
										    };
		    ricercaRicetteField.getDocument().addDocumentListener(ricercaRicetteFieldListener);
	    }
	}

	/**
	 * Imposta un listener per i cambiamenti di data sul DatePicker associato a questa sessione.
	 * 
	 * Se un listener era già presente, viene rimosso prima di aggiungere il nuovo.
	 * Questo evita l'accumulo di listener multipli che causerebbero chiamate ripetute.
	 * 
	 */
	public void setDateChangeListener(DateChangeListener listener)
	{
	    if(dateChangeListener != null)
	        getDatePicker().removeDateChangeListener(dateChangeListener);

	    this.dateChangeListener = listener;

	    if(listener != null)
	        getDatePicker().addDateChangeListener(listener);
	}
	
	 /**
     * Rimuove in modo sicuro tutti i listener registrati per prevenire memory leak
     * quando il pannello viene rimosso dal dialog.
     */
	public void disposeListeners()
	{
	    if(removeBtn != null && removeBtnActionListener != null)
	    {
	        removeBtn.removeActionListener(removeBtnActionListener);
	        removeBtnActionListener = null;
	    }

	    if(addressField != null && addressListener != null)
	    {
	        addressField.getDocument().removeDocumentListener(addressListener);
	        addressListener = null;
	    }
	    
	    if(addressField != null && addressFocusListener != null)
	    {
	        addressField.removeFocusListener(addressFocusListener);
	        addressFocusListener = null;
	    }

	    if(linkField != null && linkListener != null)
	    {
	        linkField.getDocument().removeDocumentListener(linkListener);
	        linkListener = null;
	    }
	    
	    if(linkField != null && linkFocusListener != null)
	    {
	        linkField.removeFocusListener(linkFocusListener);
	        linkFocusListener = null;
	    }

	    if(durataChangeListener != null)
	    {
	        if (oreSpinner != null)
	            oreSpinner.removeChangeListener(durataChangeListener);
	        if (minutiSpinner != null)
	            minutiSpinner.removeChangeListener(durataChangeListener);
	        durataChangeListener = null;
	    }
	    
	    if(oreSpinner != null && oreFocusListener != null)
	    {
	        oreSpinner.removeFocusListener(oreFocusListener);
	        oreFocusListener = null;
	    }
	    
	    if(minutiSpinner != null && minutiFocusListener != null)
	    {
	        minutiSpinner.removeFocusListener(minutiFocusListener);
	        minutiFocusListener = null;
	    }

	    if(datePicker != null && dateListener != null)
	    {
	        datePicker.removeDateChangeListener(dateListener);
	        dateListener = null;
	    }
	    
	    if(datePicker != null && dateFocusListener != null)
	    {
	        datePicker.getComponent(0).removeFocusListener(dateFocusListener);
	        dateFocusListener = null;
	    }
	    
	    if(datePicker != null && dateChangeListener != null)
        {
            datePicker.removeDateChangeListener(dateChangeListener);
            dateChangeListener = null;
        }
	    
	    if(timePicker != null && timeListener != null)
	    {
	        timePicker.removeTimeChangeListener(timeListener);
	        timeListener = null;
	    }
	    
	    if(timePicker != null && timeFocusListener != null)
	    {
	        timePicker.getComponent(0).removeFocusListener(timeFocusListener);
	        timeFocusListener = null;
	    }

	    if(clearButton != null && clearButtonListener != null)
	    {
	    	clearButton.removeMouseListener(clearButtonListener);
	    	clearButtonListener = null;
	    }
	    
	    if(ricercaRicetteField != null && ricercaRicetteFieldListener != null)
	    {
		    ricercaRicetteField.getDocument().removeDocumentListener(ricercaRicetteFieldListener);
		    ricercaRicetteFieldListener = null;
	    }
	}
	
    public void popolaDatiSessione(LocalDate dataSessione, LocalTime orario, int durataMinuti, String link, String indirizzo, List<Integer> idRicetteSelezionate)
    {
        LocalDate today = LocalDate.now();
        boolean isPassedOrToday = dataSessione != null && !dataSessione.isAfter(today);

        DatePickerSettings s = datePicker.getSettings();
        DateVetoPolicy oldVeto = s.getVetoPolicy();
        s.setVetoPolicy(null);
        datePicker.setDate(dataSessione);

        timePicker.setTime(orario);
        oreSpinner.setValue(durataMinuti / 60);
        minutiSpinner.setValue(durataMinuti % 60);

        if(isPassedOrToday)
        {
            datePicker.setEnabled(false);
            timePicker.setEnabled(false);
            oreSpinner.setEnabled(false);
            minutiSpinner.setEnabled(false);
            removeBtn.setVisible(false);

            s.setVetoPolicy(d -> d != null && dataSessione != null && d.equals(dataSessione));

            if(pratica)
            {
                if(addressField != null) addressField.setEnabled(false);
                if(ricercaRicetteField != null) ricercaRicetteField.setEnabled(false);

                idsSelectedRecipes.clear();
                if(idRicetteSelezionate != null)
                {
                    for(int i = 0; i < idsRecipes.size(); i++)
                    {
                        JCheckBox cb = ricettaChecks.get(i);
                        cb.setEnabled(false);
                        if(idRicetteSelezionate.contains(idsRecipes.get(i)))
                        {
                            cb.setSelected(true);
                            idsSelectedRecipes.add(idsRecipes.get(i));
                        }
                        else cb.setSelected(false);
                    }
                }
                else
                {
                    for(JCheckBox cb : ricettaChecks)
                    {
                        cb.setEnabled(false);
                        cb.setSelected(false);
                    }
                }
                if(addressField != null) addressField.setText(indirizzo);
            }
            else
            {
                if(linkField != null)
                {
                    linkField.setEnabled(false);
                    linkField.setText(link);
                }
            }

            if(dateChangeListener != null)
                datePicker.removeDateChangeListener(dateChangeListener);
        }
        else
        {
            datePicker.setEnabled(true);
            timePicker.setEnabled(true);
            oreSpinner.setEnabled(true);
            minutiSpinner.setEnabled(true);
            removeBtn.setVisible(true);

            if(oldVeto == null || dataSessione == null || oldVeto.isDateAllowed(dataSessione))
                s.setVetoPolicy(oldVeto);
            else
                s.setVetoPolicy(null);

            if(pratica)
            {
                if(addressField != null) addressField.setEnabled(true);
                if(ricercaRicetteField != null) ricercaRicetteField.setEnabled(true);

                idsSelectedRecipes.clear();
                for(int i = 0; i < idsRecipes.size(); i++)
                {
                    JCheckBox cb = ricettaChecks.get(i);
                    cb.setEnabled(true);
                    if(idRicetteSelezionate != null && idRicetteSelezionate.contains(idsRecipes.get(i)))
                        cb.setSelected(true);
                    else
                        cb.setSelected(false);
                }
                if(addressField != null) addressField.setText(indirizzo);
            }
            else
            {
                if(linkField != null)
                {
                    linkField.setEnabled(true);
                    linkField.setText(link);
                }
            }
        }
    }

	/**
	 * Controlla se la data della sessione è uguale o precedente ad oggi.
	 */
	public boolean isPassed()
	{
	    LocalDate sessionDate = getDataSessione();
	    return sessionDate != null && !sessionDate.isAfter(LocalDate.now());
	}
	
	
	public void setDataPrevista(LocalDate minDate, LocalDate maxDate, boolean disable)
	{
	    if(disable)
	    {
	        datePicker.setEnabled(false);
	        if(minDate != null) 
	        	datePicker.setDate(minDate);
	        datePicker.getSettings().setVetoPolicy(null);
	        if(dateChangeListener != null)
	            datePicker.removeDateChangeListener(dateChangeListener);
	    }
	    else
	    {
	        datePicker.setEnabled(true);
	        LocalDate current = datePicker.getDate();
	        boolean changed = false;
	        if(current == null || current.isBefore(minDate) || (maxDate != null && current.isAfter(maxDate))) 
	        {
	            if(minDate != null) 
	            {
	                datePicker.setDate(minDate);
	                changed = true;
	            }
	        }
	        
	       if(changed && parent != null && dateChangeListener != null) 
	            SwingUtilities.invokeLater(() -> parent.rescheduleSessions());
	    }
	}
	

	private JXLabel createLabel(String text)
	{
	    JXLabel label = new JXLabel(text);
	    label.setFont(fieldFont);
	    return label;
	}
	

	private void showError(JComponent comp, boolean errore, String tooltip)
	{
		if(errore)
		{
			comp.putClientProperty("JComponent.outline", "error");
			if(tooltip != null) 
				comp.setToolTipText(tooltip);
		}
		else
		{
			comp.putClientProperty("JComponent.outline", null);
			comp.setToolTipText(null);
		}
	}

	public void aggiornaNumero(int nuovoNumero)
	{
		numero = nuovoNumero;
		numeroLabel.setText("Sessione #" + nuovoNumero);
	}

    public void setPanelEnabled(boolean enabled)
    {
        datePicker.setEnabled(enabled);
        timePicker.setEnabled(enabled);
        oreSpinner.setEnabled(enabled);
        minutiSpinner.setEnabled(enabled);
        removeBtn.setEnabled(enabled);

        if(addressField != null)
        {
            addressField.setEnabled(enabled);
            ricercaRicetteField.setEnabled(enabled);
            scrollRicette.setEnabled(enabled);
            ricettePanel.setEnabled(enabled);
            for(JCheckBox cb : ricettaChecks)
                cb.setEnabled(enabled);
            if(clearButton != null)
                clearButton.setEnabled(enabled);
        }
        else if (linkField != null)
        {
            linkField.setEnabled(enabled);
        }
        
        if (!enabled)
        {
           this.setFocusable(false); 
           KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
        }
        else
        {
            this.setFocusable(true);
        }
    }
	
	
	public boolean isValidSession()
	{
	    focusSet = false;
	    boolean valido = true;
	    String errori = new String();

	    if(!validateData())
	    {
	        valido = false;
	        errori += "• Data sessione obbligatoria\n";
	    }
	    
	    if(!validateTime())
	    {
	        valido = false;
	        errori += "• Orario sessione obbligatorio\n";
	    }

	    if(!validateDurata())
	    {
	        valido = false;
	        errori += "• Durata sessione obbligatoria\n";
	    }
	    
	    if(pratica)
	    {
	    	if(!validateRicetta())
		    {
		        valido = false;
		        errori += "• Seleziona almeno una ricetta\n";
		    }
	    	if(!validateAddress())
		    {
		        valido = false;
		        errori += "• Indirizzo della sessione pratica obbligatorio\n";
		    }
	    }
	    else
	    	if(!validateLink())
		    {
		        valido = false;
		        errori += "• Link Riunione della sessione online obbligatoria\n";
		    }
	    
	    if(!valido)
	        JOptionPane.showMessageDialog(this, errori.toString(), "Campi mancanti per la sessione #" + this.numero , JOptionPane.ERROR_MESSAGE);
	    
	    return valido;
	}


	private boolean validateField(JComponent comp, boolean errore, String tooltip)
	{
	    showError(comp, errore, tooltip);
	    if(errore && !focusSet)
	    {
	        comp.requestFocusInWindow();
	        focusSet = true;
	    }
	    return !errore;
	}


	private boolean validateData()
	{
	    return validateField(datePicker, datePicker.getDate() == null, "Data obbligatoria");
	}


	private boolean validateTime()
	{
	    return validateField(timePicker, timePicker.getTime() == null, "Orario obbligatorio");
	}

	
	private boolean validateDurata()
	{
	    return validateField(oreSpinner, getDurata() <= 0, "Durata non valida") && validateField(minutiSpinner, getDurata() <= 0, "Durata non valida");
	}

	
	private boolean validateAddress()
	{
	    return validateField(addressField, addressField.getText().trim().isEmpty(), "Indirizzo obbligatorio");
	}

	private boolean validateRicetta()
	{
	    boolean errore = idsSelectedRecipes.isEmpty();
	    showError(scrollRicette, errore, "Seleziona almeno una ricetta");

	    if(errore && !focusSet)
	    {
	        for(JCheckBox cb : ricettaChecks)
	        {
	            if(cb.isVisible() && !cb.isSelected())
	            {
	                cb.requestFocusInWindow();
	                scrollRicette.getViewport().scrollRectToVisible(cb.getBounds());
	                focusSet = true;
	                break;
	            }
	        }
	        
	        if(!focusSet && ricercaRicetteField != null)
	        {
	            ricercaRicetteField.requestFocusInWindow();
	            scrollRicette.getViewport().scrollRectToVisible(ricercaRicetteField.getBounds());
	            focusSet = true;
	        }
	    }

	    return !errore;
	}


	private boolean validateLink()
	{
	    return validateField(linkField, linkField.getText().trim().isEmpty(), "Link riunione obbligatorio");
	}
}