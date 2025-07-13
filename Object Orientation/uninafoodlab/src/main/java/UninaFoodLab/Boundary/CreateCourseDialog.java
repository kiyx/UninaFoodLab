package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.time.*;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.NumberFormatter;

import org.jdesktop.swingx.*;

import com.github.lgooddatepicker.components.*;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import com.github.lgooddatepicker.zinternaltools.DateVetoPolicyMinimumMaximumDate;

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
    private JXPanel buttons, container, detailPanel, infoPanel, leftPanel, mainPanel, sessionPanel, sessionsContainer;
    private JXLabel aggiungiSessioneLabel, limitLabel, sessionTitle, title;
    private JXButton addBtn, cancelBtn, confirmBtn, goBackBtn;
    private JScrollPane rootScroll, scrollArgomenti, scrollDescrizione, scrollSessions;
    private JComboBox<FrequenzaSessioni> frequencyList;
    private JCheckBox praticoCheck; 
    private JDialog addSessionDialog;
    private JFormattedTextField costField, limitField;
    private JSpinner onlineSpinner, praticheSpinner;
    private JXTextArea descrizioneArea;
    private JXTextField nameField;
    private DatePicker dataInizioField;
    
    private ActionListener addSessionsListener, cancelAddSessionsListener, confirmBtnListener, goBackBtnListener;
    private MouseListener aggiungiSessioniMouseListener;
    private ItemListener praticoCheckListener;

    private List<CreateSessionPanel> sessionCards;
    private List<JCheckBox> argumentsCheck;
    
    
    public CreateCourseDialog(JXFrame parent)
    {
        super(parent, "Crea nuovo corso", true);
        setMinimumSize(new Dimension(1670, 700));
        setPreferredSize(new Dimension(1200, 700));
        setLocationRelativeTo(parent);
        setResizable(true);
        setIconImage(parent.getIconImage());
        
        sessionCards = new ArrayList<>();

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

        mainPanel = new JXPanel(new MigLayout("fill, insets 20", "[min!][grow, fill]", "[grow]"));
        mainPanel.setBackground(BACKGROUND_COLOR);
        container.add(mainPanel, BorderLayout.CENTER);

        initLeftPanel(mainPanel);
        initRightPanel(mainPanel);
    }

    private void initLeftPanel(JXPanel mainPanel)
    {
        leftPanel = new JXPanel(new MigLayout("wrap 1, gapy 20", "[grow,fill]"));
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setPreferredSize(new Dimension(700, 700));

        title = new JXLabel("Inserisci i dettagli del nuovo corso");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        leftPanel.add(title, "align center");

        // Info panel
        infoPanel = new JXPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(mainBorder);

        nameField = new JXTextField();
        infoPanel.add(new JLabel("Nome corso:"));
        infoPanel.add(nameField, "h 30!");

        descrizioneArea = new JXTextArea();
        descrizioneArea.setRows(4);
        descrizioneArea.setColumns(20);
        descrizioneArea.setLineWrap(true);
        descrizioneArea.setWrapStyleWord(true);
        scrollDescrizione = new JScrollPane(descrizioneArea);
        scrollDescrizione.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        infoPanel.add(new JLabel("Descrizione:"));
        infoPanel.add(scrollDescrizione, "h 80!");

        frequencyList = new JComboBox<>(FrequenzaSessioni.values());
        infoPanel.add(new JLabel("Frequenza:"));
        infoPanel.add(frequencyList, "h 30!");

        argumentsCheck = new ArrayList<>();
        argomentiPanel = new JPanel(new GridLayout(0, 1));
        argomentiPanel.setOpaque(false);

        for(Argomento a : Controller.getController().loadArgomenti())
        {
            JCheckBox cb = new JCheckBox(a.getNome());
            argumentsCheck.add(cb);
            argomentiPanel.add(cb);
        }

        scrollArgomenti = new JScrollPane(argomentiPanel);
        scrollArgomenti.setPreferredSize(new Dimension(200, 100));
        scrollArgomenti.setOpaque(false);
        scrollArgomenti.getViewport().setOpaque(false);
        scrollArgomenti.getVerticalScrollBar().setUnitIncrement(14);

        infoPanel.add(new JLabel("Argomenti:"));
        infoPanel.add(scrollArgomenti, "span 2");

        leftPanel.add(infoPanel);

        // Dettagli corso
        detailPanel = new JXPanel(new MigLayout("wrap 3", "[right][grow,fill][]"));
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(mainBorder);

        DateVetoPolicy vetoPolicy = new DateVetoPolicyMinimumMaximumDate(LocalDate.now().plusDays(1), null);
        DatePickerSettings settings = new DatePickerSettings();
        dataInizioField = new DatePicker(settings);
        settings.setVetoPolicy(vetoPolicy);
        detailPanel.add(new JLabel("Data Inizio:"));
        detailPanel.add(dataInizioField, "h 30!, span 2");

        costField = new JFormattedTextField(euroFormatter());
        costField.setValue(0.0);
        detailPanel.add(new JLabel("Costo:"));
        detailPanel.add(costField, "h 30!");
        detailPanel.add(new JLabel("€"));

        praticoCheck = new JCheckBox();
        praticoCheck.setBackground(Color.WHITE);
        detailPanel.add(new JLabel("Pratico:"));
        detailPanel.add(praticoCheck, "span 2, left");

        limitLabel = new JXLabel("Limite partecipanti:");
        limitLabel.setVisible(false);
        limitField = new JFormattedTextField(integerFormatter());
        limitField.setVisible(false);
        detailPanel.add(limitLabel, "newline");
        detailPanel.add(limitField, "h 30!, span 2");

        leftPanel.add(detailPanel);

        // Pulsanti
        buttons = new JXPanel(new MigLayout("center", "[]20[]"));
        buttons.setBackground(BACKGROUND_COLOR);

        confirmBtn = new JXButton("Crea Corso");
        confirmBtn.setBackground(BUTTON_COLOR);
        confirmBtn.setForeground(Color.WHITE);

        goBackBtn = new JXButton("Annulla");

        buttons.add(confirmBtn, "w 120!, h 35!");
        buttons.add(goBackBtn, "w 120!, h 35!");

        leftPanel.add(buttons);

        mainPanel.add(leftPanel, "cell 0 0, growy");
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
        mainPanel.add(sessionPanel, "cell 1 0, grow");
    }

    private void initListeners()
    {
        // Listener come classi interne anonime

        aggiungiSessioniMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                showAddSessionsDialog();
            }
        };
        aggiungiSessioneLabel.addMouseListener(aggiungiSessioniMouseListener);

        confirmBtnListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(sessionCards.isEmpty())
                {
                    JOptionPane.showMessageDialog(CreateCourseDialog.this, "Devi aggiungere almeno una sessione.", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for(CreateSessionPanel card : sessionCards)
                {
                    if(!card.isValidSession())
                    {
                        JOptionPane.showMessageDialog(CreateCourseDialog.this, "Errore nei dati di una sessione. Controlla data, ora e ricetta/link.", "Errore", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // TODO: invia dati al Controller
                dispose();
            }
        };
        confirmBtn.addActionListener(confirmBtnListener);

        goBackBtnListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        };
        goBackBtn.addActionListener(goBackBtnListener);

        praticoCheckListener = new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                if(e.getStateChange() == ItemEvent.SELECTED)
                    mostraLimitePartecipanti(true);
                else if (e.getStateChange() == ItemEvent.DESELECTED)
                    gestisciDeselezionePratico();

            }
        };
        praticoCheck.addItemListener(praticoCheckListener);
    } 

    @Override
    public void dispose()
    {
        disposeListeners();
        super.dispose();
    }

    private void removeDialogListeners(JXButton addBtn, JXButton cancelBtn)
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
    }

    public void disposeListeners()
    {
        if(aggiungiSessioneLabel != null && aggiungiSessioniMouseListener != null)
        {
        	aggiungiSessioneLabel.removeMouseListener(aggiungiSessioniMouseListener);
        }  

        if(confirmBtn != null && confirmBtnListener != null)
        {
        	confirmBtn.removeActionListener(confirmBtnListener);
        	confirmBtnListener = null;
        }          

        if(goBackBtn != null && goBackBtnListener != null)
        {
        	goBackBtn.removeActionListener(goBackBtnListener);
        	goBackBtnListener = null;
        }      

        if(praticoCheck != null && praticoCheckListener != null)
        {
        	praticoCheck.removeItemListener(praticoCheckListener);
        	praticoCheckListener = null; 
        }
            
        addSessionsListener = null;
        cancelAddSessionsListener = null;

        // Rimuovi i listener dei pannelli sessione
        for (CreateSessionPanel card : sessionCards)
        	card.disposeListeners();
    }
    
    private void gestisciDeselezionePratico()
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
                praticoCheck.setSelected(false); // Disattiva il checkbox perché hai rimosso le pratiche
                mostraLimitePartecipanti(false);
            }
            else
                praticoCheck.setSelected(true); // Ripristina lo stato perché non si vogliono rimuovere
        }
        else
            mostraLimitePartecipanti(false);
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
    
    private void mostraLimitePartecipanti(boolean visibile)
    {
        limitLabel.setVisible(visibile);
        limitField.setVisible(visibile);
    }

    private void showAddSessionsDialog()
    {
        addSessionDialog = new JDialog(this, "Seleziona numero sessioni", true);
        addSessionDialog.setLayout(new MigLayout("wrap 2", "[right][grow,fill]"));

        onlineSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
        addSessionDialog.add(new JXLabel("Sessioni Online:"));
        addSessionDialog.add(onlineSpinner);

        if(praticoCheck.isSelected())
        {
            praticheSpinner = new JSpinner(new SpinnerNumberModel(1, 0, 20, 1));
            addSessionDialog.add(new JXLabel("Sessioni Pratiche:"));
            addSessionDialog.add(praticheSpinner);
        }

        addBtn = new JXButton("Aggiungi");
        cancelBtn = new JXButton("Annulla");

        addSessionDialog.add(addBtn, "span 1, split 2, right");
        addSessionDialog.add(cancelBtn);

        addSessionsListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int onlineCount = (Integer) onlineSpinner.getValue();
                int praticheCount = praticheSpinner != null ? (Integer) praticheSpinner.getValue() : 0;

                for (int i = 0; i < onlineCount; i++)
                    addNewSessionCard(false);

                for (int i = 0; i < praticheCount; i++)
                    addNewSessionCard(true);

                removeDialogListeners(addBtn, cancelBtn);
                addSessionDialog.dispose();
            }
        };
        addBtn.addActionListener(addSessionsListener);

        cancelAddSessionsListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                removeDialogListeners(addBtn, cancelBtn);
                addSessionDialog.dispose();
            }
        };
        cancelBtn.addActionListener(cancelAddSessionsListener);

        addSessionDialog.pack();
        addSessionDialog.setLocationRelativeTo(this);
        addSessionDialog.setVisible(true);
    }

    private void addNewSessionCard(boolean pratica)
    {
        CreateSessionPanel card = new CreateSessionPanel(sessionCards.size() + 1, pratica, this);
        sessionCards.add(card);
        sessionsContainer.add(card, "growx, growy, w 33%");


        updateSessionLayout();
        sessionsContainer.revalidate();
        sessionsContainer.repaint();
    }

    public void removeSessionCard(CreateSessionPanel panel)
    {
        panel.disposeListeners();
        sessionCards.remove(panel);
        sessionsContainer.remove(panel);

        for(int i=0; i<sessionCards.size(); i++)
            sessionCards.get(i).aggiornaNumero(i + 1);
        
        updateSessionLayout();
        sessionsContainer.revalidate();
        sessionsContainer.repaint();
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

        sessionsContainer.setLayout(new MigLayout("wrap " + Math.min(count, 3) + ", gap 10 10", columns));
    }

    private NumberFormatter euroFormatter()
    {
        NumberFormatter formatter = new NumberFormatter(NumberFormat.getNumberInstance(Locale.ITALY));
        formatter.setValueClass(Double.class);
        formatter.setMinimum(0.0);
        formatter.setAllowsInvalid(false);
        return formatter;
    }

    private NumberFormatter integerFormatter()
    {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.ITALY);
        format.setGroupingUsed(false);
        NumberFormatter formatter = new NumberFormatter(format);
        formatter.setValueClass(Integer.class);
        formatter.setMinimum(1);
        formatter.setAllowsInvalid(false);
        return formatter;
    }
}