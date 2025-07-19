package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.jdesktop.swingx.*;
import org.kordamp.ikonli.materialdesign.*;
import org.kordamp.ikonli.swing.*;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class ProfileDropdownPanel extends JXPanel
{
	private static final long serialVersionUID = 1L;
	
	/** Riferimento al frame genitore che ospita questa sidebar. */
	private JXFrame parentFrame;
	
	private CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
			new LineBorder(new Color(220, 220, 220), 1, true),
			new EmptyBorder(10, 12, 10, 12)
	);
	private JXLabel userLabel;
	private JXPanel separator;
	private JXButton profileItemBtn, changePwItemBtn, logoutItemBtn;
	
	private ActionListener profileItemBtnListener, changePwItemBtnListener, logoutItemBtnListener;
	private MouseAdapter hoverListener;
	
	
	  /**
     * Costruisce un nuovo {@code ProfileDropdownPanel} associato al frame principale dell'applicazione.
     * Inizializza componenti, stile e listener.
     *
     * @param parentFrame Il {@code JXFrame} genitore a cui è associato questo menu.
     */
	public ProfileDropdownPanel(JXFrame parentFrame)
	{
		this.parentFrame = parentFrame;
		
		setBorder(defaultBorder);
    	setBackground(Color.WHITE);
        setOpaque(true);
		setLayout(new MigLayout("insets 10, wrap 1", "[fill, grow]"));
		
    	initComponents();
    	styleComponents();
    	initListeners();
	}

	 /**
     * Inizializza i componenti del dropdown: etichetta utente, separatore,
     * pulsanti "Profilo" e "Logout".
     * <p>
     */
	private void initComponents()
	{
		userLabel = new JXLabel(Controller.getController().getLoggedUser().getUsername(), FontIcon.of(MaterialDesign.MDI_ACCOUNT_CIRCLE, 20, new Color(80, 80, 80)), JXLabel.LEFT);
    	separator = new JXPanel();
    	profileItemBtn = new JXButton("  Profilo", FontIcon.of(MaterialDesign.MDI_ACCOUNT, 18, new Color(60, 60, 60)));
    	changePwItemBtn = new JXButton(" Cambia Password", FontIcon.of(MaterialDesign.MDI_LOCK, 18, new Color(60, 60, 60)));
    	logoutItemBtn = new JXButton("  Logout", FontIcon.of(MaterialDesign.MDI_LOGOUT, 18, new Color(60, 60, 60)));
    	
    	userLabel.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 16));
        userLabel.setForeground(new Color(40, 40, 40));
        userLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
        add(userLabel, "gaptop 0");

        separator.setBackground(new Color(200, 200, 200));
        separator.setPreferredSize(new Dimension(1, 2));
        separator.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        add(separator, "h 2!, gapbottom 8");
          
        add(profileItemBtn);
        add(changePwItemBtn);
        add(logoutItemBtn);
	}
	
	  /**
     * Inizializza i listener di azione per i pulsanti "Profilo" e "Logout"
     * e i listener per gli effetti hover.
     * <p>
     * I listener chiamano i metodi corrispondenti nel {@code Controller}.
     */
	private void initListeners()
	{
		profileItemBtnListener = new ActionListener()
								 {
									 @Override
									 public void actionPerformed(ActionEvent e)
									 {
										 Controller.getController().goToProfile(parentFrame);
									 }
								 };
		profileItemBtn.addActionListener(profileItemBtnListener);
		
		changePwItemBtnListener = new ActionListener()
								  {
									 @Override
									 public void actionPerformed(ActionEvent e)
									 {
										 Controller.getController().showChangePasswordDialog(parentFrame);					 
									 }
	
								  };
		changePwItemBtn.addActionListener(changePwItemBtnListener);		

		logoutItemBtnListener = new ActionListener()
								{
									@Override
									public void actionPerformed(ActionEvent e)
									{
										Controller.getController().logout(parentFrame);
									}
								};
		logoutItemBtn.addActionListener(logoutItemBtnListener);
		
		hoverListener = new MouseAdapter() 
						{
						    @Override
						    public void mouseEntered(MouseEvent e) 
						    {
						        if(e.getSource() instanceof JXButton) 
						        {
						            JXButton btn = (JXButton) e.getSource();
						            btn.setBackground(new Color(230, 230, 230));
						            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						        }
						    }
						    @Override
						    public void mouseExited(MouseEvent e) 
						    {
						        if(e.getSource() instanceof JXButton) 
						        {
						            JXButton btn = (JXButton) e.getSource();
						            btn.setBackground(new Color(245, 245, 245));
						        }
						    }
						 };
		 profileItemBtn.addMouseListener(hoverListener);
		 logoutItemBtn.addMouseListener(hoverListener);
	}
	
	  /**
     * Rimuove tutti i listener registrati su pulsanti e mouse.
     * <p>
     * Va chiamato prima di rimuovere il dropdown per evitare memory leak.
     * Rimuove in sicurezza anche i listener hover.
     */
	public void disposeListeners()
	{
		if(profileItemBtn!= null && profileItemBtnListener != null)
		{
			profileItemBtn.removeActionListener(profileItemBtnListener);
			profileItemBtnListener = null;
		}
			
		if(logoutItemBtn != null && logoutItemBtnListener != null)
		{
			logoutItemBtn.removeActionListener(logoutItemBtnListener);	
			logoutItemBtnListener = null;
		}
			
		if(changePwItemBtn != null && changePwItemBtnListener != null)
		{
			changePwItemBtn.removeActionListener(changePwItemBtnListener);
			changePwItemBtnListener = null;
		}	
	
        if(hoverListener != null) 
        {
            if(profileItemBtn != null) 
            	profileItemBtn.removeMouseListener(hoverListener);
            if(logoutItemBtn != null) 
            	logoutItemBtn.removeMouseListener(hoverListener);
            
            hoverListener = null;
        }
	}
	
	 /**
     * Applica lo stile grafico di base a tutti i componenti del dropdown.
     * <p>
     * Imposta colori, font, padding e altri attributi visivi.
     */
	private void styleComponents()
	{
		if(parentFrame instanceof ProfileFrame)
			profileItemBtn.setEnabled(false);
		styleDropdownButton(profileItemBtn);
		styleDropdownButton(changePwItemBtn);
		styleDropdownButton(logoutItemBtn);
	}
	
	
	 /**
     * Applica uno stile coerente a un pulsante all'interno del menu dropdown del profilo.
     * Imposta allineamento, font, colori e bordi.
     *
     * @param button Il pulsante JXButton da stilizzare.
     */
    private void styleDropdownButton(JXButton button)
    {
        button.setHorizontalAlignment(JXButton.LEFT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setContentAreaFilled(true);
        button.setBackground(new Color(245, 245, 245));
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setForeground(new Color(60, 60, 60));
    }
    
    /**
     * Posiziona dinamicamente il pannello dropdown del profilo subito sotto l'header
     * e allineato al margine destro del frame.
     * <p>
     * Questo metodo deve essere chiamato ogni volta che il frame viene ridimensionato o
     * che l'header cambia posizione, in modo da mantenere il dropdown ancorato
     * visivamente sotto l'icona del profilo utente.
     * <p>
     * Il pannello viene posizionato con:
     * <ul>
     *   <li><b>x</b> = larghezza del layered pane - larghezza del pannello - 1 (margine destro)</li>
     *   <li><b>y</b> = altezza dell'header (calcolata rispetto al layered pane)</li>
     * </ul>
     *
     * @param header Il pannello header della finestra, utilizzato per calcolare l'altezza da cui iniziare il dropdown.
     * @param layeredPane Il layered pane del frame, necessario per il calcolo delle coordinate assolute.
     */
    public void updatePosition(JXPanel header, JLayeredPane layeredPane)
    {
        if (isVisible())
        {
        	Point headerBottomLeft = SwingUtilities.convertPoint(header, 0, header.getHeight(), layeredPane);
            int x = layeredPane.getWidth() - getWidth() - 1;
            int y = headerBottomLeft.y;
            setLocation(x, y);
        }
    }
}