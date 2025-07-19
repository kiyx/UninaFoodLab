package UninaFoodLab.Boundary;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXPanel;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

/**
 * Pannello popup per la selezione degli argomenti di un corso.
 * <p>
 * Carica dinamicamente la lista di argomenti dal {@link Controller},
 * crea una {@link JCheckBox} per ciascun argomento e un pulsante "Applica".
 * Al click di "Applica", raccoglie gli ID selezionati e invoca
 * {@code filterByTopicIds(List<Integer>)} sull'oggetto che implementa {@link TopicFilterable}.
 * </p>
 */
public class FilterPanel extends JXPanel 
{
    private static final long serialVersionUID = 1L;
    
    /** Bottone di applicazione filtri */
    private JXButton applyBtn;
    
    /** Lista degli ID degli argomenti caricati dal Controller */
    private List<Integer> idsArgomenti = new ArrayList<>();
    
    /** Lista dei nomi corrispondenti agli ID */
    private List<String> namesArgomenti = new ArrayList<>();
    
    /** Checkbox per ogni argomento mostrato */
    private List<JCheckBox> checkboxes = new ArrayList<>();
    
    /** Riferimento all’oggetto che implementa TopicFilterable per applicare il filtro */
    private TopicFilterable filterable;

    /** Listener per il pulsante "Applica" */
    private ActionListener applyBtnClicker;
    
    /**
     * Costruisce un nuovo {@code FilterPanel}.
     * <p>
     * Carica gli argomenti dal {@link Controller}, costruisce
     * dinamicamente le checkbox e aggiunge il pulsante "Applica".
     * </p>
     *
     * @param filterable l’oggetto che implementa {@link TopicFilterable};
     *                   riceverà la lista di ID selezionati al click
     */
    public FilterPanel(TopicFilterable filterable) 
    {
        super(new MigLayout("wrap 1, insets 10", "[grow,fill]", ""));
        this.filterable = filterable;

        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200,200,200), 1, true),
            BorderFactory.createEmptyBorder(10,10,10,10)
        ));
   
        initComponents();
        initListeners();
    }
    
    /**
     * Inizializza le componenti grafiche:
     * carica argomenti, crea le checkbox e il pulsante "Applica".
     */
    private void initComponents()
    {
    	Controller.getController().loadArgomenti(idsArgomenti, namesArgomenti);
        
        for(int i = 0; i < idsArgomenti.size(); i++) 
        {
            JCheckBox cb = new JCheckBox(namesArgomenti.get(i));
            cb.setBackground(Color.WHITE);
            checkboxes.add(cb);
            add(cb);
        }

        applyBtn = new JXButton("Applica");
        applyBtn.setBackground(new Color(225,126,47));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFocusPainted(false);
        applyBtn.setPreferredSize(new Dimension(100, 35));
        add(applyBtn, "gapy 10, align right");
    }
    
    /**
     * Registra il listener per il pulsante "Applica", che invoca {@link #filterIds()}.
     */
    private void initListeners()
    {
    	applyBtnClicker = new ActionListener() 
					       {
					           @Override
					           public void actionPerformed(ActionEvent e) { filterIds(); }
					       };
    	applyBtn.addActionListener(applyBtnClicker);
    }
       
    /**
     * Rimuove il listener associato al pulsante "Applica".
     * Da chiamare quando il pannello non è più in uso per evitare memory leak.
     */
    public void disposeListeners()
    {
    	if(applyBtn != null && applyBtnClicker != null)
    	{
    		applyBtn.removeActionListener(applyBtnClicker);
    		applyBtnClicker = null;
    	}	
    }
   
    /**
     * Raccoglie gli ID degli argomenti selezionati e invoca
     * {@link TopicFilterable#filterByTopicIds(List)} sull'oggetto.
     */
    private void filterIds()
    {
    	List<Integer> selectedIds = new ArrayList<>();
        for(int idx = 0; idx < checkboxes.size(); idx++) 
            if(checkboxes.get(idx).isSelected())
                selectedIds.add(idsArgomenti.get(idx));
        
        filterable.filterByArgumentsIds(selectedIds);
    }
}