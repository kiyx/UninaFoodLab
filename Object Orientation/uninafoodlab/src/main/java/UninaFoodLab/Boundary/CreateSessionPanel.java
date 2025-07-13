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
import java.util.List;

/**
 * {@code CreateSessionPanel} rappresenta un pannello grafico Swing per la creazione di una singola
 * sessione (pratica o online) all'interno del dialog {@link CreateCourseDialog}.
 *
 * <p>Il pannello consente all'utente di configurare i dettagli relativi a una sessione specifica:
 * <ul>
 *   <li>Tipo di sessione (pratica o online)</li>
 *   <li>Data e orario</li>
 *   <li>Durata (in ore e minuti)</li>
 *   <li>Indirizzo e ricetta (solo per sessioni pratiche)</li>
 *   <li>Link della riunione (solo per sessioni online)</li>
 * </ul>
 *
 * <p>Il pannello fornisce validazione in tempo reale sui campi obbligatori, gestione dinamica dei listener
 * e supporto per la rimozione della sessione tramite callback al pannello genitore.
 *
 */

public class CreateSessionPanel extends JXPanel
{
	private static final long serialVersionUID = 1L;

	private Font fieldFont = new Font("Segoe UI", Font.PLAIN, 12);
	private int numero;
	private boolean pratica;
	private CreateCourseDialog parent;

	private JXPanel durataPanel;
	private JXLabel numeroLabel;
	private DatePicker datePicker;
	private TimePicker timePicker;
	private JSpinner oreSpinner, minutiSpinner;
	private JXButton removeBtn;
	private JXTextField addressField, linkField;
	
	private ActionListener removeBtnActionListener;
	private DocumentListener addressListener, linkListener;
	private ChangeListener durataChangeListener;
	private TimeChangeListener timeListener;
	private DateChangeListener dateListener;
	private ItemListener ricettaListener;

	private JComboBox<Ricetta> ricettaCombo;
	

	 /**
     * Costruisce un nuovo pannello per la creazione di una sessione.
     *
     * @param numero Numero progressivo della sessione (per l'etichetta)
     * @param pratica True se la sessione è pratica, false se online
     * @param parent Il dialog genitore a cui notificare la rimozione
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
     */
	public String getTipo()
	{
		return pratica ? "Pratica" : "Online";
	}

	 /**
     * Restituisce la data selezionata per la sessione.
     *
     */
	public LocalDate getData()
	{
		return datePicker.getDate();
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
     * Restituisce la durata della sessione in minuti.
     *
     */
	public int getDurata()
	{
		int ore = (int) oreSpinner.getValue();
		int minuti = (int) minutiSpinner.getValue();
		return ore * 60 + minuti;
	}
	
	 /**
     * Restituisce la ricetta selezionata (solo per sessioni pratiche).
     *
     */
	public String getRicetta()
	{
		return pratica && ricettaCombo.getSelectedItem() != null ? ricettaCombo.getSelectedItem().toString().trim() : null;
	}

	
	/**
     * Restituisce il link riunione (solo per sessioni online).
     *
     */
	public String getLinkRiunione()
	{
		return !pratica ? linkField.getText().trim() : null;
	}

	 /**
     * Restituisce l'indirizzo della sessione (solo se pratica).
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

		// Spinner per ore
		oreSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 24, 1));
		oreSpinner.setFont(fieldFont);
		((JSpinner.DefaultEditor) oreSpinner.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		oreSpinner.setToolTipText("Ore di durata");

		// Spinner per minuti 
		minutiSpinner = new JSpinner(new SpinnerListModel(new Integer[] {0, 15, 30, 45, 59}));
		((JSpinner.DefaultEditor) minutiSpinner.getEditor()).getTextField().setHorizontalAlignment(SwingConstants.CENTER);
		minutiSpinner.setFont(fieldFont);
		minutiSpinner.setToolTipText("Minuti di durata");

		// Pannello combinato
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

			add(createLabel("Ricetta:"));
			List<Ricetta> ricette = Controller.getController().loadRicette();
			ricettaCombo = new JComboBox<>(ricette.toArray(new Ricetta[0]));
			ricettaCombo.setFont(fieldFont);
			ricettaCombo.setEditable(false);
			add(ricettaCombo, "h 30!");
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
     * Inizializza i listener per la validazione e gli eventi utente.
     */
	private void initListeners()
	{
	    // Remove button listener
	    removeBtnActionListener = new ActionListener()
	    {
	        @Override
	        public void actionPerformed(ActionEvent e)
	        {
	            parent.removeSessionCard(CreateSessionPanel.this);
	        }
	    };
	    removeBtn.addActionListener(removeBtnActionListener);

	    // DocumentListener for addressField (only if pratica)
	    if (pratica && addressField != null)
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
	    }

	    // DocumentListener for linkField (only if non pratica)
	    if (!pratica && linkField != null)
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
	    }

	    // ChangeListener for durata spinners (ore and minuti)
	    durataChangeListener = new ChangeListener()
	    {
	        @Override
	        public void stateChanged(ChangeEvent e)
	        {
	            int durata = getDurata();
	            boolean errore = durata <= 0;
	            showError(oreSpinner, errore, errore ? "Durata non valida" : null);
	            showError(minutiSpinner, errore, errore ? "Durata non valida" : null);
	        }
	    };
	    oreSpinner.addChangeListener(durataChangeListener);
	    minutiSpinner.addChangeListener(durataChangeListener);

	    // DateChangeListener for datePicker
	    dateListener = new DateChangeListener()
	    {
	        @Override
	        public void dateChanged(DateChangeEvent event)
	        {
	            showError(datePicker, datePicker.getDate() == null, "Data obbligatoria");
	        }
	    };
	    datePicker.addDateChangeListener(dateListener);

