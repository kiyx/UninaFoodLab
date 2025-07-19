package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.jdesktop.swingx.*;
import org.kordamp.ikonli.materialdesign.*;
import org.kordamp.ikonli.swing.*;
import net.miginfocom.swing.*;
import UninaFoodLab.Controller.Controller;

/**
 * Pannello header personalizzato.
 * 
 * Questo pannello contiene:
 * <ul>
 *   <li>Logo cliccabile per tornare alla homepage.</li>
 *   <li>Bottone hamburger per mostrare/nascondere la sidebar laterale.</li>
 *   <li>Bottone filtri (non implementato in listeners, ma pronto per estensioni).</li>
 *   <li>Campo di ricerca con pulsante di ricerca.</li>
 *   <li>Bottone profilo che mostra un pannello dropdown del profilo utente.</li>
 * </ul>
 * 
 * Gestisce dinamicamente la posizione e la visibilità della sidebar e del dropdown profilo,
 * reagendo a eventi di ridimensionamento e click esterni.
 * 
 * I listener sono mantenuti come campi e possono essere rimossi con {@link #disposeListeners()} 
 * per evitare memory leak.
 * 
 * <p>Supporta anche il filtraggio dinamico dei corsi tramite
 * l'interfaccia {@link CourseFilterable}, se fornita nel costruttore.
 * In tal caso vengono aggiunti listener per aggiornare il filtro al variare del testo di ricerca
 * o al click sul pulsante Cerca.</p>
 */
public class HeaderPanel extends JXPanel
{
	private static final long serialVersionUID = 1L;

	/** Riferimento al frame genitore */
	private JXFrame parentFrame;
	
	/** Layered pane che ospita sidebar e dropdown */
	private JLayeredPane layeredPane;
	
	 /** Label contenente il logo cliccabile */
	private JXLabel logoLabel;
	
	/** Pulsanti di interazione: hamburger menu, filtri, ricerca, profilo */
	private JXButton hamburgerBtn, filterBtn, searchBtn, profileBtn;
	
	/** Campo di testo per la ricerca */
	private JXTextField searchField;
	
	/** Eventuale classe che implementa l'interfaccia **/
	private SearchFilterable filterCallback;
	
	 /** Pannello dropdown profilo */
	private ProfileDropdownPanel dropdownPanel;
	
	/** Sidebar laterale */
	private SidebarPanel sidebar;
	
	/** Pannello di argomenti che viene mostrato al click del bottone */
	private FilterPanel filterPanel;

	/** Listeners */
	private DocumentListener searchListener;
	private MouseListener logoClickListener;
    private ActionListener profileBtnListener, hamburgerBtnListener, searchBtnListener;
    private AWTEventListener dropdownClickListener;
    private ComponentListener componentResizeListener;
	
    /**
     * Costruisce un HeaderPanel associato a un frame genitore e a un layered pane.
     * 
     * Inizializza componenti grafici, layout e listener.
     * Imposta il focus iniziale sul campo di ricerca.
     * 
     * @param parentFrame il frame genitore che contiene questo header
     * @param layeredPane il layered pane dove saranno aggiunti sidebar e dropdown
     */
	public HeaderPanel(JXFrame parentFrame, JLayeredPane layeredPane)
	{
		this.parentFrame = parentFrame;
		this.layeredPane = layeredPane;
		
		setLayout(new MigLayout(
	            "insets 10 20 10 20",   
	            "[40!]10[12!]10[90!]10[25!]0[90!]0[grow, 70::1600]0[90!]10[grow, 15::120][40!][25!]",
	            "[75!]"                
	    ));
		setBackground(Color.WHITE);
        setBorder(new MatteBorder(0, 0, 1, 0, new Color(0xDDDDDD)));
        
        initComponents();
        initListeners();
        updateVisibility();
        
        parentFrame.getRootPane().setDefaultButton(searchBtn);
        
        // Setto il focus iniziale sul bottone search (prima era sul menu hamburger)
        EventQueue.invokeLater(() -> searchField.requestFocusInWindow());
	}

