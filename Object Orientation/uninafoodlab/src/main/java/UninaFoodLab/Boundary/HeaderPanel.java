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
	private boolean filterPanelVisible = false;
	
	/** Listeners */
	private DocumentListener searchListener;
	private MouseListener logoClickListener;
    private ActionListener profileBtnListener, hamburgerBtnListener, searchBtnListener, filterBtnClicker;
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

        EventQueue.invokeLater(() -> searchField.requestFocusInWindow());
	}


    
	public HeaderPanel(JXFrame parentFrame, JLayeredPane layeredPane, SearchFilterable filterCallback)
	{
	    this(parentFrame, layeredPane);
	    this.filterCallback = filterCallback;

	    initSearchFilterListeners();
	    
	    if(filterCallback instanceof ArgumentFilterable argumentFilterable) 
	    {
	        filterPanel = new FilterPanel(argumentFilterable);
	        filterPanel.setVisible(false);
	        layeredPane.add(filterPanel, JLayeredPane.POPUP_LAYER);

	        initArgumentFilterListeners();  
	    }
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
	

	private void initSearchFilterListeners()
	{  
	
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
    

	private void initArgumentFilterListeners()
	{
		filterBtnClicker = new ActionListener() 
        {
            @Override
            public void actionPerformed(ActionEvent e) 
            {
                filterPanelVisible = !filterPanelVisible;

                if(filterPanelVisible) 
                {
                    Point loc = SwingUtilities.convertPoint(filterBtn, 0, filterBtn.getHeight(), layeredPane);
                    filterPanel.setSize(filterPanel.getPreferredSize());
                    filterPanel.setLocation(loc.x, loc.y);
                    filterPanel.setVisible(true);
                } 
                else
                    filterPanel.setVisible(false);
            }
        };
		filterBtn.addActionListener(filterBtnClicker);
	}
	
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
        	
        	if(filterBtn != null && filterBtnClicker != null)
        	{
        		filterBtn.removeActionListener(filterBtnClicker);   		
        		filterBtnClicker = null;
        	}
        }
    }
	
	/**
     * Aggiorna la visibilità dei componenti (es. search bar) 
     * in base al frame genitore attuale.
     */
    public void updateVisibility()
    {
    	boolean isProfile = parentFrame instanceof ProfileFrame;
    	
    	if(isProfile || parentFrame instanceof MyRecipesFrame)
    		filterBtn.setVisible(false);
    	
    	if(isProfile)
    	{
    		searchField.setVisible(false);
            searchBtn.setVisible(false);
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