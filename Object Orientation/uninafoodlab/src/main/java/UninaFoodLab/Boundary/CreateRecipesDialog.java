package UninaFoodLab.Boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import com.github.lgooddatepicker.zinternaltools.DateVetoPolicyMinimumMaximumDate;

import UninaFoodLab.Controller.Controller;
import UninaFoodLab.DTO.Argomento;
import UninaFoodLab.DTO.FrequenzaSessioni;
import UninaFoodLab.DTO.LivelloDifficolta;
import net.miginfocom.swing.MigLayout;

public class CreateRecipesDialog extends JXDialog
{

	private static final long serialVersionUID = 1L;

    private JPanel  scrollContentWrapper;
    private JXPanel buttons, container, specifichePanel, infoPanel, leftPanel, mainPanel, ingredientiPanel, ingredientiContainer;
    private JXLabel aggiungiIngredienteLabel, ingredientiTitle, title;
    private JXButton addBtn, cancelBtn, confirmBtn, goBackBtn;
    private JScrollPane rootScroll, scrollAllergeni, scrollIngredienti;
    private JComboBox<LivelloDifficolta> difficoltaList;
    private JDialog addSessionDialog;
    private JFormattedTextField costField, limitField;
    private JSpinner onlineSpinner, praticheSpinner;
    private JXTextArea allergeniArea;
    private JXTextField nameField, provenienzaField, tempoField, calorieField;
    private DatePicker dataInizioField;
    
    private ActionListener addSessionsListener, cancelAddSessionsListener, confirmBtnListener, goBackBtnListener;
    private MouseListener aggiungiIngredienteMouseListener;
    private ItemListener praticoCheckListener;
    
    private static final Color BACKGROUND_COLOR = new Color(245, 248, 250);
    private static final Color BORDER_COLOR = new Color(220, 225, 230);
    private static final Color BUTTON_COLOR = new Color(225, 126, 47, 220);

