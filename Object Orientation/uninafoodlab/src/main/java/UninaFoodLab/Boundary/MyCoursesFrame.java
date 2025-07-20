package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import org.jdesktop.swingx.*;
import org.kordamp.ikonli.materialdesign.*;
import org.kordamp.ikonli.swing.*;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class MyCoursesFrame extends JXFrame implements ArgumentFilterable
{
    private static final long serialVersionUID = 1L;

    private int currentPage = 0;
    private final int CARDS_PER_PAGE = 9;
    
    private ImageIcon windowLogo;
    private HeaderPanel header;
    private JXPanel rootPanel, contentPanel, coursesPanel, navPanel;
    private JXButton leftArrow, rightArrow;
    private JXLabel titleLabel, addCourseLabel;
    private JScrollPane scrollPane;
    
    private List<Integer> idsCorsi = new ArrayList<>();
    private List<String> namesCorsi = new ArrayList<>();
    private List<List<Integer>> idsArguments = new ArrayList<>();
    private List<List<String>> namesArguments = new ArrayList<>();
    private List<Date> startDates = new ArrayList<>();
    private List<Integer> sessionsNumbers = new ArrayList<>();
    
    private String currentSearchText = "";
    private List<Integer> currentSelectedArgumentsIds = new ArrayList<>();
    private List<CourseCardPanel> allCourseCards = new ArrayList<>();
    private List<CourseCardPanel> filteredCourseCards = new ArrayList<>();

    private ActionListener leftArrowClicker, rightArrowClicker;
    private MouseAdapter addCourseClicker, cardClickListener;

    public MyCoursesFrame()
    {
        super("UninaFoodLab - " + ((Controller.getController().isChefLogged()) ? "I miei corsi" : "Le mie iscrizioni" ));

        setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1230, 960));
        setExtendedState(MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        initComponents();
        initListeners();
        loadInitialCards();
        filter("");

        loadPage(0);
        setVisible(true);
    }

    private void initComponents()
    {
    	windowLogo = new ImageIcon(getClass().getResource("/logo_finestra.png"));
		setIconImage(windowLogo.getImage());
		
        rootPanel = new JXPanel(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));
        rootPanel.setBackground(Color.WHITE);
        setContentPane(rootPanel);

        header = new HeaderPanel(this, getLayeredPane(), this);
        rootPanel.add(header, "dock north");

        contentPanel = new JXPanel(new MigLayout("fill, insets 15, wrap 1", "[grow]", "[]push[]"));
        contentPanel.setBackground(Color.white);
        rootPanel.add(contentPanel, "grow");

        titleLabel = new JXLabel(Controller.getController().isChefLogged() ? "I MIEI CORSI" : "LE MIE ISCRIZIONI");
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 30));
        titleLabel.setForeground(new Color(225, 126, 47, 220));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(titleLabel, "growx, align center");
        
        if(Controller.getController().isChefLogged())
        {
        	addCourseLabel = new JXLabel("<html><u>Aggiungi un corso</u></html>");
        	addCourseLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        	addCourseLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	contentPanel.add(addCourseLabel , "align right");
        }  	

        coursesPanel = new JXPanel(new MigLayout(
        	    "wrap 3, gap 15 15, insets 15",
        	    "[33%][33%][33%]"
        	));

        coursesPanel.setBackground(Color.WHITE);
        coursesPanel.setOpaque(true);
        
        scrollPane = new JScrollPane(coursesPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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

     	contentPanel.add(scrollPane, "grow, pushy");

     	navPanel = new JXPanel(new MigLayout("insets 0, gap 15", "[pref!][grow][pref!]", "[]"));
     	navPanel.setBackground(Color.WHITE);
     	navPanel.add(leftArrow, "aligny center");
     	navPanel.add(Box.createHorizontalGlue(), "growx");
     	navPanel.add(rightArrow, "aligny center");
     	contentPanel.add(navPanel, "gaptop 10, center");
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
    	
    	if(addCourseLabel != null)
    	{
    		addCourseClicker= new MouseAdapter()
						      {
						          @Override
						          public void mouseClicked(MouseEvent e)
						          {
						        	  Controller.getController().showCreateCourseDialog(MyCoursesFrame.this);						             
						          }
						      };	   
	        addCourseLabel.addMouseListener(addCourseClicker);
    	}
    	 	
    	cardClickListener = new MouseAdapter() 
    	{
    	    @Override
    	    public void mouseClicked(MouseEvent e) 
    	    {
    	        CourseCardPanel card = (CourseCardPanel)e.getSource();
    	        Controller.getController().showCourseDetail(MyCoursesFrame.this, card.getId());
    	    }
    	};
    }
    
    private void disposeListeners()
    {
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
    	
    	if(addCourseLabel != null && addCourseClicker != null)
    	{
    		addCourseLabel.removeMouseListener(addCourseClicker);
    		addCourseClicker = null;
    	}
    	
    	if(cardClickListener != null)
    	{
    		for(CourseCardPanel card : allCourseCards)
      	        card.removeCourseClickListener(cardClickListener);

      	cardClickListener = null;
    	}	  
    }
    
    @Override
    public void dispose()
    {
    	disposeListeners();
    	super.dispose();
    }
    
    @Override
    public void filter(String filter)
    {
        currentSearchText = filter; 
        applyFilters();
    }
    
    @Override
    public void filterByArgumentsIds(List<Integer> idsArgomenti)
    {
    	currentSelectedArgumentsIds = idsArgomenti;
        applyFilters();
    } 
    
    private void applyFilters()
    {
        filteredCourseCards.clear();
        
        for(CourseCardPanel card : allCourseCards) 
            if(card.matchesText(currentSearchText) && card.matchesArguments(currentSelectedArgumentsIds)) 
                filteredCourseCards.add(card);

        currentPage = 0;
        loadPage(currentPage);
    }
    
    private void loadInitialCards()
    {
    	allCourseCards.clear();

    	Controller.getController().loadCorsiForMyCourses(idsCorsi, namesCorsi, idsArguments, namesArguments, startDates, sessionsNumbers);    	
    	
        for(int i = 0; i < idsCorsi.size(); i++)
        {
        	CourseCardPanel card = new CourseCardPanel(idsCorsi.get(i), namesCorsi.get(i), idsArguments.get(i), namesArguments.get(i),
		 			startDates.get(i), sessionsNumbers.get(i));
        	card.addCourseClickListener(cardClickListener);
        	allCourseCards.add(card);
        }
             
        loadPage(currentPage);
    }
    
    /**
     * Aggiunge una nuova CourseCardPanel alla lista e, se
     * rispetta i filtri correnti, la rende visibile subito.
     */
    public void addCourseCard(CourseCardPanel card) 
    {
        allCourseCards.add(card);

        if(card.matchesText(currentSearchText) && card.matchesArguments(currentSelectedArgumentsIds))
            filteredCourseCards.add(card);

        loadPage(currentPage);
    }

    private void loadPage(int page)
    {
        int maxPage = (filteredCourseCards.size() - 1) / CARDS_PER_PAGE;

        if(page < 0 || page > maxPage)
            return;

        coursesPanel.removeAll();

        int startIndex = page * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, filteredCourseCards.size());

        for(int i = startIndex; i < endIndex; i++)
        	coursesPanel.add(filteredCourseCards.get(i), "grow, push");

        // Riempio di pannelli vuoti per il grid 3x3
        for(int i =  endIndex - startIndex; i < CARDS_PER_PAGE; i++) 
        {
            JXPanel empty = new JXPanel();
            empty.setOpaque(false);
            coursesPanel.add(empty, "grow, push");
        }
        
        SwingUtilities.invokeLater(() -> 
        { 
        	int available = scrollPane.getViewport().getHeight();       
        
        	for(Component c : coursesPanel.getComponents())
        			c.setPreferredSize(new Dimension(0, available));

            coursesPanel.revalidate();
            coursesPanel.repaint();
        });

        currentPage = page;
        updateArrows();
    }

    private void updateArrows()
    {
        int maxPage = (filteredCourseCards.size() - 1) / CARDS_PER_PAGE;
        leftArrow.setEnabled(currentPage > 0);
        rightArrow.setEnabled(currentPage < maxPage);
    }
    
    public void resetView()
    {
    	loadPage(0);
    }
}