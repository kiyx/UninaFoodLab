package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.jdesktop.swingx.*;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;

public class LoginFrame extends JXFrame
{
	private static final long serialVersionUID = 1L;
	
	// Costanti di controllo
	private static final int USERNAME_MIN_LENGTH = 4;
	private static final int USERNAME_MAX_LENGTH = 20;
	private static final int PASSWORD_MIN_LENGTH = 8;
	private static final int PASSWORD_MAX_LENGTH = 30;
	
	// Icone 
	private FontIcon eyeIcon = FontIcon.of(MaterialDesign.MDI_EYE, 18);
	private FontIcon eyeOffIcon = FontIcon.of(MaterialDesign.MDI_EYE_OFF, 18);
	
	// Componenti Swing e Bordi
	private JXPanel panel;
	private CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
				new LineBorder(Color.LIGHT_GRAY, 1), 
			  	new EmptyBorder(0, 6, 0, 0));
	private CompoundBorder errorBorder = BorderFactory.createCompoundBorder(
	        	new LineBorder(Color.RED, 1),
	        	new EmptyBorder(0, 6, 0, 0));
	private ImageIcon windowLogo, paneLogo;
	private JXLabel logoLabel, userErrorLabel, passErrorLabel, userLabel, passLabel, orLabel;
	private JXTextField userField;
	private JPasswordField passField;
	private JToggleButton showPassBtn;
	private JXButton loginBtn, registerBtn;
	
	DocumentListener userFieldDocumentListener, passFieldDocumentListener;
	FocusAdapter userFieldFocusListener, passFieldFocusListener;
	ActionListener showPassBtnActionListener, registerBtnActionListener, loginBtnActionListener;	

	public LoginFrame()
	{
		setTitle("UninaFoodLab - Login");
		setSize(400, 500);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
		setResizable(false);
		
		initComponents();
		initListeners();
		setVisible(true);
	}
	
	private void initComponents()
	{
		userErrorLabel = new JXLabel(" ");
		passErrorLabel = new JXLabel(" ");
		userLabel = new JXLabel("Username:");
		passLabel = new JXLabel("Password:");
		showPassBtn = new JToggleButton();
		loginBtn = new JXButton("Login");
		orLabel = new JXLabel("Oppure, se non sei ancora registrato");
		registerBtn  = new JXButton("Registrati");
		
		windowLogo = new ImageIcon(getClass().getResource("/logo_finestra.png"));
		setIconImage(windowLogo.getImage());
		
		panel = new JXPanel(new MigLayout("wrap 2, insets 15", "[right][grow, fill]", "[][3][][3][][10][]"));
		setContentPane(panel);
		
		paneLogo = new ImageIcon(getClass().getResource("/logo_schermata.png"));
		logoLabel = new JXLabel(new ImageIcon(paneLogo.getImage().getScaledInstance(200, 160, Image.SCALE_SMOOTH)));
		panel.add(logoLabel, "span 2, align center, gapbottom 15");		
		
		userErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		userErrorLabel.setForeground(Color.RED);
		panel.add(userErrorLabel, "span 2, center, gapbottom 3");
		
		userLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		userField = new JXTextField();
		userField.setPreferredSize(new Dimension(250, 30));
		userField.setBorder(defaultBorder);
		panel.add(userLabel);
		panel.add(userField, "w 250!, h 25!, gapbottom 5"); 
		
		passErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
		passErrorLabel.setForeground(Color.RED);
		panel.add(passErrorLabel, "span 2, center, gapbottom 3");
		
		passLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		passField = new JPasswordField();
		passField.setPreferredSize(new Dimension(250, 30));
		panel.add(passLabel); 
		panel.add(passField, "w 250!, h 25!, gapbottom 18, split2"); 
		
		showPassBtn.setIcon(eyeOffIcon);
		showPassBtn.setPreferredSize(new Dimension(30, 30));
		showPassBtn.setFocusable(false);
		showPassBtn.setToolTipText("Mostra/Nascondi password");
		showPassBtn.setBorderPainted(false);
		showPassBtn.setContentAreaFilled(false);
		panel.add(showPassBtn, "w 30!, h 25!, gapleft 5, gapbottom 18");
	
		loginBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
		loginBtn.setPreferredSize(new Dimension(120, 30));
		loginBtn.setBackground(new Color(225, 126, 47, 220));
		loginBtn.setForeground(Color.WHITE);
		loginBtn.setOpaque(true);
		loginBtn.setFocusPainted(false);
		loginBtn.setBorderPainted(false);
		loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		panel.add(loginBtn, "span 2, center, gaptop 10");
		getRootPane().setDefaultButton(loginBtn);
		
		orLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		panel.add(orLabel, "span 2, center");
		
		registerBtn.setFont(new Font("SansSerif", Font.BOLD, 18));
		registerBtn.setPreferredSize(new Dimension(120, 30));
		registerBtn.setBackground(new Color(225, 126, 47, 220));
		registerBtn.setForeground(Color.WHITE);
		registerBtn.setOpaque(true);
		registerBtn.setFocusPainted(false);
		registerBtn.setBorderPainted(false);
		registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
		panel.add(registerBtn, "span 2, center");
	}
	
	private void initListeners()
	{
		userFieldDocumentListener = new DocumentListener()
		{
			@Override
		    public void insertUpdate(DocumentEvent e) { checkUser(); }
		    @Override
		    public void removeUpdate(DocumentEvent e) { checkUser(); }
		    @Override
		    public void changedUpdate(DocumentEvent e) { checkUser(); }
		};
		userField.getDocument().addDocumentListener(userFieldDocumentListener);
		
		passFieldDocumentListener = new DocumentListener()
							{
								@Override
							    public void insertUpdate(DocumentEvent e) { checkPass(); }
							    @Override
							    public void removeUpdate(DocumentEvent e) { checkPass(); }
							    @Override
							    public void changedUpdate(DocumentEvent e) { checkPass(); }
							};
		passField.getDocument().addDocumentListener(passFieldDocumentListener);
		
		userFieldFocusListener = new FocusAdapter()
								   {
									@Override
								    public void focusGained(FocusEvent e)
									{ 
										if(!userField.getText().isEmpty())
										    userField.selectAll();
									}
								   };
		userField.addFocusListener(userFieldFocusListener);
		
		passFieldFocusListener = new FocusAdapter()
								   {	
										@Override
									    public void focusGained(FocusEvent e) 
										{ 
											if(!(passField.getPassword().length == 0))
												passField.selectAll(); 
										}
								   };
	    passField.addFocusListener(passFieldFocusListener);
	    
	    showPassBtnActionListener = new ActionListener()
									  {
										@Override 
										public void actionPerformed(ActionEvent e)
										{
											if(showPassBtn.isSelected())
											{
										    	passField.setEchoChar('•');
										    	showPassBtn.setIcon(eyeOffIcon);
										    } 
										    else
										    {
										    	passField.setEchoChar((char)0);
										    	showPassBtn.setIcon(eyeIcon);
										    }
										}				
									  };
		showPassBtn.addActionListener(showPassBtnActionListener);
		
		loginBtnActionListener = new ActionListener()
						{
							@Override 
							public void actionPerformed(ActionEvent e)
							{
						        if(!checkUser())
						        	userField.requestFocus();
						        else if(!checkPass())
						        	passField.requestFocus();
						        else
						        {
						        	loginBtn.setEnabled(false);
						        	registerBtn.setEnabled(false);
						        	Controller.getController().checkLogin(LoginFrame.this, userField.getText().trim(), passField.getPassword());
						        }
						    }
						};
		loginBtn.addActionListener(loginBtnActionListener);
				
		registerBtnActionListener = new ActionListener()
									  {
										@Override 
										public void actionPerformed(ActionEvent e)
										{	
											Controller.getController().goToRegister(LoginFrame.this);
										}
									  };
		registerBtn.addActionListener(registerBtnActionListener);
		
	}
	
	/**
     * Rimuove tutti i listener registrati per evitare
     * memory leak o comportamenti indesiderati alla chiusura.
     */
	private void disposeListeners() 
	{
        if(userField != null && userFieldDocumentListener != null)
        {
        	userField.getDocument().removeDocumentListener(userFieldDocumentListener);
        	userFieldDocumentListener = null;
        }
            

        if(passField != null && passFieldDocumentListener != null)
        {
        	passField.getDocument().removeDocumentListener(passFieldDocumentListener);
        	passFieldDocumentListener = null;
        }
            

        if(userField != null && userFieldFocusListener != null)
        {
        	userField.removeFocusListener(userFieldFocusListener);
        	userFieldFocusListener = null;
        }
            

        if(passField != null && passFieldFocusListener != null)
        {
        	passField.removeFocusListener(passFieldFocusListener);
        	passFieldFocusListener = null;
        }
            

        if(showPassBtn != null && showPassBtnActionListener != null) 
        {
        	showPassBtn.removeActionListener(showPassBtnActionListener);
        	showPassBtnActionListener = null;
        }
            

        if(loginBtn != null && loginBtnActionListener != null)
        {
        	loginBtn.removeActionListener(loginBtnActionListener);
        	loginBtnActionListener = null;
        }    	

        if(registerBtn != null && registerBtnActionListener != null)
        {
        	registerBtn.removeActionListener(registerBtnActionListener);
        	registerBtnActionListener = null;
        }
    }
    
    /**
     * Rimuove tutti i listener registrati e libera risorse
     * prima di chiudere la finestra, per evitare memory leak.
     */
    @Override
    public void dispose()
    {
    	disposeListeners();
        super.dispose();
    }

	/**
     * Valida il campo username in tempo reale.
     * Controlla che:
     * - non ci siano spazi, tab o newline
     * - la lunghezza sia tra 4 e 20 caratteri
     *
     * @return true se il campo è valido, false altrimenti
     */
	private boolean checkUser() 
	{
		boolean check = true;
	    String text = userField.getText().trim();
	      
	    if(text.contains(" ") || text.contains("\t") || text.contains("\n"))
	    {
	    	userField.setBorder(errorBorder);
	    	userErrorLabel.setText("L'username non può contenere spazi!");
	    	check = false;
	    }
	    else if(text.length() < USERNAME_MIN_LENGTH  || text.length() > USERNAME_MAX_LENGTH ) 
	 	{
	 	    userField.setBorder(errorBorder);
	 	    userErrorLabel.setText("L'username deve essere tra 4 e 20 caratteri!");
	 	    check = false;
	 	} 
	    else
	    {
	        userField.setBorder(defaultBorder);
	        userErrorLabel.setText(" ");
	    }
	    
	    return check;
	} 

	/**
     * Valida il campo password in tempo reale.
     * Controlla che la lunghezza sia tra 8 e 30 caratteri.
     *
     * @return true se il campo è valido, false altrimenti
     */
	private boolean checkPass()
	{
		boolean check = true;
		String text = new String(passField.getPassword()).trim();

	    if (text.length() < PASSWORD_MIN_LENGTH || text.length() > PASSWORD_MAX_LENGTH)
	    {
	        passField.setBorder(errorBorder);
	        passErrorLabel.setText("La password deve essere tra 8 e 30 caratteri.");
	        check = false;
	    } 
	    else
	    {
	        passField.setBorder(defaultBorder);
	        passErrorLabel.setText(" ");
	    }
	    
	    return check; 
	}

    /**
     * Mostra un messaggio di errore tramite dialog.
     * Riabilita i pulsanti di login e registrazione
     * (usato dopo il fallimento di un login).
     * @param msg messaggio da mostrare
     */
	public void showError(String msg)
	{
		JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
		loginBtn.setEnabled(true);
	    registerBtn.setEnabled(true);
	}	
}