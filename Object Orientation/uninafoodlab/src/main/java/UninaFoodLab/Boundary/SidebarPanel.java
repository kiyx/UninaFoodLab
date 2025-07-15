package UninaFoodLab.Boundary;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.border.MatteBorder;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXPanel;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

/**
 * {@code SidebarPanel} è un componente grafico riutilizzabile che rappresenta la barra
 * laterale sinistra per la navigazione.
 * <p>
 * Viene usato all'interno dei frame principali per offrire un accesso rapido alle sezioni:
 * <ul>
 *   <li><b>Homepage</b></li>
 *   <li><b>I miei corsi</b></li>
 *   <li><b>Le mie ricette</b> (se l'utente è uno chef)</li>
 *   <li><b>Visualizza Report</b> (se l'utente è uno chef)</li>
 * </ul>
 *
 * I pulsanti interagiscono con il {@link Controller} per gestire la navigazione tra le schermate.
 * Include anche effetti di hover e gestione sicura dei listener per evitare memory leak.
 *
 * <p><strong>Comportamento:</strong></p>
 * <ul>
 *   <li>Si adatta dinamicamente all'altezza residua del frame, ancorandosi sotto l'header.</li>
 *   <li>Supporta la personalizzazione visiva tramite {@code styleSidebarButton()}.</li>
 *   <li>Eventi gestiti automaticamente per ogni pulsante.</li>
 * </ul>
 *
 */

public class SidebarPanel extends JXPanel
{

	private static final long serialVersionUID = 1L;

	/** Riferimento al frame genitore che ospita questa sidebar. */
	private JXFrame parentFrame;
	
	/** Pulsanti di navigazione. */
	private JXButton homeBtn, coursesBtn, recipesBtn, reportBtn;
	
	/** Listener per gli eventi click sui pulsanti. */
	private ActionListener homeBtnListener, coursesBtnListener, recipesBtnListener, reportBtnListener;
	
	/** Listener per effetto hover sui pulsanti. */
	private MouseAdapter hoverListener;


