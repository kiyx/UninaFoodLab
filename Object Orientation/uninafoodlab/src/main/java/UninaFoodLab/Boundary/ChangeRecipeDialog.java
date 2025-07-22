package UninaFoodLab.Boundary;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTextArea;
import org.jdesktop.swingx.JXTextField;

import UninaFoodLab.Controller.Controller;
import net.miginfocom.swing.MigLayout;

public class ChangeRecipeDialog extends JDialog {
	private static final long serialVersionUID = 1L;

    private JPanel  scrollContentWrapper;
	private JXButton aggiungiIngredienteBtn;
    private JXPanel buttons, container, infoPanel, leftPanel, mainPanel, ingredientiPanel, ingredientiContainer;
    private JXLabel aggiungiIngredienteLabel, ingredientiTitle, title, notIngrediente;
    private JXButton confirmBtn, goBackBtn;
    private JScrollPane rootScroll, scrollAllergeni, scrollIngredienti;
    private JComboBox<String> difficoltaList;
    private JXTextArea allergeniArea;
    private JXTextField nameField, provenienzaField;
    private JSpinner tempoSpinner, calorieSpinner;
    private JXLabel tempoLabel, calorieLabel, minLabel, kcalLabel;
    
	private CompoundBorder defaultBorder = BorderFactory.createCompoundBorder(
			new LineBorder(Color.LIGHT_GRAY, 1),
		  	new EmptyBorder(0, 6, 0, 0));
	private CompoundBorder errorBorder = BorderFactory.createCompoundBorder(
        	new LineBorder(Color.RED, 1),
        	new EmptyBorder(0, 6, 0, 0));
	
    private ActionListener confirmBtnListener, goBackBtnListener, aggiungiIngredienteBtnListener;
    private MouseListener aggiungiIngredienteMouseListener;
    
    private static final Color BACKGROUND_COLOR = new Color(245, 248, 250);
    private static final Color BORDER_COLOR = new Color(220, 225, 230);
    private static final Color BUTTON_COLOR = new Color(225, 126, 47, 220);

    private static final CompoundBorder mainBorder = new CompoundBorder(
        new LineBorder(BORDER_COLOR, 1, true),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)
    );
    
    private ArrayList<CreateUtilizzoPanel> newIngredientiCards;
    private ArrayList<CreateUtilizzoPanel>	oldIngredientiCards;
    
    private ArrayList<String> nomiIngredienti= new ArrayList<>();
    private ArrayList<Integer> idIngredienti= new ArrayList<>();
    
    private ArrayList<String> nomiIngredientiUtil=new ArrayList<>();
    private ArrayList<Integer> idIngredientiUtil= new ArrayList<>();
    
    private ArrayList<Integer> idIngredientiForInit=new ArrayList<>();
    private ArrayList<String> nomiIngredientiForInit;
    private ArrayList<Double> quantitaIngredientiForInit;
    private ArrayList<String> udmIngredientiForInit;
    
    private ArrayList<Integer> idIngredientiDeleted=new ArrayList<>();
    
    private ArrayList<Integer> newIdIngredienti = new ArrayList<>();
    private ArrayList<Double> newQuantitaIngredienti = new ArrayList<>();
    private ArrayList<String> newUdmIngredienti = new ArrayList<>();
    
    private ArrayList<Integer> oldIdIngredienti = new ArrayList<>();
