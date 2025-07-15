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
import UninaFoodLab.DTO.Ricetta;

import net.miginfocom.swing.MigLayout;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

/**
 * La classe {@code CreateSessionPanel} rappresenta un pannello Swing responsabile della
 * configurazione, validazione e gestione interattiva di una singola sessione, sia pratica
 * che online, all'interno del dialogo {@link CreateCourseDialog}.
 * 
 * <p>Ogni pannello sessione include:</p>
 * <ul>
 *   <li>Selezione della data e orario</li>
 *   <li>Configurazione della durata tramite ore/minuti</li>
 *   <li>Input dell'indirizzo (per sessioni pratiche)</li>
 *   <li>Selezione multipla di ricette (per sessioni pratiche)</li>
 *   <li>Link riunione (per sessioni online)</li>
 *   <li>Validazione e focus automatico sui campi non validi</li>
 *   <li>Rimozione dinamica del pannello dal dialogo genitore</li>
 *   <li>Init e dispose sicura dei listener</li>
 * </ul>
 */

public class CreateSessionPanel extends JXPanel
{
	private static final long serialVersionUID = 1L;

	private Font fieldFont = new Font("Segoe UI", Font.PLAIN, 12);
	private int numero;
	private boolean pratica;
	private CreateCourseDialog parent;

	// Componenti principali UI
	private JXPanel durataPanel;
	private JXLabel numeroLabel, clearButton;
	private DatePicker datePicker;
	private TimePicker timePicker;
	private JSpinner oreSpinner, minutiSpinner;
	private JXButton removeBtn;
	private JXTextField addressField, linkField;
	private boolean focusSet = false;

	// Listeners sui campi e di validazione
	private ActionListener removeBtnActionListener;
	private DocumentListener addressListener, linkListener, ricercaRicetteFieldListener;
	private ChangeListener durataChangeListener;
	private TimeChangeListener timeListener;
	private DateChangeListener dateListener, dateChangeListener;
	private FocusListener addressFocusListener, linkFocusListener, dateFocusListener, timeFocusListener,
						  oreFocusListener, minutiFocusListener;
	private MouseAdapter clearButtonListener;

	// Componenti per show + gestione dinamica filtrata delle ricette
	private JPanel ricettePanel;
	private JScrollPane scrollRicette;
	private JXTextField ricercaRicetteField;
	private List<Ricetta> ricette;
	private List<JCheckBox> ricettaChecks = new ArrayList<>();
	private List<Integer> ricetteSelezionate = new ArrayList<>();

	 /**
     * Costruttore principale per creare un pannello di sessione.
     * 
     * @param numero Numero identificativo progressivo della sessione
     * @param pratica {@code true} se la sessione è pratica, {@code false} se online
     * @param parent Riferimento al dialogo contenitore {@link CreateCourseDialog}
     */
	public CreateSessionPanel(int numero, boolean pratica, CreateCourseDialog parent)
	{
		this.numero = numero;
		this.pratica = pratica;
		this.parent = parent;
		
		initComponents();
		initListeners();
	}
	
	/**
	 * Restituisce il tipo della sessione come stringa.
	 *
	 * @return "Pratica" se la sessione è pratica, "Online" altrimenti
	 */
	public String getTipo()
	{
		return pratica ? "Pratica" : "Online";
	}

	/**
	 * Restituisce la data selezionata per la sessione.
	 *
	 * @return Oggetto {@link LocalDate} rappresentante la data scelta, oppure {@code null} se non selezionata
	 */
	public LocalDate getDataSessione()
	{
		return datePicker.getDate();
	}
	
	/**
	 * Imposta la data selezionata nel componente {@link DatePicker}.
	 * 
	 * @param data Data da impostare
	 */
	public void setData(LocalDate data)
	{
	    datePicker.setDate(data);
	}

	/**
	 * Restituisce il componente {@link DatePicker} della data.
	 * 
	 * @return Il componente {@link DatePicker} utilizzato per la selezione della data
	 */
	public DatePicker getDatePicker()
	{
		return datePicker;
	}
	
	/**
	 * Restituisce il listener registrato per gli eventi di cambio data associato a questo pannello.
	 *
	 * @return l'istanza di {@link DateChangeListener} attualmente registrata,
	 *         oppure {@code null} se nessun listener è stato impostato.
	 */
	public DateChangeListener getDateChangeListener()
	{
		return dateChangeListener;
	}
	
