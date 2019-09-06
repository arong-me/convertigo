package com.twinsoft.convertigo.engine.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.engine.Engine;

public class ProjectUrlParser {
	private String projectName;
	private String gitUrl;
	private String gitRepo;
	private String projectPath;
	private String gitBranch;
	private String projectUrl;
	
	public ProjectUrlParser(String projectUrl) {
		setUrl(projectUrl);
	}
	
	public boolean isValid() {
		return projectUrl != null;
	}
	
	public void setUrl(String projectUrl) {
		this.projectUrl = projectName = gitUrl = gitRepo = projectPath = gitBranch = null;
		
		Pattern p = Pattern.compile("(.*?)=(.*/(?:(.*?)/\\.git)|(?:.*/(.*?)\\.git))(.*)");
		Pattern pOpt = Pattern.compile(":(.*?)=([^:]*)");
		Matcher m = p.matcher(projectUrl);
		if (m.matches()) {
			projectName = m.group(1);
			gitUrl = m.group(2);
			gitRepo = m.group(3) != null ? m.group(3) : m.group(4);
			Matcher mOpt = pOpt.matcher(m.group(5));
			while (mOpt.find()) {
				String key = mOpt.group(1);
				String value = mOpt.group(2);
				switch (key) {
				case "path": projectPath = value; break;
				case "branch": gitBranch = value; break;
				}
			}
			this.projectUrl = projectUrl; 
		} else {
			projectName = projectUrl;
		}
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	public String getGitUrl() {
		return gitUrl;
	}
	
	public String getGitRepo() {
		return gitRepo;
	}
	
	public String getProjectPath() {
		return projectPath;
	}
	
	public String getGitBranch() {
		return gitBranch;
	}
	
	public String getProjectUrl() {
		return projectUrl;
	}
	
	public String toString() {
		return isValid() ? getProjectUrl() : getProjectName();
	}
	
	public void setProjectName(String projectName) {
		ProjectUrlParser parser = makeUrl(projectName, gitUrl, projectPath, gitBranch);
		if (parser.isValid()) {
			this.projectName = projectName;
			setUrl(parser.getProjectUrl());
		}
	}

	public void setGitUrl(String gitUrl) {
		ProjectUrlParser parser = makeUrl(projectName, gitUrl, projectPath, gitBranch);
		if (parser.isValid()) {
			this.gitUrl = gitUrl;
			setUrl(parser.getProjectUrl());
		}
	}

	public void setProjectPath(String projectPath) {
		ProjectUrlParser parser = makeUrl(projectName, gitUrl, projectPath, gitBranch);
		if (parser.isValid()) {
			this.projectPath = projectPath;
			setUrl(parser.getProjectUrl());
		}
	}

	public void setGitBranch(String gitBranch) {
		ProjectUrlParser parser = makeUrl(projectName, gitUrl, projectPath, gitBranch);
		if (parser.isValid()) {
			this.gitBranch = gitBranch;
			setUrl(parser.getProjectUrl());
		}
	}

	public void setProjectUrl(String projectUrl) {
		ProjectUrlParser parser = makeUrl(projectName, gitUrl, projectPath, gitBranch);
		if (parser.isValid()) {
			this.projectUrl = projectUrl;
			setUrl(parser.getProjectUrl());
		}
	}
	
	static public ProjectUrlParser makeUrl(String projectName, String gitUrl, String projectPath, String gitBranch) {
		StringBuilder url = new StringBuilder(projectName).append('=').append(gitUrl);
		if (projectPath != null && !projectPath.isEmpty()) {
			url.append(":path=").append(projectPath);
		}
		if (gitBranch != null && !gitBranch.isEmpty()) {
			url.append(":branch=").append(gitBranch);
		}
		return new ProjectUrlParser(url.toString());
	}
	
	static public String getUrl(String projectName) {
		try {
			Project project = Engine.theApp.databaseObjectsManager.getOriginalProjectByName(projectName);
			if (project != null) {
				return getUrl(project);
			}
		} catch (Exception e) {
			// skip
		}
		return projectName;
	}
	
	static public String getUrl(Project project) {
		String projectName = project.getName();
		try {
			File prjDir = project.getDirFile();
			File wrkDir = GitUtils.getWorkingDir(prjDir);
			String remote = GitUtils.getRemote(wrkDir);
			if (remote != null) {
				String path = prjDir.getCanonicalPath().substring(wrkDir.getCanonicalPath().length());
				if (!path.isEmpty()) {
					remote += ":path=" + path.substring(1).replace('\\', '/');
				}
				String branch = GitUtils.getBranch(wrkDir);
				if (!StringUtils.isEmpty(branch)) {
					remote += ":branch=" + branch;
				}
				projectName = projectName + "=" + remote;
			}
		} catch (Exception e) {
			// skip
		}
		return projectName;
	}
}
