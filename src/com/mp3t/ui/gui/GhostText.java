package com.mp3t.ui.gui;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

public class GhostText extends StyledText {
	
	public static final String INCONSISTENT_FIELDS = "...";
	
	public static final RGB ghostForegroundRGB = new RGB(100, 100, 100);
	public static final RGB normalForegroundRGB = new RGB(0, 0, 0);
	
	private Color ghostForeground;
	private Color normalForeground;
	
	boolean ghostText = false;
	String text = "";

	public GhostText(Composite arg0, int arg1) {
		super(arg0, arg1);
		
		ghostForeground = new Color(getDisplay(), ghostForegroundRGB);
		normalForeground = new Color(getDisplay(), normalForegroundRGB);
		
		setForeground(normalForeground);
		
		this.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				if (ghostText)
					clearGhostText();
			}
			
			public void focusLost(FocusEvent e) {
				if (getCharCount() == 0 && text != null) {
					activateGhostText(text);
				}
			}
		});
	}
	
	public String getText() {
		if (ghostText)
			return "";
		else
			return super.getText();
	}
	
	public void setGhostText(String text) {
		this.text = text;
	}

	public void activateGhostText(String text) {
		ghostText = true;
		this.text = text;
		setText(text);
		setForeground(ghostForeground);
	}
	
	public void clearGhostText() {
		setText("");
		setForeground(normalForeground);
		ghostText = false;
	}
	
	public void removeGhostText() {
		this.text = null;
		setText("");
		setForeground(normalForeground);
		ghostText = false;
	}
}
