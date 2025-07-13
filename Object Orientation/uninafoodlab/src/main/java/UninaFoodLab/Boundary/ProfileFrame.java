package UninaFoodLab.Boundary;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Period;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextField;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import com.formdev.flatlaf.FlatLightLaf;
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import com.github.lgooddatepicker.zinternaltools.DateVetoPolicyMinimumMaximumDate;

import UninaFoodLab.Controller.Controller;
import UninaFoodLab.DTO.Chef;
import UninaFoodLab.DTO.Utente;
import net.miginfocom.swing.MigLayout;

public class ProfileFrame extends JXFrame
{

	private static final long serialVersionUID = 1L;
	
	private JXLabel logoLabel;
	
    private ImageIcon windowLogo;
    private ImageIcon paneLogo;
    
	private JPanel contentPane;
	
	//private JXButton hamburgerBtn;
	//private JXButton profileBtn;
	private JXButton modifyBtn;
	//private SidebarPanel sidebar;
	private JXPanel rootPanel;
	private HeaderPanel header;
	private JXPanel mainContentPanel;
	
	private ProfileDropdownPanel dropdownPanel;
	private JXLabel BenvenutoLabel;
	private JXLabel DatiLabel;
	private JXLabel NomeLabel;
	private JXTextField NomeField;
	private JXLabel CognomeLabel;
	private JXTextField CognomeField;
	private JXLabel CodFiscaleLabel;
	private JXLabel CodFiscaleEffettivoField;
	private JXLabel EmailLabel;
	private JXTextField EmailField;
	private JXLabel CurriculumLabel;
	private JXButton VisualizzaCurriculumBtn;
	private JXLabel UsernameLabel;
	private JXTextField UsernameField;
	private JXButton EliminaProfiloBtn;
	private JXButton AnnullaBtn;
	private JXButton ConfermaBtn;
	private JXButton ScegliCurriculumBtn;
	private JFileChooser fileChooser;
	private JXLabel fileLabel;
	private File selectedFile;
	
