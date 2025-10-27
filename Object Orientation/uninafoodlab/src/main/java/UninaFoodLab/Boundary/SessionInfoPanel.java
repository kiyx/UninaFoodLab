package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
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
    private JXLabel titleLbl, dataLbl, orarioLabel, durataLabel, ricetteLabel, luogoLabel, linkLabel, adesioniLabel;
    private final int idSession, number, durata;
    private final boolean pratica;
    private final LocalDate dataStr;
    private final LocalTime orarioStr;
    private final String luogo, linkRiunione;
    private final List<String> ricette;
    private final int idCorso; 
    
    private ActionListener adesioneListener;

    public SessionInfoPanel(int idSession, int number, boolean pratica, LocalDate dataStr, LocalTime orarioStr, int durata, List<String> ricette, String luogo
			, String linkRiunione, int idCorso)
	{
		this.idSession = idSession;
		this.number = number;
		this.pratica = pratica;
		this.dataStr = dataStr;
		this.orarioStr = orarioStr;
		this.durata = durata;
		this.ricette = ricette;
		this.luogo = luogo;
		this.linkRiunione = linkRiunione;
		this.idCorso = idCorso;

		setLayout(new MigLayout("fillx, insets 10", "[pref!][grow, fill]", "[][][][][][][][]"));
		
		boolean isPast = dataStr.isBefore(LocalDate.now());
		setBackground(isPast ? new Color(248, 248, 248) : Color.WHITE);
		
		Color topBorderColor = isPast ? new Color(224, 224, 224) : new Color(255, 152, 0);
		setBorder(BorderFactory.createCompoundBorder(
		BorderFactory.createMatteBorder(2, 0, 0, 0, topBorderColor), 
		BorderFactory.createEmptyBorder(10, 10, 10, 10))); 
		
		initComponents();
		initListeners();
	}

    private void initComponents()
    {
        boolean isPast = dataStr.isBefore(LocalDate.now());

        titleLbl = new JXLabel(String.format("Sessione %d - %s", number, pratica ? "Pratica" : "Online"));
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(isPast ? Color.GRAY : new Color(255, 87, 34));
        add(titleLbl, "span 2, wrap");

        dataLbl = new JXLabel(dataStr.toString());
        add(new JLabel("Data:"), "align right");
        add(dataLbl, "wrap");

        orarioLabel = new JXLabel(orarioStr.toString());
        add(new JLabel("Orario:"), "align right");
        add(orarioLabel, "wrap");

        durataLabel = new JXLabel(String.valueOf(durata));
        add(new JLabel("Durata (minuti):"), "align right");
        add(durataLabel, "wrap");

        if(pratica)
        {
            ricetteLabel = new JXLabel(String.join(", ", ricette));
            luogoLabel = new JXLabel(luogo != null ? luogo : "-");
            adesioniLabel = new JXLabel(String.valueOf(Controller.getController().getNumeroAdesioni(idSession)));

            add(new JLabel("Ricette:"), "align right");
            add(ricetteLabel, "wrap");

            add(new JLabel("Luogo:"), "align right");
            add(luogoLabel, "wrap");

            add(new JLabel("Numero di Adesioni:"), "align right");
            add(adesioniLabel, "wrap");
        }
        else
        {
            add(new JLabel("Link Riunione:"), "align right");
            linkLabel = new JXLabel("<html><a href='" + linkRiunione + "'>" + linkRiunione + "</a></html>");
            linkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            linkLabel.setForeground(new Color(255, 87, 34));
            add(linkLabel, "wrap");
        }

        btnPanel = new JXPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnPanel.setOpaque(false);

        boolean canShowAdesione = pratica
                && !Controller.getController().isChefLogged()
                && !isPast
                && Boolean.TRUE.equals(Controller.getController().checkIscritto(idCorso));

        if (canShowAdesione)
        {
            boolean alreadyAdesione = Controller.getController().checkAdesione(idSession);

            if(alreadyAdesione)
                createAdesioneButton("Elimina adesione", new Color(244, 67, 54), MaterialDesign.MDI_CLOSE);
            else
                createAdesioneButton("Aderisci", new Color(76, 175, 80), MaterialDesign.MDI_CHECK);

            btnPanel.add(btnAdesione);
        }

        add(btnPanel, "span 2, growx, align right");
    }

    private void createAdesioneButton(String text, Color baseColor, MaterialDesign icon)
    {
        btnAdesione = new JXButton(text, FontIcon.of(icon, 16));
        btnAdesione.setBackground(baseColor);
        btnAdesione.setForeground(Color.WHITE);
        btnAdesione.setBorderPainted(false); 
        btnAdesione.setFocusable(false);     

        btnAdesione.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                btnAdesione.setBackground(baseColor.darker());
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                btnAdesione.setBackground(baseColor);
            }
        });
    }

    private void initListeners()
    {
        if(btnAdesione != null)
        {
        	adesioneListener = new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    boolean alreadyAdesione = Controller.getController().checkAdesione(idSession);

                    if(alreadyAdesione)
                    {
                        Controller.getController().removeAdesione(SessionInfoPanel.this, idSession);
                        showMessage("Hai eliminato l’adesione alla sessione " + number);
                        adesioniLabel.setText(String.valueOf(Controller.getController().getNumeroAdesioni(idSession)));
                        updateAdesioneButton(false);
                    }
                    else
                    {
                        Controller.getController().saveAdesione(SessionInfoPanel.this, idSession);
                        showMessage("Hai aderito alla sessione " + number);
                        adesioniLabel.setText(String.valueOf(Controller.getController().getNumeroAdesioni(idSession)));
                        updateAdesioneButton(true);
                    }
                }
            };
            btnAdesione.addActionListener(adesioneListener);	
        } 
    }

    private void updateAdesioneButton(boolean aderito)
    {
        btnPanel.removeAll();
        
        if(aderito)
            createAdesioneButton("Elimina adesione", new Color(244, 67, 54), MaterialDesign.MDI_CLOSE);
        else
            createAdesioneButton("Aderisci", new Color(76, 175, 80), MaterialDesign.MDI_CHECK);

        btnAdesione.addActionListener(adesioneListener);
        btnPanel.add(btnAdesione);
        btnPanel.revalidate();
        btnPanel.repaint();
    }

    public void disposeListeners()
    {
        if(btnAdesione != null && adesioneListener != null)
        {
            btnAdesione.removeActionListener(adesioneListener);
            adesioneListener = null;
        }
    }
    
    public void showMessage(String msg) { JOptionPane.showMessageDialog(this, msg); }
}