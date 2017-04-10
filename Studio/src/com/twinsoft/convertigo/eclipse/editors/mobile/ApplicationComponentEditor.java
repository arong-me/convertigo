/*
 * Copyright (c) 2001-2016 Convertigo SA.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 * $URL$
 * $Author$
 * $Revision$
 * $Date$
 */

package com.twinsoft.convertigo.eclipse.editors.mobile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.teamdev.jxbrowser.chromium.Browser;
import com.twinsoft.convertigo.eclipse.ConvertigoPlugin;
import com.twinsoft.convertigo.eclipse.swt.C8oBrowser;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.ProjectExplorerView;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.util.ProcessUtils;

public class ApplicationComponentEditor extends EditorPart implements ISelectionChangedListener {

	private ProjectExplorerView projectExplorerView = null;
	private ApplicationComponentEditorInput applicationEditorInput;
	
	ScrolledComposite browserScroll;
	GridData browserGD;
	
	private C8oBrowser c8oBrowser;
	private Browser browser;
	private String baseUrl = null;
	private String pageName = null;
	private Collection<Process> processes = new LinkedList<>();
	
	private static Pattern pIsServerRunning = Pattern.compile(".*?server running: (http\\S*).*");
	private static Pattern pRemoveEchap = Pattern.compile("\\x1b\\[\\d+m");
	
