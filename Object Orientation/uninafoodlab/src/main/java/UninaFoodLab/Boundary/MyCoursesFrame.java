package UninaFoodLab.Boundary;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

import org.jdesktop.swingx.*;
import org.kordamp.ikonli.materialdesign.*;
import org.kordamp.ikonli.swing.*;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

import com.formdev.flatlaf.FlatLightLaf;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

/**
 * {@code CoursesFrame} rappresenta la finestra principale della sezione "I miei corsi"
 * <p>
 * La finestra è costruita utilizzando Swing, con layout gestiti tramite MigLayout.
 * Presenta un'interfaccia utente con supporto per:
 * <ul>
 *   <li>Header con logo, barra di ricerca, filtro, hamburger menu e profilo utente</li>
 *   <li>Sidebar navigabile per le sezioni principali (Homepage, Corsi, Ricette, Report)</li>
 *   <li>Dropdown del profilo con opzioni di visualizzazione e logout</li>
 *   <li>Gestione eventi centralizzata per navigazione, ridimensionamento e usabilità</li>
 * </ul>
 * <p>
 * I componenti `ProfileDropdownPanel` e `SidebarPanel` sono aggiunti dinamicamente 
 * al `JLayeredPane` per essere visualizzati sopra il contenuto principale.
 * <p>
 * La classe gestisce correttamente eventi AWT globali, listener di interazione e
 * rimozione sicura degli stessi per evitare memory leak.
 *
 * @see SidebarPanel
 * @see ProfileDropdownPanel
 * @see Controller
 */

public class MyCoursesFrame extends JXFrame 
{

    private static final long serialVersionUID = 1L;

    private JXPanel rootPanel,  mainContentPanel, coursesPanel;
    private JScrollPane scrollPane;
    private HeaderPanel header;
    
    // Listeners
    

    public static void main(String[] args) 
    {
        EventQueue.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
                new MyCoursesFrame().setVisible(true);
    
            } catch (UnsupportedLookAndFeelException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Costruttore della finestra {@code CoursesFrame}.
     * Inizializza dimensioni, layout, componenti grafici e listener.
     * La finestra viene massimizzata e resa visibile.
     */  
    public MyCoursesFrame()
    {
        setTitle("UninaFoodLab - I miei corsi");
        setMinimumSize(new Dimension(700, 600));
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setExtendedState(JXFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JXFrame.EXIT_ON_CLOSE);
        setResizable(true);
        
        initComponents();
        initListeners(); 
        setVisible(true);
    }
   
 

    private void initComponents()
    {
    	// Icona finestra
        ImageIcon windowLogo = new ImageIcon(getClass().getResource("/logo_finestra.png"));
        setIconImage(windowLogo.getImage());

        // Root panel
        rootPanel = new JXPanel(new MigLayout("fill, insets 0", "[grow]", "[][grow]"));
        rootPanel.setBackground(new Color(0xFAFAFA));
        setContentPane(rootPanel);

        // Header
        header = new HeaderPanel(this, getLayeredPane());
        rootPanel.add(header, "dock north");

        // Pannello principale dei contenuti
        mainContentPanel = new JXPanel(new MigLayout("fill, insets 15", "[grow, fill]", "[][grow, fill]"));
        rootPanel.add(mainContentPanel, "grow");

        coursesPanel = new JXPanel(new MigLayout("wrap 4, fillx, insets 10", "[]", "[]"));
        scrollPane = new JScrollPane(coursesPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainContentPanel.add(scrollPane, "grow");

        // Esempio: apertura dialog di creazione corso
        CreateCourseDialog dialog = new CreateCourseDialog(this);
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }
 
 
    
    private void initListeners()
    {
        /*newCourseButton.addActionListener(e -> {
            CreateCourseDialog dialog = new CreateCourseDialog(this);
            dialog.setVisible(true);
            // Aggiungi un listener per ricaricare i corsi quando il dialogo si chiude
        });*/
    }
    
    
    private void disposeListeners() 
    {
        header.disposeListeners();
    }
 
   
    @Override
    public void dispose()
    {
    	disposeListeners();
        super.dispose();
    }
    
    /**
     * Ripristina la visualizzazione iniziale della finestra:
     * <ul>
     *   <li>Svuota il campo di ricerca</li>
     *   <li>Imposta il focus sul campo di ricerca</li>

     * </ul>
     */
    public void resetView() 
    {
        header.resetView();
        // scrollPane.getVerticalScrollBar().setValue(0); // se hai scroll
        // refreshTableModel(); // se carichi dati
    }
   
  
}
