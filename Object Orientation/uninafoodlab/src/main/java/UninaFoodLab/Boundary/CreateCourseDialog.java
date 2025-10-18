package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.*;
import java.util.List;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;

import org.jdesktop.swingx.*;

import com.github.lgooddatepicker.components.*;
import com.github.lgooddatepicker.components.DatePickerSettings.*;
import com.github.lgooddatepicker.optionalusertools.*;
import com.github.lgooddatepicker.zinternaltools.*;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class CreateCourseDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    /** Colori principali utilizzati */
    private static final Color BACKGROUND_COLOR = new Color(245, 248, 250);
    private static final Color BORDER_COLOR = new Color(220, 225, 230);
    private static final Color BUTTON_COLOR = new Color(225, 126, 47, 220);

    /**
     * Bordo composto per sezioni con contorno e padding interno.
     */
    private static final CompoundBorder mainBorder = new CompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)
    );
    
    /**
     * Indica se è già stato impostato il focus sul primo componente con errore.
     */
    private boolean focusSet = false;

    /** Componenti swing */
    private JPanel argomentiPanel, scrollContentWrapper;
    private JXPanel buttons, container, detailPanel, infoPanel, formPanel, leftPanel, mainPanel, sessionPanel, sessionsContainer;
    private JXLabel aggiungiSessioneLabel, sessioniLabel, limitLabel, sessionTitle, title;
    private JXButton addBtn, cancelBtn, confirmBtn, goBackBtn;
    private JScrollPane rootScroll, scrollArgomenti, scrollDescrizione, scrollSessions;
    private JCheckBox praticoCheck; 
    private JDialog addSessionDialog;
    private JSpinner numeroSessioniSpinner, onlineSpinner, costSpinner, limitSpinner, praticheSpinner;
    private JXTextArea descrizioneArea;
    private JXTextField nameField;
    private DatePicker dataInizioField;

    /** Listeners per i vari componenti */
    private ActionListener addSessionsListener, cancelAddSessionsListener, confirmBtnListener, goBackBtnListener;
    private MouseListener aggiungiSessioniMouseListener;
    private ItemListener praticoCheckListener, argomentiCheckBoxListener, frequencyListListener;
    private DateChangeListener dataInizioListener;
    private WindowAdapter windowListener;
    
    /** ComboBox per selezionare la frequenza delle sessioni (giornaliera, settimanale, etc.). */
    private JComboBox<String> frequencyList;
    
    /** Elenco delle card delle sessioni create. */
    private List<CreateSessionPanel> sessionCards = new ArrayList<>();
    
    /** Lista di checkbox per gli argomenti caricati. */
    private List<JCheckBox> argumentsCheck = new ArrayList<>();
    
    /** Liste temporanee per id e nomi degli argomenti disponibili e selezionati. */
    private List<Integer> idsArguments = new ArrayList<>();
    private List<String> namesArguments = new ArrayList<>();
    private List<Integer> idsSelectedArguments = new ArrayList<>();
    
    private JFrame parentFrame;
    private boolean editMode;
    private int idCorsoDaModificare;
    
    public CreateCourseDialog(JFrame parentFrame, boolean editMode)
    {
        super(parentFrame, true);
        this.parentFrame = parentFrame;
        this.editMode = editMode; 

        initComponents();

        if(editMode)
        {
            setTitle("Modifica corso");
            confirmBtn.setText("Salva Modifiche");
            title.setText("Modifica i dettagli del corso");
        }
        else
        {
            setTitle("Crea corso");
            confirmBtn.setText("Crea corso");
            title.setText("Inserisci i dettagli del nuovo corso");
        }
       
        setMinimumSize(new Dimension(1670, 725));
        setPreferredSize(new Dimension(1200, 700));
        setLocationRelativeTo(parentFrame);
        setResizable(true);
        setIconImage(parentFrame.getIconImage());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
          
        initListeners();
        pack();
    }

    private void initComponents()
    {
        rootScroll = new JScrollPane();
        rootScroll.getVerticalScrollBar().setUnitIncrement(16);
        rootScroll.setBorder(null);

        container = new JXPanel(new BorderLayout());
        rootScroll.setViewportView(container);
        setContentPane(rootScroll);

        mainPanel = new JXPanel(new MigLayout("fill, insets 20", "[pref!][grow, fill]", "[grow, fill]"));
        mainPanel.setBackground(BACKGROUND_COLOR);
        container.add(mainPanel, BorderLayout.CENTER);

        initLeftPanel(mainPanel);
        initRightPanel(mainPanel);
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
        getRootPane().setDefaultButton(confirmBtn);
    }

    private void initLeftPanel(JXPanel mainPanel)
    {
    	leftPanel = new JXPanel(new MigLayout(
    		    "wrap 1, fill, insets 0",
    		    "[grow]",
    		    "[]10[grow, fill]10[]"
    		));
    	leftPanel.setBackground(BACKGROUND_COLOR);
    	leftPanel.setMinimumSize(new Dimension(400, 400));

        title = new JXLabel("Inserisci i dettagli del nuovo corso");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        leftPanel.add(title, "align center");

        formPanel = new JXPanel(new MigLayout("wrap 1, gap 10 10", "[grow, fill]", "[][pref!]"));
        formPanel.setBackground(BACKGROUND_COLOR);
        
        infoPanel = new JXPanel(new MigLayout(
        	    "wrap 2, fill, insets 10",
        	    "[right][grow, fill]",
        	    "[][][][]"
        	));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(mainBorder);

        nameField = new JXTextField();
        nameField.setToolTipText("Inserisci il nome del corso");
        infoPanel.add(new JXLabel("Nome corso:"));
        infoPanel.add(nameField, "h 36!, growx");

        descrizioneArea = new JXTextArea();
        descrizioneArea.setToolTipText("Inserisci una breve descrizione del corso");
        descrizioneArea.setLineWrap(true);
        descrizioneArea.setWrapStyleWord(true);
        scrollDescrizione = new JScrollPane(descrizioneArea);
        scrollDescrizione.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollDescrizione.setMinimumSize(new Dimension(100, 100));
        scrollDescrizione.setPreferredSize(new Dimension(100, 150));
        infoPanel.add(new JXLabel("Descrizione:"));
        infoPanel.add(scrollDescrizione, "span 2, growx, growy");

        frequencyList = new JComboBox<>(Controller.getController().loadFrequenza()); 
        infoPanel.add(new JXLabel("Frequenza:"));
        infoPanel.add(frequencyList, "h 36!, growx");
        
        argomentiPanel = new JPanel(new GridLayout(0, 1));
        argomentiPanel.setOpaque(false);

        Controller.getController().loadArgomenti(idsArguments, namesArguments);
        
        for(int i = 0; i < idsArguments.size(); i++)
        {
        	final int j = i;
            JCheckBox cb = new JCheckBox(namesArguments.get(i));
            
            cb.addItemListener( new ItemListener()
            {
                @Override
                public void itemStateChanged(ItemEvent e)
                {
                    // Questo listener è attivo solo in modalità CREAZIONE,
                    // perché in EDIT le checkbox sono disabilitate da popolaDatiCorso.
                    if(!editMode)
                    {
                        int argumentId = idsArguments.get(j);

                        if(cb.isSelected() && !idsSelectedArguments.contains(argumentId))
                        {
                            idsSelectedArguments.add(argumentId);
                        }
                        else
                        {
                            int index = idsSelectedArguments.indexOf(argumentId);
                            if(index >= 0)
                                idsSelectedArguments.remove(index);
                        }

                        // Se raggiungo il limite (5) disabilito quelle non selezionate
                        for(JCheckBox otherCb : argumentsCheck)
                            if(!otherCb.isSelected())
                               otherCb.setEnabled(!(idsSelectedArguments.size() >= 5));
                    }
                }
            });
            argumentsCheck.add(cb);
            argomentiPanel.add(cb);
        }

        scrollArgomenti = new JScrollPane(argomentiPanel);
        scrollArgomenti.setMinimumSize(new Dimension(100, 100));
        scrollArgomenti.setPreferredSize(new Dimension(100, 140));
        scrollArgomenti.setOpaque(false);
        scrollArgomenti.getViewport().setOpaque(false);
        scrollArgomenti.getVerticalScrollBar().setUnitIncrement(14);

        infoPanel.add(new JXLabel("Argomenti:"));
        infoPanel.add(scrollArgomenti, "span 2, growx, hmin 80, hmax 140");

        formPanel.add(infoPanel, "grow, push");

        detailPanel = new JXPanel(new MigLayout(
        	    "wrap 3",
        	    "[right][grow, fill][]",
        	    "[][grow 0]"
        	));
        detailPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(mainBorder);

        DateVetoPolicy vetoPolicy = new DateVetoPolicyMinimumMaximumDate(LocalDate.now().plusDays(1), null);
        DatePickerSettings settings = new DatePickerSettings(); 
        applyOrangeTheme(settings);
        dataInizioField = new DatePicker(settings);
        dataInizioField.setDate(LocalDate.now().plusDays(1));
        settings.setVetoPolicy(vetoPolicy);
        detailPanel.add(new JXLabel("Data Inizio:"));
        detailPanel.add(dataInizioField, "h 36!, growx, span 2");

        sessioniLabel = new JXLabel("Numero sessioni:");
        numeroSessioniSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        removeSpinnerButtons(numeroSessioniSpinner);
        numeroSessioniSpinner.setEnabled(false);
        numeroSessioniSpinner.setValue(0);
        ((JSpinner.DefaultEditor)numeroSessioniSpinner.getEditor()).getTextField().setColumns(5);

        detailPanel.add(sessioniLabel, "newline");
        detailPanel.add(numeroSessioniSpinner, "h 36!, growx, span 2");
        
        SpinnerNumberModel costModel = new SpinnerNumberModel(0.0, 0.0, 10000.0, 0.5);
        costSpinner = new JSpinner(costModel);
        JSpinner.NumberEditor costEditor = new JSpinner.NumberEditor(costSpinner, "#,##0.00 €");
        costSpinner.setEditor(costEditor);
        costSpinner.setPreferredSize(new Dimension(100, 36));
        detailPanel.add(new JXLabel("Costo:"));
        detailPanel.add(costSpinner, "h 36!, growx, span 2");

        praticoCheck = new JCheckBox();
        praticoCheck.setBackground(Color.WHITE);
        detailPanel.add(new JXLabel("Pratico:"));
        detailPanel.add(praticoCheck, "span 2, left");

        limitLabel = new JXLabel("Limite partecipanti:");
        limitLabel.setVisible(false);

        SpinnerNumberModel limitModel = new SpinnerNumberModel(1, 1, 1000, 1);
        limitSpinner = new JSpinner(limitModel);
        limitSpinner.setVisible(false);
        ((JSpinner.DefaultEditor)limitSpinner.getEditor()).getTextField().setColumns(5);

        detailPanel.add(limitLabel, "newline");
        detailPanel.add(limitSpinner, "h 36!, growx, span 2");

        formPanel.add(detailPanel, "growx");
        leftPanel.add(formPanel, "grow, pushy");
        
        // Pulsanti
        buttons = new JXPanel();
        buttons.setLayout(new MigLayout("fillx, insets 0", "[grow][grow]", "[]"));
        buttons.setBackground(BACKGROUND_COLOR);
        buttons.setMinimumSize(new Dimension(260, 45));

        confirmBtn = new JXButton("Crea Corso");
        confirmBtn.setBackground(BUTTON_COLOR);
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setPreferredSize(new Dimension(120, 35));

        goBackBtn = new JXButton("Annulla");
        goBackBtn.setPreferredSize(new Dimension(120, 35));

        buttons.add(confirmBtn, "growx, h 35!");
        buttons.add(goBackBtn, "growx, h 35!");

        leftPanel.add(buttons, "align center");

        mainPanel.add(leftPanel, "cell 0 0, grow, push");
    }

    private void initRightPanel(JXPanel mainPanel)
    {
        sessionPanel = new JXPanel(new MigLayout("wrap 3, insets 10, gap 10 10"));
        sessionPanel.setBackground(Color.WHITE);
        sessionPanel.setBorder(mainBorder);
        sessionPanel.setMinimumSize(new Dimension(900, 500));

        sessionTitle = new JXLabel("Sessioni del Corso");
        sessionTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sessionPanel.add(sessionTitle);

        aggiungiSessioneLabel = new JXLabel("<html><u>Aggiungi sessioni</u></html>");
        aggiungiSessioneLabel.setForeground(Color.ORANGE.darker());
        aggiungiSessioneLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sessionPanel.add(aggiungiSessioneLabel, "left, span 2, wrap");

        scrollContentWrapper = new JPanel();
        scrollContentWrapper.setLayout(new BoxLayout(scrollContentWrapper, BoxLayout.Y_AXIS));
        scrollContentWrapper.setBackground(Color.WHITE);

        sessionsContainer = new JXPanel(new MigLayout("wrap 3, gap 10 10", "[grow, fill][grow, fill][grow, fill]"));
        sessionsContainer.setBackground(Color.WHITE);

        scrollContentWrapper.add(sessionsContainer);
        scrollContentWrapper.add(Box.createVerticalGlue());

        scrollSessions = new JScrollPane(
            scrollContentWrapper,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );
        scrollSessions.getVerticalScrollBar().setUnitIncrement(16);
        scrollSessions.setBorder(BorderFactory.createEmptyBorder());
        scrollSessions.getViewport().setBackground(Color.WHITE);
        scrollSessions.setBackground(Color.WHITE);

        sessionPanel.add(scrollSessions, "grow, push");
        mainPanel.add(sessionPanel, "cell 1 0, grow, push");
    }

    /**
     * Registra i listener per tutti i componenti interattivi del dialog.
     */
    private void initListeners()
    {		
    	 /**
         * Listener per il pulsante "Annulla" che chiude il dialog.
         */
    	goBackBtnListener = new ActionListener()
					        {
					            @Override
					            public void actionPerformed(ActionEvent e)
					            {
					                dispose();
					            }
					        };
        goBackBtn.addActionListener(goBackBtnListener);
        
        /**
         * Listener per l'etichetta "Aggiungi sessioni".
         * Al click mostra il dialog per selezionare il numero di sessioni.
         */
        aggiungiSessioniMouseListener = new MouseAdapter()
								        {
								            @Override
								            public void mouseClicked(MouseEvent e)
								            {
								                showAddSessionsDialog();
								            }
								        };
        aggiungiSessioneLabel.addMouseListener(aggiungiSessioniMouseListener);

        /**
         * ItemListener per il checkbox "Pratico".
         * Se selezionato mostra il limite partecipanti, altrimenti gestisce la rimozione automatica delle 
         * sessioni pratiche su conferma utente.
         */
        praticoCheckListener = new ItemListener()
						       {
						           @Override
						           public void itemStateChanged(ItemEvent e)
						           {
						               if(e.getStateChange() == ItemEvent.SELECTED)
						                   togglePartecipantiLimit(true);
						               else if(e.getStateChange() == ItemEvent.DESELECTED)
						                   onPraticoDeselected();			
						           }
						       };
        praticoCheck.addItemListener(praticoCheckListener);
        
        /**
         * Listener per la combo "Frequenza".
         * Quando cambia, rischedula le date delle sessioni in base alla nuova frequenza.
         */
        frequencyListListener = new ItemListener()
						        {
						            @Override
						            public void itemStateChanged(ItemEvent e)
						            {
						                if(e.getStateChange() == ItemEvent.SELECTED)
						                    rescheduleSessions();
						            }
						        };
        frequencyList.addItemListener(frequencyListListener);
             
        /**
         * Listener per il DatePicker "Data Inizio".
         * Quando la data di inizio cambia, rischedula le date delle sessioni.
         */
        dataInizioListener = new DateChangeListener()
        {
            @Override
            public void dateChanged(DateChangeEvent e)
            {
                rescheduleSessions();
            }
        };
        dataInizioField.addDateChangeListener(dataInizioListener);
        
        /**
         * ActionListener per il pulsante "Crea Corso".
         * Valida i campi, raccoglie i dati delle sessioni e invoca il controller per la creazione del corso.
         */
     // In CreateCourseDialog.java -> initListeners()

        confirmBtnListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                boolean isDataValid = dataInizioField.isEnabled() ? validateDataInizio() : true;
                boolean isValid = editMode
                    ? (validateNome() && validateDescrizione() && isDataValid)
                    : isValidCourse();

                if(isValid)
                {
                    if(sessionCards.isEmpty())
                    {
                         JOptionPane.showMessageDialog(CreateCourseDialog.this, "Il corso deve avere almeno una sessione.", "Errore", JOptionPane.ERROR_MESSAGE);
                         return;
                    }

                    // Raccolta dati dalle sessioni presenti nella GUI
                    List<Integer> durateOnline = new ArrayList<>();
                    List<Time> orariOnline = new ArrayList<>();
                    List<LocalDate> dateOnline = new ArrayList<>();
                    List<String> linksOnline = new ArrayList<>();
                    List<Integer> duratePratiche = new ArrayList<>();
                    List<Time> orariPratiche = new ArrayList<>();
                    List<LocalDate> datePratiche = new ArrayList<>();
                    List<String> indirizziPratiche = new ArrayList<>();
                    List<ArrayList<Integer>> ricettePratiche = new ArrayList<>();
                    boolean checkSessionValidity = true;

                    for(CreateSessionPanel card : sessionCards)
                    {
                        if(!card.isPassed() && !card.isValidSession())
                        {
                            checkSessionValidity = false;
                            break;
                        }

                        if(card.getTipo().equals("Online"))
                        {
                           durateOnline.add(card.getDurata());
                           LocalTime orarioOnline = card.getOrario();                       
                           orariOnline.add(orarioOnline != null ? Time.valueOf(orarioOnline) : null);
                           dateOnline.add(card.getDataSessione());
                           linksOnline.add(card.getLinkRiunione());
                        }
                        else
                        {
                           duratePratiche.add(card.getDurata());
                           LocalTime orarioPratica = card.getOrario();
                           orariPratiche.add(orarioPratica != null ? Time.valueOf(orarioPratica) : null);
                           datePratiche.add(card.getDataSessione());
                           indirizziPratiche.add(card.getIndirizzo());
                           List<Integer> ricetteSelezionate = card.getIdRicetteSelezionate();
                           ricettePratiche.add(ricetteSelezionate != null ? new ArrayList<>(ricetteSelezionate) : new ArrayList<>());
                        }
                    } 

                    // Procedi solo se le sessioni future sono valide
                    if(checkSessionValidity)
                    {
                        // Controlla il vincolo della sessione pratica (se corso pratico)
                        if(!praticoCheck.isSelected() || (praticoCheck.isSelected() && !duratePratiche.isEmpty()))
                        {
                            if(editMode)
                            {
                                LocalDate dataInizioDaPassare = dataInizioField.getDate();
                                String frequenzaScelta = frequencyList.getSelectedItem().toString();

                                Controller.getController().editCourse(
                                    parentFrame, CreateCourseDialog.this, idCorsoDaModificare,
                                    nameField.getText().trim(),
                                    dataInizioDaPassare, 
                                    sessionCards.size(),
                                    frequenzaScelta,
                                    (int) limitSpinner.getValue(),
                                    descrizioneArea.getText(),
                                    BigDecimal.valueOf((double)costSpinner.getValue()), 
                                    praticoCheck.isSelected(),
                                    idsSelectedArguments,
                                    durateOnline, orariOnline, dateOnline, linksOnline, duratePratiche,
                                    orariPratiche, datePratiche, indirizziPratiche, ricettePratiche
                                );
                            }
                            else // Modalità Creazione
                            {
                                Controller.getController().createCourse(
                                    parentFrame, CreateCourseDialog.this, nameField.getText().trim(), dataInizioField.getDate(),
                                    sessionCards.size(), frequencyList.getSelectedItem().toString(), (int) limitSpinner.getValue(),
                                    descrizioneArea.getText(), BigDecimal.valueOf((double)costSpinner.getValue()), praticoCheck.isSelected(),
                                    idsSelectedArguments, durateOnline, orariOnline, dateOnline, linksOnline, duratePratiche,
                                    orariPratiche, datePratiche, indirizziPratiche, ricettePratiche
                                );
                            }
                        }
                        else
                        {
                            JOptionPane.showMessageDialog(CreateCourseDialog.this, "Se il corso è pratico, devi aggiungere almeno una sessione pratica.", "Errore", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        };
        confirmBtn.addActionListener(confirmBtnListener);
    } 

    /**
     * Rimuove i listener associati alla finestra di aggiunta sessioni.
     */
    private void removeDialogListeners()
    {
        if(addBtn != null && addSessionsListener != null)
        {
            addBtn.removeActionListener(addSessionsListener);
            addSessionsListener = null;
        }

        if(cancelBtn != null && cancelAddSessionsListener != null)
        {
            cancelBtn.removeActionListener(cancelAddSessionsListener);
            cancelAddSessionsListener = null;
        }
        
        if(addSessionDialog != null && windowListener != null) 
        {
            addSessionDialog.removeWindowListener(windowListener);
            windowListener = null;
        }
    }

    /**
     * Rimuove tutti i listener associati al dialog e ai pannelli di sessione.
     */
    public void disposeListeners()
    {
        if(goBackBtn != null && goBackBtnListener != null)
        {
        	goBackBtn.removeActionListener(goBackBtnListener);
        	goBackBtnListener = null;
        } 

        if(aggiungiSessioneLabel != null && aggiungiSessioniMouseListener != null)
        {
        	aggiungiSessioneLabel.removeMouseListener(aggiungiSessioniMouseListener);
        	 aggiungiSessioniMouseListener = null;
        }

        if(praticoCheck != null && praticoCheckListener != null)
        {
        	praticoCheck.removeItemListener(praticoCheckListener);
        	praticoCheckListener = null;
        }

        if(frequencyList != null && frequencyListListener != null)
        {
        	frequencyList.removeItemListener(frequencyListListener);
        	frequencyListListener = null;
        }  

        if(dataInizioField != null && dataInizioListener != null)
        {
        	dataInizioField.removeDateChangeListener(dataInizioListener);
        	dataInizioListener = null; 
        }   

        if(confirmBtn != null && confirmBtnListener != null)
        {
        	confirmBtn.removeActionListener(confirmBtnListener);
        	confirmBtnListener = null;
        }            

        if(argumentsCheck != null && argomentiCheckBoxListener != null)
        {
            for(JCheckBox cb : argumentsCheck)
                cb.removeItemListener(argomentiCheckBoxListener);
            argomentiCheckBoxListener = null;
        }

        // Rimuovi i listener dei pannelli sessione
        for(CreateSessionPanel card : sessionCards)
        {
        	card.setDateChangeListener(null);
        	card.disposeListeners();
        }       

        removeDialogListeners();
    }
    
    /**
     * Disposa il dialog liberando le risorse e i listeners, pulendo la cache nel controller.
     */
    @Override
    public void dispose()
    {
        disposeListeners();
        super.dispose();
    } 
    
    /**
     * Popola i campi principali del corso con dati.
     * Chiamato dal Controller.
     */
    public void popolaDatiCorso(int idCorso, String nome, String descrizione, LocalDate dataInizio, double costo, String frequenza, boolean isPratico, int limite, List<Integer> idArgomentiSelezionati, boolean canChangeAddAndFreq) // Rinominato ultimo parametro
    {
        this.idCorsoDaModificare = idCorso;

        nameField.setText(nome);
        descrizioneArea.setText(descrizione);
        dataInizioField.setDate(dataInizio);
        dataInizioField.setEnabled(false);

        if(dataInizioListener != null)
        {
            dataInizioField.removeDateChangeListener(dataInizioListener);
        }
        dataInizioField.getSettings().setVetoPolicy(date -> date != null && date.equals(dataInizio));


        // Blocca altri campi fissi
        costSpinner.setValue(costo);
        costSpinner.setEnabled(false);
        praticoCheck.setSelected(isPratico);
        praticoCheck.setEnabled(false);
        if(isPratico)
        {
            limitSpinner.setValue(limite);
            togglePartecipantiLimit(true);
        }
        limitSpinner.setEnabled(false);
        limitLabel.setEnabled(false);

        frequencyList.setEnabled(canChangeAddAndFreq);
        frequencyList.setSelectedItem(frequenza);

        idsSelectedArguments.clear();
        for(int i = 0; i < idsArguments.size(); i++)
        {
            int idArgomentoAttuale = idsArguments.get(i);
            JCheckBox cb = argumentsCheck.get(i);
            if(idArgomentiSelezionati.contains(idArgomentoAttuale))
            {
                cb.setSelected(true);
                idsSelectedArguments.add(idArgomentoAttuale);
            }
            else
            {
                 cb.setSelected(false);
            }
            cb.setEnabled(false);
        }
        sessionCards.clear();
        sessionsContainer.removeAll();
    }

    /**
     * Aggiunge una card sessione e la popola con i dati
     * Chiamato dal Controller
     */
    public void aggiungiSessionePopolata(LocalDate dataSessione, LocalTime orario, int durataMinuti, boolean isPratica, String link, String indirizzo, List<Integer> idRicetteSelezionate)
    {
        CreateSessionPanel card = new CreateSessionPanel(sessionCards.size() + 1, isPratica, this);
        card.popolaDatiSessione(dataSessione, orario, durataMinuti, link, indirizzo, idRicetteSelezionate);

        sessionCards.add(card);
        sessionsContainer.add(card, "growx, growy, w 33%");
    }
    
    public void triggerInitialReschedule()
    {
        refreshSessionLayout(); // Aggiorna il layout prima
        rescheduleSessions();   // Poi applica le logiche di data iniziali
    }
    
    /**
     * Riesegue la schedulazione automatica delle date delle sessioni in base a data iniziale e frequenza.
     */
    void rescheduleSessions()
    {
        LocalDate currentDataInizioCorso = dataInizioField.getDate();
        String frequenza = frequencyList.getSelectedItem().toString();

        if(currentDataInizioCorso != null)
        {
        	if(frequenza.equals("Libera"))
                rescheduleLibera(currentDataInizioCorso);
            else
                rescheduleFissa(currentDataInizioCorso, frequenza);
        }
    }
    
    /**
     * Reschedula le date delle sessioni per frequenza fissa (giornaliera, settimanale, etc.).
     * @param dataInizio Data di inizio del corso.
     * @param frequenza Stringa che rappresenta la frequenza.
     */
    private void rescheduleFissa(LocalDate dataInizioCorso, String frequenza)
    {
        int giorniFrequenza = switch(frequenza)
                              {
                                  case "Giornaliera" -> 1;
                                  case "Settimanale" -> 7;
                                  case "Bisettimanale" -> 14;
                                  case "Mensile" -> 30; // Considera approx. per mensile
                                  default -> 0; // Frequenza non valida o libera
                              };

        if(giorniFrequenza > 0)
        {
            int sessionIndex = 0;
            for(int i = 0; i < sessionCards.size(); i++)
            {
                CreateSessionPanel panel = sessionCards.get(i);
                if(!panel.isPassed())
                {
                    LocalDate dataPrevista = dataInizioCorso.plusDays((long)giorniFrequenza * sessionIndex);
                    
                    panel.setDataPrevista(dataPrevista, null, true);
                     if(panel.getDateChangeListener() != null)
                     {
                         panel.getDatePicker().removeDateChangeListener(panel.getDateChangeListener());
                         panel.setDateChangeListener(null);
                     }
                    sessionIndex++;
                }
                 else
                 {
                     panel.getDatePicker().setEnabled(false);
                     if(panel.getDateChangeListener() != null)
                     {
                         panel.getDatePicker().removeDateChangeListener(panel.getDateChangeListener());
                         panel.setDateChangeListener(null);
                     }
                 }
            }
        }
    }
    
    /**
     * Abilita la selezione manuale delle date delle sessioni (frequenza libera)
     * e imposta le Veto Policy corrette.
     * @param dataInizioCorso Data minima consentita per le sessioni.
     */

    private void rescheduleLibera(LocalDate dataInizioCorso)
    {
        for(CreateSessionPanel panel : sessionCards)
        {
            if(!panel.isPassed())
            {
                panel.getDatePicker().setEnabled(true);
                panel.getDatePicker().getSettings().setVetoPolicy(getVetoPolicy(dataInizioCorso, panel));
                updateSelectedDate(panel, dataInizioCorso);
                updateSessionListener(panel);
            }
            else
            {
                panel.getDatePicker().setEnabled(false);
                
                if(panel.getDateChangeListener() != null)
                {
                    panel.getDatePicker().removeDateChangeListener(panel.getDateChangeListener());
                    panel.setDateChangeListener(null);
                }
            }
        }

         for(CreateSessionPanel panel : sessionCards)
            if(!panel.isPassed())
                updateSelectedDate(panel, dataInizioCorso);
    }
    
    /**
     * Crea una politica di veto sulle date per evitare sovrapposizioni o date antecedenti.
     * @param dataInizio Data di inizio obbligatoria per le sessioni.
     * @param currentPanel Pannello della sessione corrente.
     * @return Politica di veto per il DatePicker.
     */
    private DateVetoPolicy getVetoPolicy(final LocalDate dataInizio, final CreateSessionPanel currentPanel)
    {
        return new DateVetoPolicy()
			       {
			           @Override
			           public boolean isDateAllowed(LocalDate date)
			           {
			               if(date == null || date.isBefore(dataInizio.plusDays(1)))
			                   return false;
			
			               for(CreateSessionPanel other : sessionCards)
			               {
			                   if(other == currentPanel)
			                       continue;
			
			                   LocalDate selected = other.getDatePicker().getDate();
			                   if(selected != null && selected.equals(date))
			                       return false;
			                }
			
			               return true;
			            }
			        };
    }
    
    /**
     * Imposta la data selezionata su un pannello se mancante o precedente al minimo.
     * Garantisce che tutte le schede di sessione abbiano sempre una data valida, sia che non ne abbiano mai avuta una, 
     * sia che la precedente non sia più conforme ai nuovi vincoli. 
     * @param panel Pannello di sessione da aggiornare.
     * @param dataInizio Data minima consentita.
     */
    private void updateSelectedDate(CreateSessionPanel panel, LocalDate dataInizio)
    {
        LocalDate selected = panel.getDatePicker().getDate();
        
        if(selected == null || selected.isBefore(dataInizio.plusDays(1)))
            panel.getDatePicker().setDate(dataInizio.plusDays(1));
    }

    /**
     * Aggiorna il listener di modifica data di un pannello di sessione.
     * @param panel Pannello di sessione da aggiornare.
     */
    private void updateSessionListener(CreateSessionPanel panel)
    {
    	 if(panel.getDateChangeListener() != null)
    		 panel.getDatePicker().removeDateChangeListener(panel.getDateChangeListener());

    	 DateChangeListener listener = new DateChangeListener()
							    	   {
							    	       @Override
							    	       public void dateChanged(DateChangeEvent e)
							    	       {
							    	           rescheduleSessions();
							    	       }
							    	   };
        panel.setDateChangeListener(listener);

        SwingUtilities.invokeLater(() -> panel.getDatePicker().addDateChangeListener(listener));
    }
  
    /**
     * Aggiunge una nuova scheda di sessione al container.
     * @param pratica True se la sessione è pratica, false se online.
     */
    private void addNewSessionCard(boolean pratica)
    {
        CreateSessionPanel card = new CreateSessionPanel(sessionCards.size() + 1, pratica, this);
        sessionCards.add(card);
        sessionsContainer.add(card, "growx, growy, w 33%");

        refreshSessionLayout();
        rescheduleSessions();
    }

    /**
     * Rimuove una scheda di sessione e riorganizza le restanti.
     * @param panel Pannello di sessione da rimuovere.
     */
    public void removeSessionCard(CreateSessionPanel panel)
    {  
        sessionCards.remove(panel);
        sessionsContainer.remove(panel);
        panel.disposeListeners();
        
        for(int i = 0; i < sessionCards.size(); i++)
            sessionCards.get(i).aggiornaNumero(i + 1);
        
        if(editMode)
        {
            frequencyList.setSelectedItem("Libera");
            frequencyList.setEnabled(false);
        }
        
        refreshSessionLayout();
        rescheduleSessions();
    }
    
    /**
     * Mostra il dialog per selezionare il numero di sessioni da aggiungere.
     */
    private void showAddSessionsDialog()
    {
        addSessionDialog = new JDialog(this, "Seleziona numero sessioni", true);
        addSessionDialog.setResizable(false);
        addSessionDialog.setLayout(new MigLayout("wrap 2", "[right][grow,fill]"));

        // Rimuovi vecchi listener se ci sono
        removeDialogListeners();

        windowListener = new WindowAdapter() 
        {
            @Override
            public void windowClosed(WindowEvent e) 
            {
                removeDialogListeners();
            }

            @Override
            public void windowClosing(WindowEvent e) 
            {
                removeDialogListeners();
            }
        };
        addSessionDialog.addWindowListener(windowListener);
        
        onlineSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        addSessionDialog.add(new JXLabel("Sessioni Online:"));
        addSessionDialog.add(onlineSpinner);

        praticheSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        if(praticoCheck.isSelected())
        {
            addSessionDialog.add(new JXLabel("Sessioni Pratiche:"));
            addSessionDialog.add(praticheSpinner);
        }

        // Pulsanti
        addBtn = new JXButton("Aggiungi");
        cancelBtn = new JXButton("Annulla");
        addSessionDialog.add(addBtn, "span 1, split 2, right");
        addSessionDialog.add(cancelBtn);

        // LISTENER: Aggiungi sessioni
        addSessionsListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int onlineCount = (Integer) onlineSpinner.getValue();
                int praticheCount = (praticoCheck.isSelected() && praticheSpinner != null) ? (Integer) praticheSpinner.getValue() : 0;

                for (int i = 0; i < onlineCount; i++)
                    addNewSessionCard(false);

                for (int i = 0; i < praticheCount; i++)
                    addNewSessionCard(true);

                removeDialogListeners();
                addSessionDialog.dispose();
            }
        };
        addBtn.addActionListener(addSessionsListener);

        // LISTENER: Annulla
        cancelAddSessionsListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                removeDialogListeners();
                addSessionDialog.dispose();
            }
        };
        cancelBtn.addActionListener(cancelAddSessionsListener);

        addSessionDialog.pack();
        addSessionDialog.setLocationRelativeTo(this);
        addSessionDialog.setVisible(true);
    }

    /**
     * Rimuove automaticamente tutte le sessioni di tipo pratico.
     */
    private void rimuoviSessioniPratiche()
    {
        List<CreateSessionPanel> toRemove = new ArrayList<>();
        
        for(CreateSessionPanel card : sessionCards)
            if("Pratica".equals(card.getTipo()))
                toRemove.add(card);
        
        for(CreateSessionPanel card : toRemove)
            removeSessionCard(card);
    }   
    
    /**
     * Gestisce la deselezione del checkbox "pratico", chiedendo conferma per rimuovere le sessioni pratiche.
     */
    private void onPraticoDeselected()
    {
        boolean hasPratiche = false;

        // Controlla se esistono sessioni pratiche
        for(CreateSessionPanel card : sessionCards)
        {
            if("Pratica".equals(card.getTipo()))
            {
                hasPratiche = true;
                break;
            }
        }

        if(hasPratiche)
        {
            int result = JOptionPane.showConfirmDialog(CreateCourseDialog.this,
                "Hai già aggiunto sessioni pratiche. Vuoi rimuoverle automaticamente?",
                "Conferma",
                JOptionPane.YES_NO_OPTION);

            if(result == JOptionPane.YES_OPTION)
            {
                rimuoviSessioniPratiche();
                praticoCheck.setSelected(false);
                togglePartecipantiLimit(false);
            }
        }
        else
            togglePartecipantiLimit(false);
    }
    
    /**
     * Applica o rimuove uno stato di errore visivo su un componente.
     * @param comp Componente target.
     * @param errore True per mostrare l'errore, false per rimuoverlo.
     * @param tooltip Testo del tooltip di errore.
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
     * Valida i campi obbligatori del corso e mostra un messaggio di errore se necessario.
     * @return True se tutti i campi sono validi.
     */
	public boolean isValidCourse()
	{
	    focusSet = false;
	    boolean valido = true;
	    String errori = new String();

	    if(!validateNome())
	    {
	        valido = false;
	        errori += "• Nome Corso obbligatorio\n";
	    }

	    if(!validateDescrizione())
	    {
	        valido = false;
	        errori += "• Descrizione Corso obbligatoria\n";
	    }

	    if(!validateArgomenti())
	    {
	        valido = false;
	        errori += "• Inserire almeno un argomento\n";
	    }

	    if(!validateDataInizio())
	    {
	        valido = false;
	        errori += "• Data Inizio obbligatoria\n";
	    }

	    if(!valido)
	        JOptionPane.showMessageDialog(this, errori.toString(), "Campi mancanti", JOptionPane.ERROR_MESSAGE);

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
     * Controlla la validità del nome del corso.
     * @return True se il nome non è vuoto.
     */
	private boolean validateNome()
	{
	    return validateField(nameField, nameField.getText().length()<=0 , "Nome Corso obbligatorio");
	}
	
    /**
     * Controlla la validità della data di inizio.
     * @return True se è stata selezionata una data.
     */
	private boolean validateDataInizio()
	{
	    return validateField(dataInizioField, dataInizioField.getDate() == null, "Data obbligatoria");
	}

    /**
     * Controlla che sia stato selezionato almeno un argomento.
     * @return True se c'è almeno un argomento selezionato.
     */
	private boolean validateArgomenti()
	{
	    return validateField(argomentiPanel, idsSelectedArguments.size() <= 0, "Inserire almeno un argomento");
	}

	 /**
     * Controlla la validità della descrizione del corso.
     * @return True se la descrizione non è vuota.
     */
	private boolean validateDescrizione()
	{
	    return validateField(descrizioneArea, descrizioneArea.getText().length() <= 0 , "Descrizione Corso obbligatoria");
	}
    

    /**
     * Aggiorna la numerica totale delle sessioni e il layout.
     */
    private void refreshSessionLayout()
    {
        updateSessionTotal();
        updateSessionLayout();
        sessionsContainer.revalidate();
        sessionsContainer.repaint();
    }

    /**
     * Sincronizza il valore dello spinner con il numero di sessioni.
     */
    private void updateSessionTotal()
    {
        numeroSessioniSpinner.setValue(sessionCards.size());
    }
    
    /**
     * Aggiorna il layout delle sessioni in base al numero di sessioni presente.
     */
    private void updateSessionLayout()
    {
        int count = sessionCards.size();
        String columns;

        if(count == 1)
            columns = "[grow, center]";
        else if(count == 2)
            columns = "[grow, right][grow, left]";
        else
            columns = "[grow, fill][grow, fill][grow, fill]";

        sessionsContainer.setLayout(new MigLayout("wrap " + Math.min(count, 3) + ", gap 10 10, insets 5", columns));
    }
    
    /**
     * Mostra o nasconde i campi per il limite di partecipanti.
     * @param visibile True per mostrare, false per nascondere.
     */
    private void togglePartecipantiLimit(boolean visibile)
    {
        limitLabel.setVisible(visibile);
        limitSpinner.setVisible(visibile);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    /**
     * Rimuove i pulsanti di aumento/diminuzione da uno spinner per uno stile più pulito.
     * @param spinner Spinner da cui rimuovere i pulsanti.
     */
    private void removeSpinnerButtons(JSpinner spinner)
    {
        JComponent editor = spinner.getEditor();
        if(editor instanceof JSpinner.DefaultEditor)
        {
            Component[] components = spinner.getComponents();
            
            for (Component comp : components)
                if(comp instanceof JButton)
                    spinner.remove(comp);
            
            spinner.revalidate();
            spinner.repaint();
        }
    }
    
    /**
     * Applica un tema arancione al componente DatePicker.
     * @param settings Impostazioni del DatePicker da personalizzare.
     */
    public void applyOrangeTheme(DatePickerSettings settings)
    {
        Color orange = new Color(225, 126, 47);
        Color orangeDark = new Color(140, 85, 20);
        Color background = new Color(255, 255, 255);
        Color lightBackground = new Color(255, 250, 245);
        Color grayText = new Color(60, 60, 60);
        Color disabledText = new Color(160, 160, 160);

        settings.setColor(DateArea.BackgroundOverallCalendarPanel, lightBackground);
        settings.setColor(DateArea.CalendarBackgroundNormalDates, background);
        settings.setColor(DateArea.CalendarTextNormalDates, grayText);
        settings.setColor(DateArea.CalendarBackgroundSelectedDate, orange);
        settings.setColor(DateArea.CalendarBorderSelectedDate, orangeDark);
        settings.setColor(DateArea.DatePickerTextValidDate, grayText);
        settings.setColor(DateArea.BackgroundTodayLabel, background);
        settings.setColor(DateArea.TextTodayLabel, orangeDark);
        settings.setColor(DateArea.CalendarBackgroundVetoedDates, new Color(245, 245, 245));
        settings.setColor(DateArea.DatePickerTextVetoedDate, disabledText);
        settings.setColor(DateArea.TextFieldBackgroundDisabled, new Color(245, 245, 245));
        settings.setColor(DateArea.DatePickerTextDisabled, disabledText);
        settings.setColor(DateArea.BackgroundMonthAndYearNavigationButtons, orange);
        settings.setColor(DateArea.TextMonthAndYearNavigationButtons, Color.WHITE);
        settings.setColor(DateArea.CalendarTextWeekdays, orangeDark);
        settings.setColor(DateArea.CalendarTextWeekNumbers, orangeDark);
        settings.setColor(DateArea.BackgroundMonthAndYearMenuLabels, background);
    }
}