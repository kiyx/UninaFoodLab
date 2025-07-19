package UninaFoodLab.Boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class MyRecipesFrame extends JXFrame implements SearchFilterable
{

    private static final long serialVersionUID = 1L;

    private JXButton leftArrow;
    private JXButton rightArrow;
    
    private JXPanel rootPanel,  mainContentPanel, recipesPanel, titlePanel;
    private JXLabel titleLabel, newRecipeLabel;
    private JScrollPane scrollPane;
    private HeaderPanel header;
    
    private List<JXPanel> allRecipeCards;
    private List<JXPanel> filteredRecipeCards;
    
    private int currentPage = 0;
    private final int CARDS_PER_PAGE = 9;
    
    private ArrayList<String> nomiRicette = new ArrayList<>();
    private ArrayList<String> difficoltaRicette = new ArrayList<>();
    private ArrayList<Integer> calorieRicette = new ArrayList<>();
    private ArrayList<Integer> idRicette = new ArrayList<>();
    
    private MouseListener nuovaRicettaMouseListener;


	public MyRecipesFrame()
	{
        setTitle("UninaFoodLab - Le mie ricette");
        setMinimumSize(new Dimension(700, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JXFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        allRecipeCards = new ArrayList<>();
        filteredRecipeCards = new ArrayList<>();
        
        initComponents();
        initListeners(); 
        setVisible(true);
    }
   
 

    private void initComponents()
    {
    	// Icona finestra
        ImageIcon windowLogo = new ImageIcon(getClass().getResource("/logo_finestra.png"));
        setIconImage(windowLogo.getImage());

        // Root panel
        rootPanel = new JXPanel(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));
        rootPanel.setBackground(new Color(0xFAFAFA));
        setContentPane(rootPanel);

        // Header
        header = new HeaderPanel(this, getLayeredPane(), this);
        rootPanel.add(header, "dock north");

        // Pannello principale dei contenuti
        mainContentPanel = new JXPanel(new MigLayout("fill, insets 15", "[grow, fill]", "[min!][grow, fill]"));
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(0xFFE5B4), 1), 
            BorderFactory.createEmptyBorder(30, 50, 30, 50)));
        rootPanel.add(mainContentPanel, "grow");

        titlePanel = new JXPanel(new MigLayout("", "[grow, fill][grow, fill][]", "[grow, fill]"));
        titlePanel.setBackground(Color.WHITE);
        titleLabel = new JXLabel("Le mie ricette");
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 30));
        titleLabel.setForeground(new Color(0x9C5B17)); 
        titlePanel.add(titleLabel, "cell 1 0, center");
        
        newRecipeLabel = new JXLabel("<html><u>Aggiungi ricetta</u></html>");
        newRecipeLabel.setForeground(Color.ORANGE.darker());
        newRecipeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        titlePanel.add(newRecipeLabel, "cell 2 0, right, wrap");
        
    	mainContentPanel.add(titlePanel, "cell 0 0");
    	
    	
    	
        recipesPanel = new JXPanel(new MigLayout("wrap 4, fillx, insets 10", "[grow, fill]", "[]"));
        recipesPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(recipesPanel);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //mainContentPanel.add(scrollPane, "grow, cell 0 1, span 2");

        leftArrow = new JXButton(FontIcon.of(MaterialDesign.MDI_ARROW_LEFT, 25));
        leftArrow.setFont(new Font("Segoe UI", Font.BOLD, 24));
        leftArrow.setFocusable(false);
        leftArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
     	leftArrow.setEnabled(false);
     	leftArrow.addActionListener(e -> caricaPagina(currentPage - 1));

     	rightArrow = new JXButton(FontIcon.of(MaterialDesign.MDI_ARROW_RIGHT, 25));
     	rightArrow.setFont(new Font("Segoe UI", Font.BOLD, 24));
     	rightArrow.setFocusable(false);
     	rightArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
     	rightArrow.setEnabled(false);
     	rightArrow.addActionListener(e -> caricaPagina(currentPage + 1));
     	
     	JXPanel navPanel = new JXPanel(new MigLayout("insets 0, gap 15", "[pref!][grow][pref!]", "[]"));
     	navPanel.setBackground(Color.WHITE);
     	navPanel.add(leftArrow, "aligny center");
     	navPanel.add(scrollPane, "grow");
     	navPanel.add(rightArrow, "aligny center");

     	mainContentPanel.add(navPanel, "dock south, align center");
     	
        Controller.getController().loadAllRicette(nomiRicette, difficoltaRicette, calorieRicette, idRicette);
		populateRecipeCards();
        
        
    }
 
 
    
    private void initListeners()
    {
        nuovaRicettaMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	addNewRicetta();
            }
        };
        newRecipeLabel.addMouseListener(nuovaRicettaMouseListener);
    }
    
    
    private void disposeListeners() 
    {
        header.disposeListeners();
    }
 
   
    @Override
    public void dispose()
    {
    	disposeListeners();
        super.dispose();
    }
    
    private void addNewRicetta()
    {
    	Controller.getController().showCreateRecipeDialog(this);
    }
    
    private void populateRecipeCards()
	{

		for (int i=0; i<nomiRicette.size(); i++)
		{			
			JXPanel card = creaCardRicetta(nomiRicette.get(i), difficoltaRicette.get(i), calorieRicette.get(i));
			recipesPanel.add(card, "grow, push");
			allRecipeCards.add(card);
		}
		
		recipesPanel.revalidate();
        recipesPanel.repaint();
	}
    
    /**
     * Ripristina la visualizzazione iniziale della finestra:
     * <ul>
     *   <li>Svuota il campo di ricerca</li>
     *   <li>Imposta il focus sul campo di ricerca</li>

     * </ul>
     */
    
    public void resetView() 
    {
        header.resetView();
        // scrollPane.getVerticalScrollBar().setValue(0); // se hai scroll
        // refreshTableModel(); // se carichi dati
    }
    
    private JXPanel creaCardRicetta (String titolo, String difficolta, int calorie)
    {
		
        JXPanel card = new JXPanel(new BorderLayout());
        card.setName(titolo);
        card.setPreferredSize(null);
        card.setMinimumSize(new Dimension(200, 120));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setBackground(Color.WHITE);

        // Ombra leggera con SwingX DropShadowBorder
        DropShadowBorder shadow = new DropShadowBorder();
        shadow.setShadowColor(new Color(0,0,0,50));
        shadow.setShadowSize(6);
        card.setBorder(shadow);

        JXLabel titoloLabel = new JXLabel(titolo);
        titoloLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titoloLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        card.add(titoloLabel, BorderLayout.NORTH);

        card.add(new JLabel("Difficoltà: " + difficolta), BorderLayout.CENTER);
		card.add(new JLabel("Calorie: " + calorie), BorderLayout.AFTER_LAST_LINE);

        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	for(int i=0; i<nomiRicette.size(); i++)
            	{
            		if(card.getName().equals(nomiRicette.get(i)))
            			Controller.getController().showDetailRecipe(idRicette.get(i), MyRecipesFrame.this);
            	}               
            }
        });

        return card;
    }
    
    
    @Override
    public void filter(String filter)
    {
        String testo = filter.toLowerCase();
        filteredRecipeCards.clear();

        for(JXPanel card : allRecipeCards)
        {
            JXLabel label = (JXLabel)card.getComponent(0);
            if(label.getText().toLowerCase().contains(testo))
            {
            	filteredRecipeCards.add(card);
            }
        }

        currentPage = 0;
        caricaPagina(currentPage);
    }
    
    private void caricaPagina(int page)
    {
        int maxPage = (filteredRecipeCards.size() - 1) / CARDS_PER_PAGE;

        if(page < 0 || page > maxPage)
            return;

        recipesPanel.removeAll();

        int startIndex = page * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, filteredRecipeCards.size());

        for(int i = startIndex; i < endIndex; i++)
        	recipesPanel.add(filteredRecipeCards.get(i), "grow");

        // Riempie i buchi per mantenere layout 3x3 pieno
        int cardsThisPage = endIndex - startIndex;
        for(int i = 0; i < CARDS_PER_PAGE - cardsThisPage; i++)
        {
            JXPanel filler = new JXPanel();
            filler.setMinimumSize(new Dimension(200, 150));
            filler.setBackground(Color.WHITE);
            recipesPanel.add(filler, "grow");
        }

        SwingUtilities.invokeLater(() -> 
        {
            int availableHeight = scrollPane.getViewport().getHeight();

            for(Component comp : recipesPanel.getComponents())
                if(comp instanceof JXPanel)
                    comp.setPreferredSize(new Dimension(0, availableHeight));

            recipesPanel.revalidate();
            recipesPanel.repaint();
        });

        currentPage = page;
        aggiornaStatoFrecce();
    }

    private void aggiornaStatoFrecce()
    {
        int maxPage = (filteredRecipeCards.size() - 1) / CARDS_PER_PAGE;
        leftArrow.setEnabled(currentPage > 0);
        rightArrow.setEnabled(currentPage < maxPage);
    }
    
	public void showError(String msg)
	{
		JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
	}	
	
	public void showSuccess(String msg) 
	{
        JOptionPane.showMessageDialog(this, msg, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }

}
