package UninaFoodLab.Boundary;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXFrame;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class DetailedRecipeDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private JPanel panel;
    private JLabel nomeLabel, provenienzaLabel, calorieLabel, difficoltaLabel,
                   tempoPreparazioneLabel, ingredientiLabel;
    private JScrollPane allergeniScrollPane;
    private JTextArea allergeniTextArea;
    private JButton modificaButton, eliminaButton;
    private JXFrame parent;
    private ActionListener modificaListener, eliminaListener; // Aggiunto eliminaListener

    private String nomeRicetta;
    private String provenienzaRicetta;
    private int calorieRicetta;
    private String difficoltaRicetta;
    private String allergeniRicetta;
    private int tempoRicetta;
    private int idRicetta;
    private ArrayList<String> nomiIngredienti;
    private ArrayList<Double> quantitaIngredienti;
    private ArrayList<String> udmIngredienti;

    public DetailedRecipeDialog(JXFrame parent, int idRicetta,  String nomeRicetta, String provenienzaRicetta, int calorieRicetta, String difficoltaRicetta, String allergeniRicetta, int tempoRicetta, ArrayList<String> nomiIngredienti, ArrayList<Double> quantitaIngredienti, ArrayList<String> udmIngredienti) {
        super(parent, "Dettagli Ricetta", true);
        this.parent = parent;
        this.nomeRicetta = nomeRicetta;
        this.provenienzaRicetta = provenienzaRicetta;
        this.calorieRicetta = calorieRicetta;
        this.difficoltaRicetta = difficoltaRicetta;
        this.allergeniRicetta = allergeniRicetta;
        this.tempoRicetta = tempoRicetta;
        this.idRicetta=idRicetta;
        this.nomiIngredienti = nomiIngredienti;
        this.quantitaIngredienti = quantitaIngredienti;
        this.udmIngredienti = udmIngredienti;

        setPreferredSize(new Dimension(550, 700));
        setMinimumSize(new Dimension(450, 600));

        initComponents();
        initListeners();
        setLocationRelativeTo(parent);
        setResizable(true);
        pack();
    }

    private void initComponents() {
        // Vincoli di riga aggiornati: l'ultima riga ora contiene un pannello orizzontale per i bottoni.
        panel = new JPanel(new MigLayout(
            "wrap, fill",
            "[grow, fill]",
            "[]18" +          // nomeLabel
            "[]10" +           // provenienzaLabel
            "[]10" +           // calorieLabel
            "[]10" +           // difficoltaLabel
            "[]10" +           // tempoPreparazioneLabel
            "[]10" +           // Etichetta "Allergeni:"
            "[grow 1, push, 60:pref:100]10" + // JScrollPane allergeni
            "[]10" +           // ingredientiLabel
            "[grow 2, push, 80:pref:200]35" + // JScrollPane ingredienti (Min 80 per gli ingredienti)
            "[]"));            // NUOVO: riga per il pannello dei bottoni (pref!)

        panel.setBackground(new Color(245, 245, 245));
        panel.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(panel);

        Font headerFont = new Font("Segoe UI", Font.BOLD, 24);
        Font sectionHeaderFont = new Font("Segoe UI", Font.BOLD, 17);
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 16);
        Color primaryTextColor = new Color(30, 30, 30);
        Color accentColor = new Color(225, 126, 47); // Arancione per Modifica

        nomeLabel = new JLabel(nomeRicetta);
        nomeLabel.setFont(headerFont);
        nomeLabel.setForeground(primaryTextColor);
        panel.add(nomeLabel, "align left");

        provenienzaLabel = new JLabel("Provenienza: " + provenienzaRicetta);
        provenienzaLabel.setFont(labelFont);
        provenienzaLabel.setForeground(primaryTextColor);
        panel.add(provenienzaLabel, "align left");

        calorieLabel = new JLabel("Calorie: " + calorieRicetta + " kcal");
        calorieLabel.setFont(labelFont);
        calorieLabel.setForeground(primaryTextColor);
        panel.add(calorieLabel, "align left");

        difficoltaLabel = new JLabel("Difficoltà: " + difficoltaRicetta);
        difficoltaLabel.setFont(labelFont);
        difficoltaLabel.setForeground(primaryTextColor);
        panel.add(difficoltaLabel, "align left");

        tempoPreparazioneLabel = new JLabel("Tempo di preparazione: " + tempoRicetta + " minuti");
        tempoPreparazioneLabel.setFont(labelFont);
        tempoPreparazioneLabel.setForeground(primaryTextColor);
        panel.add(tempoPreparazioneLabel, "align left");

        JLabel allergeniHeaderLabel = new JLabel("Allergeni:");
        allergeniHeaderLabel.setFont(labelFont);
        allergeniHeaderLabel.setForeground(primaryTextColor);
        panel.add(allergeniHeaderLabel, "align left, gaptop 10");

        allergeniTextArea = new JTextArea();
        allergeniTextArea.setFont(labelFont);
        allergeniTextArea.setForeground(primaryTextColor);
        allergeniTextArea.setEditable(false);
        allergeniTextArea.setLineWrap(true);
        allergeniTextArea.setWrapStyleWord(true);
        allergeniTextArea.setBackground(new Color(235, 235, 235));
        allergeniTextArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        String displayAllergeni = (allergeniRicetta == null || allergeniRicetta.trim().isEmpty()) ? "Nessuno" : allergeniRicetta;
        allergeniTextArea.setText(displayAllergeni);

        allergeniScrollPane = new JScrollPane(allergeniTextArea);
        allergeniScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        allergeniScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        allergeniScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        allergeniScrollPane.getViewport().setBackground(new Color(235, 235, 235));
        allergeniScrollPane.getVerticalScrollBar().setUnitIncrement(10);

        panel.add(allergeniScrollPane, "grow, pushy, height 60:pref:100");

        ingredientiLabel = new JLabel("Ingredienti:");
        ingredientiLabel.setFont(sectionHeaderFont);
        ingredientiLabel.setForeground(primaryTextColor);
        panel.add(ingredientiLabel, "align left, gaptop 10");

        JPanel ingredientsPanel = new JPanel(new MigLayout("wrap", "[grow, fill]", "[]"));
        ingredientsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        ingredientsPanel.setBackground(Color.WHITE);

        for (int i = 0; i < nomiIngredienti.size(); i++) {
            JLabel utilizzoLabel = new JLabel("• " + nomiIngredienti.get(i) + " " + quantitaIngredienti.get(i) + " " + udmIngredienti.get(i));
            utilizzoLabel.setFont(labelFont);
            utilizzoLabel.setForeground(primaryTextColor);
            ingredientsPanel.add(utilizzoLabel, "align left, pad 6 12 6 12, growx");
        }

        JPanel fillerPanel = new JPanel();
        fillerPanel.setBackground(Color.WHITE);
        ingredientsPanel.add(fillerPanel, "growy, pushy");

        JScrollPane scrollPane = new JScrollPane(ingredientsPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

        panel.add(scrollPane, "grow, pushy, height 80:pref:200"); // Min 80, Pref, Max 200

        
        JPanel buttonPanel = new JPanel(new MigLayout("insets 0, align center", "[grow, fill][15][grow, fill]", "[]"));
        buttonPanel.setOpaque(false); 

        modificaButton = new JButton("Modifica Ricetta");
        modificaButton.setFont(new Font("Segoe UI", Font.BOLD, 17));
        modificaButton.setPreferredSize(new Dimension(200, 50));
        modificaButton.setBackground(accentColor);
        modificaButton.setForeground(Color.WHITE);
        modificaButton.setOpaque(true);
        modificaButton.setFocusPainted(false);
        modificaButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor.darker(), 1),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)));
        modificaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(modificaButton, "grow, push"); 

        
        buttonPanel.add(new JLabel(""), "width 15"); 

        eliminaButton = new JButton("Elimina Ricetta");
        eliminaButton.setFont(new Font("Segoe UI", Font.BOLD, 17));
        eliminaButton.setPreferredSize(new Dimension(200, 50));
        eliminaButton.setBackground(new Color(0xD32F2F)); 
        eliminaButton.setForeground(Color.WHITE);
        eliminaButton.setOpaque(true);
        eliminaButton.setFocusPainted(false);
        eliminaButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD32F2F).darker(), 1),
            BorderFactory.createEmptyBorder(10, 25, 10, 25)));
        eliminaButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buttonPanel.add(eliminaButton, "grow, push");

        panel.add(buttonPanel, "center, gaptop 20"); 
        
        //if(Controller.getController().checkPreparazioni(idRicetta))
        	//buttonPanel.setVisible(false);
    }

    private void initListeners() {
        modificaListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.getController().showChangeRecipeDialog(DetailedRecipeDialog.this, parent,idRicetta,  nomeRicetta, provenienzaRicetta, calorieRicetta, difficoltaRicetta, allergeniRicetta, tempoRicetta, nomiIngredienti, quantitaIngredienti, udmIngredienti);
            }
        };
        modificaButton.addActionListener(modificaListener);

        eliminaListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Controller.getController().deleteRicetta(parent,DetailedRecipeDialog.this, idRicetta);
            }
        };
        eliminaButton.addActionListener(eliminaListener);
    }

    private void disposeListeners() {
        if (modificaButton != null && modificaListener != null) {
            modificaButton.removeActionListener(modificaListener);
        }
        if (eliminaButton != null && eliminaListener != null) { // Rimuovi anche il listener di elimina
            eliminaButton.removeActionListener(eliminaListener);
        }
    }

    @Override
    public void dispose() {
        disposeListeners();
        super.dispose();
    }
}