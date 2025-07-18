package UninaFoodLab.Boundary;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXButton;
import org.jdesktop.swingx.JXFrame;

import UninaFoodLab.Controller.Controller;

public class HomepageFrame extends JXFrame
{
	private static final long serialVersionUID = 1L;

	public HomepageFrame()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		JPanel contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		JXButton prova = new JXButton("cliccami");
		Controller.getController().goToMyCourses(this);
		prova.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent e) {}});
		contentPane.add(prova);
	}
}
