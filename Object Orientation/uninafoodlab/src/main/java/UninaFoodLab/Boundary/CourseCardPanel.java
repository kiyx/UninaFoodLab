package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.MouseListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.*;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import net.miginfocom.swing.MigLayout;


public class CourseCardPanel extends JXPanel 
{
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final List<Integer> idsArguments;
    private final List<String> namesArguments;
    private final Date startDate;
    private final int sessionNumber;

    private JXPanel header, argumentsPanel;
    private JXLabel lblSessions, lblName, lblDate;
    private JTextArea tagLabel;
    private DropShadowBorder shadow; 
    
    
    public CourseCardPanel(int id, String name, List<Integer> idsArguments, List<String> namesArguments, Date startDate, int sessionNumber) 
    {
    	super(new MigLayout("fill, gapy 0", "[grow,fill]", "[40!][pref!][pref!][pref!]"));
        this.id               = id;
        this.name             = name;
        this.idsArguments     = idsArguments;
        this.namesArguments   = namesArguments;
        this.startDate        = startDate;
        this.sessionNumber    = sessionNumber;
        
        setMinimumSize(new Dimension(0, 160));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBackground(Color.WHITE);

        initComponents();
    }

    /**
     * Inizializza i componenti grafici della card.
     */
    private void initComponents()
    {
        shadow = new DropShadowBorder();
        shadow.setShadowColor(new Color(0, 0, 0, 50));
        shadow.setShadowSize(6);
        setBorder(shadow);

        header = new JXPanel(new BorderLayout());
        header.setBackground(new Color(225, 126, 47));
        header.setPreferredSize(new Dimension(0, 40));

        lblName = new JXLabel(name, SwingConstants.CENTER);
        lblName.setForeground(Color.WHITE);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(lblName, BorderLayout.CENTER);
        add(header, "cell 0 0, growx, gaptop 0, gapbottom 0");

        argumentsPanel = new JXPanel(new MigLayout("wrap, align left, gap 4px 4px", "[min!][min!][min!]", "[]"));
        argumentsPanel.setBackground(Color.WHITE);
        argumentsPanel.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 2));

        for(String topic : namesArguments) 
        {
        	tagLabel = new JTextArea(topic);
        	tagLabel.setLineWrap(true);
        	tagLabel.setWrapStyleWord(true);
        	tagLabel.setRows(2);
        	tagLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        	tagLabel.setHighlighter(null);
        	tagLabel.setEditable(false);
        	tagLabel.setCursor(Cursor.getDefaultCursor());
        	tagLabel.setFocusable(false);
        	tagLabel.setOpaque(true);
        	tagLabel.setBackground(new Color(255, 230, 180));
        	tagLabel.setForeground(new Color(100, 50, 10));	
        	tagLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));       	
        	tagLabel.setMaximumSize(new Dimension(150, tagLabel.getPreferredSize().height));
        	tagLabel.setPreferredSize(null);
            argumentsPanel.add(tagLabel);
        }
        add(argumentsPanel, "cell 0 1, gaptop 0, growx, gapy 4px, wrap");

        Icon calendarIcon = FontIcon.of(MaterialDesign.MDI_CALENDAR, 18, new Color(80, 80, 80));
        lblDate = new JXLabel("Data Inizio: " + new SimpleDateFormat("dd/MM/yyyy").format(startDate), calendarIcon, SwingConstants.LEADING);
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDate.setBorder(BorderFactory.createEmptyBorder(4, 8, 2, 8));
        add(lblDate, "cell 0 2, growx, wrap");

        Icon bookIcon = FontIcon.of(MaterialDesign.MDI_BOOK_OPEN_PAGE_VARIANT, 18, new Color(80, 80, 80));
        lblSessions = new JXLabel("Numero Sessioni: " + sessionNumber, bookIcon, SwingConstants.LEADING);
        lblSessions.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblSessions.setBorder(BorderFactory.createEmptyBorder(0, 8, 6, 8));
        add(lblSessions, "cell 0 3, growx");
    }
    
    
    public int getId() 
    {
        return id;
    }
    
   
    public boolean matchesText(String search) 
    {
    	return (search == null || search.isBlank()) ? true : name.toLowerCase().contains(search.trim().toLowerCase());
    }

    
    public boolean matchesArguments(List<Integer> selectedArgIds) 
    {
        if(selectedArgIds == null || selectedArgIds.isEmpty())
            return true;
        
        return idsArguments.containsAll(selectedArgIds);
    }

   
    public void addCourseClickListener(MouseListener ml) 
    {
        this.addMouseListener(ml);
    }

    
    public void removeCourseClickListener(MouseListener ml) 
    {
        this.removeMouseListener(ml);
    }
}