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


public class FilterPanel extends JXPanel 
{
    private static final long serialVersionUID = 1L;
    
    /** Bottone di applicazione filtri */
    private JXButton applyBtn, resetBtn;;
    private JScrollPane scroll;
    private JXPanel topPanel;
    private JPanel argumentsContainer;
    	
    /** Lista degli ID degli argomenti caricati dal Controller */
    private List<Integer> idsArgomenti = new ArrayList<>();
    
    /** Lista dei nomi corrispondenti agli ID */
    private List<String> namesArgomenti = new ArrayList<>();
    
    /** Checkbox per ogni argomento mostrato */
    private List<JCheckBox> checkboxes = new ArrayList<>();
    
    /** Riferimento all’oggetto che implementa TopicFilterable per applicare il filtro */
    private ArgumentFilterable filterable;

    /** Listener per il pulsante "Applica" e "Reset" */
    private ActionListener applyBtnClicker, resetBtnClicker;
    

    public FilterPanel(ArgumentFilterable filterable) 
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
    

    private void initComponents()
    {
        argumentsContainer = new JPanel(new MigLayout("wrap 1", "[grow,fill]"));
        argumentsContainer.setBackground(Color.WHITE);
        Controller.getController().loadArgomenti(idsArgomenti, namesArgomenti);

        for(int i = 0; i < idsArgomenti.size(); i++) 
        {
            JCheckBox cb = new JCheckBox(namesArgomenti.get(i));
            cb.setBackground(Color.WHITE);
            checkboxes.add(cb);
            argumentsContainer.add(cb);
        }

        scroll = new JScrollPane(argumentsContainer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setPreferredSize(new Dimension(300, 150));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        
        topPanel = new JXPanel(new MigLayout("insets 0", "[grow][]", "[]"));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(scroll, "grow"); 
        
        resetBtn = new JXButton("Reset filtri");
        resetBtn.setBackground(new Color(225,126,47));
        resetBtn.setForeground(Color.WHITE); 
        resetBtn.setFocusPainted(false);
        resetBtn.setPreferredSize(new Dimension(120, 35));
        topPanel.add(resetBtn, "gapleft 10, aligny top, pushx");

        add(topPanel, "grow, wrap");

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
    	
    	resetBtnClicker = new ActionListener()
					      {
					          @Override
					          public void actionPerformed(ActionEvent e)
					          {
					              for(JCheckBox cb : checkboxes)
					                  cb.setSelected(false);
					
					              filterable.filterByArgumentsIds(new ArrayList<>());
					          }
					      };
        resetBtn.addActionListener(resetBtnClicker);
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

        if(resetBtn != null && resetBtnClicker != null)
        {
            resetBtn.removeActionListener(resetBtnClicker);
            resetBtnClicker = null;
        }
    }
   

    private void filterIds()
    {
    	List<Integer> selectedIds = new ArrayList<>();
        for(int idx = 0; idx < checkboxes.size(); idx++) 
            if(checkboxes.get(idx).isSelected())
                selectedIds.add(idsArgomenti.get(idx));
        
        filterable.filterByArgumentsIds(selectedIds);
    }
}