package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.*;
import org.jdesktop.swingx.*;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class SessionInfoPanel extends JPanel
{
    private static final long serialVersionUID = 1L;
    
    private JXPanel btnPanel;
    private JXButton btnAdesione;
    private JXLabel titleLbl, dataLbl, orarioLabel, durataLabel, ricetteLabel, luogoLabel, partecipantiLabel, linkLabel;
    private final int idSession, number, durata;
    private final Integer numeroPartecipanti;
    private final boolean pratica;
    private final LocalDate dataStr;
    private final LocalTime orarioStr;
    private final String luogo, linkRiunione, userContext;
    private final List<String> ricette;
    
    private ActionListener adesioneListener;

    public SessionInfoPanel(int idSession, int number, boolean pratica, LocalDate dataStr, LocalTime orarioStr, int durata, List<String> ricette, String luogo,
    						Integer numeroPartecipanti, String linkRiunione, String userContext)
    {
    	this.idSession = idSession;
        this.number = number;
        this.pratica = pratica;
        this.dataStr = dataStr;
        this.orarioStr = orarioStr;
        this.durata = durata;
        this.ricette = ricette;
        this.luogo = luogo;
        this.numeroPartecipanti = numeroPartecipanti;
        this.linkRiunione = linkRiunione;
        this.userContext = userContext;
        
        setLayout(new MigLayout("fill, insets 10", "[grow, fill][pref!]", "[][][][][][][][]"));
        setBackground(dataStr.isBefore(LocalDate.now()) ? new Color(245, 245, 245) : new Color(255, 250, 240));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 152, 0)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        initComponents();
        initListeners();
    }

    private void initComponents()
    {
    
    	titleLbl = new JXLabel(String.format("Sessione %d - %s", number, pratica ? "Pratica" : "Online"));
    	titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
    	titleLbl.setForeground(dataStr.isAfter(LocalDate.now()) ? Color.GRAY : new Color(255, 87, 34));
        add(titleLbl, "span 2, wrap");

        dataLbl = new JXLabel(dataStr.toString());
        add(new JLabel("Data:"), "right");
        add(dataLbl, "wrap");

        orarioLabel = new JXLabel(orarioStr.toString());
        add(new JLabel("Orario:"), "right");
        add(orarioLabel, "wrap");

        durataLabel = new JXLabel(String.valueOf(durata));
        add(new JLabel("Durata (minuti):"), "right");
        add(durataLabel, "wrap");

        if(pratica)
        {
            ricetteLabel = new JXLabel(String.join(", ", ricette));
            luogoLabel = new JXLabel(luogo != null ? luogo : "-");
            partecipantiLabel = new JXLabel(String.valueOf(numeroPartecipanti));

            add(new JLabel("Ricette:"), "right");
            add(ricetteLabel, "wrap");

            add(new JLabel("Luogo:"), "right");
            add(luogoLabel, "wrap");

            add(new JLabel("Numero Partecipanti:"), "right");
            add(partecipantiLabel, "wrap");
        }
        else
        {
            add(new JLabel("Link Riunione:"), "right");
            linkLabel = new JXLabel("<html><a href='" + linkRiunione + "'>" + linkRiunione + "</a></html>");
            linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            linkLabel.setForeground(new Color(255, 87, 34));
            add(linkLabel, "wrap");
        }

        btnPanel = new JXPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);
        
        btnAdesione = new JXButton("Adesione", FontIcon.of(MaterialDesign.MDI_CHECK, 16));
        btnAdesione.setBackground(new Color(76, 175, 80));
        btnAdesione.setForeground(Color.WHITE);

        boolean canAdesione = pratica && !Controller.getController().isChefLogged() && userContext.equals("MyCourses") &&
            !dataStr.isBefore(LocalDate.now()) && !(ChronoUnit.DAYS.between(LocalDate.now(), dataStr) <= 3);

        btnAdesione.setEnabled(canAdesione);

        if(canAdesione)
            btnPanel.add(btnAdesione);


        add(btnPanel, "span 2, growx, align right");
    }

    private void initListeners()
    {
        adesioneListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
               JOptionPane.showMessageDialog(SessionInfoPanel.this, "Adesione alla sessione " + number);
               Controller.getController().saveAdesione(idSession); 
            }
        };
        btnAdesione.addActionListener(adesioneListener);
    }

    public void disposeListeners()
    {
        if(btnAdesione != null && adesioneListener != null)
        {
            btnAdesione.removeActionListener(adesioneListener);
            adesioneListener = null;
        }
    }
}