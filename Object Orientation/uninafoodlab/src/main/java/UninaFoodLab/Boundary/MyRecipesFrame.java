package UninaFoodLab.Boundary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class MyRecipesFrame extends JXFrame implements SearchFilterable
{

    private static final long serialVersionUID = 1L;
    
    private int currentPage = 0;
    private final int CARDS_PER_PAGE = 9;
    
    private JXButton leftArrow;
    private JXButton rightArrow;
    
    private JXPanel rootPanel,  mainContentPanel, recipesPanel, navPanel;
    private JXLabel titleLabel, newRecipeLabel;
    private JScrollPane scrollPane;
    private HeaderPanel header;
    
    private String currentSearchText = "";
    private List<RecipeCardPanel> allRecipeCards;
    private List<JXPanel> filteredRecipeCards;
        
    private ArrayList<String> nomiRicette = new ArrayList<>();
    private ArrayList<String> difficoltaRicette = new ArrayList<>();
    private ArrayList<Integer> calorieRicette = new ArrayList<>();
    private ArrayList<Integer> idRicette = new ArrayList<>();
    
    private ActionListener leftArrowClicker, rightArrowClicker;
    private MouseAdapter nuovaRicettaMouseListener, cardClickListener;


	public MyRecipesFrame()
	{
        setTitle("UninaFoodLab - Le mie ricette");
        setMinimumSize(new Dimension(1230, 960));
        setExtendedState(MAXIMIZED_BOTH);
        setExtendedState(JXFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        allRecipeCards = new ArrayList<>();
        filteredRecipeCards = new ArrayList<>();
        
        initComponents();
        initListeners(); 
        loadInitialCards();
        filter("");

        loadPage(0);
        setVisible(true);
    }
   
 

    private void initComponents()
    {
        ImageIcon windowLogo = new ImageIcon(getClass().getResource("/logo_finestra.png"));
        setIconImage(windowLogo.getImage());

        rootPanel = new JXPanel(new MigLayout("fill, insets 15, wrap 1", "[grow]", "[]push[]"));
        rootPanel.setBackground(Color.WHITE);
        setContentPane(rootPanel);

        header = new HeaderPanel(this, getLayeredPane(), this);
        rootPanel.add(header, "dock north");

        mainContentPanel = new JXPanel(new MigLayout("fill, insets 15, wrap 1", "[grow]", "[]push[]"));
        mainContentPanel.setBackground(Color.white);
        rootPanel.add(mainContentPanel, "grow");

        
        titleLabel = new JXLabel("LE MIE RICETTE");
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 30));
        titleLabel.setForeground(new Color(225, 126, 47, 220));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainContentPanel.add(titleLabel, "growx, align center");
        
        
        newRecipeLabel = new JXLabel("<html><u>Aggiungi ricetta</u></html>");
        newRecipeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        newRecipeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        mainContentPanel.add(newRecipeLabel , "align right");
    	    	
        recipesPanel = new JXPanel(new MigLayout(
        	    "wrap 3, gap 15 15, insets 15",
        	    "[33%][33%][33%]"
        	));
        recipesPanel.setBackground(Color.WHITE);
        recipesPanel.setOpaque(true);

        scrollPane = new JScrollPane(recipesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);

        leftArrow = new JXButton(FontIcon.of(MaterialDesign.MDI_ARROW_LEFT, 25));
        leftArrow.setFont(new Font("Segoe UI", Font.BOLD, 24));
        leftArrow.setFocusable(false);
        leftArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
     	leftArrow.setEnabled(false);

     	rightArrow = new JXButton(FontIcon.of(MaterialDesign.MDI_ARROW_RIGHT, 25));
     	rightArrow.setFont(new Font("Segoe UI", Font.BOLD, 24));
     	rightArrow.setFocusable(false);
     	rightArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
     	rightArrow.setEnabled(false);
     	
     	mainContentPanel.add(scrollPane, "grow, pushy");

     	navPanel = new JXPanel(new MigLayout("insets 0, gap 15", "[pref!][grow][pref!]", "[]"));
     	navPanel.setBackground(Color.WHITE);
     	navPanel.add(leftArrow, "aligny center");
     	navPanel.add(Box.createHorizontalGlue(), "growx");
     	navPanel.add(rightArrow, "aligny center");
     	mainContentPanel.add(navPanel, "gaptop 10, center");
     	
        Controller.getController().loadAllRicette(nomiRicette, difficoltaRicette, calorieRicette, idRicette);
        
        
    }
 
 
    
    private void initListeners()
    {
    	leftArrowClicker = new ActionListener()
		   {
				@Override
				public void actionPerformed(ActionEvent e)
				{
					loadPage(currentPage - 1);
				}
		   };    		   
		   
		leftArrow.addActionListener(leftArrowClicker);
		
		rightArrowClicker = new ActionListener()
				    {
						 @Override
						 public void actionPerformed(ActionEvent e)
						 {
							 loadPage(currentPage + 1);
						 }
				    }; 
		rightArrow.addActionListener(rightArrowClicker);

        nuovaRicettaMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	Controller.getController().showCreateRecipeDialog(MyRecipesFrame.this);
            }
        };
        newRecipeLabel.addMouseListener(nuovaRicettaMouseListener);
        
        cardClickListener = new MouseAdapter() 
    	{
    	    @Override
    	    public void mouseClicked(MouseEvent e) 
    	    {
    	        RecipeCardPanel card = (RecipeCardPanel)e.getSource();
    	        Controller.getController().showDetailRecipe(card.getId(), MyRecipesFrame.this);
    	    }
    	};
  }
    
    
    private void disposeListeners() 
    {
        header.disposeListeners();
        if(leftArrow != null && leftArrowClicker != null)
    	{
    		leftArrow.removeActionListener(leftArrowClicker);
    		leftArrowClicker = null;   		
    	}
    	
    	if(rightArrow != null && rightArrowClicker != null)
    	{
    		rightArrow.removeActionListener(rightArrowClicker);
    		rightArrowClicker = null;   		
    	}
    	
    	if(newRecipeLabel != null && nuovaRicettaMouseListener != null)
    	{
    		newRecipeLabel.removeMouseListener(nuovaRicettaMouseListener);
    		nuovaRicettaMouseListener = null;
    	}
    	
    	if(cardClickListener != null)
    	{
    		for(RecipeCardPanel card : allRecipeCards)
      	        card.removeRecipeClickListener(cardClickListener);

      	cardClickListener = null;
    	}
    }
 
   
    @Override
    public void dispose()
    {
    	Controller.getController().clearMyRecipesCache();
    	disposeListeners();
        super.dispose();
    }
    
    @Override
    public void filter(String filter)
    {
        currentSearchText = filter; 
        applyFilters();
    }
    
    private void applyFilters()
    {
        filteredRecipeCards.clear();
        
        for(RecipeCardPanel card : allRecipeCards) 
            if(card.matchesText(currentSearchText)) 
            	filteredRecipeCards.add(card);

        currentPage = 0;
        loadPage(currentPage);
    }
    
    private void loadInitialCards()
    {
    	allRecipeCards.clear();   	
    	
        for(int i = 0; i < idRicette.size(); i++)
        {
        	RecipeCardPanel card = new RecipeCardPanel(idRicette.get(i), nomiRicette.get(i), difficoltaRicette.get(i), calorieRicette.get(i));
        	card.addRecipeClickListener(cardClickListener);
        	allRecipeCards.add(card);
        }
             
        loadPage(currentPage);
    }
    
    public void newRecipeCard(RecipeCardPanel card)
    {
    	idRicette.add(card.getId());
		calorieRicette.add(card.getCalorie());
		difficoltaRicette.add(card.getDifficolta());
		nomiRicette.add(card.getNome());
		card.addRecipeClickListener(cardClickListener);
		allRecipeCards.add(card);

        if(card.matchesText(currentSearchText))
            filteredRecipeCards.add(card);

        loadPage(currentPage);
    }
    
    public void addRecipeCard(RecipeCardPanel card) 
    {
        allRecipeCards.add(card);

        if(card.matchesText(currentSearchText))
            filteredRecipeCards.add(card);

        loadPage(currentPage);
    }

    
    public void deleteCard(int id) {


        for (int i = allRecipeCards.size() - 1; i >= 0; i--) {
            RecipeCardPanel card = allRecipeCards.get(i);
            if (card.getId() == id) {
                // Rimuovi la card dalla lista principale
                allRecipeCards.remove(i);
              
                    idRicette.remove(i);               
                    calorieRicette.remove(i);            
                    difficoltaRicette.remove(i);                
                    nomiRicette.remove(i);                
                break;
            }
        }


        filteredRecipeCards.clear(); 
        for (RecipeCardPanel card : allRecipeCards) {
            if (card.matchesText(currentSearchText)) {
                filteredRecipeCards.add(card);
            }
        }

        loadPage(currentPage);
    }
    
    public void updateRecipeCard(int id, RecipeCardPanel cardToAdd)
    {
        int indexInModel = -1;
        int indexInFiltered = -1;

        // Aggiungo il listener alla nuova card
        cardToAdd.addRecipeClickListener(cardClickListener);

        // trova l'indice nel modello principale (allRecipeCards)
        for (int i = 0; i < allRecipeCards.size(); i++) {
            if (allRecipeCards.get(i).getId() == id) {
                indexInModel = i;
                break;
            }
        }
        
        if (indexInModel != -1) {
            // Sostituzione nell'ArrayList principale
            allRecipeCards.set(indexInModel, cardToAdd);
            
            // Sincronizzazione delle liste parallele
            idRicette.remove(indexInModel);               
            calorieRicette.remove(indexInModel);            
            difficoltaRicette.remove(indexInModel);                
            nomiRicette.remove(indexInModel);
            
            idRicette.add(indexInModel, cardToAdd.getId());
            calorieRicette.add(indexInModel, cardToAdd.getCalorie());
            difficoltaRicette.add(indexInModel, cardToAdd.getDifficolta());
            nomiRicette.add(indexInModel, cardToAdd.getNome());
        } else {
            System.err.println("Errore: Card con ID " + id + " non trovata nella lista modello.");
            return;
        }
        
        // Cerchiamo l'indice nella lista filtrata
        for (int i = 0; i < filteredRecipeCards.size(); i++) {
            if (((RecipeCardPanel)filteredRecipeCards.get(i)).getId() == id) {
                 indexInFiltered = i;
                 filteredRecipeCards.set(indexInFiltered, cardToAdd);
                 break;
            }
        }


        loadPage(currentPage);        
    }
    
    private void loadPage(int page)
    {
        int maxPage = (filteredRecipeCards.size() - 1) / CARDS_PER_PAGE;

        if(page < 0 || page > maxPage)
            return;

        recipesPanel.removeAll();

        int startIndex = page * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, filteredRecipeCards.size());

        for(int i = startIndex; i < endIndex; i++)
        	recipesPanel.add(filteredRecipeCards.get(i), "grow, push");

        // Riempio di pannelli vuoti per il grid 3x3
        for(int i =  endIndex - startIndex; i < CARDS_PER_PAGE; i++) 
        {
            JXPanel empty = new JXPanel();
            empty.setOpaque(false);
            recipesPanel.add(empty, "grow, push");
        }
        
        SwingUtilities.invokeLater(() -> 
        { 
        	int available = scrollPane.getViewport().getHeight();       
        
        	for(Component c : recipesPanel.getComponents())
        			c.setPreferredSize(new Dimension(0, available));

        	recipesPanel.revalidate();
        	recipesPanel.repaint();
        });

        currentPage = page;
        updateArrows();
    }
    
    private void updateArrows()
    {
        int maxPage = (filteredRecipeCards.size() - 1) / CARDS_PER_PAGE;
        leftArrow.setEnabled(currentPage > 0);
        rightArrow.setEnabled(currentPage < maxPage);
    }
    
    public void resetView() 
    {
        header.resetView();
        loadPage(0);
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
