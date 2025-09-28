package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.JXButton;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import UninaFoodLab.Controller.Controller;
import UninaFoodLab.DTO.Ricetta;
import UninaFoodLab.DTO.Sessione;
import UninaFoodLab.DTO.SessioneOnline;
import UninaFoodLab.DTO.SessionePratica;
import net.miginfocom.swing.MigLayout;

public class DetailedCourseFrame extends JDialog
{
    private static final long serialVersionUID = 1L;

    private Window owner;
    private int idCorso;
    private LocalDate courseStartDate;
    private Integer courseLimitePartecipanti;
    private String userContext;

    private JPanel courseInfoPanel, buttonsPanel, sessionsPanel;
    private JLabel lblNome, lblDataInizio, lblNumeroSessioni, lblFrequenza, lblLimitePartecipanti, lblCosto;
    private JTextArea txtDescrizione;

    private JXButton btnEditCourse, btnDeleteCourse, btnIscrivitiCorso, btnDisiscrivitiCorso;
    private JScrollPane sessionsScrollPane;

    private final LocalDate today = LocalDate.now();

    public DetailedCourseFrame(Window owner)
    {
        super(owner, "Dettaglio Corso", ModalityType.APPLICATION_MODAL);
        this.owner = owner;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setLayout(new MigLayout("fill, insets 15", "[grow]", "[][grow][pref!]"));

        initComponents();
        initListeners();
    }

