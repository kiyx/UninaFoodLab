package UninaFoodLab.Boundary;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.*;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.DefaultCategoryDataset;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.swing.FontIcon;

import net.miginfocom.swing.MigLayout;

public class ReportFrame extends JXFrame 
{
	private static final long serialVersionUID = 1L;
	
	private final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private final Font LABEL_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private final Color COLOR_CORSI = new Color(225, 126, 47);
    private final Color COLOR_RICETTE = new Color(60, 130, 200);

    private ImageIcon windowLogo;
    private JXLabel lblInfoCorsi, lblInfoRicette;
    private ChartPanel panelCorsi, panelRicette;

    public ReportFrame() 
    {
        setTitle("UninaFoodLab - Report Mensile");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        initComponents();
        setVisible(true);
    }

    private void initComponents() 
    {
        JXPanel main = new JXPanel(new MigLayout(
            "wrap 1, insets 20, gapy 20", "[grow]", "[]10[][grow]10[]10[][grow]10[]"
        ));
        setContentPane(main);
        main.setBackground(new Color(240, 240, 240));
        
        windowLogo = new ImageIcon(getClass().getResource("/logo_finestra.png"));
		setIconImage(windowLogo.getImage());
		
        LocalDate fine = LocalDate.now().minusDays(1);
        LocalDate inizio = fine.minusDays(29);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.ITALIAN);

        String titolo = String.format("Report - Ultimi 30 giorni (%s – %s)",
                inizio.format(fmt), fine.format(fmt));

        JLabel title = new JLabel(titolo, SwingConstants.CENTER);
        title.setIcon(FontIcon.of(MaterialDesign.MDI_CHART_BAR, 28, new Color(90, 90, 90)));
        title.setFont(TITLE_FONT);
        main.add(title, "growx");

        panelCorsi = createEmptyChartPanel();
        lblInfoCorsi = createTextLabel();
        main.add(panelCorsi, "growx, growy");
        main.add(lblInfoCorsi, "growx");

        panelRicette = createEmptyChartPanel();
        lblInfoRicette = createTextLabel();
        main.add(panelRicette, "growx, growy");
        main.add(lblInfoRicette, "growx");
    }

    public void setReportData(int totCorsi, int totOnline, int totPratiche, int minRicette, int maxRicette, double avgRicette) 
    {
        // --- CORSI ---
    	
        if(totCorsi <= 0) 
        {
            panelCorsi.setChart(null);
            panelCorsi.setBackground(new Color(250, 250, 250));
            lblInfoCorsi.setIcon(FontIcon.of(MaterialDesign.MDI_BOOK, 24, Color.GRAY));
            lblInfoCorsi.setText("Nessun corso disponibile negli ultimi 30 giorni.");
        } 
        else 
        {
            DefaultCategoryDataset datasetCorsi = new DefaultCategoryDataset();
            datasetCorsi.addValue(totCorsi, "Totale", "Corsi");
            datasetCorsi.addValue(totOnline, "Totale", "Online");
            datasetCorsi.addValue(totPratiche, "Totale", "In presenza");

            panelCorsi.setChart(createBarChart("Corsi e Sessioni", datasetCorsi, COLOR_CORSI));
            lblInfoCorsi.setToolTipText("Statistiche dei corsi negli ultimi 30 giorni");
            lblInfoCorsi.setIcon(FontIcon.of(MaterialDesign.MDI_BOOK_OPEN_PAGE_VARIANT, 24, COLOR_CORSI));
            lblInfoCorsi.setText(String.format("Corsi: %d   |   Online: %d   |   In presenza: %d", totCorsi, totOnline, totPratiche));
        }

        // --- RICETTE ---
        if(totPratiche <= 0) 
        {
            panelRicette.setChart(null);
            panelRicette.setBackground(new Color(250, 250, 250));
            lblInfoRicette.setText("Nessuna sessione pratica disponibile.");
            lblInfoRicette.setIcon(FontIcon.of(MaterialDesign.MDI_ALERT_CIRCLE_OUTLINE, 24, Color.GRAY));
            return;
        }

        DefaultCategoryDataset datasetRicette = new DefaultCategoryDataset();
        datasetRicette.addValue(minRicette, "Ricette", "Min");
        datasetRicette.addValue(avgRicette, "Ricette", "Media");
        datasetRicette.addValue(maxRicette, "Ricette", "Max");

        panelRicette.setChart(createBarChart("Statistiche Ricette", datasetRicette, COLOR_RICETTE));
        lblInfoRicette.setToolTipText("Statistiche delle ricette nelle sessioni pratiche");
        lblInfoRicette.setIcon(FontIcon.of(MaterialDesign.MDI_SILVERWARE_FORK, 24, COLOR_RICETTE));
        lblInfoRicette.setText(String.format("Minimo: %d   |   Massimo: %d   |   Media: %.2f", minRicette, maxRicette, avgRicette));
    }


    private JXLabel createTextLabel() 
    {
        JXLabel label = new JXLabel(" ", SwingConstants.CENTER);
        label.setFont(LABEL_FONT);
        label.setForeground(new Color(50, 50, 50));
        label.setIconTextGap(8);
        return label;
    }

    private ChartPanel createEmptyChartPanel() 
    {
        ChartPanel panel = new ChartPanel(null);
        panel.setBackground(Color.WHITE);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    private JFreeChart createBarChart(String title, DefaultCategoryDataset data, Color color) 
    {
        JFreeChart chart = ChartFactory.createBarChart(title, "", "", data);
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI", Font.BOLD, 18));
        chart.removeLegend();

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setInsets(new RectangleInsets(10, 20, 10, 20));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 13));
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 10));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelFont(new Font("Segoe UI", Font.PLAIN, 13));
        rangeAxis.setRange(0, Math.max(10, getMaxValue(data) * 1.25));

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, color);
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelFont(new Font("Segoe UI", Font.BOLD, 13));
        renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());

        return chart;
    }

    private double getMaxValue(DefaultCategoryDataset dataset) 
    {
        double max = 0;
        for (int row = 0; row < dataset.getRowCount(); row++)
            for (int col = 0; col < dataset.getColumnCount(); col++) 
            {
                Number n = dataset.getValue(row, col);
                if (n != null) max = Math.max(max, n.doubleValue());
            }
        return max;
    }
}