	/**
	 * Restituisce l'orario selezionato per la sessione.
	 *
	 * @return Oggetto {@link LocalTime} rappresentante l'orario scelto, oppure {@code null} se non selezionato
	 */
	public LocalTime getOrario()
	{
		return timePicker.getTime();
	}

	/**
	 * Restituisce la durata totale della sessione in minuti.
	 *
	 * @return Valore intero pari a (ore * 60 + minuti)
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
	 * @return Lista di ID numerici delle ricette selezionate
	 */
	public List<Integer> getIdRicetteSelezionate()
	{
	    if(!pratica)
	        throw new IllegalStateException("La sessione non è pratica: nessuna ricetta selezionabile.");
	    return new ArrayList<>(ricetteSelezionate);
	}

	/**
	 * Restituisce il link della riunione (solo per sessioni online).
	 *
	 * @return Stringa contenente il link della riunione, oppure {@code null} per sessioni pratiche
	 */
	public String getLinkRiunione()
	{
		return !pratica ? linkField.getText().trim() : null;
	}

	/**
	 * Restituisce l'indirizzo della sessione (solo per sessioni pratiche).
	 *
	 * @return Stringa contenente l'indirizzo, oppure {@code null} per sessioni online
	 */
	public String getIndirizzo()
	{
		return pratica && addressField != null ? addressField.getText().trim() : null;
	}
	
	/**
	 * Imposta la politica di selezione della data consentita.
	 * <p>Se {@code disable} è vero, disabilita il selettore e imposta una data fissa.</p>
	 * 
	 * @param minDate Data minima consentita (inclusa)
	 * @param maxDate Data massima consentita (inclusa), o {@code null} per nessun limite superiore
	 * @param disable Se {@code true}, disabilita la selezione e imposta la data a {@code minDate}
	 */
	public void setDataPrevista(LocalDate minDate, LocalDate maxDate, boolean disable)
    {
        if(disable)
        {
            datePicker.setEnabled(false);
            datePicker.setDate(minDate);
        }
        else
        {
            datePicker.setEnabled(true);
            datePicker.getSettings().setVetoPolicy(new DateVetoPolicy()
            {
                @Override
                public boolean isDateAllowed(LocalDate date)
                {
                    if(date == null)
                        return false;
                    return (!date.isBefore(minDate)) && (!date.isAfter(maxDate));
                }
            });

            LocalDate current = datePicker.getDate();
            if(current == null || current.isBefore(minDate) || current.isAfter(maxDate))
                datePicker.setDate(minDate);
        }
    }