    private static final CompoundBorder mainBorder = new CompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)
    );
    
    private List<CreateUtilizzoPanel> ingredientiCards;
    private List<JCheckBox> ingredientiCheck;
    
	public CreateRecipesDialog(JXFrame parent)
	{
        super(parent, "Crea nuova ricetta", true);
        setMinimumSize(new Dimension(1670, 700));
        setPreferredSize(new Dimension(1200, 700));
        setLocationRelativeTo(parent);
        setResizable(true);
        setIconImage(parent.getIconImage());
        
        ingredientiCards = new ArrayList<>();

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

	        title = new JXLabel("Inserisci i dettagli della nuova ricetta");
	        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
	        leftPanel.add(title, "align center");

	        // Info panel
	        infoPanel = new JXPanel(new MigLayout("wrap 2", "[right][grow,fill]"));
	        infoPanel.setBackground(Color.WHITE);
	        infoPanel.setBorder(mainBorder);

	        nameField = new JXTextField();
	        infoPanel.add(new JLabel("Nome ricetta:"));
	        infoPanel.add(nameField, "h 30!");

	        provenienzaField = new JXTextField();
	        infoPanel.add(new JLabel("Provenienza ricetta:"));
	        infoPanel.add(provenienzaField, "h 30!");
	        
	        difficoltaList = new JComboBox<>(LivelloDifficolta.values());
	        infoPanel.add(new JLabel("Livello di difficoltà:"));
	        infoPanel.add(difficoltaList, "h 30!");

	        allergeniArea = new JXTextArea();
	        allergeniArea.setRows(4);
	        allergeniArea.setColumns(20);
	        allergeniArea.setLineWrap(true);
	        allergeniArea.setWrapStyleWord(true);
	        scrollAllergeni = new JScrollPane(allergeniArea);
	        scrollAllergeni.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
	        infoPanel.add(new JLabel("Eventuali allergeni:"));
	        infoPanel.add(scrollAllergeni, "h 80!");
	        
	        specifichePanel = new JXPanel(new MigLayout("wrap 3", "[right][grow,fill][]"));
	        tempoField = new JXTextField();
	        specifichePanel.add(new JLabel("Tempo di preparazione:"));
	        specifichePanel.add(nameField, "h 30!");
	        specifichePanel.add(new JLabel(" min"));
	        
	        calorieField = new JXTextField();
	        specifichePanel.add(new JLabel("Calorie per porzione:"));
	        specifichePanel.add(nameField, "h 30!");
	        specifichePanel.add(new JLabel(" kcal"));
	        
	        infoPanel.add(specifichePanel);
	        
	        

	        leftPanel.add(infoPanel);

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
	        ingredientiPanel = new JXPanel(new MigLayout("wrap 3, insets 10, gap 10 10"));
	        ingredientiPanel.setBackground(Color.WHITE);
	        ingredientiPanel.setBorder(mainBorder);
	        ingredientiPanel.setMinimumSize(new Dimension(900, 500));

	        ingredientiTitle = new JXLabel("Ingredienti della ricetta");
	        ingredientiTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
	        ingredientiPanel.add(ingredientiTitle);

	        aggiungiIngredienteLabel = new JXLabel("<html><u>Aggiungi ingredienti</u></html>");
	        aggiungiIngredienteLabel.setForeground(Color.ORANGE.darker());
	        aggiungiIngredienteLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        ingredientiPanel.add(aggiungiIngredienteLabel, "left, span 2, wrap");

	        scrollContentWrapper = new JPanel();
	        scrollContentWrapper.setLayout(new BoxLayout(scrollContentWrapper, BoxLayout.Y_AXIS));
	        scrollContentWrapper.setBackground(Color.WHITE);

	        ingredientiContainer = new JXPanel(new MigLayout("wrap 3, gap 10 10", "[grow, fill][grow, fill][grow, fill]"));
	        ingredientiContainer.setBackground(Color.WHITE);

	        scrollContentWrapper.add(ingredientiContainer);
	        scrollContentWrapper.add(Box.createVerticalGlue());

	        scrollIngredienti = new JScrollPane(
	            scrollContentWrapper,
	            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
	        );
	        scrollIngredienti.getVerticalScrollBar().setUnitIncrement(16);
	        scrollIngredienti.setBorder(BorderFactory.createEmptyBorder());
	        scrollIngredienti.getViewport().setBackground(Color.WHITE);
	        scrollIngredienti.setBackground(Color.WHITE);

	        ingredientiPanel.add(scrollIngredienti, "grow, push");
	        mainPanel.add(ingredientiPanel, "cell 1 0, grow");
	    }
	    
	private void initListeners()
	{
		aggiungiIngredienteMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	addNewUtilizzoCard();
            }
        };
        aggiungiIngredienteLabel.addMouseListener(aggiungiIngredienteMouseListener);

        confirmBtnListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(ingredientiCards.isEmpty())
                {
                    JOptionPane.showMessageDialog(CreateRecipesDialog.this, "Devi aggiungere almeno un ingrediente.", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for(CreateUtilizzoPanel card : ingredientiCards)
                {
                    if(!card.isValidUtilizzo())
                    {
                        JOptionPane.showMessageDialog(CreateRecipesDialog.this, "Errore nei dati di un ingredeinte utilizzato", "Errore", JOptionPane.ERROR_MESSAGE);
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
	}

	@Override
	public void dispose()
	{
	    disposeListeners();
	    super.dispose();
	}

	public void disposeListeners()
	{
	    if(aggiungiIngredienteLabel != null && aggiungiIngredienteMouseListener != null)
	    {
	    	aggiungiIngredienteLabel.removeMouseListener(aggiungiIngredienteMouseListener);
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
	        
	    addSessionsListener = null;
	    cancelAddSessionsListener = null;
	
	    // Rimuovi i listener dei pannelli sessione
	    for (CreateUtilizzoPanel card : ingredientiCards)
	    	card.disposeListeners();
	}

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

    private void addNewUtilizzoCard()
    {
        CreateUtilizzoPanel card = new CreateUtilizzoPanel(this);
        ingredientiCards.add(card);
        ingredientiContainer.add(card, "growx, growy, w 33%");

        updateUtilizziLayout();
        ingredientiContainer.revalidate();
        ingredientiContainer.repaint();
    }

    public void removeSessionCard(CreateSessionPanel panel)
    {
        panel.disposeListeners();
        ingredientiCards.remove(panel);
        ingredientiContainer.remove(panel);

        updateUtilizziLayout();
        ingredientiContainer.revalidate();
        ingredientiContainer.repaint();
    }

    private void updateUtilizziLayout()
    {
        int count = ingredientiCards.size();
        String columns;

        if(count == 1)
            columns = "[grow, center]";
        else if(count == 2)
            columns = "[grow, right][grow, left]";
        else
            columns = "[grow, fill][grow, fill][grow, fill]";

        ingredientiContainer.setLayout(new MigLayout("wrap " + Math.min(count, 3) + ", gap 10 10", columns));
    }
}