	/**
     * Costruisce un HeaderPanel associato a un frame genitore e a un layered pane,
     * aggiungendo la funzionalità di filtraggio dei corsi tramite un callback.
     * 
     * <p>Se il parametro {@code filterCallback} è non null,
     * vengono aggiunti listener sul campo di testo e sul pulsante Cerca
     * che richiamano il metodo {@code filtraCorsi(String)} del callback.</p>
     * 
     * @param parentFrame il frame genitore che contiene questo header
     * @param layeredPane il layered pane dove saranno aggiunti sidebar e dropdown
     * @param filterCallback callback per filtrare i corsi in base al testo di ricerca
     */
	public HeaderPanel(JXFrame parentFrame, JLayeredPane layeredPane, SearchFilterable filterCallback)
	{
	    this(parentFrame, layeredPane);
	    this.filterCallback = filterCallback;

	    initSearchFilterListeners();
	}
	
	/**
     * Inizializza tutti i componenti grafici del pannello header,
     * inclusi sidebar, dropdown profilo, pulsanti, campo di ricerca e logo.
     */
	private void initComponents()
    {
        sidebar = new SidebarPanel(parentFrame);
        sidebar.setVisible(false);
        layeredPane.add(sidebar, JLayeredPane.POPUP_LAYER);

        dropdownPanel = new ProfileDropdownPanel(parentFrame);
        dropdownPanel.setVisible(false);
        layeredPane.add(dropdownPanel, JLayeredPane.POPUP_LAYER);

        hamburgerBtn = new JXButton();
        hamburgerBtn.setIcon(FontIcon.of(MaterialDesign.MDI_MENU, 32, Color.DARK_GRAY));
        hamburgerBtn.setContentAreaFilled(false);
        hamburgerBtn.setBorder(BorderFactory.createEmptyBorder());
        hamburgerBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(hamburgerBtn, "cell 0 0, w 40!, h 40!, shrink 0");

        ImageIcon logo = new ImageIcon(getClass().getResource("/logo_schermata.png"));
        Image scaled = logo.getImage().getScaledInstance(94, 75, Image.SCALE_SMOOTH);
        logoLabel = new JXLabel(new ImageIcon(scaled));
        logoLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoLabel.setToolTipText("Torna alla homepage");
        add(logoLabel, "cell 2 0, w 94!, h 75!, gapleft 0, shrink 0");
        
        filterBtn = new JXButton("Filtri");
        filterBtn.setIcon(FontIcon.of(MaterialDesign.MDI_FILTER, 16, new Color(80, 80, 80))); 
        filterBtn.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        filterBtn.setForeground(new Color(40, 40, 40)); 
        filterBtn.setFocusPainted(false);
        filterBtn.setContentAreaFilled(true);
        filterBtn.setBackground(new Color(238, 238, 238));
        filterBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(180, 180, 180), 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16) 
        ));
        filterBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(filterBtn, "cell 4 0, h 40!, w 95!, shrink 0"); 

        searchField = new JXTextField();
        searchField.setPrompt("Cerca per nome " + ((parentFrame instanceof MyRecipesFrame) ? "Ricetta" : "Corso") +  "...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        searchField.setBackground(Color.WHITE);
        searchField.setOpaque(true);
        add(searchField, "cell 5 0, h 40!, growx, wmin 150, wmax 1600");

        searchBtn = new JXButton("Cerca");
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        searchBtn.setFocusPainted(false);
        searchBtn.setBackground(new Color(225, 126, 47));
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setOpaque(true);
        searchBtn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(225, 126, 47), 1, true),
            BorderFactory.createEmptyBorder(7, 20, 7, 20)
        ));
        searchBtn.setPreferredSize(new Dimension(90, 40));
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(searchBtn, "cell 6 0, h 40!, w 90!, shrink 0");
        
        profileBtn = new JXButton();
        profileBtn.setIcon(FontIcon.of(MaterialDesign.MDI_ACCOUNT_CIRCLE, 26, Color.DARK_GRAY));
        profileBtn.setContentAreaFilled(false);
        profileBtn.setBorder(BorderFactory.createEmptyBorder());
        profileBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        add(profileBtn, "cell 8 0, w 40!, h 40!, align right");
    }
	
	 /**
     * Inizializza tutti i listener per gestire interazioni utente:
     * <ul>
     *  <li>Click sul logo (navigazione homepage)</li>
     *  <li>Click sul pulsante profilo (toggle dropdown)</li>
     *  <li>Click sul pulsante hamburger (toggle sidebar)</li>
     *  <li>Click esterni per chiudere il dropdown profilo</li>
     *  <li>Eventi di ridimensionamento e spostamento finestra per aggiornare posizione sidebar e dropdown</li>
     * </ul>
     */
	private void initListeners()
	{
		 /*
         * Listener di navigazione
         */
		logoClickListener = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Controller.getController().goToHomepage(parentFrame);
            }
        };
        logoLabel.addMouseListener(logoClickListener);
        
        /**
         * Gestisce il click sul pulsante del profilo.
         * Mostra o nasconde il pannello dropdown del profilo posizionandolo correttamente
         * sotto l'header e allineato al bordo destro della finestra.
         * Garantisce che il dropdown appaia sempre nella posizione visibile e corretta.
         */
        profileBtnListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            	if(dropdownPanel.isVisible())
            	    dropdownPanel.setVisible(false);
            	else
            	{		
            		dropdownPanel.setSize(dropdownPanel.getPreferredSize());
                    Point headerBottomLeft = SwingUtilities.convertPoint(HeaderPanel.this, 0, getHeight(), layeredPane);
                    int x = layeredPane.getWidth() - dropdownPanel.getWidth() - 1;
                    int y = headerBottomLeft.y;
                    dropdownPanel.setLocation(x, y);
                    dropdownPanel.setVisible(true);
            	}
            }
        };
        profileBtn.addActionListener(profileBtnListener);
 
        /**
         * Gestisce il click sul pulsante hamburger del menu.
         * Alterna la visibilità della sidebar laterale.
         * Quando la sidebar viene mostrata, viene posizionata sotto l'header
         * e adattata all'altezza della finestra corrente.
         * Quando la sidebar viene nascosta, viene semplicemente disabilitata.
         */
        hamburgerBtnListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(!sidebar.isVisible())
                {
                    Point headerBottomLeft = SwingUtilities.convertPoint(HeaderPanel.this, 0, getHeight(), layeredPane);
                    sidebar.setBounds(
                        0,
                        headerBottomLeft.y,
                        190,
                        parentFrame.getHeight() - headerBottomLeft.y
                    );
                    layeredPane.setLayer(sidebar, JLayeredPane.POPUP_LAYER);
                }
                sidebar.setVisible(!sidebar.isVisible());
            }
        };
        hamburgerBtn.addActionListener(hamburgerBtnListener);

        /**
         * Listener globale AWT per la gestione della chiusura automatica del dropdown del profilo.
         * <p>
         * Questo listener viene registrato sul {@link Toolkit} per intercettare tutti gli eventi
         * di tipo {@link MouseEvent} nell'intera finestra. Viene utilizzato per rilevare clic
         * effettuati al di fuori del pannello {@code dropdownPanel} o del pulsante {@code profileBtn}
         * e, in tal caso, nascondere il menu dropdown del profilo utente.
         * <p>
         * Il controllo viene effettuato confrontando le coordinate dello schermo del clic
         * con i bounding box (in coordinate schermo) del pannello dropdown e del pulsante profilo.
         * 
         * <p><b>Funzionalità:</b>
         * <ul>
         *   <li>Chiude automaticamente il dropdown quando si clicca fuori da esso</li>
         *   <li>Evita conflitti con il clic sul pulsante profilo stesso</li>
         *   <li>Utilizza coordinate assolute dello schermo per una maggiore precisione</li>
         * </ul>
         *
         * @see Toolkit#addAWTEventListener(AWTEventListener, long)
         * @see MouseEvent
         * @see ProfileDropdownPanel
         */
        dropdownClickListener = new AWTEventListener()
        {
            @Override
            public void eventDispatched(AWTEvent event)
            {
                if(dropdownPanel.isVisible() && dropdownPanel.isShowing())
                {
                	if(event instanceof MouseEvent me && me.getID() == MouseEvent.MOUSE_PRESSED)
                    {
                        // Coordinate click in screen space
                        Point clickPoint = me.getLocationOnScreen();

                        // Bounds del dropdown in screen space
                        Rectangle dropdownBounds = dropdownPanel.getBounds();
                        Point dropdownLoc = dropdownPanel.getLocationOnScreen();
                        Rectangle dropdownScreenBounds = new Rectangle(dropdownLoc, dropdownBounds.getSize());

                        // Bounds del pulsante profilo in screen space
                        Rectangle profileBtnBounds = profileBtn.getBounds();
                        Point profileBtnLoc = profileBtn.getLocationOnScreen();
                        Rectangle profileBtnScreenBounds = new Rectangle(profileBtnLoc, profileBtnBounds.getSize());

                        if(!dropdownScreenBounds.contains(clickPoint) && !profileBtnScreenBounds.contains(clickPoint))
                            dropdownPanel.setVisible(false);
                    }
                }
            }
        };
        Toolkit.getDefaultToolkit().addAWTEventListener(dropdownClickListener, AWTEvent.MOUSE_EVENT_MASK);

        /**
         * Listener per eventi di ridimensionamento e spostamento della finestra.
         * Aggiorna la posizione e dimensione della sidebar e del dropdown del profilo
         * per mantenere un corretto posizionamento rispetto all'header e alla finestra.
         * Garantisce che i componenti popup rimangano ben allineati e visibili anche al resize o spostamento.
         */
        componentResizeListener = new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                updatePositions();
            }

            @Override
            public void componentMoved(ComponentEvent e)
            {
                updatePositions();
            }
        };
        parentFrame.addComponentListener(componentResizeListener);
    }
	
	/**
     * Inizializza i listener dedicati al filtraggio dei corsi.
     * 
     * <p>Aggiunge un {@link DocumentListener} al campo di testo di ricerca
     * per aggiornare dinamicamente il filtro ad ogni modifica del testo.
     * Inoltre aggiunge un {@link ActionListener} al pulsante Cerca
     * per applicare il filtro al click.</p>
     * 
     * <p>Questi listener richiamano il metodo
     * {@link SearchFilterable#filter(String)} con il testo corrente.</p>
     */
	private void initSearchFilterListeners()
	{  
		/**
		 * Un {@link DocumentListener} collegato al documento
         * del campo di testo {@code searchField} che intercetta ogni modifica del testo,
         * richiamando il metodo {@code filter} del callback con il testo aggiornato.
		 */
    	searchListener = new DocumentListener()
				         {
    						 @Override	
				             public void insertUpdate(DocumentEvent e)  { aggiornaFiltro(); }
    						 @Override
				             public void removeUpdate(DocumentEvent e)  { aggiornaFiltro(); }
    						 @Override
				             public void changedUpdate(DocumentEvent e) { aggiornaFiltro(); }
				
				             private void aggiornaFiltro()
				             {
				                 String testo = searchField.getText().trim().toLowerCase();
				                 if(testo.equals(""))
				                 	filterCallback.filter(testo);
				             }
				         };
        searchField.getDocument().addDocumentListener(searchListener);

        /**
         * Un {ActionListener} collegato al pulsante {@code searchBtn}
         * che, al click, esegue la stessa azione di filtraggio con il testo corrente.
         */
        searchBtnListener = new ActionListener()
			        		{
        						@Override
        						public void actionPerformed(ActionEvent e)
        						{
        							String testo = searchField.getText().trim().toLowerCase();
    					            filterCallback.filter(testo);
        						}
					            
			        		};
        searchBtn.addActionListener(searchBtnListener);      
	}
	
	/**
     * Aggiorna la visibilità dei componenti (es. search bar) 
     * in base al frame genitore attuale.
     */
    public void updateVisibility()
    {
    	boolean isProf = parentFrame instanceof ProfileFrame;
    	
    	if(isProf || parentFrame instanceof MyRecipesFrame)
    		filterBtn.setVisible(false);
    	
    	if(isProf)
    	{
    		searchField.setVisible(false);
            searchBtn.setVisible(false);
    	}
    }
    
    /**
     * Rimuove tutti i listener aggiunti ai componenti e al toolkit,
     * da chiamare quando il pannello non è più utilizzato per evitare memory leak.
     * Rimuove inoltre listener da sidebar e dropdownPanel.
     * 
     * <p>Rimuove anche i listener di filtraggio se è stato impostato un
     * {@link CourseFilterable} e sono stati registrati i listener su
     * {@code searchField} e {@code searchBtn}.</p>
     */
	public void disposeListeners()
    {
		if(logoLabel != null && logoClickListener != null)
        {
            logoLabel.removeMouseListener(logoClickListener);
            logoClickListener = null;
        }

        if(profileBtn != null && profileBtnListener != null)
        {
            profileBtn.removeActionListener(profileBtnListener);
            profileBtnListener = null;
        }

        if(hamburgerBtn != null && hamburgerBtnListener != null)
        {
            hamburgerBtn.removeActionListener(hamburgerBtnListener);
            hamburgerBtnListener = null;
        }

        if(dropdownClickListener != null)
        {
            Toolkit.getDefaultToolkit().removeAWTEventListener(dropdownClickListener);
            dropdownClickListener = null;
        }

        if(componentResizeListener != null)
        {
            parentFrame.removeComponentListener(componentResizeListener);
            componentResizeListener = null;
        }

        if(sidebar != null)
        	sidebar.disposeListeners();
        
        if(dropdownPanel != null)
        	dropdownPanel.disposeListeners();
        
        if(filterPanel != null)
        	filterPanel.disposeListeners();
             
        if(filterCallback != null)
        {
        	if(searchField != null && searchListener != null)
        	{
        		searchField.getDocument().removeDocumentListener(searchListener);
        		searchListener = null;
        	}
        	
        	if(searchBtn != null && searchBtnListener != null)
        	{
        		searchBtn.removeActionListener(searchBtnListener);
        		searchBtnListener = null;
        	}
        }
    }
	
	 /**
     * Aggiorna dinamicamente la posizione e dimensione di dropdown profilo e sidebar
     * in base alla dimensione corrente del layered pane e del frame genitore.
     * Viene chiamato in seguito a resize o spostamento della finestra.
     */
    private void updatePositions()
    {
        if(dropdownPanel.isVisible())
        {
            Point headerBottomLeft = SwingUtilities.convertPoint(this, 0, getHeight(), layeredPane);
            int x = layeredPane.getWidth() - dropdownPanel.getWidth() - 1;
            int y = headerBottomLeft.y;
            dropdownPanel.setLocation(x, y);
        }

        if(sidebar.isVisible())
        {
            Point headerBottomLeft = SwingUtilities.convertPoint(this, 0, getHeight(), layeredPane);
            sidebar.setBounds(
                0,
                headerBottomLeft.y,
                190,
                parentFrame.getHeight() - headerBottomLeft.y
            );
        }
    }

    /**
     * Resetta la vista impostando il campo di ricerca vuoto
     * e mettendo il focus su di esso.
     */
    public void resetView()
    {
        searchField.setText("");
        searchField.requestFocusInWindow();
    }
}