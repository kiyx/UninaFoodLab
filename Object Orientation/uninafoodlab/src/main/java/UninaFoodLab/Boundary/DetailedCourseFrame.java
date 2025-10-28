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
import org.jdesktop.swingx.JXLabel;
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
    private JXLabel lblNome, lblDataInizio, lblNumeroSessioni, lblFrequenza, lblLimitePartecipanti, lblCosto, lblIscrizioneStatus, lblNumeroIscritti;
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
        setLayout(new MigLayout("fill, insets 15, gapy 10", "[grow]", "[][grow]"));
        getContentPane().setBackground(Color.WHITE);

        initComponents();
        initListeners();
    }

    private void initComponents()
    {
        courseInfoPanel = new JPanel(new MigLayout("wrap 2, ins 15, fillx", 
                                                   "[pref!][grow, fill]", 
                                                   "[pref!][grow, 60:120][pref!][pref!][pref!][pref!][pref!][pref!][pref!][pref!]"));
        courseInfoPanel.setBackground(new Color(255, 249, 240));
        courseInfoPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 183, 77), 1), 
                                                  "Informazioni Corso",
                                                  TitledBorder.LEFT,
                                                  TitledBorder.TOP,
                                                  new Font("Segoe UI", Font.BOLD, 16),
                                                  new Color(255, 87, 34)));

        courseInfoPanel.add(new JXLabel("Nome:"), "align right");
        lblNome = new JXLabel();
        lblNome.setFont(lblNome.getFont().deriveFont(Font.BOLD));
        courseInfoPanel.add(lblNome, "growx");

        courseInfoPanel.add(new JXLabel("Descrizione:"), "top, align right");
        txtDescrizione = new JTextArea();
        txtDescrizione.setLineWrap(true);
        txtDescrizione.setWrapStyleWord(true);
        txtDescrizione.setEditable(false);
        txtDescrizione.setFocusable(false);
        txtDescrizione.setFont(txtDescrizione.getFont().deriveFont(13f));
        txtDescrizione.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        txtDescrizione.setBackground(courseInfoPanel.getBackground());
        courseInfoPanel.add(txtDescrizione, "growx, growy"); 

        courseInfoPanel.add(new JXLabel("Data Inizio:"), "align right");
        lblDataInizio = new JXLabel();
        courseInfoPanel.add(lblDataInizio);

        courseInfoPanel.add(new JXLabel("Numero Sessioni:"), "align right");
        lblNumeroSessioni = new JXLabel();
        courseInfoPanel.add(lblNumeroSessioni);

        courseInfoPanel.add(new JXLabel("Frequenza Sessioni:"), "align right");
        lblFrequenza = new JXLabel();
        courseInfoPanel.add(lblFrequenza);

        courseInfoPanel.add(new JXLabel("Limite Partecipanti (solo pratico):"), "align right");
        lblLimitePartecipanti = new JXLabel();
        courseInfoPanel.add(lblLimitePartecipanti);

        courseInfoPanel.add(new JXLabel("Numero Iscritti Attuali:"), "align right");
        lblNumeroIscritti = new JXLabel();
        courseInfoPanel.add(lblNumeroIscritti);
        
        courseInfoPanel.add(new JLabel("Costo (€):"), "align right");
        lblCosto = new JXLabel();
        courseInfoPanel.add(lblCosto);

        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonsPanel.setOpaque(false);

        btnEditCourse = new JXButton("Modifica", FontIcon.of(MaterialDesign.MDI_PENCIL, 16));
        btnEditCourse.setBackground(new Color(255, 152, 0));
        btnEditCourse.setForeground(Color.WHITE);
        btnEditCourse.setBorderPainted(false); 
        btnEditCourse.setFocusable(false);     

        btnDeleteCourse = new JXButton("Elimina", FontIcon.of(MaterialDesign.MDI_DELETE, 16));
        btnDeleteCourse.setBackground(new Color(244, 67, 54));
        btnDeleteCourse.setForeground(Color.WHITE);
        btnDeleteCourse.setBorderPainted(false);
        btnDeleteCourse.setFocusable(false);

        btnIscrivitiCorso = new JXButton("Iscriviti", FontIcon.of(MaterialDesign.MDI_ACCOUNT_PLUS, 16));
        btnIscrivitiCorso.setBackground(new Color(255, 152, 0));
        btnIscrivitiCorso.setForeground(Color.WHITE);
        btnIscrivitiCorso.setBorderPainted(false);
        btnIscrivitiCorso.setFocusable(false);

        btnDisiscrivitiCorso = new JXButton("Disiscriviti", FontIcon.of(MaterialDesign.MDI_DELETE_SWEEP, 16));
        btnDisiscrivitiCorso.setBackground(new Color(244, 67, 54));
        btnDisiscrivitiCorso.setForeground(Color.WHITE);
        btnDisiscrivitiCorso.setBorderPainted(false);
        btnDisiscrivitiCorso.setFocusable(false);

        buttonsPanel.add(btnIscrivitiCorso);
        buttonsPanel.add(btnDisiscrivitiCorso);
        buttonsPanel.add(btnEditCourse);
        buttonsPanel.add(btnDeleteCourse);

        courseInfoPanel.add(buttonsPanel, "span, growx, align right");
        
        lblIscrizioneStatus = new JXLabel();
        lblIscrizioneStatus.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblIscrizioneStatus.setForeground(new Color(244, 67, 54)); 
        lblIscrizioneStatus.setVisible(false); 

        lblIscrizioneStatus.setIcon(FontIcon.of(MaterialDesign.MDI_CLOSE_CIRCLE, 18, new Color(244, 67, 54)));

        courseInfoPanel.add(lblIscrizioneStatus, "span, growx, wrap");

        add(courseInfoPanel, "growx, wrap");

        sessionsPanel = new JPanel(new MigLayout("wrap 1, insets 5, gapy 10", "[grow]"));
        sessionsPanel.setBackground(Color.WHITE);
        sessionsPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(new Color(255, 183, 77), 1),
                "Dettaglio Sessioni",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(255, 87, 34)));

        sessionsScrollPane = new JScrollPane(sessionsPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sessionsScrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        sessionsScrollPane.getVerticalScrollBar().setUnitIncrement(25);

        add(sessionsScrollPane, "grow, pushy, wrap"); 
    }

    private void initListeners()
    {
        btnEditCourse.addActionListener(_ -> onEditCourse());
        btnDeleteCourse.addActionListener(_ -> onDeleteCourse());
        btnIscrivitiCorso.addActionListener(_ -> onIscrivitiCorso());
        btnDisiscrivitiCorso.addActionListener(_ -> 
        {
            Controller.getController().disiscriviCorso(owner, DetailedCourseFrame.this, idCorso);
            lblNumeroIscritti.setText(String.valueOf(Controller.getController().getNumeroIscritti(idCorso)));
        });

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
    		((SessionInfoPanel)s).disposeListeners();
    	
    	super.dispose();
    }
    
    private void updateButtonsVisibility()
    {
        boolean corsoIniziato = courseStartDate != null && courseStartDate.isBefore(today);
        boolean isChef = Controller.getController().isChefLogged();

        btnIscrivitiCorso.setVisible(false);
        btnDisiscrivitiCorso.setVisible(false);
        btnEditCourse.setVisible(false);
        btnDeleteCourse.setVisible(false);

        if(isChef)
        {
        	if(userContext.equals("MyCourses"))
        	{
        		btnEditCourse.setVisible(true);
                btnDeleteCourse.setVisible(true);
        	}   
        }
        else
        {
        	if("Homepage".equals(userContext))
        	{
        	    boolean canIscriviti = !corsoIniziato &&
        	            (courseLimitePartecipanti == null || Controller.getController().getNumeroIscritti(idCorso) < courseLimitePartecipanti);

        	    if(Controller.getController().checkIscritto(idCorso))
        	    {
        	        lblIscrizioneStatus.setVisible(false);
        	        btnIscrivitiCorso.setText("Iscritto");
        	        btnIscrivitiCorso.setEnabled(false);
        	        btnIscrivitiCorso.setVisible(true);
        	    }
        	    else
        	    {
        	        if(canIscriviti)
        	        {
        	            lblIscrizioneStatus.setVisible(false);
        	            btnIscrivitiCorso.setText("Iscriviti");
        	            btnIscrivitiCorso.setEnabled(true);
        	            btnIscrivitiCorso.setVisible(true);
        	        }
        	        else
        	        {
        	            btnIscrivitiCorso.setVisible(false);
        	            lblIscrizioneStatus.setText(" Non è più possibile iscriversi al corso");
        	            lblIscrizioneStatus.setVisible(true);
        	        }
        	    }
        	}
            else if("MyCourses".equals(userContext))
            {
                btnDisiscrivitiCorso.setVisible(true);
            }
        }
    }
    
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
        lblNumeroIscritti.setText(String.valueOf(Controller.getController().getNumeroIscritti(courseId)));
        lblCosto.setText(String.format("€ %.2f", costo));

        updateButtonsVisibility();
    }

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
                        (!pratica) ? ((SessioneOnline) s).getLinkRiunione() : null, this.idCorso), "growx");
            }
        }

        sessionsPanel.revalidate();
        sessionsPanel.repaint();
    }

    private void onIscrivitiCorso()
    {
        Controller.getController().registerIscrizione(idCorso);
        JOptionPane.showMessageDialog(this, "Iscritto al corso: " + lblNome.getText());
        btnIscrivitiCorso.setEnabled(false);  
        lblNumeroIscritti.setText(String.valueOf(Controller.getController().getNumeroIscritti(idCorso)));
        btnIscrivitiCorso.setText("Iscritto");
    }

    private void onEditCourse()
    {
        JOptionPane.showMessageDialog(this, "Modifica corso " + lblNome.getText());
        Controller.getController().openEditCourseDialog(owner, this, idCorso);
    }

    private void onDeleteCourse()
    {
        if(JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare questo corso?", "Elimina Corso", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)
        	Controller.getController().eliminaCorso(owner, this, idCorso);
    }
    
    public void showMessage(String msg) { JOptionPane.showMessageDialog(this, msg); }
}