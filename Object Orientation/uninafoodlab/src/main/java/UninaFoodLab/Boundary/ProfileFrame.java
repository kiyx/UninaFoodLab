package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import org.jdesktop.swingx.*;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import com.github.lgooddatepicker.components.*;
import com.github.lgooddatepicker.optionalusertools.DateVetoPolicy;
import com.github.lgooddatepicker.zinternaltools.DateVetoPolicyMinimumMaximumDate;

import UninaFoodLab.Controller.Controller;
import UninaFoodLab.DTO.Chef;
import net.miginfocom.swing.MigLayout;

public class ProfileFrame extends JXFrame
{

	private static final long serialVersionUID = 1L;
	
	private CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFFDAB9)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
	private CompoundBorder errorBorder = BorderFactory.createCompoundBorder(
        	new LineBorder(Color.RED),
        	BorderFactory.createEmptyBorder(5, 10, 5, 10));
    private ImageIcon windowLogo;
    private HeaderPanel header;
	private JXButton modifyBtn, visualizzaCurriculumBtn, eliminaProfiloBtn, annullaBtn, confermaBtn, scegliCurriculumBtn;
	private JButton datePickerButton;
	private JXPanel rootPanel, mainContentPanel;
	private JXLabel benvenutoLabel, datiLabel, nomeLabel, cognomeLabel, codFiscaleLabel, codFiscaleEffettivoField, 
					emailLabel, curriculumLabel, usernameLabel, fileLabel, nomeErrorLabel, cognomeErrorLabel, 
					dataErrorLabel, dataLabel, luogoErrorLabel, luogoLabel, codFiscErrorLabel, emailErrorLabel, userErrorLabel;
	private JTextField datePickerTextField, nomeField, cognomeField, emailField, usernameField, luogoField;
	private JFileChooser fileChooser;
	private File selectedFile;
	private DatePicker dataPicker;
	
    // Listeners
	private ActionListener scegliBtnActionListener, CurriculumBtnListener, modifyBtnListener, EliminaBtnListener, AnnullaBtnListener,
						   ConfermaBtnListener;		

    private FocusAdapter nomeFieldFocusListener, cognomeFieldFocusListener, luogoFieldFocusListener, emailFieldFocusListener, userFieldFocusListener; 
    
    private DocumentListener nomeFieldDocumentListener, cognomeFieldDocumentListener, luogoFieldDocumentListener, emailFieldDocumentListener,
    					     userFieldDocumentListener;
    
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
    	mainContentPanel = new JXPanel(new MigLayout("fill, insets 40",
    		    "[push][grow]10[grow]30[grow]10[grow][push]",
    		    "[]20[]10[]10[]10[]10[]10[]10[]10[]10[]10[]30[]"));
    	
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFFE5B4), 1), 
            BorderFactory.createEmptyBorder(30, 50, 30, 50)
        ));
        rootPanel.add(mainContentPanel, "grow, align center");
        
    	benvenutoLabel = new JXLabel("BENVENUTO "+ Controller.getController().getLoggedUser().getNome().toUpperCase() +" !");
    	benvenutoLabel.setFont(new Font("Roboto", Font.BOLD, 36));
    	benvenutoLabel.setForeground(new Color(0x9C5B17)); 
    	mainContentPanel.add(benvenutoLabel, "span 4, cell 1 0, center, wrap 50");
    	    	
    	datiLabel = new JXLabel("I tuoi dati:");
    	datiLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
    	datiLabel.setForeground(new Color(0x9C5B17));
    	mainContentPanel.add(datiLabel, "cell 1 1, right, gaptop 10");	
    	
    	modifyBtn.setIcon(FontIcon.of(MaterialDesign.MDI_PENCIL, 24, new Color(0x604A3C)));
        modifyBtn.setContentAreaFilled(false);
        modifyBtn.setBorder(BorderFactory.createEmptyBorder());
        modifyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        modifyBtn.setToolTipText("Modifica i tuoi dati");
        mainContentPanel.add(modifyBtn, "cell 2 1, left, gaptop 10");
        
        nomeErrorLabel = new JXLabel(" ");
        nomeErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		nomeErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(nomeErrorLabel, "span 2, cell 1 2, center");
		
    	nomeLabel = new JXLabel("Nome:");
    	nomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	nomeLabel.setForeground(new Color(0xD86F00)); 
    	mainContentPanel.add(nomeLabel, "cell 1 3, right");
    	
    	nomeField = new JXTextField();
    	nomeField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	nomeField.setForeground(new Color(0x333333));
    	nomeField.setText(Controller.getController().getLoggedUser().getNome());
    	nomeField.setEditable(false);
    	nomeField.setFocusable(false);
    	nomeField.setBorder(defaultBorder);
    	nomeField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(nomeField, "cell 2 3, growx");
    	
    	cognomeErrorLabel = new JXLabel(" ");
        cognomeErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		cognomeErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(cognomeErrorLabel, "span 2, cell 3 2, center");
		
    	cognomeLabel = new JXLabel("Cognome:");
    	cognomeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	cognomeLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(cognomeLabel, "cell 3 3, right");
    	
    	cognomeField = new JXTextField();
    	cognomeField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	cognomeField.setForeground(new Color(0x333333));
    	cognomeField.setText(Controller.getController().getLoggedUser().getCognome());
    	cognomeField.setEditable(false);
    	cognomeField.setFocusable(false);
    	cognomeField.setBorder(defaultBorder);
    	cognomeField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(cognomeField, "cell 4 3, growx, left"); 
    	
    	dataErrorLabel = new JXLabel(" ");
    	dataErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    	dataErrorLabel.setForeground(Color.RED);
    	mainContentPanel.add(dataErrorLabel, "span 2, cell 1 4, center");
		
		dataLabel = new JXLabel("Data di nascita: ");
		dataLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		dataLabel.setForeground(new Color(0xD86F00));
		dataLabel.setSize(getMaximumSize());
		mainContentPanel.add(dataLabel, "cell 1 5, right");
		
		DateVetoPolicy vetoPolicy = new DateVetoPolicyMinimumMaximumDate(null, LocalDate.now());
		DatePickerSettings settings = new DatePickerSettings();
		dataPicker = new DatePicker(settings);
		dataPicker.setDate(Controller.getController().getLoggedUser().getDataDiNascita().toLocalDate());
		settings.setVetoPolicy(vetoPolicy);
		
        
		datePickerTextField = dataPicker.getComponentDateTextField();
        if(datePickerTextField != null) 
        {
            datePickerTextField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(0xFFDAB9)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            datePickerTextField.setBackground(new Color(0xFFFBF5));
            datePickerTextField.setFont(new Font("SansSerif", Font.PLAIN, 16));
            datePickerTextField.setForeground(new Color(0xD86F00));
        }

        datePickerButton = dataPicker.getComponentToggleCalendarButton();
        if(datePickerButton != null) 
        {
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
		mainContentPanel.add(dataPicker, "cell 2 5, left");
		
		luogoErrorLabel = new JXLabel(" ");
		luogoErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		luogoErrorLabel.setForeground(Color.RED);
		mainContentPanel.add(luogoErrorLabel, "span 2, cell 3 4, center");
		
		luogoLabel = new JXLabel("Luogo di nascita:");
		luogoLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
		luogoLabel.setForeground(new Color(0xD86F00));
		mainContentPanel.add(luogoLabel, "cell 3 5, right");
    	
		luogoField = new JXTextField();
		luogoField.setFont(new Font("SansSerif", Font.PLAIN, 16));
		luogoField.setForeground(new Color(0x333333));
		luogoField.setText(Controller.getController().getLoggedUser().getLuogoDiNascita());
		luogoField.setEditable(false);
		luogoField.setFocusable(false);
		luogoField.setBorder(defaultBorder);
		luogoField.setBackground(new Color(0xFFFBF5));
		mainContentPanel.add(luogoField, "cell 4 5, growx, left");
		
    	codFiscErrorLabel = new JXLabel(" ");
    	codFiscErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    	codFiscErrorLabel.setForeground(Color.RED);
    	mainContentPanel.add(codFiscErrorLabel, "span 2, cell 1 6, center");
		
    	codFiscaleLabel = new JXLabel("Codice Fiscale:");
    	codFiscaleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	codFiscaleLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(codFiscaleLabel, "cell 1 7, right");
    	
    	codFiscaleEffettivoField = new JXLabel(Controller.getController().getLoggedUser().getCodiceFiscale());
    	codFiscaleEffettivoField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	codFiscaleEffettivoField.setForeground(new Color(0x333333));
    	codFiscaleEffettivoField.setBackground(new Color(0xFFFBF5));
    	codFiscaleEffettivoField.setOpaque(true);
    	mainContentPanel.add(codFiscaleEffettivoField, "cell 2 7, growx");
    	
    	emailErrorLabel = new JXLabel(" ");
    	emailErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    	emailErrorLabel.setForeground(Color.RED);
    	mainContentPanel.add(emailErrorLabel, "span 2, cell 3 6, center");
		
    	emailLabel = new JXLabel("Email:");
    	emailLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	emailLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(emailLabel, "cell 3 7, right");	

    	emailField = new JXTextField();
    	emailField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	emailField.setForeground(new Color(0x333333));
    	emailField.setText(Controller.getController().getLoggedUser().getEmail());
    	emailField.setEditable(false);
    	emailField.setFocusable(false);
    	emailField.setBorder(defaultBorder);
    	emailField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(emailField, "cell 4 7, growx");
    	
    	userErrorLabel = new JXLabel(" ");
    	userErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    	userErrorLabel.setForeground(Color.RED);
    	mainContentPanel.add(userErrorLabel, "span 2, cell 1 8, center");
		
    	usernameLabel = new JXLabel("Username:") ;
    	usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	usernameLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(usernameLabel, "cell 1 9, right"); 
  
    	usernameField = new JXTextField();
    	usernameField.setFont(new Font("SansSerif", Font.PLAIN, 16));
    	usernameField.setForeground(new Color(0x333333));
    	usernameField.setText(Controller.getController().getLoggedUser().getUsername());
    	usernameField.setEditable(false);
    	usernameField.setFocusable(false);
    	usernameField.setBorder(defaultBorder);
    	usernameField.setBackground(new Color(0xFFFBF5));
    	mainContentPanel.add(usernameField, "cell 2 9, growx");
    	
    	curriculumLabel = new JXLabel ("Curriculum:");
    	curriculumLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    	curriculumLabel.setForeground(new Color(0xD86F00));
    	mainContentPanel.add(curriculumLabel, "cell 1 11, right");
    	
    	visualizzaCurriculumBtn = new JXButton ("Vedi curriculum");
    	visualizzaCurriculumBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
    	visualizzaCurriculumBtn.setPreferredSize(new Dimension(150, 35));
    	visualizzaCurriculumBtn.setBackground(new Color(0xFFA726));
    	visualizzaCurriculumBtn.setForeground(Color.WHITE);
    	visualizzaCurriculumBtn.setOpaque(true);
    	visualizzaCurriculumBtn.setFocusPainted(false);
    	visualizzaCurriculumBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    	visualizzaCurriculumBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    	mainContentPanel.add(visualizzaCurriculumBtn, "cell 2 11, left");
    	
    	scegliCurriculumBtn = new JXButton("Cambia curriculum");
    	scegliCurriculumBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
    	scegliCurriculumBtn.setPreferredSize(new Dimension(150, 35));
    	scegliCurriculumBtn.setBackground(new Color(0xFF9800)); // Orange for action buttons
    	scegliCurriculumBtn.setForeground(Color.WHITE);
    	scegliCurriculumBtn.setOpaque(true);
    	scegliCurriculumBtn.setFocusPainted(false);
    	scegliCurriculumBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
    	scegliCurriculumBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    	mainContentPanel.add(scegliCurriculumBtn, "cell 2 11, left");
    	
    	fileLabel = new JXLabel("Nessun file selezionato");
		fileLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
		fileLabel.setForeground(new Color(0x604A3C));
		mainContentPanel.add(fileLabel,  "cell 3 11, span 2, left, gapleft 20");
		
		if(Controller.getController().isPartecipanteLogged())
		{
			curriculumLabel.setEnabled(false);
			curriculumLabel.setVisible(false);
			visualizzaCurriculumBtn.setEnabled(false);
			visualizzaCurriculumBtn.setVisible(false);
			scegliCurriculumBtn.setEnabled(false);
			scegliCurriculumBtn.setEnabled(false);
			fileLabel.setEnabled(false);
			fileLabel.setVisible(false);
		}
		else
			selectedFile = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator
								    + ((Chef)Controller.getController().getLoggedUser()).getCurriculum());

		
    	eliminaProfiloBtn = new JXButton ("Elimina Profilo");
    	eliminaProfiloBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
    	eliminaProfiloBtn.setPreferredSize(new Dimension(150, 35));
    	eliminaProfiloBtn.setBackground(new Color(0xD32F2F));
    	eliminaProfiloBtn.setForeground(Color.WHITE);
		eliminaProfiloBtn.setOpaque(true);
		eliminaProfiloBtn.setFocusPainted(false);
		eliminaProfiloBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		eliminaProfiloBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		mainContentPanel.add(eliminaProfiloBtn, "cell 4 12, right, gaptop 40, span 2");
		
		confermaBtn = new JXButton ("Conferma");
		confermaBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
		confermaBtn.setPreferredSize(new Dimension(120, 35));
		confermaBtn.setBackground(new Color(0x4CAF50));
		confermaBtn.setForeground(Color.WHITE);
		confermaBtn.setOpaque(true);
		confermaBtn.setFocusPainted(false);
		confermaBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		confermaBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		mainContentPanel.add(confermaBtn, "cell 3 12, right, gaptop 40");
		
		annullaBtn = new JXButton ("Annulla");
		annullaBtn.setFont(new Font("SansSerif", Font.BOLD, 15));
		annullaBtn.setPreferredSize(new Dimension(120, 35));
		annullaBtn.setBackground(new Color(0x9E9E9E));
		annullaBtn.setForeground(Color.WHITE);
		annullaBtn.setOpaque(true);
		annullaBtn.setFocusPainted(false);
		annullaBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		annullaBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		mainContentPanel.add(annullaBtn, "cell 2 12, right, gaptop 40");
		
		
		setEditMode(false);
    }
    
    private void setEditMode(boolean editable) 
    {
        nomeField.setEditable(editable);
        nomeField.setFocusable(editable);
        //NomeField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        nomeField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));

        cognomeField.setEditable(editable);
        cognomeField.setFocusable(editable);
        //CognomeField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        cognomeField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));
        
        luogoField.setEditable(editable);
        luogoField.setFocusable(editable);
        //CognomeField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        luogoField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));
        
        emailField.setEditable(editable);
        emailField.setFocusable(editable);
        //EmailField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        emailField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));
        
        usernameField.setEditable(editable);
        usernameField.setFocusable(editable);
        //UsernameField.setBorder(editable ? new LineBorder(new Color(0xFF9800)) : BorderFactory.createCompoundBorder(new LineBorder(new Color(0xFFDAB9)), BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        usernameField.setBackground(editable ? Color.WHITE : new Color(0xFFFBF5));
        
        modifyBtn.setVisible(!editable);
        modifyBtn.setEnabled(!editable);
        
        annullaBtn.setVisible(editable);
        annullaBtn.setEnabled(editable);
        confermaBtn.setVisible(editable);
        confermaBtn.setEnabled(editable);
        eliminaProfiloBtn.setVisible(editable);
        eliminaProfiloBtn.setEnabled(editable);
        
        if(Controller.getController().isPartecipanteLogged())
        {
        	scegliCurriculumBtn.setVisible(false);
        	scegliCurriculumBtn.setEnabled(false);
        	fileLabel.setVisible(false);
        	fileLabel.setEnabled(false);
        }
        else
        {
        	scegliCurriculumBtn.setVisible(editable);
        	scegliCurriculumBtn.setEnabled(editable);
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

        if(!editable && Controller.getController().isChefLogged())
            fileLabel.setText("Nessun file selezionato");
    }
    
    private void initListeners()
    {
		   modifyBtnListener = new ActionListener()
							   {
								   @Override
								   public void actionPerformed(ActionEvent e)
								   {
									   visualizzaCurriculumBtn.setEnabled(false);
									   setEditMode(true);
								   }
							   };
		   modifyBtn.addActionListener(modifyBtnListener);
		   
		   CurriculumBtnListener = new ActionListener()
								{
								   @Override
								   public void actionPerformed(ActionEvent e)
								   {
									 if(Desktop.isDesktopSupported()) 
									 {
									    try 
									    {
									        File myFile = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" 
									        		               + File.separator + ((Chef)Controller.getController().getLoggedUser()).getCurriculum());
									        if(myFile.exists()) 
									            Desktop.getDesktop().open(myFile);
									    } 
									    catch (IOException ex) 
									    {
									        ex.printStackTrace();
									    }
									} 
								   }
								};
		   visualizzaCurriculumBtn.addActionListener(CurriculumBtnListener);

		   AnnullaBtnListener = new ActionListener()
							{
							   @Override
							   public void actionPerformed(ActionEvent e)
							   {
							    	nomeField.setText(Controller.getController().getLoggedUser().getNome());
							    	cognomeField.setText(Controller.getController().getLoggedUser().getCognome());
							    	dataPicker.setDate(Controller.getController().getLoggedUser().getDataDiNascita().toLocalDate());
							    	luogoField.setText(Controller.getController().getLoggedUser().getLuogoDiNascita());
									emailField.setText(Controller.getController().getLoggedUser().getEmail());
									usernameField.setText(Controller.getController().getLoggedUser().getUsername());
									
									if(Controller.getController().isChefLogged())
									{
										visualizzaCurriculumBtn.setEnabled(true);
										selectedFile = new File(System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + 
															    File.separator + ((Chef)Controller.getController().getLoggedUser()).getCurriculum());
									}
									setEditMode(false);
							   }
							};
		   annullaBtn.addActionListener(AnnullaBtnListener);
		   
		   ConfermaBtnListener = new ActionListener()
								 {
								    @Override
								    public void actionPerformed(ActionEvent e)
								    {
									     if(!checkNome())
									 		 nomeField.requestFocus();
										 else if(!checkCognome())
										  	 cognomeField.requestFocus();
										 else if(!checkData())
											 dataPicker.requestFocus();
										 else if(!checkLuogo())
											 luogoField.requestFocus();
										 else if(!checkEmail())
											 emailField.requestFocus();
										 else if(!checkUser())
								        	 usernameField.requestFocus();
								         else
								         { 
											 setEditMode(false);
											 visualizzaCurriculumBtn.setEnabled(true);
								        	 Controller.getController().checkmodifyProfile(ProfileFrame.this,
												     nomeField.getText(), cognomeField.getText(),dataPicker.getDate(), luogoField.getText(),
												     emailField.getText(), usernameField.getText().trim(), selectedFile);
								         }				   
								    }
								 };
		   confermaBtn.addActionListener(ConfermaBtnListener);

		   EliminaBtnListener = new ActionListener()
							    {
								   @Override
								   public void actionPerformed(ActionEvent e)
								   {
									   ConfirmEliminationDialog dialog = new ConfirmEliminationDialog(ProfileFrame.this);
							           dialog.setVisible(true);
								   }
							    };
		  eliminaProfiloBtn.addActionListener(EliminaBtnListener);
			
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
						
									       if(returnValue == JFileChooser.APPROVE_OPTION) 
									       {
									    	   selectedFile = fileChooser.getSelectedFile();		        	
						
									    	   System.out.println("stampa1");
									           fileLabel.setText(selectedFile.getName());
									           System.out.println(selectedFile.getName());
									            
									       } 
									       else
									    	   System.out.println("Selezione annullata.");
									   }				
									 };
			scegliCurriculumBtn.addActionListener(scegliBtnActionListener);
        
					  
			nomeFieldFocusListener = new FocusAdapter()
									  {
										 @Override
										 public void focusGained(FocusEvent e)
										 { 
											if(!nomeField.getText().isEmpty())
												nomeField.selectAll();
										 }
									  };
			nomeField.addFocusListener(nomeFieldFocusListener);
					
			nomeFieldDocumentListener = new DocumentListener()
										 {
											 @Override
										     public void insertUpdate(DocumentEvent e) { checkNome(); }
										     @Override
										     public void removeUpdate(DocumentEvent e) { checkNome(); }
										     @Override
										     public void changedUpdate(DocumentEvent e) { checkNome(); }
										 };
			nomeField.getDocument().addDocumentListener(nomeFieldDocumentListener);
					
			cognomeFieldFocusListener = new FocusAdapter()
									     {
											@Override
										    public void focusGained(FocusEvent e)
											{ 
												if(!cognomeField.getText().isEmpty())
												    cognomeField.selectAll();
											}
									     };
            cognomeField.addFocusListener(cognomeFieldFocusListener);
					
		    cognomeFieldDocumentListener = new DocumentListener()
											{
											   @Override
											   public void insertUpdate(DocumentEvent e) { checkCognome(); }
											   @Override
											   public void removeUpdate(DocumentEvent e) { checkCognome(); }
											   @Override
											   public void changedUpdate(DocumentEvent e) { checkCognome(); }
											};
			cognomeField.getDocument().addDocumentListener(cognomeFieldDocumentListener);
					
					
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
											if(!emailField.getText().isEmpty())
												emailField.selectAll();
										 }
									  };
		
			emailField.addFocusListener(emailFieldFocusListener);
			
			emailFieldDocumentListener = new DocumentListener()
										 {
											 @Override
										     public void insertUpdate(DocumentEvent e) { checkEmail(); }
										     @Override
										     public void removeUpdate(DocumentEvent e) { checkEmail(); }
										     @Override
										     public void changedUpdate(DocumentEvent e) { checkEmail(); }
										 };
		   
			emailField.getDocument().addDocumentListener(emailFieldDocumentListener);
			
			userFieldDocumentListener = new DocumentListener()
										{
											@Override
										    public void insertUpdate(DocumentEvent e) { checkUser(); }
										    @Override
										    public void removeUpdate(DocumentEvent e) { checkUser(); }
										    @Override
										    public void changedUpdate(DocumentEvent e) { checkUser(); }
										};
		   
			usernameField.getDocument().addDocumentListener(userFieldDocumentListener);
			
			userFieldFocusListener = new FocusAdapter()
									 {
											@Override
											public void focusGained(FocusEvent e)
											{ 
												if(!usernameField.getText().isEmpty())
													usernameField.selectAll();
											}
									  };
			  
		    usernameField.addFocusListener(userFieldFocusListener);

    }
    
    private void disposeListeners() 
    {
    	header.disposeListeners();

        if (scegliCurriculumBtn != null && scegliBtnActionListener != null)
            scegliCurriculumBtn.removeActionListener(scegliBtnActionListener);
		
        if (modifyBtn != null && modifyBtnListener != null)
        	modifyBtn.removeActionListener(modifyBtnListener);

		if(nomeField != null && nomeFieldFocusListener != null)
			nomeField.removeFocusListener(nomeFieldFocusListener);
		
		if(nomeField != null && nomeFieldDocumentListener != null)
			nomeField.getDocument().removeDocumentListener(nomeFieldDocumentListener);
		
		if(cognomeField != null && cognomeFieldFocusListener != null)
			cognomeField.removeFocusListener(cognomeFieldFocusListener);
		
		if(cognomeField != null && cognomeFieldDocumentListener != null)
			cognomeField.getDocument().removeDocumentListener(cognomeFieldDocumentListener);
		
		if(luogoField != null && luogoFieldFocusListener != null)
			luogoField.removeFocusListener(luogoFieldFocusListener);
		
		if(luogoField != null && luogoFieldDocumentListener != null)
			luogoField.getDocument().removeDocumentListener(luogoFieldDocumentListener);
		
		if(emailField != null && emailFieldFocusListener != null)
			emailField.removeFocusListener(emailFieldFocusListener);
		
		if(emailField != null && emailFieldDocumentListener != null)
			emailField.getDocument().removeDocumentListener(emailFieldDocumentListener);
				
		if(usernameField != null && userFieldDocumentListener != null)
			usernameField.getDocument().removeDocumentListener(userFieldDocumentListener);
		
		if(usernameField != null && userFieldFocusListener != null)
			usernameField.removeFocusListener(userFieldFocusListener);

		if(eliminaProfiloBtn != null && EliminaBtnListener != null)
			eliminaProfiloBtn.removeActionListener(EliminaBtnListener);
		
		if(confermaBtn != null && ConfermaBtnListener != null)
			confermaBtn.removeActionListener(ConfermaBtnListener);
		
		if(annullaBtn != null && AnnullaBtnListener != null)
			annullaBtn.removeActionListener(AnnullaBtnListener);
		
		if(visualizzaCurriculumBtn != null && CurriculumBtnListener != null)
			visualizzaCurriculumBtn.removeActionListener(CurriculumBtnListener);
    }
    
    @Override
    public void dispose()
    {
    	disposeListeners();
        super.dispose();
    }
    
    private boolean checkNome()
	{
		boolean check = true;
	    String text = nomeField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	nomeField.setBorder(errorBorder);
	    	nomeErrorLabel.setText("Bisogna inserire un nome!");
	    	check = false;
	    }
	    else
	    {
	        nomeField.setBorder(defaultBorder);
	        nomeErrorLabel.setText(" ");
	    }
	    
	    return check;
	}
	
	private boolean checkCognome()
	{
		boolean check = true;
	    String text = cognomeField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	cognomeField.setBorder(errorBorder);
	    	cognomeErrorLabel.setText("Bisogna inserire un cognome!");
	    	check = false;
	    }
	    else
	    {
	        cognomeField.setBorder(defaultBorder);
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
	    else if(Period.between(dataPicker.getDate(), oggi).getYears() < 18)
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
	    String text = emailField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	emailField.setBorder(errorBorder);
	    	emailErrorLabel.setText("Bisogna inserire un' email!");
	    	check = false;
	    } 
	    else if(!text.matches("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$")) 
	    {
	        emailField.setBorder(errorBorder);
	        emailErrorLabel.setText("Email non valida!");
	        check = false;
	    }
	    else
	    {
	    	emailField.setBorder(defaultBorder);
	    	emailErrorLabel.setText(" ");
	    }
	    
	    return check;
	}	
	
	private boolean checkUser() 
	{
		boolean check = true;
	    String text = usernameField.getText().trim();
	      
	    if(text.contains(" ") || text.contains("\t") || text.contains("\n"))
	    {
	    	usernameField.setBorder(errorBorder);
	    	userErrorLabel.setText("L'username non può contenere spazi!");
	    	check = false;
	    }
	    else if(text.isEmpty())
	    {
	    	usernameField.setBorder(errorBorder);
	    	userErrorLabel.setText("Bisogna inserire un username!");
	    	check = false;
	    }
	    else if(text.length() < 4 || text.length() > 20) 
	 	{
	    	usernameField.setBorder(errorBorder);
	 	    userErrorLabel.setText("L'username deve essere tra 4 e 20 caratteri!");
	 	    check = false;
	 	} 
	    else
	    {
	    	usernameField.setBorder(defaultBorder);
	        userErrorLabel.setText(" ");
	    }
	    
	    return check;
	}

	
	public void showError(String msg)
	{
		JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
		confermaBtn.setEnabled(true);
	    annullaBtn.setEnabled(true);
	}	
	
	public void showSuccess(String msg) 
	{
        JOptionPane.showMessageDialog(this, msg, "Successo", JOptionPane.INFORMATION_MESSAGE);
        confermaBtn.setEnabled(true);
	    annullaBtn.setEnabled(true);
    }
	
    
}