	    // TimeChangeListener for timePicker
	    timeListener = new TimeChangeListener()
	    {
	        @Override
	        public void timeChanged(TimeChangeEvent event)
	        {
	            showError(timePicker, timePicker.getTime() == null, "Orario obbligatorio");
	        }
	    };
	    timePicker.addTimeChangeListener(timeListener);

	    // ItemListener for ricettaCombo (only if pratica)
	    if (pratica && ricettaCombo != null)
	    {
	        ricettaListener = new ItemListener()
	        {
	            @Override
	            public void itemStateChanged(ItemEvent e)
	            {
	                if(e.getStateChange() == ItemEvent.SELECTED)
	                {
	                    Ricetta selected = (Ricetta) ricettaCombo.getSelectedItem();
	                    boolean errore = (selected == null || selected.toString().trim().isEmpty());
	                    showError(ricettaCombo, errore, "Ricetta obbligatoria");
	                }
	            }
	        };
	        ricettaCombo.addItemListener(ricettaListener);
	    }
	}

	/**
	 * Disconnette tutti i listener associati ai componenti di input.
	 * <p>Chiamare questo metodo prima di rimuovere il pannello per evitare memory leak.</p>
	 */
	public void disposeListeners()
	{
	    if(removeBtn != null && removeBtnActionListener != null)
	    {
	        removeBtn.removeActionListener(removeBtnActionListener);
	        removeBtnActionListener = null;
	    }

	    if(addressListener != null && addressField != null)
	    {
	        addressField.getDocument().removeDocumentListener(addressListener);
	        addressListener = null;
	    }

	    if(linkListener != null && linkField != null)
	    {
	        linkField.getDocument().removeDocumentListener(linkListener);
	        linkListener = null;
	    }

	    if(durataChangeListener != null)
	    {
	        if (oreSpinner != null)
	            oreSpinner.removeChangeListener(durataChangeListener);
	        if (minutiSpinner != null)
	            minutiSpinner.removeChangeListener(durataChangeListener);
	        
	        durataChangeListener = null;
	    }

	    if(dateListener != null && datePicker != null)
	    {
	        datePicker.removeDateChangeListener(dateListener);
	        dateListener = null;
	    }

	    if(timeListener != null && timePicker != null)
	    {
	        timePicker.removeTimeChangeListener(timeListener);
	        timeListener = null;
	    }

	    if(ricettaListener != null && ricettaCombo != null)
	    {
	        ricettaCombo.removeItemListener(ricettaListener);
	        ricettaListener = null;
	    }
	}

	/**
	 * Aggiorna il numero visualizzato della sessione.
	 *
	 * @param nuovoNumero il nuovo numero da assegnare alla sessione
	 */
	public void aggiornaNumero(int nuovoNumero)
	{
		numero = nuovoNumero;
		numeroLabel.setText("Sessione #" + nuovoNumero);
	}

	/**
	 * Valida tutti i campi obbligatori della sessione, mostrando errori visivi se presenti.
	 *
	 * @return {@code true} se tutti i campi obbligatori sono compilati correttamente, {@code false} altrimenti
	 */
	public boolean isValidSession()
	{
		boolean valido = true;

		if(datePicker.getDate() == null)
		{
			showError(datePicker, true, "Data obbligatoria");
			valido = false;
		}
		else
			showError(datePicker, false, null);

		if(timePicker.getTime() == null)
		{
			showError(timePicker, true, "Orario obbligatorio");
			valido = false;
		}
		else
			showError(timePicker, false, null);

		if(getDurata()<= 0)
		{
			showError(oreSpinner, true, "Durata non valida");
			showError(minutiSpinner, true, "Durata non valida");
			valido = false;
		}
		else
		{
			showError(oreSpinner, false, null);
			showError(minutiSpinner, false, null);
		}

		if(pratica)
		{
			Ricetta selected = (Ricetta) ricettaCombo.getSelectedItem();
			if(selected == null || selected.toString().trim().isEmpty())
			{
				showError(ricettaCombo, true, "Ricetta obbligatoria");
				valido = false;
			}
			else
				showError(ricettaCombo, false, null);

			if(addressField.getText().trim().isEmpty())
			{
				showError(addressField, true, "Indirizzo obbligatorio");
				valido = false;
			}
			else
				showError(addressField, false, null);
		}
		else
		{
			if(linkField.getText().trim().isEmpty())
			{
				showError(linkField, true, "Link riunione obbligatorio");
				valido = false;
			}
			else
				showError(linkField, false, null);
		}

		return valido;
	}

	 /**
     * Crea un'etichetta JXLabel con lo stile coerente.
     *
     * @param text Il testo dell'etichetta
     * @return Un JXLabel formattato
     */
	private JXLabel createLabel(String text)
	{
	    JXLabel label = new JXLabel(text);
	    label.setFont(fieldFont);
	    return label;
	}
	
	 /**
     * Applica o rimuove la visualizzazione di un errore su un componente.
     *
     * @param comp Componente su cui applicare l'errore
     * @param errore true se l'errore deve essere mostrato, false altrimenti
     * @param tooltip Messaggio da mostrare come tooltip (null per rimuoverlo)
     */
	private void showError(JComponent comp, boolean errore, String tooltip)
	{
		if(errore)
		{
			comp.putClientProperty("JComponent.outline", "error");
			if (tooltip != null) comp.setToolTipText(tooltip);
		}
		else
		{
			comp.putClientProperty("JComponent.outline", null);
			comp.setToolTipText(null);
		}
	}
}