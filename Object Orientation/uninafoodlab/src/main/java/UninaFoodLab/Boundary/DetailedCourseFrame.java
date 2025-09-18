package UninaFoodLab.Boundary;

import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import org.jdesktop.swingx.JXButton;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;
import net.miginfocom.swing.MigLayout;

public class DetailedCourseFrame extends JDialog
{
    private static final long serialVersionUID = 1L;

    private int courseId;
    private LocalDate courseStartDate;
    private Integer courseLimitePartecipanti;
    private boolean isChef;
    private String userContext; // "Homepage" | "MyCourses"

    private JPanel courseInfoPanel;
    private JLabel lblNome;
    private JTextArea txtDescrizione;
    private JLabel lblDataInizio;
    private JLabel lblNumeroSessioni;
    private JLabel lblFrequenza;
    private JLabel lblLimitePartecipanti;
    private JLabel lblCosto;

    private JPanel buttonsPanel;
    private JXButton btnEditCourse;
    private JXButton btnDeleteCourse;
    private JXButton btnIscrivitiCorso;

    private JPanel sessionsPanel;
    private JScrollPane sessionsScrollPane;

    private final LocalDate today = LocalDate.now();

    public DetailedCourseFrame(Window owner)
    {
        super(owner, "Dettaglio Corso", ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 650));
        setLayout(new MigLayout("fill, insets 15", "[grow]", "[][grow][pref!]"));

        initComponents();
        initListeners();
    }

    private void initComponents()
    {
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

        buttonsPanel.add(btnEditCourse);
        buttonsPanel.add(btnDeleteCourse);
        buttonsPanel.add(btnIscrivitiCorso);

        courseInfoPanel.add(buttonsPanel, "span, growx, align right");

        add(courseInfoPanel, "growx, wrap");

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

        add(sessionsScrollPane, "growx, pushy, wrap");
    }

    private void initListeners()
    {
        btnEditCourse.addActionListener(e -> onEditCourse());
        btnDeleteCourse.addActionListener(e -> onDeleteCourse());
        btnIscrivitiCorso.addActionListener(e -> onIscrivitiCorso());
    }

    public void setCourseData(int courseId, String nome, String descrizione, LocalDate dataInizio, int numeroSessioni,
                              String frequenza, Integer limitePartecipanti, double costo, boolean isChef, boolean isParticipant,
                              String userContext)
    {
        this.courseId = courseId;
        this.courseStartDate = dataInizio;
        this.courseLimitePartecipanti = limitePartecipanti;
        this.isChef = isChef;
        this.isParticipant = isParticipant;
        this.userContext = userContext;

        lblNome.setText(nome);
        txtDescrizione.setText(descrizione);
        lblDataInizio.setText(dataInizio.toString());
        lblNumeroSessioni.setText(String.valueOf(numeroSessioni));
        lblFrequenza.setText(frequenza);
        lblLimitePartecipanti.setText(limitePartecipanti != null ? String.valueOf(limitePartecipanti) : "-");
        lblCosto.setText(String.format("€ %.2f", costo));

        updateButtonsVisibility();
    }

    private void updateButtonsVisibility()
    {
        boolean corsoIniziato = courseStartDate != null && courseStartDate.isBefore(today);

        btnEditCourse.setVisible(isChef && userContext.equals("MyCourses") && !corsoIniziato);
        btnDeleteCourse.setVisible(isChef && userContext.equals("MyCourses") && !corsoIniziato);

        boolean canIscriviti = userContext.equals("Homepage") && !corsoIniziato &&
                (courseLimitePartecipanti == null || getNumeroIscritti() < courseLimitePartecipanti);

        btnIscrivitiCorso.setVisible(canIscriviti);
        btnIscrivitiCorso.setEnabled(canIscriviti);
    }

    /*public void setSessions(List<Sessione> sessioni)
    {
        sessionsPanel.removeAll();

        if(sessioni != null)
        {
            for(Sessione s : sessioni)
            {
                sessionsPanel.add(new SessionInfoPanel(s, isParticipant, userContext, today), "growx");
            }
        }

        sessionsPanel.revalidate();
        sessionsPanel.repaint();
    }*/

    private int getNumeroIscritti()
    {
        // TODO: collegare al DB o al controller
        return 0;
    }

    private void onEditCourse()
    {
        JOptionPane.showMessageDialog(this, "Modifica corso " + courseId);
        // TODO: aprire dialog modifica corso
    }

    private void onDeleteCourse()
    {
        int result = JOptionPane.showConfirmDialog(this, "Sei sicuro di voler eliminare questo corso?", "Elimina Corso", JOptionPane.YES_NO_OPTION);
        if(result == JOptionPane.YES_OPTION)
        {
            JOptionPane.showMessageDialog(this, "Corso eliminato: " + courseId);
            dispose();
            // TODO: chiamare controller per eliminazione
        }
    }

    private void onIscrivitiCorso()
    {
        JOptionPane.showMessageDialog(this, "Iscritto al corso " + courseId);
        // TODO: chiamare controller iscrizione
    }
}