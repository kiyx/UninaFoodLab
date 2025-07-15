package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
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
import UninaFoodLab.DTO.Argomento;
import UninaFoodLab.DTO.FrequenzaSessioni;
import net.miginfocom.swing.MigLayout;

public class CreateCourseDialog extends JDialog
{
    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND_COLOR = new Color(245, 248, 250);
    private static final Color BORDER_COLOR = new Color(220, 225, 230);
    private static final Color BUTTON_COLOR = new Color(225, 126, 47, 220);

    private static final CompoundBorder mainBorder = new CompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)
    );
    
    private JPanel argomentiPanel, scrollContentWrapper;
    private JXPanel buttons, container, detailPanel, infoPanel, formPanel, leftPanel, mainPanel, sessionPanel, sessionsContainer;
    private JXLabel aggiungiSessioneLabel, sessioniLabel, limitLabel, sessionTitle, title;
    private JXButton addBtn, cancelBtn, confirmBtn, goBackBtn;
    private JScrollPane rootScroll, scrollArgomenti, scrollDescrizione, scrollSessions;
    private JComboBox<FrequenzaSessioni> frequencyList;
    private JCheckBox praticoCheck; 
    private JDialog addSessionDialog;
    private JSpinner numeroSessioniSpinner, onlineSpinner, costSpinner, limitSpinner, praticheSpinner;
    private JXTextArea descrizioneArea;
    private JXTextField nameField;
    private DatePicker dataInizioField;

    private ActionListener addSessionsListener, cancelAddSessionsListener, confirmBtnListener, goBackBtnListener;
    private MouseListener aggiungiSessioniMouseListener;
    private ItemListener praticoCheckListener, argomentiCheckBoxListener, frequencyListListener;
    private DateChangeListener dataInizioListener;
    private WindowAdapter windowListener;
    
    private List<CreateSessionPanel> sessionCards = new ArrayList<>();
    private List<JCheckBox> argumentsCheck = new ArrayList<>();
    
    public CreateCourseDialog(JXFrame parent)
    {
        super(parent, "Crea nuovo corso", true);
        setMinimumSize(new Dimension(1670, 725));
        setPreferredSize(new Dimension(1200, 700));
        setLocationRelativeTo(parent);
        setResizable(true);
        setIconImage(parent.getIconImage());

        initComponents();
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
    		    "wrap 1, fill, insets 0", // rimosso `dock`, evita confusione
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

        frequencyList = new JComboBox<>(FrequenzaSessioni.values());
        infoPanel.add(new JXLabel("Frequenza:"));
        infoPanel.add(frequencyList, "h 36!, growx");
        
        argomentiPanel = new JPanel(new GridLayout(0, 1));
        argomentiPanel.setOpaque(false);
        
        argomentiCheckBoxListener = new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                int selectedCount = 0;
                for(JCheckBox cb : argumentsCheck)
                    if(cb.isSelected())
                        selectedCount++;

                if(selectedCount >= 5)
                {
                    for(JCheckBox cb : argumentsCheck)
                        if(!cb.isSelected())
                            cb.setEnabled(false);
                }
                else
                   for(JCheckBox cb : argumentsCheck)
                       cb.setEnabled(true);
            }
        };
        
        for(Argomento a : Controller.getController().loadArgomenti())
        {
            JCheckBox cb = new JCheckBox(a.getNome());
            cb.addItemListener(argomentiCheckBoxListener);
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

    private void initListeners()
    {					          
    	goBackBtnListener = new ActionListener()
					        {
					            @Override
					            public void actionPerformed(ActionEvent e)
					            {
					                dispose();
					            }
					        };
        goBackBtn.addActionListener(goBackBtnListener);
        
        aggiungiSessioniMouseListener = new MouseAdapter()
								        {
								            @Override
								            public void mouseClicked(MouseEvent e)
								            {
								                showAddSessionsDialog();
								            }
								        };
        aggiungiSessioneLabel.addMouseListener(aggiungiSessioniMouseListener);

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
        
        
        dataInizioListener = new DateChangeListener()
        {
            @Override
            public void dateChanged(DateChangeEvent e)
            {
                rescheduleSessions();
            }
        };
        dataInizioField.addDateChangeListener(dataInizioListener);
        
        confirmBtnListener = new ActionListener()
        {
        	
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(sessionCards.isEmpty())               
                    JOptionPane.showMessageDialog(CreateCourseDialog.this, "Devi aggiungere almeno una sessione.", "Errore", JOptionPane.ERROR_MESSAGE);
                else
                {
                	for(CreateSessionPanel card : sessionCards)
                    {
                        if(!card.isValidSession())
                        {
                            JOptionPane.showMessageDialog(CreateCourseDialog.this, "Errore nei dati di una sessione. Controlla i campi", "Errore", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // TODO: invia dati al Controller
                    dispose();
                }
                
            }
        };
        confirmBtn.addActionListener(confirmBtnListener);
    } 

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

        // Listener del dialog addSessionDialog (se esiste)
        removeDialogListeners();
    }
    
    @Override
    public void dispose()
    {
        disposeListeners();
        super.dispose();
    } 
    
    private void rescheduleSessions()
    {
        LocalDate dataInizio = dataInizioField.getDate();
        FrequenzaSessioni frequenza = (FrequenzaSessioni) frequencyList.getSelectedItem();

        if(dataInizio != null && frequenza != null)
        {
        	if(frequenza == FrequenzaSessioni.Libera)
                rescheduleLibera(dataInizio);
            else
                rescheduleFissa(dataInizio, frequenza);
        }
    }
    
    private void rescheduleFissa(LocalDate dataInizio, FrequenzaSessioni frequenza)
    {
        int giorniFrequenza = switch(frequenza)
						      {
						          case Giornaliera -> 1;
						          case Settimanale -> 7;
						          case Bisettimanale -> 14;
						          case Mensile -> 30;
						          default -> -1;
						      };

        if(giorniFrequenza > 0)
        {
        	for(int i = 0; i < sessionCards.size(); i++)
            {
                CreateSessionPanel panel = sessionCards.get(i);
                LocalDate start = dataInizio.plusDays(giorniFrequenza * i);
                LocalDate end = start.plusDays(giorniFrequenza - 1);

                panel.setDataPrevista(start, i == 0 ? start : end, i == 0);
                panel.setData(start);
            }
        }
    }
    
    private void rescheduleLibera(LocalDate dataInizio)
    {
        for(CreateSessionPanel panel : sessionCards)
        {
            panel.getDatePicker().getSettings().setVetoPolicy(getVetoPolicy(dataInizio, panel));
            updateSelectedDate(panel, dataInizio);
            updateSessionListener(panel);
        }
    }
    
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
    
    private void updateSelectedDate(CreateSessionPanel panel, LocalDate dataInizio)
    {
        LocalDate selected = panel.getDatePicker().getDate();
        
        if(selected == null || selected.isBefore(dataInizio.plusDays(1)))
            panel.getDatePicker().setDate(dataInizio.plusDays(1));
    }
    
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
  
    private void addNewSessionCard(boolean pratica)
    {
        CreateSessionPanel card = new CreateSessionPanel(sessionCards.size() + 1, pratica, this);
        sessionCards.add(card);
        sessionsContainer.add(card, "growx, growy, w 33%");

        refreshSessionLayout();
        rescheduleSessions();
    }

    public void removeSessionCard(CreateSessionPanel panel)
    {  
        sessionCards.remove(panel);
        sessionsContainer.remove(panel);
        panel.disposeListeners();
        
        for(int i = 0; i < sessionCards.size(); i++)
            sessionCards.get(i).aggiornaNumero(i + 1);
        
        refreshSessionLayout();
        rescheduleSessions();
    }
    
    private void showAddSessionsDialog()
    {
        addSessionDialog = new JDialog(this, "Seleziona numero sessioni", true);
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
        
        // Spinner per sessioni online
        onlineSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        addSessionDialog.add(new JXLabel("Sessioni Online:"));
        addSessionDialog.add(onlineSpinner);

        // Spinner per pratiche: inizializza sempre, ma lo aggiungi solo se pratico è selezionato
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

    private void rimuoviSessioniPratiche()
    {
        List<CreateSessionPanel> toRemove = new ArrayList<>();
        
        for(CreateSessionPanel card : sessionCards)
            if("Pratica".equals(card.getTipo()))
                toRemove.add(card);
        
        for(CreateSessionPanel card : toRemove)
            removeSessionCard(card);
    }   
    
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
    
    private void refreshSessionLayout()
    {
        updateSessionTotal();
        updateSessionLayout();
        sessionsContainer.revalidate();
        sessionsContainer.repaint();
    }
    
    private void updateSessionTotal()
    {
        numeroSessioniSpinner.setValue(sessionCards.size());
    }
    
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
    
    private void togglePartecipantiLimit(boolean visibile)
    {
        limitLabel.setVisible(visibile);
        limitSpinner.setVisible(visibile);
        detailPanel.revalidate();
        detailPanel.repaint();
    }

    private void removeSpinnerButtons(JSpinner spinner)
    {
        JComponent editor = spinner.getEditor();
        if(editor instanceof JSpinner.DefaultEditor)
        {
            Component[] components = spinner.getComponents();
            
            for (Component comp : components)
                if(comp instanceof JButton)
                    spinner.remove(comp); // rimuove i bottoni freccia ↑↓
            
            spinner.revalidate();
            spinner.repaint();
        }
    }
    
    public void applyOrangeTheme(DatePickerSettings settings)
    {
        Color orange = new Color(225, 126, 47);
        Color orangeDark = new Color(140, 85, 20);
        Color background = new Color(255, 255, 255); // bianco puro
        Color lightBackground = new Color(255, 250, 245); // arancio molto chiaro
        Color grayText = new Color(60, 60, 60);
        Color disabledText = new Color(160, 160, 160);

        // Sfondo generale calendario chiaro e pulito (bianco/arancio molto chiaro)
        settings.setColor(DateArea.BackgroundOverallCalendarPanel, lightBackground);

        // Date normali: sfondo bianco e testo grigio scuro
        settings.setColor(DateArea.CalendarBackgroundNormalDates, background);
        settings.setColor(DateArea.CalendarTextNormalDates, grayText);

        // Data selezionata: arancio pieno con bordo definito 
        settings.setColor(DateArea.CalendarBackgroundSelectedDate, orange);
        settings.setColor(DateArea.CalendarBorderSelectedDate, orangeDark);
        settings.setColor(DateArea.DatePickerTextValidDate, grayText); // testo campo input visibile

        // Evidenziazione oggi: bordi sottili arancio scuro, sfondo bianco (no riempimento)
        settings.setColor(DateArea.BackgroundTodayLabel, background);
        settings.setColor(DateArea.TextTodayLabel, orangeDark);

        // Date non selezionabili (vetoed): grigio molto chiaro e testo grigio smorzato
        settings.setColor(DateArea.CalendarBackgroundVetoedDates, new Color(245, 245, 245));
        settings.setColor(DateArea.DatePickerTextVetoedDate, disabledText);

        // Sfondo e testo input disabilitato 
        settings.setColor(DateArea.TextFieldBackgroundDisabled, new Color(245, 245, 245));
        settings.setColor(DateArea.DatePickerTextDisabled, disabledText);

        // Pulsanti di navigazione mese/anno: arancio pieno con testo bianco, senza bordi spostanti
        settings.setColor(DateArea.BackgroundMonthAndYearNavigationButtons, orange);
        settings.setColor(DateArea.TextMonthAndYearNavigationButtons, Color.WHITE);

        // Giorni settimana e numeri settimana in grigio arancio scuro
        settings.setColor(DateArea.CalendarTextWeekdays, orangeDark);
        settings.setColor(DateArea.CalendarTextWeekNumbers, orangeDark);

        // Rimuove la barra azzurra di sfondo del mese (per sicurezza)
        settings.setColor(DateArea.BackgroundMonthAndYearMenuLabels, background);
    }
}