	/**
     * Inizializza i componenti grafici del pannello.
     */
	private void initComponents()
	{
		// Inizializza il layout e lo stile del pannello
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

		// Label con numero sessione
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

		// Spinner per ore e minuti della durata con formattazione centrata
		oreSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 23, 1));
		oreSpinner.setFont(fieldFont);
		((JSpinner.DefaultEditor) oreSpinner.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		oreSpinner.setToolTipText("Ore di durata");

		// Spinner per minuti 
		minutiSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 5));
		((JSpinner.DefaultEditor) minutiSpinner.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		minutiSpinner.setFont(fieldFont);
		minutiSpinner.setToolTipText("Minuti di durata");

		// Costruzione del pannello durata con etichette "h" e "m"
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

		    // Campo ricerca sopra la lista ricette
		    ricercaRicetteField = new JXTextField();
		    ricercaRicetteField.setFont(fieldFont);
		    ricercaRicetteField.putClientProperty("JTextField.placeholderText", "Cerca ricetta...");
		    ricercaRicetteField.setLayout(new BorderLayout());

		    clearButton = new JXLabel(FontIcon.of(MaterialDesign.MDI_CLOSE_CIRCLE_OUTLINE, 16, new Color(150, 150, 150)));
		    clearButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		    clearButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
		    clearButton.setVisible(false); // appare solo quando c'è testo

		    ricercaRicetteField.add(clearButton, BorderLayout.EAST);
		    add(ricercaRicetteField, "span, growx, gaptop 10, gapbottom 5");

		    ricettePanel = new JXPanel(new MigLayout("wrap 1, insets 0, gap 4", "[grow,fill]"));
		    ricettePanel.setAutoscrolls(true);
		    ricettePanel.setOpaque(false);
		    ricettePanel.setBackground(Color.WHITE);

		    // Inizializzazione e popolamento lista ricette (solo se pratica)
		    ricette = Controller.getController().loadRicette();

		    for(Ricetta r : ricette)
		    {
		        JCheckBox cb = new JCheckBox(r.getNome());
		        
		        // Impostazioni base checkbox (font, focus, cursor)
		        cb.setFont(fieldFont);
		        cb.setFocusPainted(false);
		        cb.setOpaque(false);
		        cb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		        
		        // Listener per aggiornare set quando selezionato/deselezionato
		        cb.addItemListener(new ItemListener() 
							       {
							           @Override
							           public void itemStateChanged(ItemEvent e) 
							           {
							        	   int id = r.getId();
							        	   if(cb.isSelected() && !ricetteSelezionate.contains(id))
							        	        ricetteSelezionate.add(id);
							        	    else
							        	        ricetteSelezionate.remove(Integer.valueOf(id));
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

	 /**
     * Inizializza e registra tutti i listener necessari per la validazione in tempo reale,
     * l'interazione utente (clic, input, focus) e la gestione dinamica del pannello. 
     * Ogni listener è associato al rispettivo campo e viene rimosso con {@link #disposeListeners()}.
     */
	private void initListeners()
	{
		/**
		 * Listener associato al pulsante di rimozione della sessione.
		 * <p>Invoca {@link CreateCourseDialog#removeSessionCard(CreateSessionPanel)} per eliminare dinamicamente il pannello dal dialogo genitore.
		 */
	    removeBtnActionListener = new ActionListener()
								  {
								      @Override
								      public void actionPerformed(ActionEvent e)
								      {
								          parent.removeSessionCard(CreateSessionPanel.this);
								      }
								  };
	    removeBtn.addActionListener(removeBtnActionListener);

	    if(pratica && addressField != null)
	    {
	    	/**
	    	 * Listener che valida il campo indirizzo in tempo reale.
	    	 * <p>Attivato su ogni modifica al contenuto del campo {@code addressField}.
	    	 */
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
	    	/**
	    	 * Listener che valida il campo link della riunione in tempo reale.
	    	 * <p>Attivato su ogni modifica al contenuto del campo {@code linkField}.
	    	 */
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

	    /**
	     * Listener che valida la durata della sessione ogni volta che gli spinner cambiano.
	     * <p>Controlla che ore e minuti sommati non siano zero.<p>
	     */
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

	    /**
	     * Listener per la validazione della data selezionata.
	     * <p>Attivato ogni volta che l'utente seleziona o modifica la data tramite {@code datePicker}.
	     */
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

	    /**
	     * Listener per la validazione dell’orario selezionato.
	     * <p>Attivato ogni volta che l’utente modifica l’orario nel {@code timePicker}.
	     */
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
	    
	    /**    
	     * <p><b>Filtro Ricette:</b> include un {@link DocumentListener} dinamico sul campo {@code ricercaRicetteField},
	     * che filtra in tempo reale la lista di ricette visualizzate nel pannello. Il listener:</p>
	     * <ul>
	     *   <li>Recupera il testo di filtro digitato</li>
	     *   <li>Mostra o nasconde il pulsante di reset</li>
	     *   <li>Rimuove tutte le checkbox dal pannello</li>
	     *   <li>Riaggiunge solo le checkbox che matchano il filtro</li>
	     *   <li>Effettua repaint e revalidate del pannello ricette</li>
	     * </ul>
	     */
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
										    	  for(int i = 0; i < ricette.size(); i++)
										    	  {
										    	      Ricetta r = ricette.get(i);
										    	      JCheckBox cb = ricettaChecks.get(i);

										    	      if(r.getNome().toLowerCase().contains(filtro))
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
	 * @param listener il nuovo {@link DateChangeListener} da associare;
	 *                 può essere {@code null} per rimuovere il listener esistente senza sostituirlo.
	 */
	public void setDateChangeListener(DateChangeListener listener)
	{
	    if(this.dateChangeListener != null)
	        getDatePicker().removeDateChangeListener(this.dateChangeListener);

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
	/**
	 * Crea un'etichetta {@link JXLabel} con font coerente.
	 *
	 * @param text Testo da visualizzare nell'etichetta
	 * @return Etichetta formattata
	 */
	private JXLabel createLabel(String text)
	{
	    JXLabel label = new JXLabel(text);
	    label.setFont(fieldFont);
	    return label;
	}
	
	/**
     * Mostra o rimuove un errore visivo su un componente Swing con tooltip e outline.
     * 
     * @param comp Componente target (es. JTextField, JSpinner)
     * @param errore {@code true} per mostrare l'errore, {@code false} per rimuoverlo
     * @param tooltip Messaggio di errore da visualizzare come tooltip
     */
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
	
	/**
	 * Aggiorna l'etichetta con il nuovo numero progressivo della sessione.
	 *
	 * @param nuovoNumero Nuovo numero da visualizzare
	 */
	public void aggiornaNumero(int nuovoNumero)
	{
		numero = nuovoNumero;
		numeroLabel.setText("Sessione #" + nuovoNumero);
	}

	 /**
     * Valida tutti i campi della sessione e imposta il focus sul primo campo errato.
     * 
     * @return {@code true} se tutti i campi obbligatori sono validi, {@code false} altrimenti
     */
	public boolean isValidSession()
	{
	    focusSet = false;
	    boolean valido = true;

	    valido &= validateData();
	    valido &= validateTime();
	    valido &= validateDurata();

	    if(pratica)
	    {
	        valido &= validateRicetta();
	        valido &= validateAddress();
	    }
	    else
	    	valido &= validateLink();

	    return valido;
	}

	/**
	 * Metodo di utilità per validare un singolo campo e impostare il focus se errato.
	 *
	 * @param comp Componente da validare
	 * @param errore {@code true} se c'è errore, {@code false} altrimenti
	 * @param tooltip Messaggio di errore da mostrare
	 * @return {@code true} se il campo è valido, {@code false} se c'è errore
	 */
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

	/**
	 * Verifica che la data sia selezionata.
	 *
	 * @return {@code true} se la data è valida, {@code false} altrimenti
	 */
	private boolean validateData()
	{
	    return validateField(datePicker, datePicker.getDate() == null, "Data obbligatoria");
	}

	/**
	 * Verifica che l'orario sia selezionato.
	 *
	 * @return {@code true} se l'orario è valido, {@code false} altrimenti
	 */
	private boolean validateTime()
	{
	    return validateField(timePicker, timePicker.getTime() == null, "Orario obbligatorio");
	}

	/**
	 * Verifica che la durata sia maggiore di zero.
	 *
	 * @return {@code true} se la durata è valida, {@code false} altrimenti
	 */
	private boolean validateDurata()
	{
	    return validateField(oreSpinner, getDurata() <= 0, "Durata non valida") && validateField(minutiSpinner, getDurata() <= 0, "Durata non valida");
	}

	/**
	 * Verifica che l'indirizzo sia compilato correttamente (solo per sessioni pratiche).
	 *
	 * @return {@code true} se l'indirizzo è valido, {@code false} altrimenti
	 */
	private boolean validateAddress()
	{
	    return validateField(addressField, addressField.getText().trim().isEmpty(), "Indirizzo obbligatorio");
	}

	 /**
     * Verifica che una ricetta sia selezionata e scrolla sulla prima ricetta visibile non selezionata.
     * 
     * @return {@code true} se almeno una ricetta è selezionata, {@code false} altrimenti
     */
	private boolean validateRicetta()
	{
	    boolean errore = ricetteSelezionate.isEmpty();
	    showError(scrollRicette, errore, "Seleziona almeno una ricetta");

	    if(errore && !focusSet)
	    {
	        // Cerca la prima checkbox visibile e non selezionata
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
	        
	        // Fallback: campo ricerca
	        if(!focusSet && ricercaRicetteField != null)
	        {
	            ricercaRicetteField.requestFocusInWindow();
	            scrollRicette.getViewport().scrollRectToVisible(ricercaRicetteField.getBounds());
	            focusSet = true;
	        }
	    }

	    return !errore;
	}

	/**
	 * Verifica che il link della riunione sia compilato (solo per sessioni online).
	 *
	 * @return {@code true} se il link è valido, {@code false} altrimenti
	 */
	private boolean validateLink()
	{
	    return validateField(linkField, linkField.getText().trim().isEmpty(), "Link riunione obbligatorio");
	}
}