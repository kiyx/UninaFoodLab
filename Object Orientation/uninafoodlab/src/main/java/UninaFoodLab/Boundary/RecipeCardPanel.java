package UninaFoodLab.Boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.border.DropShadowBorder;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import net.miginfocom.swing.MigLayout;

public class RecipeCardPanel extends JXPanel {
	private static final long serialVersionUID = 1L;

    private final int id;
    private final String titolo;
    private final String difficolta;
    private final int calorie;

    private JXPanel header;
    private JXLabel lblCalorie, lblTitolo, lblDifficolta;
    private DropShadowBorder shadow; 
    
    public RecipeCardPanel(int id, String titolo, String difficolta, int calorie) 
    {
    	super(new MigLayout("fill, gapy 0", "[grow,fill]", "[40!][pref!][pref!][pref!]"));
        this.id = id;
        this.titolo = titolo;
        this.difficolta = difficolta;
        this.calorie = calorie;
        
        setMinimumSize(new Dimension(0, 160));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBackground(Color.WHITE);

        initComponents();
    }

    private void initComponents()
    {
        shadow = new DropShadowBorder();
        shadow.setShadowColor(new Color(0, 0, 0, 50));
        shadow.setShadowSize(6);
        setBorder(shadow);

        header = new JXPanel(new BorderLayout());
        header.setBackground(new Color(225, 126, 47));
        header.setPreferredSize(new Dimension(0, 40));

        lblTitolo = new JXLabel(titolo, SwingConstants.CENTER);
        lblTitolo.setForeground(Color.WHITE);
        lblTitolo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(lblTitolo, BorderLayout.CENTER);
        add(header, "cell 0 0, growx, gaptop 0, gapbottom 0");

        Icon chefIcon = FontIcon.of(MaterialDesign.MDI_STAR, 18, new Color(80, 80, 80));
        lblDifficolta = new JXLabel("Difficoltà: " + difficolta, chefIcon, SwingConstants.LEADING);
        lblDifficolta.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblDifficolta.setBorder(BorderFactory.createEmptyBorder(4, 8, 2, 8));
        add(lblDifficolta, "cell 0 2, growx, wrap");

        Icon fireIcon = FontIcon.of(MaterialDesign.MDI_FIRE, 18, new Color(80, 80, 80));
        lblCalorie = new JXLabel("Calorie: " + calorie, fireIcon, SwingConstants.LEADING);
        lblCalorie.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblCalorie.setBorder(BorderFactory.createEmptyBorder(4, 8, 2, 8));
        add(lblCalorie, "cell 0 3, growx");
    }
    
    public int getId() 
    {
        return id;
    }
    
    public String getNome() 
    {
        return titolo;
    }
    public int getCalorie() 
    {
        return calorie;
    }
    public String getDifficolta() 
    {
        return difficolta;
    }
    public boolean matchesText(String search) 
    {
    	return (search == null || search.isBlank()) ? true : titolo.toLowerCase().contains(search.trim().toLowerCase());
    }

    public void addRecipeClickListener(MouseListener ml) 
    {
        this.addMouseListener(ml);
    }

    public void removeRecipeClickListener(MouseListener ml) 
    {
        this.removeMouseListener(ml);
    }
}