	private JXLabel nomeErrorLabel;
	private CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFFDAB9)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
	private CompoundBorder errorBorder = BorderFactory.createCompoundBorder(
        	new LineBorder(Color.RED),
        	BorderFactory.createEmptyBorder(5, 10, 5, 10));
	private JXLabel cognomeErrorLabel;
	private JXLabel dataErrorLabel;
	private JXLabel dataLabel;
	private DatePicker dataPicker;
	private JXLabel luogoErrorLabel;
	private JXLabel luogoLabel;
	private JXTextField luogoField;
	private JXLabel codFiscErrorLabel;
	private JXLabel emailErrorLabel;
	/*private JXLabel passwordErrorLabel;
	private JXLabel passwordLabel;
	private JToggleButton showPassBtn;
	FontIcon eyeIcon = FontIcon.of(MaterialDesign.MDI_EYE, 18);
	FontIcon eyeOffIcon = FontIcon.of(MaterialDesign.MDI_EYE_OFF, 18);
	private JPasswordField passwordField;
	*/
	private JXLabel userErrorLabel;
    // Listeners
	ActionListener scegliBtnActionListener;		
	private ActionListener CurriculumBtnListener;
	private ActionListener modifyBtnListener;
    private ActionListener EliminaBtnListener;
    private ActionListener AnnullaBtnListener;
    private ActionListener ConfermaBtnListener;
    private FocusAdapter nomeFieldFocusListener;   
    private DocumentListener nomeFieldDocumentListener;
    private FocusAdapter cognomeFieldFocusListener;   
    private DocumentListener cognomeFieldDocumentListener;
    private FocusAdapter luogoFieldFocusListener;   
    private DocumentListener luogoFieldDocumentListener;
    private FocusAdapter emailFieldFocusListener;   
    private DocumentListener emailFieldDocumentListener;
    private FocusAdapter userFieldFocusListener;   
    private DocumentListener userFieldDocumentListener;
    private MouseListener logoLabelMouseListener;
    private ComponentAdapter frameComponentListener;
    boolean passChange = false;
    
	public ProfileFrame()
	{
		setTitle("UninaFoodLab - Profilo");
        setMinimumSize(new Dimension(700, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JXFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        initComponents();
        initListeners();
        
        setVisible(true);

	}
	
    private void initComponents()
    {
    	rootPanel = new JXPanel(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));
    	header = new HeaderPanel(this, getLayeredPane());
    	setContentPane(rootPanel);
    	rootPanel.add(header, "dock north");
        
    	windowLogo = new ImageIcon(getClass().getResource("/logo_finestra.png"));
        setIconImage(windowLogo.getImage());

    	modifyBtn = new JXButton();
        mainContentPanel = new JXPanel(new MigLayout("fill, insets 40", "200[grow, 20::]10[grow, 100::300]30[]10[grow, 100::300]200","[]20[]10[]10[]10[]10[]10[]10[]10[]10[]10[]30[]"));
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFFE5B4), 1), 
            BorderFactory.createEmptyBorder(30, 50, 30, 50)
        ));
        rootPanel.add(mainContentPanel, "grow, align center");
        
    	BenvenutoLabel = new JXLabel("BENVENUTO "+ Controller.getController().getLoggedUser().getNome().toUpperCase() +" !");
    	BenvenutoLabel.setFont(new Font("Roboto", Font.BOLD, 36));
    	BenvenutoLabel.setForeground(new Color(0x9C5B17)); 
    	mainContentPanel.add(BenvenutoLabel, "span 4, center, wrap 50");	
    	    	
    	DatiLabel = new JXLabel("I tuoi dati:");
    	DatiLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
    	DatiLabel.setForeground(new Color(0x9C5B17));
    	mainContentPanel.add(DatiLabel, "cell 0 1, right, gaptop 10");	
    	
    	modifyBtn.setIcon(FontIcon.of(MaterialDesign.MDI_PENCIL, 24, new Color(0x604A3C)));
        modifyBtn.setContentAreaFilled(false);
        modifyBtn.setBorder(BorderFactory.createEmptyBorder());
        modifyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        modifyBtn.setToolTipText("Modifica i tuoi dati");
        mainContentPanel.add(modifyBtn, "cell 1 1, left, gaptop 10");
        
        nomeErrorLabel = new JXLabel(" ");
        nomeErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		nomeErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(nomeErrorLabel, "span 2, cell 0 2, center");
		
    	NomeLabel = new JXLabel("Nome:");
    	NomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	NomeLabel.setForeground(new Color(0xD86F00)); 
    	mainContentPanel.add(NomeLabel, "cell 0 3, right");	
    	
    	NomeField = new JXTextField();
    	NomeField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	NomeField.setForeground(new Color(0x333333));
    	NomeField.setText(Controller.getController().getLoggedUser().getNome());
    	NomeField.setEditable(false);
    	NomeField.setFocusable(false);
    	NomeField.setBorder(defaultBorder);
    	NomeField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(NomeField, "cell 1 3, growx");	
    	
    	cognomeErrorLabel = new JXLabel(" ");
        cognomeErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		cognomeErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(cognomeErrorLabel, "span 2, cell 2 2, center");
		
    	CognomeLabel = new JXLabel("Cognome:");
    	CognomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	CognomeLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(CognomeLabel, "cell 2 3, right");	
    	
    	CognomeField = new JXTextField();
    	CognomeField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	CognomeField.setForeground(new Color(0x333333));
    	CognomeField.setText(Controller.getController().getLoggedUser().getCognome());
    	CognomeField.setEditable(false);
    	CognomeField.setFocusable(false);
    	CognomeField.setBorder(defaultBorder);
    	CognomeField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(CognomeField, "cell 3 3, growx, left");	
    	
    	dataErrorLabel = new JXLabel(" ");
    	dataErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    	dataErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(dataErrorLabel, "span 2, cell 0 4, center");
		
		dataLabel = new JXLabel("Data di nascita: ");
		dataLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		dataLabel.setForeground(new Color(0xD86F00));
		dataLabel.setSize(getMaximumSize());
		mainContentPanel.add(dataLabel, "cell 0 5, right");	
		
		DateVetoPolicy vetoPolicy = new DateVetoPolicyMinimumMaximumDate(null, LocalDate.now());
		DatePickerSettings settings = new DatePickerSettings();
		dataPicker = new DatePicker(settings);
		dataPicker.setDate(Controller.getController().getLoggedUser().getDataDiNascita().toLocalDate());
		settings.setVetoPolicy(vetoPolicy);
		
        
		JTextField datePickerTextField = dataPicker.getComponentDateTextField();
        if (datePickerTextField != null) {
            datePickerTextField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xFFDAB9)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            datePickerTextField.setBackground(new Color(0xFFFBF5));
            datePickerTextField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            datePickerTextField.setForeground(new Color(0xD86F00));
        }

        
        JButton datePickerButton = dataPicker.getComponentToggleCalendarButton();
        if (datePickerButton != null) {
            datePickerButton.setBackground(new Color(0xFFA726));
            datePickerButton.setForeground(Color.WHITE);
            datePickerButton.setOpaque(true);
            datePickerButton.setFocusPainted(false);
            datePickerButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            datePickerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
		dataPicker.setBorder(defaultBorder);
		dataPicker.setBackground(new Color(0xFFFBF5));
		dataPicker.setFont(new Font("SansSerif", Font.PLAIN, 16));
		dataPicker.setForeground(new Color(0x333333));
		mainContentPanel.add(dataPicker, "cell 1 5, left");	
		
		luogoErrorLabel = new JXLabel(" ");
		luogoErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		luogoErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(luogoErrorLabel, "span 2, cell 2 4, center");
		
		luogoLabel = new JXLabel("Luogo di nascita:");
		luogoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		luogoLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(luogoLabel, "cell 2 5, right");	
    	
		luogoField = new JXTextField();
		luogoField.setFont(new Font("SansSerif", Font.PLAIN, 16));
		luogoField.setForeground(new Color(0x333333));
		luogoField.setText(Controller.getController().getLoggedUser().getLuogoDiNascita());
		luogoField.setEditable(false);
		luogoField.setFocusable(false);
		luogoField.setBorder(defaultBorder);
		luogoField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(luogoField, "cell 3 5, growx, left");	
		
    	codFiscErrorLabel = new JXLabel(" ");
    	codFiscErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    	codFiscErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(codFiscErrorLabel, "span 2, cell 0 6, center");	
		
    	CodFiscaleLabel = new JXLabel("Codice Fiscale:");
    	CodFiscaleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	CodFiscaleLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(CodFiscaleLabel, "cell 0 7, right");	
    	
    	CodFiscaleEffettivoField = new JXLabel(Controller.getController().getLoggedUser().getCodiceFiscale());
    	CodFiscaleEffettivoField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	CodFiscaleEffettivoField.setForeground(new Color(0x333333));
    	CodFiscaleEffettivoField.setBackground(new Color(0xFFFBF5));
    	CodFiscaleEffettivoField.setOpaque(true);
    	mainContentPanel.add(CodFiscaleEffettivoField, "cell 1 7, growx");	
    	
    	emailErrorLabel = new JXLabel(" ");
    	emailErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    	emailErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(emailErrorLabel, "span 2, cell 2 6, center");
		
    	EmailLabel = new JXLabel("Email:");
    	EmailLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	EmailLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(EmailLabel, "cell 2 7, right");	

    	EmailField = new JXTextField();
    	EmailField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	EmailField.setForeground(new Color(0x333333));
    	EmailField.setText(Controller.getController().getLoggedUser().getEmail());
    	EmailField.setEditable(false);
    	EmailField.setFocusable(false);
    	EmailField.setBorder(defaultBorder);
    	EmailField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(EmailField, "cell 3 7, growx");	
    	
    	userErrorLabel = new JXLabel(" ");
    	userErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    	userErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(userErrorLabel, "span 2, cell 0 8, center");
		
    	UsernameLabel = new JXLabel("Username:") ;
    	UsernameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	UsernameLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(UsernameLabel, "cell 0 9, right");	
  
    	UsernameField = new JXTextField();
    	UsernameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	UsernameField.setForeground(new Color(0x333333));
    	UsernameField.setText(Controller.getController().getLoggedUser().getUsername());
    	UsernameField.setEditable(false);
    	UsernameField.setFocusable(false);
    	UsernameField.setBorder(defaultBorder);
    	UsernameField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(UsernameField, "cell 1 9, growx");	
    	
    	CurriculumLabel = new JXLabel ("Curriculum:");
    	CurriculumLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	CurriculumLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(CurriculumLabel, "cell 0 11, right");
    	
    	VisualizzaCurriculumBtn = new JXButton ("Vedi curriculum");
    	VisualizzaCurriculumBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
    	VisualizzaCurriculumBtn.setPreferredSize(new Dimension(150, 35));
    	VisualizzaCurriculumBtn.setBackground(new Color(0xFFA726));
    	VisualizzaCurriculumBtn.setForeground(Color.WHITE);
    	VisualizzaCurriculumBtn.setOpaque(true);
    	VisualizzaCurriculumBtn.setFocusPainted(false);
    	VisualizzaCurriculumBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    	VisualizzaCurriculumBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    	mainContentPanel.add(VisualizzaCurriculumBtn, "cell 1 11, left");
    	
    	ScegliCurriculumBtn = new JXButton("Cambia curriculum");
    	ScegliCurriculumBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
    	ScegliCurriculumBtn.setPreferredSize(new Dimension(150, 35));
    	ScegliCurriculumBtn.setBackground(new Color(0xFF9800)); // Orange for action buttons
    	ScegliCurriculumBtn.setForeground(Color.WHITE);
    	ScegliCurriculumBtn.setOpaque(true);
    	ScegliCurriculumBtn.setFocusPainted(false);
    	ScegliCurriculumBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    	ScegliCurriculumBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    	mainContentPanel.add(ScegliCurriculumBtn, "cell 1 11, left");
    	
    	fileLabel = new JXLabel("Nessun file selezionato");
		fileLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		fileLabel.setForeground(new Color(0x604A3C));
		mainContentPanel.add(fileLabel,  "cell 2 11, span 2, left, gapleft 20");
		
		if(Controller.getController().isPartecipanteLogged())
		{
			CurriculumLabel.setEnabled(false);
			CurriculumLabel.setVisible(false);
			VisualizzaCurriculumBtn.setEnabled(false);
			VisualizzaCurriculumBtn.setVisible(false);
			ScegliCurriculumBtn.setEnabled(false);
			ScegliCurriculumBtn.setEnabled(false);
			fileLabel.setEnabled(false);
			fileLabel.setVisible(false);
		}
		else
		{
			selectedFile = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + ((Chef)Controller.getController().getLoggedUser()).getCurriculum());
		}
		
    	EliminaProfiloBtn = new JXButton ("Elimina Profilo");
    	EliminaProfiloBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
    	EliminaProfiloBtn.setPreferredSize(new Dimension(150, 35));
    	EliminaProfiloBtn.setBackground(new Color(0xD32F2F));
    	EliminaProfiloBtn.setForeground(Color.WHITE);
		EliminaProfiloBtn.setOpaque(true);
		EliminaProfiloBtn.setFocusPainted(false);
		EliminaProfiloBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		EliminaProfiloBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		mainContentPanel.add(EliminaProfiloBtn, "cell 3 12, right, gaptop 40, span 2");
		
		ConfermaBtn = new JXButton ("Conferma");
		ConfermaBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
		ConfermaBtn.setPreferredSize(new Dimension(120, 35));
		ConfermaBtn.setBackground(new Color(0x4CAF50));
		ConfermaBtn.setForeground(Color.WHITE);
		ConfermaBtn.setOpaque(true);
		ConfermaBtn.setFocusPainted(false);
		ConfermaBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		ConfermaBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		mainContentPanel.add(ConfermaBtn, "cell 2 12, right, gaptop 40");
		
		AnnullaBtn = new JXButton ("Annulla");
		AnnullaBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
		AnnullaBtn.setPreferredSize(new Dimension(120, 35));
		AnnullaBtn.setBackground(new Color(0x9E9E9E));
		AnnullaBtn.setForeground(Color.WHITE);
		AnnullaBtn.setOpaque(true);
		AnnullaBtn.setFocusPainted(false);
		AnnullaBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		AnnullaBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		mainContentPanel.add(AnnullaBtn, "cell 1 12, right, gaptop 40");
		
		
		setEditMode(false);
    }
    
    private void setEditMode(boolean editable) {
        NomeField.setEditable(editable);
        NomeField.setFocusable(editable);
        //NomeField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        NomeField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));

        CognomeField.setEditable(editable);
        CognomeField.setFocusable(editable);
        //CognomeField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        CognomeField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));
        
        luogoField.setEditable(editable);
        luogoField.setFocusable(editable);
        //CognomeField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        luogoField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));
        
        EmailField.setEditable(editable);
        EmailField.setFocusable(editable);
        //EmailField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        EmailField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));
        
        UsernameField.setEditable(editable);
        UsernameField.setFocusable(editable);
        //UsernameField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        UsernameField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));
        
        modifyBtn.setVisible(!editable);
        modifyBtn.setEnabled(!editable);
        
        AnnullaBtn.setVisible(editable);
        AnnullaBtn.setEnabled(editable);
        ConfermaBtn.setVisible(editable);
        ConfermaBtn.setEnabled(editable);
        EliminaProfiloBtn.setVisible(editable);
        EliminaProfiloBtn.setEnabled(editable);
        if(Controller.getController().isPartecipanteLogged())
        {
        	ScegliCurriculumBtn.setVisible(false);
        	ScegliCurriculumBtn.setEnabled(false);
        	fileLabel.setVisible(false);
        	fileLabel.setEnabled(false);
        }
        else
        {
        	ScegliCurriculumBtn.setVisible(editable);
        	ScegliCurriculumBtn.setEnabled(editable);
        	fileLabel.setVisible(editable);
        	fileLabel.setEnabled(editable);
        }       
        dataPicker.setEnabled(editable);
		nomeErrorLabel.setVisible(editable);
		cognomeErrorLabel.setVisible(editable);
		dataErrorLabel.setVisible(editable);
		luogoErrorLabel.setVisible(editable);
		codFiscErrorLabel.setVisible(editable);
		emailErrorLabel.setVisible(editable);

        if (!editable && Controller.getController().isChefLogged()) {
            fileLabel.setText("Nessun file selezionato");
        }
    }
    
    private void initListeners()
    {
    	 /*
         * Listeners di navigazione
         */
		   modifyBtnListener = new ActionListener()
		   {
			   @Override
			   public void actionPerformed(ActionEvent e)
			   {
				   VisualizzaCurriculumBtn.setEnabled(false);
				   setEditMode(true);
			   }
		   };
		   modifyBtn.addActionListener(modifyBtnListener);
		   
		   CurriculumBtnListener = new ActionListener()
			{
			   @Override
			   public void actionPerformed(ActionEvent e)
			   {
				 if (Desktop.isDesktopSupported()) {
				    try {
				        File myFile = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + ((Chef)Controller.getController().getLoggedUser()).getCurriculum());
				        if (myFile.exists()) {
				            Desktop.getDesktop().open(myFile);
				        } else {
				        }
				    } catch (IOException ex) {
				        ex.printStackTrace();
				    }
				} 
			   }
			};
		   VisualizzaCurriculumBtn.addActionListener(CurriculumBtnListener);

		   AnnullaBtnListener = new ActionListener()
			{
			   @Override
			   public void actionPerformed(ActionEvent e)
			   {
			    	NomeField.setText(Controller.getController().getLoggedUser().getNome());
			    	CognomeField.setText(Controller.getController().getLoggedUser().getCognome());
			    	dataPicker.setDate(Controller.getController().getLoggedUser().getDataDiNascita().toLocalDate());
			    	luogoField.setText(Controller.getController().getLoggedUser().getLuogoDiNascita());
					EmailField.setText(Controller.getController().getLoggedUser().getEmail());
					UsernameField.setText(Controller.getController().getLoggedUser().getUsername());
					VisualizzaCurriculumBtn.setEnabled(true);
					selectedFile = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + ((Chef)Controller.getController().getLoggedUser()).getCurriculum());
					setEditMode(false);
			   }
			};
		   AnnullaBtn.addActionListener(AnnullaBtnListener);
		   
		   ConfermaBtnListener = new ActionListener()
			{
			   @Override
			   public void actionPerformed(ActionEvent e)
			   {
				   if(!checkNome())
						NomeField.requestFocus();
					else if(!checkCognome())
						CognomeField.requestFocus();
					else if(!checkData())
						dataPicker.requestFocus();
					else if(!checkLuogo())
						luogoField.requestFocus();
					else if(!checkEmail())
						EmailField.requestFocus();
					else if(!checkUser())
			        	UsernameField.requestFocus();
			        else
			        {
						setEditMode(false);
						VisualizzaCurriculumBtn.setEnabled(true);
			        	Controller.getController().checkmodifyProfile(ProfileFrame.this,
							     NomeField.getText(), CognomeField.getText(),dataPicker.getDate(), luogoField.getText(),
							     EmailField.getText(), UsernameField.getText().trim(), 
							     selectedFile);
			        }				   
			   }
			};
		   ConfermaBtn.addActionListener(ConfermaBtnListener);

		   EliminaBtnListener = new ActionListener()
				   {
			   @Override
			   public void actionPerformed(ActionEvent e)
			   {
				   ConfirmEliminationDialog dialog = new ConfirmEliminationDialog(ProfileFrame.this);
		            dialog.setVisible(true);
			   }
				   };
				   EliminaProfiloBtn.addActionListener(EliminaBtnListener);
			scegliBtnActionListener = new ActionListener()
			  {
				
				@Override 
				public void actionPerformed(ActionEvent e)
				{

			        fileChooser = new JFileChooser();

			        // Opzionale: Filtro per specificare i tipi di file da visualizzare
			        FileNameExtensionFilter filter = new FileNameExtensionFilter("Pdf file(.pdf)", "pdf");
			        fileChooser.setFileFilter(filter);


			        int returnValue = fileChooser.showOpenDialog(ProfileFrame.this);

			        if (returnValue == JFileChooser.APPROVE_OPTION) {
			        	selectedFile = fileChooser.getSelectedFile();		        	

			        	System.out.println("stampa1");
			            fileLabel.setText(selectedFile.getName());
			            System.out.println(selectedFile.getName());
			            
			        } else {
			            System.out.println("Selezione annullata.");
			        }

				}				
			  };
			  ScegliCurriculumBtn.addActionListener(scegliBtnActionListener);
        
					  
					  nomeFieldFocusListener = new FocusAdapter()
					   {

						@Override
					    public void focusGained(FocusEvent e)
						{ 
							if(!NomeField.getText().isEmpty())
							    NomeField.selectAll();
						}
					   };
					NomeField.addFocusListener(nomeFieldFocusListener);
					
					nomeFieldDocumentListener = new DocumentListener()
					{
						@Override
					    public void insertUpdate(DocumentEvent e) { checkNome(); }
					    @Override
					    public void removeUpdate(DocumentEvent e) { checkNome(); }
					    @Override
					    public void changedUpdate(DocumentEvent e) { checkNome(); }
					};
					NomeField.getDocument().addDocumentListener(nomeFieldDocumentListener);
					
					cognomeFieldFocusListener = new FocusAdapter()
					   {

						@Override
					    public void focusGained(FocusEvent e)
						{ 
							if(!CognomeField.getText().isEmpty())
							    CognomeField.selectAll();
						}
					   };
					CognomeField.addFocusListener(cognomeFieldFocusListener);
					
					cognomeFieldDocumentListener = new DocumentListener()
					{
						@Override
					    public void insertUpdate(DocumentEvent e) { checkCognome(); }
					    @Override
					    public void removeUpdate(DocumentEvent e) { checkCognome(); }
					    @Override
					    public void changedUpdate(DocumentEvent e) { checkCognome(); }
					};
					CognomeField.getDocument().addDocumentListener(cognomeFieldDocumentListener);
					
					
					luogoFieldFocusListener = new FocusAdapter()
					   {
						@Override
					    public void focusGained(FocusEvent e)
						{ 
							if(!luogoField.getText().isEmpty())
							    luogoField.selectAll();
						}
						
					   };
					  
					luogoField.addFocusListener(luogoFieldFocusListener);
					
					luogoFieldDocumentListener = new DocumentListener()
					{
						@Override
					    public void insertUpdate(DocumentEvent e) { checkLuogo(); }
					    @Override
					    public void removeUpdate(DocumentEvent e) { checkLuogo(); }
					    @Override
					    public void changedUpdate(DocumentEvent e) { checkLuogo(); }
					};
					
					luogoField.getDocument().addDocumentListener(luogoFieldDocumentListener);
						
					emailFieldFocusListener = new FocusAdapter()
					   {

						@Override
					    public void focusGained(FocusEvent e)
						{ 
							if(!EmailField.getText().isEmpty())
							    EmailField.selectAll();
						}
					   };
				
					EmailField.addFocusListener(emailFieldFocusListener);
					
					emailFieldDocumentListener = new DocumentListener()
					{
						@Override
					    public void insertUpdate(DocumentEvent e) { checkEmail(); }
					    @Override
					    public void removeUpdate(DocumentEvent e) { checkEmail(); }
					    @Override
					    public void changedUpdate(DocumentEvent e) { checkEmail(); }
					};
				   
					EmailField.getDocument().addDocumentListener(emailFieldDocumentListener);
					
					userFieldDocumentListener = new DocumentListener()
					{
						@Override
					    public void insertUpdate(DocumentEvent e) { checkUser(); }
					    @Override
					    public void removeUpdate(DocumentEvent e) { checkUser(); }
					    @Override
					    public void changedUpdate(DocumentEvent e) { checkUser(); }
					};
				   
					UsernameField.getDocument().addDocumentListener(userFieldDocumentListener);
					
					userFieldFocusListener = new FocusAdapter()
					   {

							@Override
						    public void focusGained(FocusEvent e)
							{ 
								if(!UsernameField.getText().isEmpty())
									UsernameField.selectAll();
							}
					   };
					  
					   UsernameField.addFocusListener(userFieldFocusListener);

    }
    
    private boolean checkNome()
	{
		boolean check = true;
	    String text = NomeField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	NomeField.setBorder(errorBorder);
	    	nomeErrorLabel.setText("Bisogna inserire un nome!");
	    	check = false;
	    }
	    else
	    {
	        NomeField.setBorder(defaultBorder);
	        nomeErrorLabel.setText(" ");
	    }
	    
	    return check;
	}
	
	private boolean checkCognome()
	{
		boolean check = true;
	    String text = CognomeField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	CognomeField.setBorder(errorBorder);
	    	cognomeErrorLabel.setText("Bisogna inserire un cognome!");
	    	check = false;
	    }
	    else
	    {
	        CognomeField.setBorder(defaultBorder);
	        cognomeErrorLabel.setText(" ");
	    }
	    
	    return check;
	}
	
	private boolean checkLuogo()
	{
		boolean check = true;
	    String text = luogoField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	luogoField.setBorder(errorBorder);
	    	luogoErrorLabel.setText("Bisogna inserire il luogo di nascita!");
	    	check = false;
	    }
	    else
	    {
	    	luogoField.setBorder(defaultBorder);
	    	luogoErrorLabel.setText(" ");
	    }
	    
	    return check;
	}	
	
	private boolean checkData()
	{
		boolean check = true;
		LocalDate oggi = LocalDate.now();

	    if(!dataPicker.isTextFieldValid()||dataPicker.getText().isEmpty())
	    {
	    	dataPicker.setBorder(errorBorder);
	    	dataErrorLabel.setText("Bisogna inserire la data di nascita!");
	    	check = false;
	    }
	    else if(Period.between(dataPicker.getDate(), oggi).getYears()<18)
	    {
	    	dataPicker.setBorder(errorBorder);
	    	dataErrorLabel.setText("L'utente deve avere almeno 18 anni!");
	    	check = false;
	    }
	    else
	    {
	    	dataPicker.setBorder(defaultBorder);
	    	dataErrorLabel.setText(" ");
	    }
	    
	    return check;
	}	
	
	private boolean checkEmail()
	{
		boolean check = true;
	    String text = EmailField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	EmailField.setBorder(errorBorder);
	    	emailErrorLabel.setText("Bisogna inserire un' email!");
	    	check = false;
	    } 
	    else if(!text.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) 
	    {
	        EmailField.setBorder(errorBorder);
	        emailErrorLabel.setText("Email non valida!");
	        check = false;
	    }
	    else
	    {
	    	EmailField.setBorder(defaultBorder);
	    	emailErrorLabel.setText(" ");
	    }
	    
	    return check;
	}	
	
	private boolean checkUser() 
	{
		boolean check = true;
	    String text = UsernameField.getText().trim();
	      
	    if(text.contains(" ") || text.contains("\t") || text.contains("\n"))
	    {
	    	UsernameField.setBorder(errorBorder);
	    	userErrorLabel.setText("L'username non può contenere spazi!");
	    	check = false;
	    }
	    else if(text.isEmpty())
	    {
	    	UsernameField.setBorder(errorBorder);
	    	userErrorLabel.setText("Bisogna inserire un username!");
	    	check = false;
	    }
	    else if(text.length() < 4 || text.length() > 20) 
	 	{
	    	UsernameField.setBorder(errorBorder);
	 	    userErrorLabel.setText("L'username deve essere tra 4 e 20 caratteri!");
	 	    check = false;
	 	} 
	    else
	    {
	    	UsernameField.setBorder(defaultBorder);
	        userErrorLabel.setText(" ");
	    }
	    
	    return check;
	}

	
	public void showError(String msg)
	{
		JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
		ConfermaBtn.setEnabled(true);
	    AnnullaBtn.setEnabled(true);
	}	
	
	public void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Successo", JOptionPane.INFORMATION_MESSAGE);
        ConfermaBtn.setEnabled(true);
	    AnnullaBtn.setEnabled(true);
    }
	
    private void disposeListeners() 
    {
    	header.disposeListeners();

        if (ScegliCurriculumBtn != null && scegliBtnActionListener != null)
            ScegliCurriculumBtn.removeActionListener(scegliBtnActionListener);
		
        if (modifyBtn != null && modifyBtnListener != null)
        	modifyBtn.removeActionListener(modifyBtnListener);

		if(NomeField != null && nomeFieldFocusListener != null)
			NomeField.removeFocusListener(nomeFieldFocusListener);
		
		if(NomeField != null && nomeFieldDocumentListener != null)
			NomeField.getDocument().removeDocumentListener(nomeFieldDocumentListener);
		
		if(CognomeField != null && cognomeFieldFocusListener != null)
			CognomeField.removeFocusListener(cognomeFieldFocusListener);
		
		if(CognomeField != null && cognomeFieldDocumentListener != null)
			CognomeField.getDocument().removeDocumentListener(cognomeFieldDocumentListener);
		
		if(luogoField != null && luogoFieldFocusListener != null)
			luogoField.removeFocusListener(luogoFieldFocusListener);
		
		if(luogoField != null && luogoFieldDocumentListener != null)
			luogoField.getDocument().removeDocumentListener(luogoFieldDocumentListener);
		
		if(EmailField != null && emailFieldFocusListener != null)
			EmailField.removeFocusListener(emailFieldFocusListener);
		
		if(EmailField != null && emailFieldDocumentListener != null)
			EmailField.getDocument().removeDocumentListener(emailFieldDocumentListener);
				
		if(UsernameField != null && userFieldDocumentListener != null)
			UsernameField.getDocument().removeDocumentListener(userFieldDocumentListener);
		
		if(UsernameField != null && userFieldFocusListener != null)
			UsernameField.removeFocusListener(userFieldFocusListener);

		if(EliminaProfiloBtn != null && EliminaBtnListener != null)
			EliminaProfiloBtn.removeActionListener(EliminaBtnListener);
		
		if(ConfermaBtn != null && ConfermaBtnListener != null)
			ConfermaBtn.removeActionListener(ConfermaBtnListener);
		
		if(AnnullaBtn != null && AnnullaBtnListener != null)
			AnnullaBtn.removeActionListener(AnnullaBtnListener);
		
		if(VisualizzaCurriculumBtn != null && CurriculumBtnListener != null)
			VisualizzaCurriculumBtn.removeActionListener(CurriculumBtnListener);
    }
    
    @Override
    public void dispose()
    {
    	disposeListeners();
        super.dispose();
    }
    
    /*public void resetView()
    {
    	dropdownPanel.setVisible(false);
    }*/
}