	public ApplicationComponentEditor() {
		projectExplorerView = ConvertigoPlugin.getDefault().getProjectExplorerView();
		if (projectExplorerView != null) {
			projectExplorerView.addSelectionChangedListener(this);
		}
	}

	
	@Override
	public void dispose() {
		if (projectExplorerView != null) {
			projectExplorerView.removeSelectionChangedListener(this);
		}
		
		c8oBrowser.dispose();
		
		for (Process p: processes) {
			p.destroyForcibly();
			p.destroy();
		}
		
		try {
			new ProcessBuilder("taskkill", "/F", "/IM", "node.exe").start();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		super.dispose();
	}


	@Override
	public void doSave(IProgressMonitor monitor) {
		
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		
		applicationEditorInput = (ApplicationComponentEditorInput) input;
		setPartName(applicationEditorInput.application.getProject().getName() + " [A: " + applicationEditorInput.application.getName()+"]");
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite editor = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		gl.marginBottom = gl.marginTop = gl.marginLeft = gl.marginRight = gl.marginHeight = gl.marginWidth = gl.horizontalSpacing = 0; 
		editor.setLayout(gl);
		
		createToolbar(editor);
		createBrowser(editor);
		
		setBrowserSize(-1, -1);
		
		launchBuilder();
		
		getSite().getWorkbenchWindow().getActivePage().activate(this);
	}

	private void createBrowser(Composite parent) {
		browserScroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		browserScroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		browserScroll.setExpandHorizontal(true);
		browserScroll.setExpandVertical(true);
		
		Composite canvas = new Composite(browserScroll, SWT.NONE);
		browserScroll.setContent(canvas);
		
		GridLayout gl = new GridLayout(1, false);
		gl.marginBottom = gl.marginTop = gl.marginLeft = gl.marginRight = gl.marginHeight = gl.marginWidth = 0;
		canvas.setLayout(gl);
		
		c8oBrowser = new C8oBrowser(canvas, SWT.NONE);
		browserGD = new GridData(SWT.CENTER, SWT.CENTER, true, true);
		c8oBrowser.setLayoutData(browserGD);

		browser = c8oBrowser.getBrowser();
	}

	private void createToolbar(Composite parent) {
		ToolBar toolbar = new ToolBar(parent, SWT.VERTICAL);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		
		ToolItem item = new ToolItem(toolbar, SWT.PUSH);
		item.setText("F");
		item.setToolTipText("Set fullsize");
		item.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setBrowserSize(-1, -1);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		item = new ToolItem(toolbar, SWT.PUSH);
		item.setText("P");
		item.setToolTipText("Set portrait");
		item.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setBrowserSize(360, 598);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		item = new ToolItem(toolbar, SWT.PUSH);
		item.setText("L");
		item.setToolTipText("Set landscape");
		item.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				setBrowserSize(598, 360);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
				
		item = new ToolItem(toolbar, SWT.PUSH);
		item.setText("D");
		item.setToolTipText("Show debug");
		item.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				getSite().getPage().activate(ConvertigoPlugin.getDefault().getMobileDebugView());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		item = new ToolItem(toolbar, SWT.PUSH);
		item.setText("B");
		item.setToolTipText("Open in default browser");
		item.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String url = browser.getURL();
				if (url.startsWith("http")) {
					org.eclipse.swt.program.Program.launch(url);
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	}
	
	private void setBrowserSize(int width, int height) {
		browserGD.horizontalAlignment = width < 0 ? GridData.FILL : GridData.CENTER;
		browserGD.verticalAlignment = height < 0 ? GridData.FILL : GridData.CENTER;
		browserScroll.setMinWidth(browserGD.widthHint = browserGD.minimumWidth = width);
		browserScroll.setMinHeight(browserGD.heightHint = browserGD.minimumHeight = height);
		c8oBrowser.getParent().layout();
	}
	
	@Override
	public void setFocus() {
//		c8oBrowser.getBrowserView().requestFocus();
	}

	public void refreshBrowser() {
//		if (browser != null && !browser.isDisposed()) {
//			browser.refresh();
//		}
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
//		browser.executeScript("location.href='http://www.convertigo.com'");
//		browser.executeJavaScript("location.href='http://www.convertigo.com'");
//		if (event.getSource() instanceof ISelectionProvider) {
//			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
//			TreeObject treeObject = (TreeObject) selection.getFirstElement();
//			if (treeObject != null) {
//				if (treeObject instanceof MobileUIComponentTreeObject) {
//					TreeParent treeParent = treeObject.getParent();
//					while (treeParent != null) {
//						if (treeParent instanceof MobilePageComponentTreeObject) {
//							PageComponent page = ((MobilePageComponentTreeObject)treeParent).getObject();
//							if (pageEditorInput.is(page)) {
//								getEditorSite().getPage().bringToTop(this);
//							}
//							break;
//						}
//						treeParent = treeParent.getParent();
//					}
//				}
//				else if (treeObject instanceof MobilePageComponentTreeObject) {
//					PageComponent page = ((MobilePageComponentTreeObject)treeObject).getObject();
//					if (pageEditorInput.is(page)) {
//						getEditorSite().getPage().bringToTop(this);
//					}
//				}
//			}
//		}
	}
	
	private void appendOutput(String msg) {
		if (baseUrl == null) {
			browser.executeJavaScriptAndReturnValue("loader_log").asFunction().invokeAsync(null, msg);
		}
	}
	
	private void launchBuilder() {
		try {
			browser.loadHTML(IOUtils.toString(getClass().getResourceAsStream("loader.html"), "UTF-8"));
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		
		Engine.execute(() -> {
			File ionicDir = new File(applicationEditorInput.application.getProject().getDirPath() + "/_private/ionic");
			if (!new File(ionicDir, "node_modules").exists()) {
				try {
					ProcessBuilder pb = ProcessUtils.getNpmProcessBuilder("", "npm", "install", "--progress=false");
					pb.redirectErrorStream(true);
					pb.directory(ionicDir);
					Process p = pb.start();
					processes.add(p);
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line;
					while ((line = br.readLine()) != null) {
						line = pRemoveEchap.matcher(line).replaceAll("");
						if (StringUtils.isNotBlank(line)) {						
							Engine.logStudio.info(line);
							appendOutput(line);
						}
					}
					Engine.logStudio.info(line);
					appendOutput("\\o/");
				} catch (Exception e) {
					appendOutput(":( " + e);
				}
			}

			try {
				ProcessBuilder pb = ProcessUtils.getNpmProcessBuilder("", "npm", "run", "ionic:serve", "--nobrowser");
				pb.redirectErrorStream(true);
				pb.directory(ionicDir);
				Process p = pb.start();
				processes.add(p);
				BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					line = pRemoveEchap.matcher(line).replaceAll("");
					if (StringUtils.isNotBlank(line)) {
						Engine.logStudio.info(line);
						appendOutput(line);
						Matcher m = pIsServerRunning.matcher(line);
						if (m.matches()) {
							baseUrl = m.group(1);
							doLoad();
						}
					}
				}
				appendOutput("\\o/");
			} catch (Exception e) {
				appendOutput(":( " + e);
			}
			
		});
	}

	private void doLoad() {
		if (baseUrl != null) {
			String url = baseUrl;
			if (pageName != null) {
				url += "#/" + pageName;
			}
			browser.loadURL(url);
		}
	}
	
	public String getDebugUrl() {
		if (browser != null) {
			return browser.getRemoteDebuggingURL();
		}
		return null;
	}
	
	public void selectPage(String pageName) {
		this.pageName = pageName;
		doLoad();
	}
}