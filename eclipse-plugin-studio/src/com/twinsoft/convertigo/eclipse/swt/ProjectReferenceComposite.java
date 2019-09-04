package com.twinsoft.convertigo.eclipse.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.twinsoft.convertigo.engine.util.ProjectUrlParser;

public class ProjectReferenceComposite extends Composite {
	private ProjectUrlParser parser;

	public ProjectReferenceComposite(Composite parent, int style, ProjectUrlParser parser) {
		this(parent, style, parser, null);
	}
	
	public ProjectReferenceComposite(Composite parent, int style, ProjectUrlParser parser, Runnable onChange) {
		super(parent, style);
		this.parser = parser;
		
		setLayout(new GridLayout(2, false));
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		Label label = new Label(this , SWT.NONE);
		label.setLayoutData(gd);
		label.setText("<project name>=<git URL>[:path=<optional subpath>][:branch=<optional branch>]\n\n");
		
		label = new Label(this, SWT.NONE);
		label.setText("Project remote URL");
		Text completGitUrl = new Text(this, SWT.NONE);
		completGitUrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		gd.horizontalSpan = 2;
		new Label(this, SWT.HORIZONTAL | SWT.SEPARATOR).setLayoutData(gd);
		
		label = new Label(this, SWT.NONE);
		label.setText("Project name");
		Text projectName = new Text(this, SWT.NONE);
		projectName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		projectName.addModifyListener(e -> {
			parser.setProjectName(projectName.getText());
			if (!completGitUrl.getText().equals(parser.getProjectUrl())) {
				completGitUrl.setText(parser.getProjectUrl());
			}
			if (onChange != null) {
				onChange.run();
			}
		});
		
		label = new Label(this, SWT.NONE);
		label.setText("Git URL");
		Text gitUrl = new Text(this, SWT.NONE);
		gitUrl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		gitUrl.addModifyListener(e -> {
			parser.setGitUrl(gitUrl.getText());
			String val = parser.toString();
			if (!completGitUrl.getText().equals(val)) {
				completGitUrl.setText(val);
			}
			if (onChange != null) {
				onChange.run();
			}
		});
		
		label = new Label(this, SWT.NONE);
		label.setText("Project Path");
		Text projectPath = new Text(this, SWT.NONE);
		projectPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		projectPath.addModifyListener(e -> {
			parser.setProjectPath(projectPath.getText());
			if (!completGitUrl.getText().equals(parser.getProjectUrl())) {
				completGitUrl.setText(parser.getProjectUrl());
			}
			if (onChange != null) {
				onChange.run();
			}
		});

		label = new Label(this, SWT.NONE);
		label.setText("Git branch");
		Text gitBranch = new Text(this, SWT.NONE);
		gitBranch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		gitBranch.addModifyListener(e -> {
			parser.setGitBranch(gitBranch.getText());
			if (!completGitUrl.getText().equals(parser.getProjectUrl())) {
				completGitUrl.setText(parser.getProjectUrl());
			}
			if (onChange != null) {
				onChange.run();
			}
		});
		
		completGitUrl.addModifyListener(e -> {
			parser.setUrl(completGitUrl.getText());
			if (parser.isValid()) {
				if (!projectName.getText().equals(parser.getProjectName())) {
					projectName.setText(parser.getProjectName());
				}
				if (!gitUrl.getText().equals(parser.getGitUrl())) {
					gitUrl.setText(parser.getGitUrl());
				}
			} else {
				String val = completGitUrl.getText();
				if (!projectName.getText().equals(val)) {
					projectName.setText(val);
				}
				val = "";
				if (!gitUrl.getText().equals(val)) {
					gitUrl.setText(val);
				}
				
			}
			String txt = parser.getProjectPath() == null ? "" : parser.getProjectPath();
			if (!projectPath.getText().equals(txt)) {
				projectPath.setText(txt);
			}
			txt = parser.getGitBranch() == null ? "" : parser.getGitBranch();
			if (!gitBranch.getText().equals(txt)) {
				gitBranch.setText(txt);
			}
			if (onChange != null) {
				onChange.run();
			}
		});
		
		completGitUrl.setText(parser.toString());
	}

	public ProjectUrlParser getParser() {
		return parser;
	}

}