	/**
	 * Costruisce una nuova {@code SidebarPanel} per il frame specificato.
	 *
	 * @param frame Il {@code JXFrame} genitore a cui è associata la sidebar.
	 */
	public SidebarPanel(JXFrame parentFrame)
	{
		this.parentFrame = parentFrame;
		
        setLayout(new MigLayout("insets 20, wrap 1", "[grow, fill]"));
        setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 0, 1, new Color(220, 220, 220)));
        setPreferredSize(new Dimension(190, 800));
        
        initComponents();
        styleComponents();
        initListeners();
	}
	
	/**
	 * Inizializza i componenti grafici della sidebar, inclusi
	 * i pulsanti condizionali se l'utente è un chef.
	 */
	private void initComponents()
	{
		homeBtn = new JXButton("  Homepage");
    	coursesBtn = new JXButton("  I miei corsi");
    	
    	add(homeBtn);
        add(coursesBtn);
        
        if(Controller.getController().isChefLogged())
        {
        	recipesBtn = new JXButton(" Le mie ricette");
        	reportBtn = new JXButton(" Visualizza Report");
        	add(recipesBtn);
            add(reportBtn);
        }
	}
	
	/**
	 * Applica uno stile ai pulsanti della sidebar.
	 */
	private void styleComponents()
	{
		 styleSidebarButton(homeBtn);
	     styleSidebarButton(coursesBtn);
	     
	     if(Controller.getController().isChefLogged())
	     {
	    	 styleSidebarButton(recipesBtn);
		     styleSidebarButton(reportBtn);	    	 
	     }
	}
	
	/**
     * Inizializza i listener degli eventi click e hover dei pulsanti della sidebar.
     * <p>
     * Ogni pulsante è associato a un'azione gestita dal {@code Controller}.
     * I listener hover evidenziano il pulsante al passaggio del mouse.
     */
	private void initListeners()
	{
		 /*
         * Listeners di navigazione
         */			 
		homeBtnListener = new ActionListener()
						  {
							 @Override
							 public void actionPerformed(ActionEvent e)
							 {
								 Controller.getController().goToHomepage(parentFrame);
							 }
						  };
		homeBtn.addActionListener(homeBtnListener);
		
		coursesBtnListener = new ActionListener()
							  {
								 @Override
								 public void actionPerformed(ActionEvent e)
								 {
									 Controller.getController().goToMyCourses(parentFrame);
								 }
							  };
		coursesBtn.addActionListener(coursesBtnListener);
		
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
		
		homeBtn.addMouseListener(hoverListener);
        coursesBtn.addMouseListener(hoverListener);
        
        if(Controller.getController().isChefLogged())
		{
			recipesBtnListener = new ActionListener()
			 {
				 @Override
				 public void actionPerformed(ActionEvent e)
				 {
					 Controller.getController().goToMyRecipes(parentFrame);
				 }
			 };
			 recipesBtn.addActionListener(recipesBtnListener);
			 
			 reportBtnListener = new ActionListener()
			 {
				 @Override
				 public void actionPerformed(ActionEvent e)
				 {
					 reportBtn.setEnabled(false);
					 Controller.getController().openMonthlyReport(parentFrame, reportBtn);
				 }
			 };
			 reportBtn.addActionListener(reportBtnListener);
			 
			 recipesBtn.addMouseListener(hoverListener);
	         reportBtn.addMouseListener(hoverListener);
		}
	}
	
	/**
	 * Rimuove tutti i listener registrati su pulsanti e mouse.
	 * <p>
	 * Va chiamato prima di rimuovere la sidebar per evitare memory leak.
	 */
	public void disposeListeners() 
	{
		if(homeBtn != null && homeBtnListener != null)
		{
			homeBtn.removeActionListener(homeBtnListener);
			homeBtnListener = null;
		}
	        
	    if(coursesBtn != null && coursesBtnListener != null)
	    {
	    	coursesBtn.removeActionListener(coursesBtnListener); 
	    	coursesBtnListener = null;
	    }
	           
	    if(recipesBtn != null && recipesBtnListener != null)
	    {
	    	recipesBtn.removeActionListener(recipesBtnListener);
	    	recipesBtnListener = null;
	    }
	        
	    if(reportBtn != null && reportBtnListener != null)
	    {
	    	reportBtn.removeActionListener(reportBtnListener);
	    	reportBtnListener = null;
	    }
	        
	    if(hoverListener != null) 
	    {
	        homeBtn.removeMouseListener(hoverListener);
	        coursesBtn.removeMouseListener(hoverListener);
	        if(recipesBtn != null) recipesBtn.removeMouseListener(hoverListener);
	        if(reportBtn != null) reportBtn.removeMouseListener(hoverListener);
	        
	        hoverListener = null;
	    }
	}
	
	 /**
     * Applica uno stile coerente a un pulsante della sidebar.
     * Imposta font, cursore, colori e bordi.
     *
     * @param button Il pulsante JXButton da stilizzare.
     */
    private void styleSidebarButton(JXButton button)
    {
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        button.setBackground(new Color(245, 245, 245));
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));  
    }
    
    /**
     * Aggiorna la posizione e le dimensioni della sidebar rispetto all'intestazione
     * e all'altezza complessiva del frame genitore.
     * <p>
     * Questo metodo va chiamato ogni volta che il frame viene ridimensionato o spostato,
     * per mantenere la sidebar correttamente ancorata sotto l'header.
     * <p>
     * La sidebar viene posizionata sempre allineata a sinistra (x = 0), subito sotto l'header,
     * con altezza adattata allo spazio residuo visibile nella finestra.
     *
     * @param header Il pannello header della finestra, usato per calcolare la posizione verticale.
     * @param layeredPane Il layered pane del frame, necessario per ottenere le coordinate assolute.
     * @param frameHeight L'altezza corrente della finestra, usata per calcolare l'altezza della sidebar.
     */
    public void updatePosition(JXPanel header, JLayeredPane layeredPane, int frameHeight)
    {
        if(isVisible())
        {
        	Point headerBottomLeft = SwingUtilities.convertPoint(header, 0, header.getHeight(), layeredPane);
            setBounds(0, headerBottomLeft.y, getWidth(), frameHeight - headerBottomLeft.y);
        }
    }
}