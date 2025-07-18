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

    private JXPanel rootPanel;

    private HeaderPanel header;

    private JXPanel contentPanel;

    private JXLabel titleLabel;

    private JXPanel coursesPanel;
    private JScrollPane scrollPane;

    private JXButton leftArrow;
    private JXButton rightArrow;

    private List<JXPanel> allCourseCards;
    private List<JXPanel> filteredCourseCards;

    private int currentPage = 0;
    private final int CARDS_PER_PAGE = 9;

    public MyCoursesFrame()
    {
        super("I miei corsi");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        allCourseCards = new ArrayList<>();
        filteredCourseCards = new ArrayList<>();

        initComponents();

        creaTutteLeCardDemo();

        filter("");

        setVisible(true);
    }

    private void initComponents()
    {
        // Root panel con MigLayout (fill + insets 0)
        rootPanel = new JXPanel(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));
        rootPanel.setBackground(new Color(0xFAFAFA));
        setContentPane(rootPanel);

        // Header docked top (north)
        header = new HeaderPanel(this, getLayeredPane(), this);
        rootPanel.add(header, "dock north");

        // Content panel centrato e resizable
        contentPanel = new JXPanel(new MigLayout("fill, insets 15", "[grow]", "[][grow][]"));
        contentPanel.setBackground(Color.white);
        rootPanel.add(contentPanel, "grow");

        // Titolo semplice sopra le card, no sfondo, centrato
        titleLabel = new JXLabel(Controller.getController().isChefLogged() ? "I MIEI CORSI" : "LE MIE ISCRIZIONI");
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 30));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        contentPanel.add(titleLabel, "growx, wrap, align center");


        coursesPanel = new JXPanel(new MigLayout(
        	    "wrap 3, gap 10 10, insets 10, alignx center, fillx, filly",
        	    "[grow,fill][grow,fill][grow,fill]",
        	    "[grow,fill][grow,fill][grow,fill]"
        	));

        coursesPanel.setBackground(Color.WHITE);
        coursesPanel.setOpaque(true);
        scrollPane = new JScrollPane(coursesPanel,
             JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setViewportView(coursesPanel);
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
     	leftArrow.addActionListener(e -> caricaPagina(currentPage - 1));

     	rightArrow = new JXButton(FontIcon.of(MaterialDesign.MDI_ARROW_RIGHT, 25));
     	rightArrow.setFont(new Font("Segoe UI", Font.BOLD, 24));
     	rightArrow.setFocusable(false);
     	rightArrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
     	rightArrow.setEnabled(false);
     	rightArrow.addActionListener(e -> caricaPagina(currentPage + 1));

     	// Pannello di navigazione: scrollPane + frecce
     	JXPanel navPanel = new JXPanel(new MigLayout("insets 0, gap 15", "[pref!][grow][pref!]", "[]"));
     	navPanel.setBackground(Color.WHITE);
     	navPanel.add(leftArrow, "aligny center");
     	navPanel.add(scrollPane, "grow");
     	navPanel.add(rightArrow, "aligny center");

     	contentPanel.add(navPanel, "dock south, align center");


    }

    private void creaTutteLeCardDemo()
    {
        allCourseCards.clear();

        for(int i=1; i<=25; i++)
        {
            JXPanel card = creaCardCorso("Corso " + i, "Descrizione del corso " + i);
            allCourseCards.add(card);
        }
    }

    private void caricaPagina(int page)
    {
        int maxPage = (filteredCourseCards.size() - 1) / CARDS_PER_PAGE;

        if(page < 0 || page > maxPage)
            return;

        currentPage = page;

        coursesPanel.removeAll();

        int startIndex = currentPage * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, filteredCourseCards.size());

        for(int i = startIndex; i < endIndex; i++)
        {
            JXPanel card = filteredCourseCards.get(i);
            coursesPanel.add(card, "grow, push");
        }

        coursesPanel.revalidate();
        coursesPanel.repaint();

        aggiornaStatoFrecce();
    }

    private void aggiornaStatoFrecce()
    {
        int maxPage = (filteredCourseCards.size() - 1) / CARDS_PER_PAGE;
        leftArrow.setEnabled(currentPage > 0);
        rightArrow.setEnabled(currentPage < maxPage);
    }

    private JXPanel creaCardCorso(String titolo, String descrizione)
    {
        JXPanel card = new JXPanel(new BorderLayout());
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

    @Override
    public void filter(String filter)
    {
        String testo = filter.toLowerCase();
        filteredCourseCards.clear();

        for(JXPanel card : allCourseCards)
        {
            JXLabel label = (JXLabel)card.getComponent(0);
            if(label.getText().toLowerCase().contains(testo))
            {
                filteredCourseCards.add(card);
            }
        }

        currentPage = 0;
        caricaPagina(currentPage);
    }
}