package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class MyCoursesFrame extends JXFrame implements SearchFilterable
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

    private List<JXPanel> allCourseCards = new ArrayList<>();
    private List<JXPanel> filteredCourseCards = new ArrayList<>();
    
    private ActionListener leftArrowClicker, rightArrowClicker, addCourseClicker;
    

    public MyCoursesFrame()
    {
        super("UninaFoodLab - " + ((Controller.getController().isChefLogged()) ? "I miei corsi" : "Le mie iscrizioni" ));

        setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(929, 739));
        setLocationRelativeTo(null);

        initComponents();
        initListeners();
        creaTutteLeCardDemo();
        filter("");

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
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(titleLabel, "growx, split 2, align center");
        
        if(Controller.getController().isChefLogged())
        {
        	addCourseLabel = new JXLabel("<html><u>Aggiungi un corso</u></html>");
        	addCourseLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        	contentPanel.add(addCourseLabel , "growx, align right");
        }  	

        coursesPanel = new JXPanel(new MigLayout(
        	    "wrap 3, gap 10 10, insets 10, aligny top", 
        	    "[grow,fill][grow,fill][grow,fill]", 
        	    ""
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

     	// ScrollPane al centro che si espande
     	contentPanel.add(scrollPane, "grow, pushy");

     	// Frecce sotto, pannello separato
     	navPanel = new JXPanel(new MigLayout("insets 0, gap 15", "[pref!][grow][pref!]", "[]"));
     	navPanel.setBackground(Color.WHITE);
     	navPanel.add(leftArrow, "aligny center");
     	navPanel.add(Box.createHorizontalGlue(), "growx");
     	navPanel.add(rightArrow, "aligny center");
     	contentPanel.add(navPanel, "center");
    }

    private void initListeners()
    {
    	leftArrowClicker = new ActionListener()
			    		   {
			    				@Override
			    				public void actionPerformed(ActionEvent e)
			    				{
			    					caricaPagina(currentPage - 1);
			    				}
			    		   };    		   
    	leftArrow.addActionListener(leftArrowClicker);
    	
    	rightArrowClicker = new ActionListener()
						    {
								 @Override
								 public void actionPerformed(ActionEvent e)
								 {
									 caricaPagina(currentPage + 1);
								 }
						    }; 
    	rightArrow.addActionListener(rightArrowClicker);
    	
    	addCourseClicker = new ActionListener()
			    		   {
					    		@Override
								public void actionPerformed(ActionEvent e)
								{
									new CreateCourseDialog(MyCoursesFrame.this).setVisible(true);
								}
			    		   };
    }
    
    @Override
    public void filter(String filter)
    {
        filteredCourseCards.clear();

        for(JXPanel card : allCourseCards)
        {
            JXLabel label = (JXLabel)card.getComponent(0);
            
            if(label.getText().toLowerCase().contains( filter))
                filteredCourseCards.add(card);
        }

        currentPage = 0;
        caricaPagina(currentPage);
    }
    
    private void creaTutteLeCardDemo()
    {
        allCourseCards.clear();

        for(int i=1; i<=25; i++)
            allCourseCards.add(creaCardCorso("Corso " + i, "Descrizione del corso " + i));
    }
    
    private JXPanel creaCardCorso(String titolo, String descrizione)
    {
        JXPanel card = new JXPanel(new BorderLayout());
        card.setMinimumSize(new Dimension(200, 150));
        card.setBackground(Color.WHITE);

        DropShadowBorder shadow = new DropShadowBorder();
        shadow.setShadowColor(new Color(0,0,0,50));
        shadow.setShadowSize(6);
        card.setBorder(shadow);

        JXLabel titoloLabel = new JXLabel(titolo);
        titoloLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titoloLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        card.add(titoloLabel, BorderLayout.NORTH);

        JXTextArea descArea = new JXTextArea(descrizione);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setOpaque(false);
        descArea.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        card.add(descArea, BorderLayout.CENTER);

        // Rende la card cliccabile
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                
            }
        });

        return card;
    }  

    private void caricaPagina(int page)
    {
        int maxPage = (filteredCourseCards.size() - 1) / CARDS_PER_PAGE;

        if(page < 0 || page > maxPage)
            return;

        coursesPanel.removeAll();

        int startIndex = page * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, filteredCourseCards.size());

        for(int i = startIndex; i < endIndex; i++)
            coursesPanel.add(filteredCourseCards.get(i), "grow");

        // Riempie i buchi per mantenere layout 3x3 pieno
        int cardsThisPage = endIndex - startIndex;
        for(int i = 0; i < CARDS_PER_PAGE - cardsThisPage; i++)
        {
            JXPanel filler = new JXPanel();
            filler.setBackground(Color.WHITE);
            coursesPanel.add(filler, "grow");
        }

        SwingUtilities.invokeLater(() -> 
        {
            int availableHeight = scrollPane.getViewport().getHeight();

            for(Component comp : coursesPanel.getComponents())
                if(comp instanceof JXPanel)
                    comp.setPreferredSize(new Dimension(0, availableHeight));

            coursesPanel.revalidate();
            coursesPanel.repaint();
        });

        currentPage = page;
        aggiornaStatoFrecce();
    }


    private void aggiornaStatoFrecce()
    {
        int maxPage = (filteredCourseCards.size() - 1) / CARDS_PER_PAGE;
        leftArrow.setEnabled(currentPage > 0);
        rightArrow.setEnabled(currentPage < maxPage);
    }
}