private ArrayList<Double> oldQuantitaIngredienti = new ArrayList<>();
    private ArrayList<String> oldUdmIngredienti = new ArrayList<>();
    //private ArrayList<Integer> idIngredientiRicetta = new ArrayList<>();
    //private ArrayList<Double> quantitaIngredienti = new ArrayList<>();
    //private ArrayList<String> udmIngredienti = new ArrayList<>();
    
    private MyRecipesFrame parent;
    String nomeRicetta;
    String provenienzaRicetta;
    int calorieRicetta;
    String difficoltaRicetta;
    String allergeniRicetta;
    int tempoRicetta;
    int id;
    
	public ChangeRecipeDialog(MyRecipesFrame parent, int idRicetta, String nomeRicetta, String provenienzaRicetta, int calorieRicetta, String difficoltaRicetta, String allergeniRicetta, int tempoRicetta, ArrayList<String> nomiIngredienti, ArrayList<Double> quantitaIngredienti, ArrayList<String> udmIngredienti)
	{
        super(parent, "Cambia ricetta", true);
        setMinimumSize(new Dimension(1670, 700));
        setPreferredSize(new Dimension(1200, 700));
        setLocationRelativeTo(parent);
        setResizable(true);
        setIconImage(parent.getIconImage());
        this.parent=parent;
        this.nomeRicetta=nomeRicetta;
        this.provenienzaRicetta=provenienzaRicetta;
        this.calorieRicetta=calorieRicetta;
        this.difficoltaRicetta=difficoltaRicetta;
        this.allergeniRicetta = allergeniRicetta;
        this.tempoRicetta=tempoRicetta;
        this.id=idRicetta;
        
        oldIngredientiCards = new ArrayList<>();
        newIngredientiCards = new ArrayList<>();
        
        nomiIngredientiForInit=nomiIngredienti;
        quantitaIngredientiForInit=quantitaIngredienti;
        udmIngredientiForInit=udmIngredienti;

        initComponents();
        initListeners();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        pack();
	}
	
	private void initComponents()
	{
        rootScroll = new JScrollPane();
        rootScroll.getVerticalScrollBar().setUnitIncrement(16);
        rootScroll.setBorder(null);

        container = new JXPanel(new BorderLayout());
        rootScroll.setViewportView(container);
        setContentPane(rootScroll);

        mainPanel = new JXPanel(new MigLayout("fill, insets 20", "[min!][grow, fill]", "[grow]"));
        mainPanel.setBackground(BACKGROUND_COLOR);
        container.add(mainPanel, BorderLayout.CENTER);

        Controller.getController().loadIngredienti(nomiIngredienti, idIngredienti);
        Controller.getController().loadIdIngredientiUtil(nomiIngredientiForInit, idIngredientiForInit);
        initLeftPanel(mainPanel);
        initRightPanel(mainPanel);
	}
	
	 private void initLeftPanel(JXPanel mainPanel)
	    {
		 	
	        leftPanel = new JXPanel(new MigLayout("wrap 1, gapy 20", "[grow,fill]"));
	        leftPanel.setBackground(BACKGROUND_COLOR);
	        leftPanel.setPreferredSize(new Dimension(700, 700));

	        title = new JXLabel("Inserisci i dettagli della nuova ricetta");
	        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
	        leftPanel.add(title, "align center");

	        // Info panel
	        infoPanel = new JXPanel(new MigLayout("wrap 2", "[grow,fill][grow,fill]","[][][][][][][]"));
	        infoPanel.setBackground(Color.WHITE);
	        infoPanel.setBorder(mainBorder);

	        nameField = new JXTextField();
	        nameField.setText(nomeRicetta);
	        infoPanel.add(new JLabel("Nome ricetta:"), "cell 0 0");
	        infoPanel.add(nameField, "h 30!, cell 1 0");

	        provenienzaField = new JXTextField();
	        provenienzaField.setText(provenienzaRicetta);
	        infoPanel.add(new JLabel("Provenienza ricetta:"), "cell 0 1");
	        infoPanel.add(provenienzaField, "h 30!, cell 1 1");
	        
	        difficoltaList = new JComboBox<>(Controller.getController().loadDifficolta());
	        difficoltaList.setSelectedItem(difficoltaRicetta);
	        infoPanel.add(new JLabel("Livello di difficoltà:"));
	        infoPanel.add(difficoltaList, "h 30!");

	        allergeniArea = new JXTextArea();
	        allergeniArea.setRows(4);
	        allergeniArea.setColumns(20);
	        allergeniArea.setLineWrap(true);
	        allergeniArea.setWrapStyleWord(true);
	        scrollAllergeni = new JScrollPane(allergeniArea);
	        scrollAllergeni.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
	        allergeniArea.setText(allergeniRicetta);
	        infoPanel.add(new JLabel("Eventuali allergeni:"));
	        infoPanel.add(scrollAllergeni, "h 80!");
	        
	        tempoLabel = new JXLabel("Tempo di preparazione:");
	        SpinnerNumberModel tempoModel = new SpinnerNumberModel(1, 1, null, 1);
	        tempoSpinner = new JSpinner(tempoModel);
	        ((JSpinner.DefaultEditor)tempoSpinner.getEditor()).getTextField().setColumns(5);
	        infoPanel.add(tempoLabel);
	        tempoSpinner.setValue(tempoRicetta);
	        infoPanel.add(tempoSpinner, "h 36!, growx, split 2");
	        minLabel = new JXLabel(" min");
	        infoPanel.add(minLabel);

	        calorieLabel = new JXLabel("Calorie per porzione:");
	        SpinnerNumberModel calorieModel = new SpinnerNumberModel(1, 0, null, 1);
	        calorieSpinner = new JSpinner(calorieModel);
	        ((JSpinner.DefaultEditor)calorieSpinner.getEditor()).getTextField().setColumns(5);
	        infoPanel.add(calorieLabel);
	        calorieSpinner.setValue(calorieRicetta);
	        infoPanel.add(calorieSpinner, "h 36!, growx, split 2");
	        kcalLabel = new JXLabel(" kcal");
	        infoPanel.add(kcalLabel);
	        
	        

	        leftPanel.add(infoPanel);


	        buttons = new JXPanel(new MigLayout("center", "[]20[]"));
	        buttons.setBackground(BACKGROUND_COLOR);

	        confirmBtn = new JXButton("Crea Ricetta");
	        confirmBtn.setBackground(BUTTON_COLOR);
	        confirmBtn.setForeground(Color.WHITE);

	        goBackBtn = new JXButton("Annulla");

	        buttons.add(confirmBtn, "w 120!, h 35!");
	        buttons.add(goBackBtn, "w 120!, h 35!");

	        leftPanel.add(buttons);

	        mainPanel.add(leftPanel, "cell 0 0, growy");
	    }

	    private void initRightPanel(JXPanel mainPanel)
	    {
	        ingredientiPanel = new JXPanel(new MigLayout("wrap 2, insets 10, gap 10 10"));
	        ingredientiPanel.setBackground(Color.WHITE);
	        ingredientiPanel.setBorder(mainBorder);
	        ingredientiPanel.setMinimumSize(new Dimension(900, 500));

	        ingredientiTitle = new JXLabel("Ingredienti della ricetta");
	        ingredientiTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
	        ingredientiPanel.add(ingredientiTitle);

	        aggiungiIngredienteLabel = new JXLabel("<html><u>Aggiungi ingredienti</u></html>");
	        aggiungiIngredienteLabel.setForeground(Color.ORANGE.darker());
	        aggiungiIngredienteLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        ingredientiPanel.add(aggiungiIngredienteLabel, "left, span 2, wrap");
	        
	        notIngrediente = new JXLabel("Se non trovi l'ingrediente desiderato: ");
	        ingredientiPanel.add(notIngrediente, "growx, cell 1 1");
	        notIngrediente.setVisible(false);
	        
			aggiungiIngredienteBtn = new JXButton("Crea ingrediente");
			aggiungiIngredienteBtn.setFont(new Font("Segoe UI Semibold", Font.PLAIN, 13));
		    aggiungiIngredienteBtn.setBackground(new Color(0x9E9E9E));
		    aggiungiIngredienteBtn.setForeground(Color.WHITE);
		    aggiungiIngredienteBtn.setOpaque(true);
		    aggiungiIngredienteBtn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
		    aggiungiIngredienteBtn.setFocusPainted(false);
		    ingredientiPanel.add(aggiungiIngredienteBtn, "growx, cell 1 2");
		    aggiungiIngredienteBtn.setVisible(false);
		    
	        scrollContentWrapper = new JPanel();
	        scrollContentWrapper.setLayout(new BoxLayout(scrollContentWrapper, BoxLayout.Y_AXIS));
	        scrollContentWrapper.setBackground(Color.WHITE);

	        ingredientiContainer = new JXPanel(new MigLayout("wrap 2, gap 10 10", "[grow, fill][grow, fill]"));
	        ingredientiContainer.setBackground(Color.WHITE);

	        scrollContentWrapper.add(ingredientiContainer);
	        scrollContentWrapper.add(Box.createVerticalGlue());

	        scrollIngredienti = new JScrollPane(
	            scrollContentWrapper,
	            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
	        );
	        scrollIngredienti.getVerticalScrollBar().setUnitIncrement(16);
	        scrollIngredienti.setBorder(BorderFactory.createEmptyBorder());
	        scrollIngredienti.getViewport().setBackground(Color.WHITE);
	        scrollIngredienti.setBackground(Color.WHITE);

	        ingredientiPanel.add(scrollIngredienti, "grow, push");
	        
	        for(int i=0; i<idIngredientiForInit.size(); i++)
	        {
	        	addNewUtilizzoCard(oldIngredientiCards);	        	
	        }
	        int i=0;
	        for(CreateUtilizzoPanel u: oldIngredientiCards)
	        {
	        	
		        	u.init(idIngredientiForInit.get(i), nomiIngredientiForInit.get(i), quantitaIngredientiForInit.get(i), udmIngredientiForInit.get(i));     	
		        	i++;
	        }
	        
	        mainPanel.add(ingredientiPanel, "cell 1 0, grow");
	    }
	    
	private void initListeners()
	{
		aggiungiIngredienteMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
            	addNewUtilizzoCard(newIngredientiCards);
            }
        };
        aggiungiIngredienteLabel.addMouseListener(aggiungiIngredienteMouseListener);

        aggiungiIngredienteBtnListener = new ActionListener()
        		{
        	 @Override
        	 public void actionPerformed(ActionEvent e)
        	 {
        		 Controller.getController().showCreateIngredienteDialog(ChangeRecipeDialog.this);
        		 
        	 }
        		};
        aggiungiIngredienteBtn.addActionListener(aggiungiIngredienteBtnListener);
        
        confirmBtnListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(oldIngredientiCards.isEmpty() || newIngredientiCards.isEmpty())
                {
                    JOptionPane.showMessageDialog(ChangeRecipeDialog.this, "Devi aggiungere almeno un ingrediente.", "Errore", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                for(CreateUtilizzoPanel card : oldIngredientiCards)
                {
                    if(!card.isValidUtilizzo())
                    {
                        JOptionPane.showMessageDialog(ChangeRecipeDialog.this, "Errore nei dati di un ingrediente utilizzato in passato", "Errore", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                for(CreateUtilizzoPanel card : newIngredientiCards)
                {
                    if(!card.isValidUtilizzo())
                    {
                        JOptionPane.showMessageDialog(ChangeRecipeDialog.this, "Errore nei dati di un nuovo ingrediente", "Errore", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                if(!checkNome())
                {
                	showError("Bisogna inserire il nome della ricetta.");
                }
                else if (!checkProvenienza())
                {
                	showError("Bisogna inserire la provenienza della ricetta.");
                }
                else
                {
                	newIdIngredienti = new ArrayList<>();
                    newQuantitaIngredienti = new ArrayList<>();
                    newUdmIngredienti = new ArrayList<>();
                    
                    oldIdIngredienti = new ArrayList<>();
                    oldQuantitaIngredienti = new ArrayList<>();
                    oldUdmIngredienti = new ArrayList<>();
            	    
                	for(CreateUtilizzoPanel card : oldIngredientiCards)
                    {	 
                		oldIdIngredienti.add(card.getIngrediente());
                		oldQuantitaIngredienti.add(card.getQuantita());
                        oldUdmIngredienti.add(card.getUnita());		                    
                    }
                	
                	for(CreateUtilizzoPanel card : newIngredientiCards)
                    {	 
                		newIdIngredienti.add(card.getIngrediente());
                		newQuantitaIngredienti.add(card.getQuantita());
                        newUdmIngredienti.add(card.getUnita());	      			                    
                    }

            		Controller.getController().changeRicetta(parent, ChangeRecipeDialog.this, id, nameField.getText(), provenienzaField.getText(), (int)tempoSpinner.getValue(), (int)calorieSpinner.getValue(),
							(String)difficoltaList.getSelectedItem(), allergeniArea.getText(), newIdIngredienti, newQuantitaIngredienti, newUdmIngredienti,
							oldIdIngredienti, oldQuantitaIngredienti, oldUdmIngredienti, idIngredientiDeleted);

                }
            }
        };
        confirmBtn.addActionListener(confirmBtnListener);

        goBackBtnListener = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dispose();
            }
        };
        goBackBtn.addActionListener(goBackBtnListener);
	}

	@Override
	public void dispose()
	{
		Controller.getController().clearRecipeDialogCache();
	    disposeListeners();
	    super.dispose();
	}

	public void disposeListeners()
	{

	    if(confirmBtn != null && confirmBtnListener != null)
	    {
	    	confirmBtn.removeActionListener(confirmBtnListener);
	    	confirmBtnListener = null;
	    }          
	
	    if(goBackBtn != null && goBackBtnListener != null)
	    {
	    	goBackBtn.removeActionListener(goBackBtnListener);
	    	goBackBtnListener = null;
	    }      
	
	    for (CreateUtilizzoPanel card : oldIngredientiCards)
	    	card.disposeListeners();
	    
	    for (CreateUtilizzoPanel card : newIngredientiCards)
	    	card.disposeListeners();

	    if(aggiungiIngredienteLabel != null && aggiungiIngredienteMouseListener != null)
	    {
	    	aggiungiIngredienteLabel.removeMouseListener(aggiungiIngredienteMouseListener);
	    }           

        if(confirmBtn != null && confirmBtnListener != null)
        {
        	confirmBtn.removeActionListener(confirmBtnListener);
        	confirmBtnListener = null;
        }            
 
    }

    private CreateUtilizzoPanel addNewUtilizzoCard(ArrayList<CreateUtilizzoPanel> cards)
    {
        CreateUtilizzoPanel card = new CreateUtilizzoPanel(this);
        cards.add(card);
        ingredientiContainer.add(card, "growx, growy, w 33%");

        updateUtilizziLayout();
        ingredientiContainer.revalidate();
        ingredientiContainer.repaint();
        
        return card;
    }

    public void removeUtilizzoCard(CreateUtilizzoPanel panel)
    {
    	boolean trovato=false;
        panel.disposeListeners();
        
        for(CreateUtilizzoPanel card: oldIngredientiCards)
        {
        	if(panel==card)
        	{
        		trovato=true;
        		idIngredientiDeleted.add(panel.getIngrediente());      		
        	}  
        	break;
        }
        
        for(CreateUtilizzoPanel card : newIngredientiCards)
		{
        	for(int i=0; i<idIngredienti.size(); i++)
        		if(idIngredienti.get(i)==panel.getIngrediente())
        			card.enableIngrediente(nomiIngredienti.get(i));
		}
        for(int i=0; i<idIngredientiUtil.size(); i++)
    		if(idIngredientiUtil.get(i)==panel.getIngrediente())
    		{
    			nomiIngredientiUtil.remove(i);
    			idIngredientiUtil.remove(i);
    		}
        
        
        
        if(trovato==false)
        {
        	newIngredientiCards.remove(panel);
        }
        else
        	oldIngredientiCards.remove(panel);
        
        ingredientiContainer.remove(panel);
        
        updateUtilizziLayout();
        ingredientiContainer.revalidate();
        ingredientiContainer.repaint();
    }
    
    private void updateUtilizziLayout()
    {
        int count = oldIngredientiCards.size();
        count += newIngredientiCards.size();
        
        String columns;

        if(count == 1)
        {
             columns = "[grow, center]";     

             notIngrediente.setVisible(true);
             aggiungiIngredienteBtn.setVisible(true);
        }        
        else 
            columns = "[grow, right][grow, left]";

        ingredientiContainer.setLayout(new MigLayout("wrap " + Math.min(count, 2) + ", gap 10 10", columns));
    }
 
    private boolean checkNome()
    {
		boolean check = true;
	    String text = nameField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	nameField.setBorder(errorBorder);
	    	check = false;
	    }
	    else
	    {
	    	nameField.setBorder(defaultBorder);
	    }
	    
	    return check;
    }

    private boolean checkProvenienza()
    {
		boolean check = true;
	    String text = provenienzaField.getText().trim();
	      
	    if(text.isEmpty())
	    {
	    	provenienzaField.setBorder(errorBorder);
	    	check = false;
	    }
	    else
	    {
	    	provenienzaField.setBorder(defaultBorder);
	    }
	    
	    return check;
    }
    
    public void addIngrediente(String ingNome, int ingId)
    {
    	nomiIngredienti.add(ingNome);
    	idIngredienti.add(ingId);
		for(CreateUtilizzoPanel card : newIngredientiCards)
		{
			card.refresh(ingNome, ingId);
		}
    }
    
    public void setAllResearch() 
    {
    	for(CreateUtilizzoPanel card : newIngredientiCards)
        {	 
    		card.setResearchNeutral();              
        }
    	for(CreateUtilizzoPanel card : oldIngredientiCards)
        {	 
    		card.setResearchNeutral();              
        }
    }
    
    public void getIngredienti(ArrayList<String> nomiIngredienti, ArrayList<Integer> idIngredienti)
    {
    	for(int i=0; i<this.nomiIngredienti.size(); i++)
    		nomiIngredienti.add(this.nomiIngredienti.get(i));
    	for(int i=0; i<this.idIngredienti.size(); i++)
    		idIngredienti.add(this.idIngredienti.get(i));
    }
    
    public void getIngredientiUtil(ArrayList<String> nomiIngredientiUtil, ArrayList<Integer> idIngredientiUtil)
    {
    	for(int i=0; i<this.nomiIngredientiUtil.size(); i++)
    		nomiIngredientiUtil.add(this.nomiIngredientiUtil.get(i));
    	for(int i=0; i<this.idIngredientiUtil.size(); i++)
    		idIngredientiUtil.add(this.idIngredientiUtil.get(i));
    }
    
    public void addListaUtilizzi(int oldIng, int newIng, CreateUtilizzoPanel currPanel)
    {
    	String oldIngNome=null;
    	String newIngNome=null;
    	if(oldIng != -1 && oldIng!=newIng)
    	{
    		for(int i=0; i<idIngredientiUtil.size(); i++)
    			if(idIngredientiUtil.get(i)==oldIng)
    			{
    				idIngredientiUtil.remove(i);
    				oldIngNome = nomiIngredientiUtil.get(i);
    				nomiIngredientiUtil.remove(i);
    			}   		
    	}    		
		
		for(int i=0; i<idIngredienti.size(); i++)
		{
			if(idIngredienti.get(i)==newIng)
			{
				idIngredientiUtil.add(newIng); 
				nomiIngredientiUtil.add(newIngNome = nomiIngredienti.get(i));
			}
		}   
    	
    	for(CreateUtilizzoPanel card : newIngredientiCards)
		{
			if(card!=currPanel)
			{
				card.disabilitaIngrediente(oldIngNome, newIngNome);
			}
		}
    }
    
	public void showError(String msg)
	{
		JOptionPane.showMessageDialog(this, msg, "Errore", JOptionPane.ERROR_MESSAGE);
	}
	
	public void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Successo", JOptionPane.INFORMATION_MESSAGE);
    }
}
