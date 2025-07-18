package UninaFoodLab.Boundary;

import java.awt.Color;
import java.awt.Cursor; // Import necessario per Cursor
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JToggleButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent; // Import necessario per DocumentEvent
import javax.swing.event.DocumentListener; // Import necessario per DocumentListener

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class ConfirmEliminationDialog extends JDialog {

    	private static final long serialVersionUID = 1L;
    
		FontIcon eyeIcon = FontIcon.of(MaterialDesign.MDI_EYE, 18);
		FontIcon eyeOffIcon = FontIcon.of(MaterialDesign.MDI_EYE_OFF, 18);
		
		private JXLabel passwordErrorLabel;
	    private JPasswordField passwordField;
	    private JButton confirmButton;
	    private JToggleButton showPassBtn;
	    private JXPanel panel;
	    private ActionListener confirmButtonListener;
	    ActionListener showPassBtnActionListener;
	    DocumentListener passwordFieldDocumentListener; // Dichiarazione del DocumentListener
	    private JXLabel passwordLabel;
	    
		private CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
				new LineBorder(Color.LIGHT_GRAY, 1), 
			  	new EmptyBorder(0, 6, 0, 0));
		private CompoundBorder errorBorder = BorderFactory.createCompoundBorder(
	        	new LineBorder(Color.RED, 1),
	        	new EmptyBorder(0, 6, 0, 0));
		
	    private JXFrame parent;
	    
	    public ConfirmEliminationDialog(JXFrame profile)
		{
			super(profile, "Conferma Eliminazione Profilo", true);
			parent = profile;
			initComponents();
			initListeners();
	        pack();
	        setLocationRelativeTo(profile);
	        setResizable(false);
		}
		

	    private void initComponents() {
	        setTitle("Conferma Eliminazione Profilo"); // Imposta il titolo come nell'altro dialog
	        // MigLayout con wrap 2 (2 colonne per riga) e spaziatura simile all'altro dialog
	        panel = new JXPanel(new MigLayout("wrap 2", "[grow, fill]5[]", "[]10[]10[]10[]10[]30[]"));
	        panel.setBackground(Color.WHITE); // Colore di sfondo bianco come nell'altro dialog
	        setContentPane(panel); // Imposta il panel come content pane

	        // Etichetta errore: dimensione e posizione per massima visibilità
	        passwordErrorLabel = new JXLabel(" ");
	        passwordErrorLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
	        passwordErrorLabel.setForeground(Color.RED);
	        passwordErrorLabel.setPreferredSize(new Dimension(300, 20)); // Aumenta la dimensione preferita
			panel.add(passwordErrorLabel, "span 2, align center, h 20!");	
       
	        passwordLabel = new JXLabel("Inserisci password per eliminare il profilo:");
	        passwordLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
		    panel.add(passwordLabel, "span 2, wrap, left"); // wrap per andare a capo
	        
	        passwordField = new JPasswordField(); // Rimosso 20 come argomento, usa preferedSize
	        passwordField.setPreferredSize(new Dimension(200, 30));
	        passwordField.setBorder(defaultBorder); // Applica il bordo di default
	        panel.add(passwordField, "growx"); // growx per espandersi orizzontalmente

	        showPassBtn= new JToggleButton();
	    	showPassBtn.setIcon(eyeOffIcon);
	    	showPassBtn.setPreferredSize(new Dimension(30, 30));
	    	showPassBtn.setFocusable(false);
	    	showPassBtn.setToolTipText("Mostra/Nascondi password");
	    	showPassBtn.setBorderPainted(false);
	    	showPassBtn.setContentAreaFilled(false);
			panel.add(showPassBtn, "w 30!, h 25!, wrap, gapleft 5"); // wrap per andare a capo
			        
	        confirmButton = new JButton("Conferma");
	        confirmButton.setFont(new Font("SansSerif", Font.BOLD, 18)); // Stile del font
	        confirmButton.setPreferredSize(new Dimension(120, 30)); // Dimensione preferita
			confirmButton.setBackground(new Color(225, 126, 47, 220)); // Colore di sfondo
			confirmButton.setForeground(Color.WHITE); // Colore del testo
			confirmButton.setOpaque(true); // Rende lo sfondo visibile
			confirmButton.setFocusPainted(false); // Rimuove il bordo focus
			confirmButton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursore a mano
	        panel.add(confirmButton, "span 2, align center"); // Posizione al centro, su due colonne
	    }
	    
	    private void initListeners() {
			  showPassBtnActionListener = new ActionListener()
			  {
				@Override 
				public void actionPerformed(ActionEvent e)
				{
					if(showPassBtn.isSelected())
					{
				    	passwordField.setEchoChar((char)0); // Mostra la password
				    	showPassBtn.setIcon(eyeIcon);
				    } 
				    else
				    {
				    	passwordField.setEchoChar('•'); // Nasconde la password
				    	showPassBtn.setIcon(eyeOffIcon);
				    }
				}				
			  };
			showPassBtn.addActionListener(showPassBtnActionListener);
			
			// DocumentListener per la validazione in tempo reale
	        passwordFieldDocumentListener = new DocumentListener() {
	            @Override
	            public void insertUpdate(DocumentEvent e) {
	                checkPassword();
	            }

	            @Override
	            public void removeUpdate(DocumentEvent e) {
	                checkPassword();
	            }

	            @Override
	            public void changedUpdate(DocumentEvent e) {
	                // Non usato per PlainDocument
	            }
	        };
	        passwordField.getDocument().addDocumentListener(passwordFieldDocumentListener);
			
			confirmButtonListener = new ActionListener()
			{
				@Override 
				public void actionPerformed(ActionEvent e)
				{
					if(!checkPassword()) // Usa il metodo di controllo rinominato
						passwordField.requestFocus();
			        else
			        {
			        	confirmButton.setEnabled(false);
			        	
			        	Controller.getController().checkDelete(parent, ConfirmEliminationDialog.this, passwordField.getPassword());
			        }
			    }
			};
			confirmButton.addActionListener(confirmButtonListener);
		}
		
		private boolean checkPassword() // Rinominato da checkNewPass() per chiarezza
		{
			boolean check = true;
			String text = new String(passwordField.getPassword()).trim();

		    if (text.isEmpty()) { // Aggiunto controllo per password vuota
		    	passwordField.setBorder(errorBorder);
		    	passwordErrorLabel.setText("La password non può essere vuota.");
		        check = false;
		    } else if (text.length() < 8 || text.length() > 30)
		    {
		    	passwordField.setBorder(errorBorder);
		    	passwordErrorLabel.setText("La password deve essere tra 8 e 30 caratteri.");
		        check = false;
		    } 
		    else
		    {
		    	passwordField.setBorder(defaultBorder);
		    	passwordErrorLabel.setText(" "); // Pulisce il messaggio di errore
		    }
		    
		    return check; 
		}
		
		public void showError(String msg)
		{
			JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
			confirmButton.setEnabled(true); // Riabilita il pulsante dopo un errore dal controller
		}	
		
	    // Aggiunto metodo per gestione successo per coerenza con l'altro dialog
	    public void showSuccess(String msg) {
	        JOptionPane.showMessageDialog(this, msg, "Successo", JOptionPane.INFORMATION_MESSAGE);
	        confirmButton.setEnabled(true); // Riabilita il pulsante
	        dispose(); // Chiude il dialog al successo
	    }
	    
	    private void disposeListeners() 
	    {		   
			 if (confirmButton != null && confirmButtonListener != null)
				 confirmButton.removeActionListener(confirmButtonListener);
			  
	        if (showPassBtn != null && showPassBtnActionListener != null)
	        	showPassBtn.removeActionListener(showPassBtnActionListener);

	        if(passwordField != null && passwordFieldDocumentListener != null)
	        	passwordField.getDocument().removeDocumentListener(passwordFieldDocumentListener);
	    }
	    
		@Override
	    public void dispose()
	    {
	    	disposeListeners();
	        super.dispose();
	    }
}