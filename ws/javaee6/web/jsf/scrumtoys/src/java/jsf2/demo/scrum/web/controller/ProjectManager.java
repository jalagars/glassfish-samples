/*
 	Copyright (c) 2019 Oracle and/or its affiliates. All rights reserved.
	
	This program and the accompanying materials are made available under the
	terms of the Eclipse Public License v. 2.0, which is available at
	http://www.eclipse.org/legal/epl-2.0.
	
	This Source Code may also be made available under the following Secondary
	Licenses when the conditions for such availability set forth in the
	Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
	version 2 with the GNU Classpath Exception, which is available at
	https://www.gnu.org/software/classpath/license.html.
	
	SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
*/

package jsf2.demo.scrum.web.controller;

import jsf2.demo.scrum.model.entities.Project;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dr. Spock (spock at dev.java.net)
 */
@ManagedBean(name = "projectManager")
@SessionScoped
public class ProjectManager extends AbstractManager implements Serializable {

    private static final long serialVersionUID = 1L;
    private Project currentProject;
    private DataModel<Project> projects;
    private List<SelectItem> projectItems;
    private List<Project> projectList;

    @PostConstruct
    public void construct() {
        Project project = new Project();
        setCurrentProject(project);
        init();
    }

    @PreDestroy
    public void destroy() {
        projects = null;
        if (projectItems != null) {
            projectItems.clear();
            projectItems = null;
        }
        if (projectList != null) {
            projectList.clear();
            projectList = null;
        }
        currentProject = null;
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("projectManager");
    }

    public void init() {
        try {
            setProjectList(doInTransaction(new PersistenceAction<List<Project>>() {

                @SuppressWarnings({"unchecked"}) 
                public List<Project> execute(EntityManager em) {
                    Query query = em.createNamedQuery("project.getAll");
                    return (List<Project>) query.getResultList();
                }
            }));
        } catch (ManagerException ex) {
            Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        projectItems = new LinkedList<SelectItem>();
        projectItems.add(new SelectItem(new Project(), "-- Select one project --"));
        if (getProjectList() != null) {
            projects = new ListDataModel<Project>(getProjectList());
            for (Project p : getProjectList()) {
                projectItems.add(new SelectItem(p, p.getName()));
            }
        }
    }

    public String create() {
        Project project = new Project();
        setCurrentProject(project);
        return "create";
    }

    public String save() {
        if (getCurrentProject() != null) {
            try {
                Project merged = doInTransaction(new PersistenceAction<Project>() {

                    public Project execute(EntityManager em) {
                        if (getCurrentProject().isNew()) {
                            em.persist(getCurrentProject());
                        } else if (!em.contains(currentProject)) {
                            return em.merge(getCurrentProject());
                        }
                        return getCurrentProject();
                    }
                });
                if (!currentProject.equals(merged)) {
                    setCurrentProject(merged);
                    int idx = getProjectList().indexOf(getCurrentProject());
                    if (idx != -1) {
                        getProjectList().set(idx, merged);
                    }
                }
                if (!projectList.contains(merged)) {
                    getProjectList().add(merged);
                }
            } catch (Exception e) {
                getLogger(getClass()).log(Level.SEVERE, "Error on try to save Project: " + getCurrentProject(), e);
                addMessage("Error on try to save Project", FacesMessage.SEVERITY_ERROR);
                return null;
            }
        }
        init();
        return "show";
    }

    public String edit() {
        setCurrentProject(projects.getRowData());
        // Using implicity navigation, this request come from /projects/show.xhtml and directs to /project/edit.xhtml
        return "edit";
    }

    public String remove() {
        final Project project = projects.getRowData();
        if (project != null) {
            try {
                doInTransaction(new PersistenceActionWithoutResult() {

                    public void execute(EntityManager em) {
                        if (em.contains(project)) {
                            em.remove(project);
                        } else {
                            em.remove(em.merge(project));
                        }
                    }
                });
                getProjectList().remove(project);
            } catch (Exception e) {
                getLogger(getClass()).log(Level.SEVERE, "Error on try to remove Project: " + getCurrentProject(), e);
                addMessage("Error on try to remove Project", FacesMessage.SEVERITY_ERROR);
                return null;
            }
        }
        init();
        // Using implicity navigation, this request come from /projects/show.xhtml and directs to /project/show.xhtml
        // could be null instead
        return "show";
    }

    public void checkUniqueProjectName(FacesContext context, UIComponent component, Object newValue) {
        final String newName = (String) newValue;
        try {
            Long count = doInTransaction(new PersistenceAction<Long>() {

                public Long execute(EntityManager em) {
                    Query query = em.createNamedQuery((getCurrentProject().isNew()) ? "project.new.countByName" : "project.countByName");
                    query.setParameter("name", newName);
                    if (!currentProject.isNew()) {
                        query.setParameter("currentProject", getCurrentProject());
                    }
                    return (Long) query.getSingleResult();
                }
            });
            if (count != null && count > 0) {
                throw new ValidatorException(getFacesMessageForKey("project.form.label.name.unique"));
            }
        } catch (ManagerException ex) {
            Logger.getLogger(ProjectManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String cancelEdit() {
        // Implicity navigation, this request come from /projects/edit.xhtml and directs to /project/show.xhtml
        return "show";
    }

    public String showSprints() {
        setCurrentProject(projects.getRowData());
        // Implicity navigation, this request come from /projects/show.xhtml and directs to /project/showSprints.xhtml
        return "showSprints";
    }

    public Project getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
    }

    public DataModel<Project> getProjects() {
        return projects;
    }

    public void setProjects(DataModel<Project> projects) {
        this.projects = projects;
    }

    public List<SelectItem> getProjectItems() {
        return projectItems;
    }

    public void setProjectItems(List<SelectItem> projectItems) {
        this.projectItems = projectItems;
    }

    /**
     * @return the projectList
     */
    public List<Project> getProjectList() {
        return projectList;
    }

    /**
     * @param projectList the projectList to set
     */
    public void setProjectList(List<Project> projectList) {
        this.projectList = projectList;
    }

    public String viewSprints() {
        return "/sprint/show";
    }


}
