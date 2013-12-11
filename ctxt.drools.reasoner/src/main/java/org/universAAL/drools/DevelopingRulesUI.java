package org.universAAL.drools;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.osgi.framework.BundleContext;
import org.universAAL.drools.engine.RulesEngine;

public class DevelopingRulesUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DevelopingRulesUI(final BundleContext bc) {
		super();
		this.setSize(100, 100);
		JPanel jp = new JPanel();
		JButton jb = new JButton("Restart Drools Reasoner");
		jb.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				RulesEngine r = RulesEngine.getInstance(bc);
				r.restartRulesEngine();
			}
		});
		jp.setLayout((new FlowLayout()));
		jp.add(jb);
		this.add(jp);
		this.setVisible(true);
		this.pack();
	}

}