    private void initComponents()
    {
        // ---------- INFO CORSO ----------
        courseInfoPanel = new JPanel(new MigLayout("wrap 2, ins 15", "[][grow]", "[][][][][][][][][grow][pref!]"));
        courseInfoPanel.setBackground(new Color(255, 249, 240));
        courseInfoPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 152, 0), 2),
                                                  "Informazioni Corso",
                                                  TitledBorder.LEFT,
                                                  TitledBorder.TOP,
                                                  new Font("Segoe UI", Font.BOLD, 16),
                                                  new Color(255, 87, 34)));

        courseInfoPanel.add(new JLabel("Nome:"), "right");
        lblNome = new JLabel();
        lblNome.setFont(lblNome.getFont().deriveFont(Font.BOLD));
        courseInfoPanel.add(lblNome, "growx");

        courseInfoPanel.add(new JLabel("Descrizione:"), "top, right");
        txtDescrizione = new JTextArea();
        txtDescrizione.setLineWrap(true);
        txtDescrizione.setWrapStyleWord(true);
        txtDescrizione.setEditable(false);
        txtDescrizione.setFocusable(false);
        txtDescrizione.setBackground(courseInfoPanel.getBackground());
        txtDescrizione.setFont(txtDescrizione.getFont().deriveFont(13f));
        txtDescrizione.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        courseInfoPanel.add(txtDescrizione, "growx, h 80!");

        courseInfoPanel.add(new JLabel("Data Inizio:"), "right");
        lblDataInizio = new JLabel();
        courseInfoPanel.add(lblDataInizio);

        courseInfoPanel.add(new JLabel("Numero Sessioni:"), "right");
        lblNumeroSessioni = new JLabel();
        courseInfoPanel.add(lblNumeroSessioni);

        courseInfoPanel.add(new JLabel("Frequenza Sessioni:"), "right");
        lblFrequenza = new JLabel();
        courseInfoPanel.add(lblFrequenza);

        courseInfoPanel.add(new JLabel("Limite Partecipanti (solo pratico):"), "right");
        lblLimitePartecipanti = new JLabel();
        courseInfoPanel.add(lblLimitePartecipanti);

        courseInfoPanel.add(new JLabel("Costo (€):"), "right");
        lblCosto = new JLabel();
        courseInfoPanel.add(lblCosto);

        // ---------- BOTTONI ----------
        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        btnEditCourse = new JXButton("Modifica", FontIcon.of(MaterialDesign.MDI_PENCIL, 16));
        btnEditCourse.setBackground(new Color(255, 152, 0));
        btnEditCourse.setForeground(Color.WHITE);

        btnDeleteCourse = new JXButton("Elimina", FontIcon.of(MaterialDesign.MDI_DELETE, 16));
        btnDeleteCourse.setBackground(new Color(244, 67, 54));
        btnDeleteCourse.setForeground(Color.WHITE);

        btnIscrivitiCorso = new JXButton("Iscriviti", FontIcon.of(MaterialDesign.MDI_ACCOUNT_PLUS, 16));
        btnIscrivitiCorso.setBackground(new Color(255, 152, 0));
        btnIscrivitiCorso.setForeground(Color.WHITE);

        btnDisiscrivitiCorso = new JXButton("Disiscriviti", FontIcon.of(MaterialDesign.MDI_DELETE_SWEEP, 16));
        btnDisiscrivitiCorso.setBackground(new Color(244, 67, 54));
        btnDisiscrivitiCorso.setForeground(Color.WHITE);

        buttonsPanel.add(btnIscrivitiCorso);
        buttonsPanel.add(btnDisiscrivitiCorso);
        buttonsPanel.add(btnEditCourse);
        buttonsPanel.add(btnDeleteCourse);

        courseInfoPanel.add(buttonsPanel, "span, growx, align right");
        add(courseInfoPanel, "growx, wrap");

        // ---------- SESSIONI ----------
        sessionsPanel = new JPanel(new MigLayout("wrap 1, insets 5, gapy 10", "[grow]"));
        sessionsPanel.setBackground(Color.WHITE);
        sessionsPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 152, 0)),
                "Dettaglio Sessioni",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(255, 87, 34)));

        sessionsScrollPane = new JScrollPane(sessionsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sessionsScrollPane.setPreferredSize(new Dimension(860, 350));
        sessionsScrollPane.getVerticalScrollBar().setUnitIncrement(25);

        add(sessionsScrollPane, "growx, pushy, wrap");
    }

    private void initListeners()
    {
        btnEditCourse.addActionListener(_ -> onEditCourse());
        btnDeleteCourse.addActionListener(_ -> onDeleteCourse());
        btnIscrivitiCorso.addActionListener(_ -> onIscrivitiCorso());
        btnDisiscrivitiCorso.addActionListener(_ ->  Controller.getController().disiscriviCorso(owner, DetailedCourseFrame.this, idCorso));

        // Hover: da Iscritto → mostra Disiscriviti
        btnIscrivitiCorso.addMouseListener(new MouseAdapter()
									       {
									           @Override
									           public void mouseEntered(MouseEvent e)
									           {
									               if(!btnIscrivitiCorso.isEnabled() || "Iscritto".equals(btnIscrivitiCorso.getText()))
									               {
									                   btnIscrivitiCorso.setVisible(false);
									                   btnDisiscrivitiCorso.setVisible(true);
									               }
									           }
									       });

        btnDisiscrivitiCorso.addMouseListener(new MouseAdapter()
										      {
										          @Override
										          public void mouseExited(MouseEvent e)
										          {
										              if(!btnIscrivitiCorso.isEnabled() || "Iscritto".equals(btnIscrivitiCorso.getText()))
										              {
										                  btnDisiscrivitiCorso.setVisible(false);
										                  btnIscrivitiCorso.setVisible(true);
										              }
										          }
										      });
    }
    
    @Override
    public void dispose()
    {
    	for(Component s : sessionsPanel.getComponents())
    	{
    		((SessionInfoPanel)s).disposeListeners();
    	}
    	
    	super.dispose();
    }
    
    private void updateButtonsVisibility()
    {
        boolean corsoIniziato = courseStartDate != null && courseStartDate.isBefore(today);
        boolean isChef = Controller.getController().isChefLogged();

        // Default nascondo tutto
        btnIscrivitiCorso.setVisible(false);
        btnDisiscrivitiCorso.setVisible(false);
        btnEditCourse.setVisible(false);
        btnDeleteCourse.setVisible(false);

        if(isChef)
        {
        	if(userContext.equals("MyCourses"))
        	{
        		btnEditCourse.setVisible(!corsoIniziato);
                btnDeleteCourse.setVisible(!corsoIniziato);
        	}   
        }
        else
        {
            // Utente
            if("Homepage".equals(userContext))
            {
                boolean canIscriviti = !corsoIniziato &&
                        (courseLimitePartecipanti == null || Controller.getController().getNumeroIscritti(idCorso) < courseLimitePartecipanti);

                if(Controller.getController().checkIscritto(idCorso))
                {
                    btnIscrivitiCorso.setText("Iscritto");
                    btnIscrivitiCorso.setEnabled(false);
                    btnIscrivitiCorso.setVisible(true);
                }
                else
                {
                    btnIscrivitiCorso.setText("Iscriviti");
                    btnIscrivitiCorso.setEnabled(canIscriviti);
                    btnIscrivitiCorso.setVisible(canIscriviti);
                }
            }
            else if("MyCourses".equals(userContext))
            {
                btnDisiscrivitiCorso.setVisible(true);
            }
        }
    }
    
    // ---------- SET DATA ----------
    public void setCourseData(int courseId, String nome, String descrizione, LocalDate dataInizio, int numeroSessioni,
                              String frequenza, Integer limitePartecipanti, BigDecimal costo, String nomeChef, String cognomeChef, String userContext)
    {
        this.idCorso = courseId;
        this.courseStartDate = dataInizio;
        this.courseLimitePartecipanti = limitePartecipanti;
        this.userContext = userContext;

        lblNome.setText(nome);
        txtDescrizione.setText(descrizione);
        lblDataInizio.setText(dataInizio.toString());
        lblNumeroSessioni.setText(String.valueOf(numeroSessioni));
        lblFrequenza.setText(frequenza);
        lblLimitePartecipanti.setText((limitePartecipanti != null) ? String.valueOf(limitePartecipanti) : "Nessuno");
        lblCosto.setText(String.format("€ %.2f", costo));

        updateButtonsVisibility();
    }

    // ---------- SESSIONI ----------
    public void setSessions(List<Sessione> sessioni)
    {
        sessionsPanel.removeAll();

        if(sessioni != null)
        {
            int i = 1;
            for(Sessione s : sessioni)
            {
                boolean pratica = s instanceof SessionePratica;
                List<String> recipes = new ArrayList<>();

                if(pratica)
                    for(Ricetta r : ((SessionePratica) s).getRicette())
                        recipes.add(r.getNome());

                sessionsPanel.add(new SessionInfoPanel(s.getId(), i++, pratica, s.getData().toLocalDate(), s.getOrario().toLocalTime(), s.getDurata(), recipes,
                        (pratica) ? ((SessionePratica) s).getIndirizzo() : null,
                        (pratica) ? ((SessionePratica) s).getNumeroPartecipanti() : null,
                        (!pratica) ? ((SessioneOnline) s).getLinkRiunione() : null, userContext), "growx");
            }
        }

        sessionsPanel.revalidate();
        sessionsPanel.repaint();
    }

    // ---------- ACTIONS ----------
    private void onIscrivitiCorso()
    {
        Controller.getController().registerIscrizione(idCorso);
        JOptionPane.showMessageDialog(this, "Iscritto al corso: " + lblNome.getText());
        btnIscrivitiCorso.setEnabled(false);
        btnIscrivitiCorso.setText("Iscritto");
    }

    private void onEditCourse()
    {
        JOptionPane.showMessageDialog(this, "Modifica corso " + lblNome.getText());
        // TODO: aprire dialog modifica corso
    }

    private void onDeleteCourse()
    {
        if(JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare questo corso?", "Elimina Corso", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        	Controller.getController().eliminaCorso(owner, this, idCorso);
    }
    
    public void showMessage(String msg) { JOptionPane.showMessageDialog(this, msg); }
}